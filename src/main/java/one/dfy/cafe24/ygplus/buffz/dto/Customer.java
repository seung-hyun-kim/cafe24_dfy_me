package one.dfy.cafe24.ygplus.buffz.dto;

import java.io.Serializable;

public class Customer implements Serializable {
    private Long memberId;          // 고객 ID
    private String name;      // 고객 이름
    private String email;     // 고객 이메일
    private String phone;     // 고객 전화번호
    private int grade;        // 회원 등급
    private boolean subscription; // 정기배송 여부

    // 기본 생성자
    public Customer() {}

    // 모든 필드를 포함하는 생성자
    public Customer(Long id, String name, String email, String phone, int grade, boolean subscription) {
        this.memberId = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.grade = grade;
        this.subscription = subscription;
    }

    // Getter 및 Setter 메서드
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public boolean isSubscription() {
        return subscription;
    }

    public void setSubscription(boolean subscription) {
        this.subscription = subscription;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "member_id=" + memberId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", grade=" + grade +
                ", subscription=" + subscription +
                '}';
    }
}
