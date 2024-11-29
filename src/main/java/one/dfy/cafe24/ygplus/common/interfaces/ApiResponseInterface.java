package one.dfy.cafe24.ygplus.common.interfaces;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * packageName    : com.dfy.ocean_be.common.interfaces
 * fileName       : ResponseInterface
 * author         : polarium
 * date           : 2024-07-03
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-07-03        polarium       최초 생성
 */
@Getter
@Setter
@Builder
@JsonSerialize
public class ApiResponseInterface<T> {
    private T body;
    private String version;
    private String message;

    public ApiResponseInterface(T body, @Nullable String version, String message) {
        this.body = body;
        this.message = message;
        this.version = version;
    }

    // 정적 메소드로 메시지를 설정하기 위한 빌더 반환
    public static <T> ApiResponseInterfaceBuilder<T> message(String message) {
        return ApiResponseInterface.<T>builder().message(message);
    }
}