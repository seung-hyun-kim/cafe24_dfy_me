package one.dfy.cafe24.ygplus.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import one.dfy.cafe24.ygplus.buffz.dto.CustomerResponse;
import one.dfy.cafe24.ygplus.buffz.dto.Member;
import one.dfy.cafe24.ygplus.component.ApiCallManager;
import one.dfy.cafe24.ygplus.writer.MemberItemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;

@Component
public class MemberItemProcessor {

    /*private static final Logger log = LoggerFactory.getLogger(MemberItemProcessor.class);

    private String storeName;

    public MemberItemProcessor() {
        Dotenv dotenv = Dotenv.load();
        this.storeName = dotenv.get("CAFE24_STORE");
    }

    @Autowired
    private ApiCallManager apiCallManager;

    @Bean
    @StepScope
    public ItemProcessor<Member, List<CustomerResponse>> processor(@Value("#{jobParameters['accessToken']}") String accessToken) {

        log.info("accessToken >>>>>>>>>>>>>: {}", accessToken);
        return member -> {
            // 액세스 토큰이 null이거나 비어있는 경우
            if (accessToken == null || accessToken.isEmpty()) {
                log.info("Access token is null or empty. Skipping API call for member ID: {}", member.getMemberId());
                return Collections.emptyList(); // 비어있는 리스트 반환
            }

            try {
                // 주문 목록 API 호출
                List<CustomerResponse> orders = fetchOrders(member.getMemberId(), accessToken);
                return orders; // 호출 결과 반환
            } catch (HttpClientErrorException e) {
                log.error("HTTP error while fetching orders for member ID: {} - Status: {} - Response: {}",
                        member.getMemberId(), e.getStatusCode(), e.getResponseBodyAsString());
                return Collections.emptyList(); // 오류 발생 시 비어있는 리스트 반환
            } catch (Exception e) {
                log.error("Failed to fetch orders for member ID: {} - Error: {}", member.getMemberId(), e.getMessage());
                return Collections.emptyList(); // 오류 발생 시 비어있는 리스트 반환
            }
        };
    }

    private List<CustomerResponse> fetchOrders(String memberId, String accessToken) throws Exception {
        String apiUrl = String.format("https://%s.cafe24api.com/api/v2/admin/orders?start_date=2024-01-01&end_date=2024-12-31&buyer=%s", storeName, memberId);

        // API 호출 URL 로그 출력
        log.info("API URL: " + apiUrl);

        String resItem = apiCallManager.callApi(HttpMethod.GET, apiUrl, accessToken);

        // 응답 로그 출력
        log.info("Response JSON Data: " + resItem);

        // JSON 파싱하여 Order 배열로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        CustomerResponse[] orders;
        try {
            orders = objectMapper.readValue(resItem, CustomerResponse[].class);
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류: " + e.getMessage(), e);
            return Collections.emptyList();
        }

        return orders != null ? List.of(orders) : Collections.emptyList();
    }*/
}

