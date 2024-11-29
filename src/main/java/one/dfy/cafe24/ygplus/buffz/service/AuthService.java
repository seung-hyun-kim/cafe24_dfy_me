package one.dfy.cafe24.ygplus.buffz.service;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import one.dfy.cafe24.ygplus.buffz.dto.TokenResponse;

import java.io.IOException;

public interface AuthService {
 TokenResponse obtainNewAccessToken(String authorizationCode);
 TokenResponse getAccessToken(HttpServletResponse res, HttpServletRequest req) throws IOException;
 String getAuthorizationUrl() throws IOException;
}
