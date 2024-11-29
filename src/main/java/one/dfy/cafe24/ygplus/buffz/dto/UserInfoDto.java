package one.dfy.cafe24.ygplus.buffz.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserInfoDto {
    private String memberId;
    private String userIp;
    private String useYn;
    private String orderedDate;
}
