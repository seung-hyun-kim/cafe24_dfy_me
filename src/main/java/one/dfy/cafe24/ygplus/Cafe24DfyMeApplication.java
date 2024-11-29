package one.dfy.cafe24.ygplus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication//매퍼 패키지 지정
@EnableScheduling
@MapperScan("one.dfy.cafe24.ygplus.buffz.mapper")
public class Cafe24DfyMeApplication {

    public static void main(String[] args) {
        SpringApplication.run(Cafe24DfyMeApplication.class, args);
    }

}
