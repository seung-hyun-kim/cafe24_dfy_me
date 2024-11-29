package one.dfy.cafe24.ygplus.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class JobScheduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job downgradeMembershipJob;

    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(cron = "0 10 17 * * ?") // 매일 17:10에 실행
    //corn 사용 : 0초 또는 10초 일때만 job 실행 테스트
    //@Scheduled(cron = "0/10 * * * * ?") // 10초마다 실행
    public void runJob() {
        try {
            // 액세스 토큰을 추출하는 메서드 호출
            String accessToken = extractAccessToken();

            log.info("accessToken==============: {}", accessToken);

            if (accessToken == null || accessToken.isEmpty()) {
                log.error("Access token is null or empty");
                return; // 액세스 토큰이 없으면 작업을 중단
            }

            // JobParameters에 액세스 토큰 추가
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("accessToken", accessToken) // 액세스 토큰 추가
                    .addLong("time", System.currentTimeMillis()) // 고유한 파라미터 추가
                    .toJobParameters();

            // 배치 작업 실행
            jobLauncher.run(downgradeMembershipJob, jobParameters);
        } catch (Exception e) {
            log.error("Job failed to run: {}", e.getMessage());
        }
    }
    // 액세스 토큰을 추출하는 메서드
    private String extractAccessToken() {
        String url = "https://dev.dfz.co.kr/authorization"; // 실제 URL로 변경
        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(url, null, String.class);

        // 응답에서 액세스 토큰 추출
        return tokenResponse.getBody();
    }

}

