package one.dfy.cafe24.ygplus.buffz.dto;

import lombok.Data;
import java.util.List;

@Data
public class CustomerResponse {
    private List<Customer> customers;

    @Data
    public static class Customer {
        private int shop_no;
        private String member_id;
        private int group_no; // group_no는 정수형
        private String member_authentication;
        private String use_blacklist;
        private String blacklist_type;
        private String authentication_method;
        private String sms;
        private String news_mail;
        private String solar_calendar;
        private String total_points;
        private String available_points;
        private String used_points;
        private String last_login_date;
        private String created_date;
        private String gender;
        private String use_mobile_app;
        private String available_credits;
        private String fixed_group;

        // member_id에 대한 getter 메서드
        public String getMember_id() {
            return member_id;
        }
    }
}

