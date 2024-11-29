package one.dfy.cafe24.ygplus.buffz.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import one.dfy.cafe24.ygplus.buffz.dto.MemberDTO;
import one.dfy.cafe24.ygplus.buffz.dto.UserInfoDto;
import one.dfy.cafe24.ygplus.buffz.mapper.BuffzMapper;
import one.dfy.cafe24.ygplus.buffz.service.MockDataService;
import one.dfy.cafe24.ygplus.buffz.service.OrderService;
import one.dfy.cafe24.ygplus.component.ApiCallManager;
import one.dfy.cafe24.ygplus.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private String storeName;
    private String storeAddress;

    @Autowired
    private ApiCallManager apiCallManager;

    @Autowired
    private MockDataService mockDataService;

    @Autowired
    private BuffzMapper buffzMapper;

    public OrderServiceImpl() {
        Dotenv dotenv = Dotenv.load();
        this.storeName = dotenv.get("CAFE24_STORE");
        this.storeAddress = dotenv.get("CAFE24_CUSTOM_DOMAIN");

        if (this.storeName == null || this.storeAddress == null) {
            throw new IllegalArgumentException("Environment variables CAFE24_STORE and CAFE24_CUSTOM_DOMAIN must be set.");
        }
    }

    //1)번 처리가 진행될 때 아래 API로 조회한 정기배송 상품을 회원 별로 귀사 DB에 적재
    //subscription : 정기결제 여부 (T|F)
    @Override
    public String getOrderItem(String accessToken, HttpServletResponse res, String orderId) throws IOException {

        //주문 1건을 조회할 수 있습니다.
        //주문번호, 회원아이디, 결제수단 등을 조회할 수 있습니다.
        //하위 리소스들을 embed 로 활용하면 한번의 호출에 필요한 정보를 더 많이 조회할 수 있습니다.
        String apiUrl = String.format("https://%s.cafe24api.com/api/v2/admin/orders/%s",storeName, orderId);

        // API 호출 URL 로그 출력
        log.info("API URL: " + apiUrl);

        String resItem = apiCallManager.callApi(HttpMethod.GET, apiUrl, accessToken);

        // response.getBody()가 null이거나 빈 JSON 객체일 경우 mock_data.json을 읽어옴
        if (resItem == null || JsonUtil.isEmptyJson(JsonUtil.stringToJsonNode(resItem),"order")) {
            try {

                resItem = mockDataService.getMockDataOrder();

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(resItem);
                JsonNode orderNode = jsonNode.get("order");
                if (orderNode != null) {

                    String subscription = orderNode.get("subscription").asText();

                    /*정기결제 여부(T/F)
                    T : 정기결제
                    F : 정기결제 아님*/
                    if ("T".equals(subscription)) {

                        String memberId = orderNode.get("member_id").asText();

                        if (memberId != null) {
                            // ordered_date 가져오기
                            String orderedDateStr = orderNode.get("order_date").asText();
                            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                            try {
                                // LocalDateTime으로 파싱
                                LocalDateTime orderedDateTime = LocalDateTime.parse(orderedDateStr, inputFormatter);
                                // LocalDate로 변환
                                LocalDate orderedDate = orderedDateTime.toLocalDate();
                                LocalDate updateDate = orderedDate.plusDays(31); // "yyyy-MM-dd" 형식으로 변환

                                // 날짜를 문자열로 변환
                                String formattedUpdateDate = updateDate.format(outputFormatter);

                                Integer maxId = buffzMapper.findMaxId(); // 최대 ID 조회
                                MemberDTO memberDTO = new MemberDTO();
                                int nextId = maxId+1;
                                memberDTO.setId(nextId); // tbl_member
                                memberDTO.setMemberId(memberId);
                                memberDTO.setFirstOrderDate(orderedDateTime); // LocalDateTime으로 설정
                                memberDTO.setUseYn("Y");
                                memberDTO.setUpdateDate(updateDate);

                                //int result = buffzMapper.insertUser(memberDto);
                                int result = buffzMapper.insertMember(memberDTO);

                            } catch (DateTimeParseException e) {
                                System.out.println("Error parsing date: " + e.getMessage());
                            }
                        } else {
                            log.warn("buyer 객체가 존재하지 않습니다.");
                        }

                    /*member.setSubscription(subscription);
                    memberRepository.save(member);*/
                    }



                } else {
                    log.info("order is null or not array");
                }




                return mockDataService.getMockDataOrderItems();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {

            //주문번호로 구매자 정보 가져오는 api호출 후 member_id 가져옴
            String apiUrlBuyer = String.format("https://%s.cafe24api.com/api/v2/admin/orders/%s/buyer",storeAddress, orderId);
            String buyerResponse = apiCallManager.callApi(HttpMethod.GET, apiUrlBuyer, accessToken);


        }

        return resItem;
    }

}
