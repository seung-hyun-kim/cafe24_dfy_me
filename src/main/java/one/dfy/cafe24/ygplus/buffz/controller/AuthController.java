package one.dfy.cafe24.ygplus.buffz.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import one.dfy.cafe24.ygplus.buffz.dto.AccessToken;
import one.dfy.cafe24.ygplus.buffz.dto.TokenResponse;
import one.dfy.cafe24.ygplus.buffz.mapper.TokenMapper;
import one.dfy.cafe24.ygplus.buffz.service.AuthService;
import one.dfy.cafe24.ygplus.component.ApiCallManager;
import one.dfy.cafe24.ygplus.component.TokenManager;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Autowired
    private TokenMapper tokenMapper;

    @Autowired
    private ApiCallManager apiCallManager;

    @Autowired
    private TokenManager tokenComponent;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job downgradeMembershipJob;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/authorization")
    public ResponseEntity<String> startAuthJob(HttpServletRequest request, HttpServletResponse response) throws IOException {

            String accessToken = tokenComponent.getValidAccessToken(response, request); // 액세스 토큰 생성

            if (accessToken == null || accessToken.isEmpty()) {
                log.error("Access token is null or empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Access token is required");
            }

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("accessToken", accessToken) // 액세스 토큰 추가
                    .addLong("time", System.currentTimeMillis()) // 고유한 파라미터 추가
                    .toJobParameters();

        try {
            jobLauncher.run(downgradeMembershipJob, jobParameters);

            return ResponseEntity.ok(accessToken); // 액세스 토큰만 반환
        } catch (Exception e) {
            log.error("Job failed to start", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Job failed to start");
        }
    }

    @GetMapping("/start-auth")
    public ResponseEntity<String> authorize() throws IOException {
        
        String authorizationUrl = authService.getAuthorizationUrl();

        // RestTemplate을 사용하여 인증 요청을 보내고 결과를 받음
        ResponseEntity<String> response = restTemplate.getForEntity(authorizationUrl, String.class);

        return response; // 인증 서버의 응답을 클라이언트에 반환
    }


    @GetMapping("/callback")
    public String callback(HttpServletRequest request,
                           @RequestParam(value = "code", required = false) String code,
                           @RequestParam(value = "state", required = false) String state) {
       log.info("@cafe24 callback method called : authorization_code");
        if (code == null || code.isEmpty()) {
            log.info("Authorization code not found");
            return "Code parameter is missing.";
        }

        // 세션에서 CSRF 토큰을 가져옴
        String storedCsrfToken = (String) request.getSession().getAttribute("csrfToken");
        String originalUrl = (String) request.getSession().getAttribute("originalUrl"); // 원래 URL 저장

        log.info("Stored CSRF Token: {}", storedCsrfToken); // 로그 추가

        // CSRF 토큰 검증
        if (!state.equals(storedCsrfToken)) {
            log.warn("CSRF token mismatch!");
            return "Invalid CSRF token.";
        }

        log.info("@callback:: code = {}, state = {}", code, state);

        // Access Token 요청
        TokenResponse tokenResponse = authService.obtainNewAccessToken(code);

        if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
            return "Failed to obtain access token.";
        }
        // 유효하지 않는 토큰 삭제
        tokenMapper.deleteInvalidTokens();

        // 액세스 토큰과 리프레시 토큰 저장
        AccessToken tokenEntity = new AccessToken();
        tokenEntity.setToken(tokenResponse.getAccessToken());
        tokenEntity.setRefreshToken(tokenResponse.getRefreshToken()); // 리프레시 토큰 저장
        tokenEntity.setExpirationAt(tokenResponse.getExpiresIn()); // 예: 접근 토큰은 발급 받은 후 2시간이 지나면 사용할 수 없습니다.
        tokenMapper.insertAccessToken(tokenEntity); // MyBatis를 통해 DB에 저장

        // 원래 요청한 URL로 리다이렉트
        if (originalUrl != null) {

            String response = apiCallManager.callOriginalUrl(originalUrl);//서버 호출

            return response; // 원래 URL로 리다이렉트
        }

        return "Access Token received successfully!";
    }

}
