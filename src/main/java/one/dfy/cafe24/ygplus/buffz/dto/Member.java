package one.dfy.cafe24.ygplus.buffz.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Member {
    private int id;
    private String memberId;
    private LocalDate firstOrderDate;
    private LocalDate updateDate;
    private String useYn;
    private int orderCount;
}