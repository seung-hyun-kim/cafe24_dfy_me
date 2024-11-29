package one.dfy.cafe24.ygplus.buffz.mapper;

import one.dfy.cafe24.ygplus.buffz.dto.Member;
import one.dfy.cafe24.ygplus.buffz.dto.MemberDTO;
import one.dfy.cafe24.ygplus.buffz.dto.UserInfoDto;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.binding.MapperMethod;
import java.util.List;
import java.util.Map;

@Mapper
public interface BuffzMapper {
    List<UserInfoDto> userList(Map<String, Object> params);

    int insertUser(UserInfoDto user);

    //최대 Id 조회
    @Select("SELECT COALESCE(MAX(id), 0) FROM tbl_member")
    int findMaxId();

    // MemberDTO를 tbl_member에 삽입
    @Insert("INSERT INTO tbl_member (id, member_id,first_order_date, use_yn, update_date) " +
            "VALUES (#{id}, #{memberId}, #{firstOrderDate}, #{useYn}, #{updateDate})")
    int insertMember(MemberDTO memberDTO);

    @Select("SELECT * FROM tbl_member WHERE DATE(update_date) = CURRENT_DATE AND use_yn = 'Y'")
    List<Member> findMembersToDowngrade();

    @Update("UPDATE tbl_member SET use_yn = #{member.useYn} " +
            "WHERE id = #{member.id} AND member_id = #{member.memberId}")
    void updateMember(@Param("member") Member member);

}

