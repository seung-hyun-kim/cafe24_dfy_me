package one.dfy.cafe24.ygplus.buffz.dto;

import java.time.LocalDateTime;

public class TokenResponseMap {
    private String accessToken;
    private String refreshToken;
    private LocalDateTime expiresIn;

    // 생성자
    public TokenResponseMap(String accessToken, String refreshToken, LocalDateTime expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    // Getter 및 Setter
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    public LocalDateTime getExpiresIn() {
        return expiresIn;
    }
    public void setExpiresIn(LocalDateTime expiresIn) {
        this.expiresIn = expiresIn;
    }
}

