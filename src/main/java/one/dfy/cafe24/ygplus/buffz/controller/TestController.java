package one.dfy.cafe24.ygplus.buffz.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import one.dfy.cafe24.ygplus.buffz.service.BuffzService;
import one.dfy.cafe24.ygplus.common.interfaces.ApiResponseInterface;
import one.dfy.cafe24.ygplus.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "ygplus api", description = "demo-목록")
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/{version}")
public class TestController {

    @Autowired
    private BuffzService buffzService;

    @GetMapping(value="/userList", produces = "application/json")
    @ResponseBody
    @Operation(method = "GET", summary = "목록", description = "cafe24 ygplus API DEMO")
    public ApiResponseInterface<JSONObject> totalList(@PathVariable(value="version")String version) {

        System.out.println("home controller totalList");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("offset", 10);
        paramMap.put("offset", 1);

        return ApiResponseInterface
                .<JSONObject>builder()
                .body(JsonUtil.convertObjectToJsonObject(buffzService.getTotalList(paramMap)))
                .version(version)
                .message("success")
                .build();
    }



    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Object test() {
        return "Hello World!";
    }
}
