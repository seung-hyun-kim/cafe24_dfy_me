package one.dfy.cafe24.ygplus.buffz.service;

import java.io.IOException;

public interface CustomersService {

    String getCustomers(String accessToken, String memberId) throws IOException;

}
