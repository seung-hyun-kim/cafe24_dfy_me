package one.dfy.cafe24.ygplus.buffz.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class MockDataService {

    public String getMockDataOrder() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // JSON 파일을 String으로 읽어오기
        return objectMapper.writeValueAsString(objectMapper.readValue(new File("src/main/resources/mockup/order.json"), Object.class));
    }
    public String getMockDataOrders() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // JSON 파일을 String으로 읽어오기
        return objectMapper.writeValueAsString(objectMapper.readValue(new File("src/main/resources/mockup/orders.json"), Object.class));
    }
    public String getMockDataOrderItems() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // JSON 파일을 String으로 읽어오기
        return objectMapper.writeValueAsString(objectMapper.readValue(new File("src/main/resources/mockup/order_items.json"), Object.class));
    }
    public String getMockDataOrderBuyer() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // JSON 파일을 String으로 읽어오기
        return objectMapper.writeValueAsString(objectMapper.readValue(new File("src/main/resources/mockup/order_buyer.json"), Object.class));
    }
}

