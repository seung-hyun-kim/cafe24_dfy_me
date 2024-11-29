package one.dfy.cafe24.ygplus.buffz.mapper;

import one.dfy.cafe24.ygplus.buffz.dto.AccessToken;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface TokenMapper {

    @Delete("DELETE FROM tbl_access_token WHERE expiration_at < NOW()") // 만료된 토큰 삭제
    void deleteInvalidTokens();

    @Insert("INSERT INTO tbl_access_token(token, refresh_token, code, expiration_at) VALUES(#{token}, #{refreshToken}, #{code}, #{expirationAt})")
    void insertAccessToken(AccessToken accessToken);

    @Select("SELECT * FROM tbl_access_token ORDER BY expiration_at DESC LIMIT 1")
    AccessToken findLatestToken();

    @Select("SELECT token FROM tbl_access_token WHERE expiration_at > #{currentDateTime} LIMIT 1")
    String getValidAccessToken(@Param("currentDateTime") LocalDateTime currentDateTime);

    @Select("SELECT refresh_token FROM tbl_access_token WHERE expiration_at > #{currentDateTime} LIMIT 1")
    String getRefreshAccessToken(@Param("currentDateTime") LocalDateTime currentDateTime);


}
