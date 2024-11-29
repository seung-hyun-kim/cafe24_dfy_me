package one.dfy.cafe24.ygplus.buffz.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccessToken {
    private Long id;
    private String token;
    private String refreshToken;
    private LocalDateTime expirationAt;
    private String code; // 추가된 코드 값
}
