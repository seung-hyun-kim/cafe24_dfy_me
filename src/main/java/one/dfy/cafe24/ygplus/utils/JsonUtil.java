package one.dfy.cafe24.ygplus.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // JSON 문자열을 JsonNode로 변환
    public static JsonNode stringToJsonNode(String jsonString) {
        try {
            return objectMapper.readTree(jsonString);
        } catch (Exception e) {
            return null; // 변환 실패 시 null 반환
        }
    }
    public static JSONObject convertObjectToJsonObject(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(object);
            JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
            return (JSONObject) parser.parse(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static JSONObject convertStringToJsonObject(String jsonString) {
        try {
            JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
            return (JSONObject) parser.parse(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isEmptyJson(JsonNode jsonNode, String key) {
        // 주어진 키가 존재하고, 그 값이 비어 있는 배열이거나 비어 있는 객체인 경우
        if (jsonNode.has(key)) {
            JsonNode itemsNode = jsonNode.get(key);

            // 배열인 경우
            if (itemsNode.isArray() && itemsNode.isEmpty()) {
                return true;
            }

            // 객체인 경우
            if (itemsNode.isObject() && itemsNode.size() == 0) {
                return true;
            }
        }

        return false;
    }
}
