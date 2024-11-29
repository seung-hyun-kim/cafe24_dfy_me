package one.dfy.cafe24.ygplus.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import one.dfy.cafe24.ygplus.buffz.Request.MemberRequest;
import one.dfy.cafe24.ygplus.buffz.dto.CustomerResponse;
import one.dfy.cafe24.ygplus.buffz.dto.Member;
import one.dfy.cafe24.ygplus.buffz.dto.MemberDTO;
import one.dfy.cafe24.ygplus.buffz.service.MockDataService;
import one.dfy.cafe24.ygplus.component.ApiCallManager;
import one.dfy.cafe24.ygplus.processor.MemberItemProcessor;
import one.dfy.cafe24.ygplus.reader.MemberItemReader;
import one.dfy.cafe24.ygplus.utils.JsonUtil;
import one.dfy.cafe24.ygplus.writer.MemberItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
public class BatchConfig {

    private static final int chunkSize = 10;

    private String storeName;

    public BatchConfig() {
        Dotenv dotenv = Dotenv.load();
        this.storeName = dotenv.get("CAFE24_STORE");
    }

    @Autowired
    private MemberItemReader memberItemReader;

    @Autowired
    private MemberItemProcessor memberItemProcessor;

    @Autowired
    private MemberItemWriter memberItemWriter;

    @Autowired
    private ApiCallManager apiCallManager;

    @Autowired
    private MockDataService mockDataService;

    @Bean
    public Job downgradeMembershipJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("========================================================= downgradeMembershipJob");
        return new JobBuilder("downgradeMembershipJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(downgradeMembershipStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    @JobScope
    public Step downgradeMembershipStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("========================================================= downgradeMembershipStep");
        return new StepBuilder("downgradeMembershipStep", jobRepository)
                .<Member, List<Member>>chunk(chunkSize, transactionManager)
                .reader(memberItemReader.reader())
                .processor(processor(null))// MemberItemProcessor를 주입
                .writer(memberItemWriter)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Member, List<Member>> processor(@Value("#{jobParameters['accessToken']}") String accessToken) {
        log.info("processor accessToken=====>: {}", accessToken);
        return member -> {
            // 액세스 토큰이 null이거나 비어있는 경우
            if (accessToken == null || accessToken.isEmpty()) {
                log.info("Access token is null or empty. Skipping API call for member ID: {}", member.getMemberId());
                return Collections.emptyList(); // 비어있는 리스트 반환
            }

            try {
                // 주문 목록 API 호출
                List<Member> memberList = fetchOrders(member.getId(), member.getMemberId(), accessToken);
                return memberList; // 호출 결과 반환
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

    private List<Member> fetchOrders(int id, String memberId, String accessToken) throws Exception {

        log.info("fetchOrders accessToken=====>: {}", accessToken);

        // 현재 날짜를 기준으로 3일 전의 날짜를 계산
        LocalDate endDate = LocalDate.now(); // 현재 날짜
        LocalDate startDate = endDate.minusDays(3); // 현재 날짜에서 3일 전

        // 날짜를 문자열 형식으로 포맷팅
        String startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String endDateStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        String apiUrl = String.format("https://%s.cafe24api.com/api/v2/admin/orders?start_date=%s&end_date=%s&buyer=%s",
                storeName, startDateStr, endDateStr, memberId);

        // API 호출 URL 로그 출력
        log.info("API URL>: {}", apiUrl);

        String resItem = apiCallManager.callApi(HttpMethod.GET, apiUrl, accessToken);

        // todo:테스트 데이터 개발완료 후 삭제요망
        // response.getBody()가 null이거나 빈 JSON 객체일 경우 mock_data.json을 읽어옴
        if (resItem == null || JsonUtil.isEmptyJson(JsonUtil.stringToJsonNode(resItem),"orders")) {

            resItem = mockDataService.getMockDataOrders();
        }

        // 응답 로그 출력
        log.info("Response JSON Data =====>: {}", resItem);

        // JSON 파싱하여 Order 배열로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        // resItem을 JsonNode로 변환 후 orders 필드 추출
        JsonNode jsonNode = objectMapper.readTree(resItem);
        JsonNode ordersNode = jsonNode.get("orders");

        List<Member> memberList = new ArrayList<>();

        // JSON을 파싱하여 구독 정보를 가져옴
        JsonNode orderNode = objectMapper.readTree(resItem).get("orders");

        // ordersNode가 배열인지 확인
        if (ordersNode.isArray()) {

            for (JsonNode order : ordersNode) {

                JsonNode subscriptionNode = order.get("subscription"); // 각 객체에서 subscription 가져오기
                JsonNode canceledNode = order.get("canceled");

                if (subscriptionNode != null) {
                    String subscription = subscriptionNode.asText(); // 값 가져오기

                    LocalDate updateDate = LocalDate.now(); // 업데이트 날짜 설정

                    Member memberDTO = new Member();

                    memberDTO.setId(id);
                    memberDTO.setMemberId(memberId);
                    //canceled 취소여부 F:미취소 T:취소 M:부분취소
                    if ("T".equals(subscription) && "F".equals(canceledNode.asText())) {
                        memberDTO.setUseYn("Y");
                        memberDTO.setUpdateDate(updateDate);
                    } else {
                        memberDTO.setUseYn("N");
                        this.downgradeMembershipApi(memberId, accessToken);
                    }

                    memberList.add(memberDTO);

                } else {
                    log.info("Subscription key not found in this order.");
                }

            }

        }


        return memberList != null ? memberList : Collections.emptyList();
    }


    private void downgradeMembershipApi(String memberId, String accessToken) throws Exception {

        log.info("1개월 동안 결재 이력이 없는 고객 강등 Api accessToken=====>: {}", accessToken);

        //특정 등급에 회원을 추가할 수 있습니다.
        String apiUrl = String.format("https://%s.cafe24api.com/api/v2/admin/customergroups/1/customers",storeName);
        // API 호출 URL 로그 출력
        log.info("API URL>: " + apiUrl);

        /***********
        국문몰 = 1
        영문몰 = 2
        일문몰 = 3
        중문몰 = 4
         *************/
        int shopNo = 1;
        int groupNo = 1;

        List<MemberRequest> requests = new ArrayList<>();
        /*requests.add(new MemberRequest("master13924", "T"));*/
        /***********
        //고정
        //T : 고정함 / F : 고정안함
        //DEFAULT F
         *************/
        requests.add(new MemberRequest(memberId, "F"));

        String requestBody = createRequestBody(shopNo, groupNo, requests);

        String resItem = apiCallManager.callApiPost(HttpMethod.POST, apiUrl, accessToken, requestBody);

    }

    public String createRequestBody(int shopNo, int groupNo, List<MemberRequest> requests) {
        StringBuilder requestBody = new StringBuilder();

        requestBody.append("{\n");
        requestBody.append(String.format("    \"shop_no\": %d,\n", shopNo));
        requestBody.append(String.format("    \"group_no\": %d,\n", groupNo));
        requestBody.append("    \"requests\": [\n");

        for (int i = 0; i < requests.size(); i++) {
            MemberRequest request = requests.get(i);
            requestBody.append("        {\n");
            requestBody.append(String.format("            \"member_id\": \"%s\",\n", request.getMemberId()));
            requestBody.append(String.format("            \"fixed_group\": \"%s\"\n", request.getFixedGroup()));
            requestBody.append("        }");

            // 마지막 요소가 아닐 경우 쉼표 추가
            if (i < requests.size() - 1) {
                requestBody.append(",");
            }
            requestBody.append("\n");
        }

        requestBody.append("    ]\n");
        requestBody.append("}");

        return requestBody.toString();
    }


    @PostConstruct
    public void init() {
        log.info("MemberItemReader: " + memberItemReader);
        log.info("MemberItemProcessor: " + memberItemProcessor);
        log.info("MemberItemWriter: " + memberItemWriter);
    }


}
