package one.dfy.cafe24.ygplus.buffz.service.Impl;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import one.dfy.cafe24.ygplus.buffz.service.CustomersService;
import one.dfy.cafe24.ygplus.component.ApiCallManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Slf4j
@Service
public class CustomersServiceImpl implements CustomersService {

    private String storeName;

    @Autowired
    private ApiCallManager apiCallManager;

    public CustomersServiceImpl() {
        Dotenv dotenv = Dotenv.load();
        this.storeName = dotenv.get("CAFE24_STORE");
    }

    @Override
    public String getCustomers(String accessToken, String memberId) throws IOException {

        String apiUrl = String.format("https://%s.cafe24api.com/api/v2/admin/customers?member_id=%s", storeName, memberId);

        // API 호출 URL 로그 출력
        log.info("API URL: " + apiUrl);

        String resItem = apiCallManager.callApi(HttpMethod.GET, apiUrl, accessToken);

        return resItem;
    }
}
