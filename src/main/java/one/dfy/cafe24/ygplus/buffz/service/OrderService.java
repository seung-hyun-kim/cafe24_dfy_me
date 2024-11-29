package one.dfy.cafe24.ygplus.buffz.service;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface OrderService {

    String getOrderItem(String accessToken, HttpServletResponse response, String orderId) throws IOException;
}
