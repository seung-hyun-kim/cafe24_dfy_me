package one.dfy.cafe24.ygplus.component;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class ApiCallManager {

    private final String version;

    @Autowired
    private RestTemplate restTemplate;

    public ApiCallManager() {
        Dotenv dotenv = Dotenv.load();
        this.version = dotenv.get("CAFE24_VERSION");
    }


    public String callApi(HttpMethod httpMethod, String url, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Cafe24-Api-Version", version); // 헤더에 버전 추가

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, entity, String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("API 호출 실패: " + response.getStatusCode());
            }
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("클라이언트 오류: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        } catch (HttpServerErrorException e) {
            log.info("서버 오류: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.info("예외 발생: " + e.getMessage());
            e.printStackTrace();
            throw e; // 또는 적절한 오류 처리
        }
    }
    public String callApiPost(HttpMethod httpMethod, String url, String accessToken, String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("X-Cafe24-Api-Version", version); // 헤더에 버전 추가

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers); // 요청 본문과 헤더를 포함

        try {

            log.info("API 호출: URL: {}, Method: {}, Headers: {}, Body: {}", url, httpMethod, headers, requestBody);
            ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, entity, String.class);

            // 응답 상태 코드에 따라 로그 출력
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("API 호출 성공: " + response.getStatusCode() + " - 응답 본문: " + response.getBody());
            } else if (response.getStatusCode() == HttpStatus.CREATED) {
                log.info("API 호출 성공: " + response.getStatusCode() + " - 자원 생성됨 - 응답 본문: " + response.getBody());
                // 필요한 경우 생성된 자원에 대한 추가 처리
                return response.getBody(); // 생성된 자원 정보를 반환
            } else {
                log.error("API 호출 실패: " + response.getStatusCode() + " - 응답 본문: " + response.getBody());
                throw new RuntimeException("API 호출 실패: " + response.getStatusCode());
            }

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("클라이언트 오류: " + e.getStatusCode() + " - 응답 본문: " + e.getResponseBodyAsString());
            throw e;
        } catch (HttpServerErrorException e) {
            log.error("서버 오류: " + e.getStatusCode() + " - 응답 본문: " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("예외 발생: " + e.getMessage());
            e.printStackTrace();
            throw e; // 또는 적절한 오류 처리
        }
    }

    public String callOriginalUrl(String originalUrl) {
        // 원래 요청 URL로 GET 요청
        return restTemplate.getForObject(originalUrl, String.class);
    }
}

