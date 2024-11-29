package one.dfy.cafe24.ygplus.component;

import one.dfy.cafe24.ygplus.buffz.dto.AccessToken;
import one.dfy.cafe24.ygplus.buffz.dto.TokenResponse;
import one.dfy.cafe24.ygplus.buffz.mapper.TokenMapper;
import one.dfy.cafe24.ygplus.buffz.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class TokenManager {

    private String accessToken;

    private final AuthService authService;
    private final TokenMapper tokenMapper;

    @Autowired
    public TokenManager(AuthService authService, TokenMapper tokenMapper) {
        this.authService = authService;
        this.tokenMapper = tokenMapper;
    }

    public String getValidAccessToken(HttpServletResponse res, HttpServletRequest req) throws IOException {
        // 현재 저장된 토큰 조회
        AccessToken storedToken = tokenMapper.findLatestToken(); // 가장 최근의 토큰 조회

        // 토큰이 유효한지 확인
        if (storedToken != null && !isTokenExpired(storedToken)) {
            return storedToken.getToken(); // 유효한 토큰 반환
        }

        // 토큰이 만료되었거나 존재하지 않으면 새로 요청
        TokenResponse tokenResponse = authService.getAccessToken(res, req);

        // 액세스 토큰이 유효한지 확인
        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            throw new IOException("Access token is not available.");
        }

        // 새로 발급받은 액세스 토큰을 전역적으로 설정
        setAccessToken(tokenResponse.getAccessToken());

        /*// 유효하지 않는 토큰 삭제
        tokenMapper.deleteInvalidTokens();

        // 새로 발급받은 액세스 토큰을 데이터베이스에 저장
        storeAccessToken(tokenResponse);*/

        return tokenResponse.getAccessToken();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken; // 전역 액세스 토큰 설정
    }

   // 만료 여부 확인 메소드
   public boolean isTokenExpired(AccessToken accessToken) {
       return LocalDateTime.now().isAfter(accessToken.getExpirationAt());
   }


    private void storeAccessToken(TokenResponse tokenResponse) {
        // 새로운 액세스 토큰을 데이터베이스에 저장
        AccessToken tokenEntity = new AccessToken();
        tokenEntity.setToken(tokenResponse.getAccessToken());
        tokenEntity.setRefreshToken(tokenResponse.getRefreshToken()); // 리프레시 토큰 저장
        tokenEntity.setExpirationAt(tokenResponse.getExpiresIn()); // 초 단위를 밀리초로 변환하여 만료 시간 설정 접근 토큰은 발급 받은 후 2시간이 지나면 사용할 수 없습니다.
        tokenMapper.insertAccessToken(tokenEntity); // MyBatis를 통해 DB에 저장
    }
}

