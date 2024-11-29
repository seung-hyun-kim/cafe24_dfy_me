package one.dfy.cafe24.ygplus.buffz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;                     // 아이디
    private String memberId;                // 회원 아이디
    private LocalDateTime firstOrderDate;            // 최초 주문일
    private LocalDate updateDate;                 // 갱신일
    private String useYn;                    // 사용 여부
    private Integer orderCount;              // 주문 회차
}
