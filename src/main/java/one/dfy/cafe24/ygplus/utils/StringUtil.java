package one.dfy.cafe24.ygplus.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * packageName    : com.dfy.ocean_be.utils
 * fileName       : StringUtil
 * author         : polarium
 * date           : 2024-08-01
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-08-01        polarium       최초 생성
 */

public class StringUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }


    public static String escape(String str) {
        if(str == null) return null;
        return str.replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_")
                .replace("<", "!<")
                .replace("[", "![");
    }
}
