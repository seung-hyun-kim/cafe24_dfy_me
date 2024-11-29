package one.dfy.cafe24.ygplus.buffz.service.Impl;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import one.dfy.cafe24.ygplus.buffz.dto.TokenResponse;
import one.dfy.cafe24.ygplus.buffz.mapper.TokenMapper;
import one.dfy.cafe24.ygplus.buffz.service.AuthService;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
public class TokenServiceImpl implements AuthService {

    private String storeName;
    private String accessToken;
    private long expiryTime;
    private String refreshToken;
    private String clientId;
    private String clientSecret;
    private String redirectUri;// 리다이렉트 URI 설정
    private String scope;

    private RestTemplate restTemplate;
    private final SqlSessionFactory sqlSessionFactory;

    @Autowired
    public TokenServiceImpl(RestTemplate restTemplate, SqlSessionFactory sqlSessionFactory) {
        this.restTemplate = restTemplate;
        this.sqlSessionFactory = sqlSessionFactory;
        try {
            // .env 파일에서 환경 변수를 로드
            Dotenv dotenv = Dotenv.load();
            this.storeName = dotenv.get("CAFE24_STORE");
            this.clientId = dotenv.get("CLIENT_ID");
            this.clientSecret = dotenv.get("CLIENT_SECRET");
            this.redirectUri = dotenv.get("REDIRECT_URI");
            this.scope = dotenv.get("SCOPE");
        } catch (Exception e) {
            log.error("Failed to load environment variables: {}", e.getMessage());
            throw e; // 예외를 다시 던져서 Spring이 인지하도록 합니다.
        }

    }


    public String getAuthorizationUrl(String csrfToken) throws UnsupportedEncodingException {
        // URL 인코딩
        String encodedRedirectUri = URLEncoder.encode(redirectUri, "UTF-8");
        String encodedCsrfToken = URLEncoder.encode(csrfToken, "UTF-8");

        // 인증 URL 생성
        String authorizationUrl = String.format(
                "https://%s.cafe24api.com/api/v2/oauth/authorize?response_type=code&client_id=%s&state=%s&redirect_uri=%s&scope=%s",
                storeName, clientId, encodedCsrfToken, encodedRedirectUri, scope
        );
        return authorizationUrl;
    }

    private boolean isTokenExpired() {
        return System.currentTimeMillis() >= expiryTime;
    }

    public String getAccessTokenFromDatabase() {
        LocalDateTime currentDateTime = LocalDateTime.now();

        try (SqlSession session = sqlSessionFactory.openSession()) {
            TokenMapper mapper = session.getMapper(TokenMapper.class);
            return mapper.getValidAccessToken(currentDateTime); // 유효한 액세스 토큰 반환
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 유효한 토큰이 없을 경우 null 반환
        }
    }

    //데이터베이스에서 유효한 리프레시 토큰을 가져온다
    public String getRefreshTokenFromDatabase() {
        LocalDateTime currentDateTime = LocalDateTime.now();

        try (SqlSession session = sqlSessionFactory.openSession()) {
            TokenMapper mapper = session.getMapper(TokenMapper.class);
            return mapper.getRefreshAccessToken(currentDateTime); // 유효한 액세스 토큰 반환
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 유효한 토큰이 없을 경우 null 반환
        }
    }

    @Override
    public TokenResponse getAccessToken(HttpServletResponse response, HttpServletRequest request) throws IOException {
        // Token이 만료되었으면 갱신
        // 데이터베이스에서 유효한 액세스 토큰을 가져온다
        String storedAccessToken = getAccessTokenFromDatabase(); // 데이터베이스에서 액세스 토큰 조회

        // 액세스 토큰이 유효하고 만료되지 않았다면
        if (storedAccessToken != null && !isTokenExpired()) {
            return new TokenResponse(storedAccessToken, refreshToken, null); // 유효한 액세스 토큰 반환
        }

        // 데이터베이스에서 유효한 리프레시 토큰을 가져온다
        String refreshToken = getRefreshTokenFromDatabase(); // 데이터베이스에서 리프레시 토큰 조회

        // 리프레시 토큰이 유효한지 확인
        if (refreshToken != null && !isTokenExpired()) {
            // 리프레시 토큰을 사용하여 새로운 액세스 토큰 요청
            TokenResponse newTokenResponse = refreshAccessToken(refreshToken);

            if (newTokenResponse != null) {
                return newTokenResponse; // 새로운 액세스 토큰 반환
            }
        }

        String csrfToken = generateCsrfToken();

        // CSRF 토큰을 세션에 저장
        request.getSession().setAttribute("csrfToken", csrfToken);
        // 현재 요청 URL을 세션에 저장
        String originalUrl = request.getRequestURL().toString();
        request.getSession().setAttribute("originalUrl", originalUrl);

        //토큰 갱신전 처리
        String authorizationUrl = getAuthorizationUrl(csrfToken);

        log.info("Authorization URL: {}", authorizationUrl);

        // 리다이렉트 처리를 비동기로 처리
        // 예를 들어, 세션이나 데이터베이스에 CSRF 토큰을 저장하고 클라이언트에게 안내
        // 리다이렉트 처리

        //response.sendRedirect(authorizationUrl);

        // 새로운 HTTP 요청 생성하여 authorizationUrl로 전송
        this.callAuthorizationUrl(response, authorizationUrl);

        //this.callReqAuthorizationUrl(authorizationUrl);

        return null; // 리다이렉트 후에는 null 반환

    }
    @Override
    public String getAuthorizationUrl() throws IOException{

        String csrfToken = generateCsrfToken();

        //토큰 갱신전 처리

        return getAuthorizationUrl(csrfToken);
    }

    // 리프레시 토큰을 사용하여 액세스 토큰 갱신
    public TokenResponse refreshAccessToken(String refreshToken) {
        String tokenUrl = String.format("https://%s.cafe24api.com/api/v2/oauth/token", storeName);

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");

        // 클라이언트 ID와 시크릿을 Base64로 인코딩
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);

        // 요청 본문 설정: 리프레시 토큰 포함
        String body = String.format("grant_type=refresh_token&refresh_token=%s", refreshToken);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, Map.class);
            if (response.getBody() != null) {
                String accessToken = (String) response.getBody().get("access_token");
                String newRefreshToken = (String) response.getBody().get("refresh_token"); // 새로운 리프레시 토큰 가져오기
                String expiresAtStr = (String) response.getBody().get("expires_at");
                LocalDateTime expiresAt = LocalDateTime.parse(expiresAtStr, DateTimeFormatter.ISO_DATE_TIME);

                // 갱신된 액세스 토큰과 리프레시 토큰 반환
                return new TokenResponse(accessToken, newRefreshToken, expiresAt);
            } else {
                throw new RuntimeException("Failed to refresh access token");
            }
        } catch (HttpClientErrorException e) {
            log.error("Error response from API: Status code: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Token refresh failed: " + e.getMessage());
        }
    }

    public void callAuthorizationUrl(HttpServletResponse response, String authorizationUrl) throws IOException {

        // 직접 리다이렉트 응답 작성
        response.setStatus(HttpServletResponse.SC_FOUND);// 302 상태 코드
        response.setHeader("Location", authorizationUrl);// Location 헤더 설정
        response.flushBuffer(); // 응답을 클라이언트에 즉시 전송


    }

    private String generateCsrfToken() {
        // CSRF 토큰 생성 로직을 여기에 구현합니다.
        // 예를 들어, UUID를 사용하여 간단한 CSRF 토큰을 생성할 수 있습니다.
        return java.util.UUID.randomUUID().toString();
    }


    @Override
    public TokenResponse obtainNewAccessToken(String authorizationCode) {

        // Authorization Code가 없을 경우 예외 처리
        if (authorizationCode == null || authorizationCode.isEmpty()) {
            throw new IllegalArgumentException("Authorization code not found");
        }

        String tokenUrl = String.format("https://%s.cafe24api.com/api/v2/oauth/token", storeName);

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");


        // 클라이언트 ID와 시크릿을 Base64로 인코딩
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        // Authorization 헤더 추가
        headers.set("Authorization", "Basic " + encodedAuth);

        // 요청 본문 설정: redirect_uri 추가
        //String redirectUri = "https://cafe24.dfy.me/callback"; // 실제 리다이렉트 URI로 변경
        // 요청 본문 설정: authorization_code와 redirect_uri 포함
        String body = String.format("grant_type=authorization_code&code=%s&redirect_uri=%s", authorizationCode, redirectUri);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, Map.class);

            if (response.getBody() != null) {
                String accessToken = (String) response.getBody().get("access_token");
                String refreshToken = (String) response.getBody().get("refresh_token"); // 리프레시 토큰 가져오기
                // "2021-03-01T14:00:00.000" 형식의 문자열을 LocalDateTime으로 변환
                String expiresAtStr = (String) response.getBody().get("expires_at");
                LocalDateTime expiresAt = LocalDateTime.parse(expiresAtStr, DateTimeFormatter.ISO_DATE_TIME);

                return new TokenResponse(accessToken, refreshToken, expiresAt); // TokenResponse 객체 반환
            } else {
                throw new RuntimeException("Failed to obtain access token");
            }
        } catch (HttpClientErrorException e) {
            log.error("Error response from API: Status code: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

}
