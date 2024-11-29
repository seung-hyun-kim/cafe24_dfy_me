package one.dfy.cafe24.ygplus.buffz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.minidev.json.JSONObject;
import one.dfy.cafe24.ygplus.buffz.service.OrderService;
import one.dfy.cafe24.ygplus.common.interfaces.ApiResponseInterface;
import one.dfy.cafe24.ygplus.utils.JsonUtil;
import one.dfy.cafe24.ygplus.component.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "ygplus api", description = "demo-목록")
@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private TokenManager tokenComponent;

    @GetMapping(value="/api/order/{orderId}", produces = "application/json")
    @ResponseBody
    @Operation(method = "GET", summary = "목록", description = "cafe24 ygplus API DEMO")
    public ApiResponseInterface<JSONObject> getCustomerPrivacy(HttpServletResponse res,
                                                               HttpServletRequest req,
                                                               @PathVariable String orderId) throws IOException {

        String accessToken = tokenComponent.getValidAccessToken(res, req); // 공통 메소드 호출

        // 액세스 토큰이 유효한지 확인
        if (accessToken == null) {
            return ApiResponseInterface
                    .<JSONObject>builder()
                    .message("Access token is not available or invalid.")
                    .body(null) // body를 null로 설정
                    .build();
        }

        // 주문 아이템 가져오기
        JSONObject orderItem = JsonUtil.convertStringToJsonObject(orderService.getOrderItem(accessToken, res, orderId));

        return ApiResponseInterface
                .<JSONObject>builder()
                .body(orderItem)
                .message("success")
                .build();

    }
}
