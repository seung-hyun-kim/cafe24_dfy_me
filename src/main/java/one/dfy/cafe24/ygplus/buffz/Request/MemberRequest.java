package one.dfy.cafe24.ygplus.buffz.Request;


import lombok.Data;

// 요청 정보를 담는 클래스
@Data
public class MemberRequest {
    private String memberId;
    private String fixedGroup;

    public MemberRequest(String memberId, String fixedGroup) {
        this.memberId = memberId;
        this.fixedGroup = fixedGroup;
    }
}
