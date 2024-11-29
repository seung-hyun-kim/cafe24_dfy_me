package one.dfy.cafe24.ygplus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class BatchConfigTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job downgradeMembershipJob;

    @Autowired
    private JobRepository jobRepository;

    /*@Test
    public void testDowngradeMembershipJob() throws Exception {
        // JobParameters 생성 (필요한 경우 추가 파라미터 설정)
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        // Job 실행
        JobExecution jobExecution = jobLauncher.run(downgradeMembershipJob, jobParameters);

        // Job 실행 결과 검증
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        // 추가적인 검증 로직을 여기에 추가할 수 있습니다.
    }*/
}

