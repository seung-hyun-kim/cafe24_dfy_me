package one.dfy.cafe24.ygplus.buffz.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import one.dfy.cafe24.ygplus.buffz.service.CustomersService;
import one.dfy.cafe24.ygplus.component.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class CustomerController {
    @Autowired
    private CustomersService customersService;

    @Autowired
    private TokenManager tokenComponent;

    @GetMapping("/api/customers")
    public String getCustomers(HttpServletResponse res,
                               HttpServletRequest req,
                               @RequestParam(value = "member_id", required = false) String memberId) throws IOException {

        String accessToken = tokenComponent.getValidAccessToken(res, req); // 공통 메소드 호출

        // 액세스 토큰이 유효한지 확인
        if (accessToken == null) {
            return "Access token is not available.";
        }

        return customersService.getCustomers(accessToken, memberId);
    }
}
