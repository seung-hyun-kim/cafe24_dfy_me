package one.dfy.cafe24.ygplus.buffz.service;

import net.minidev.json.JSONObject;
import one.dfy.cafe24.ygplus.buffz.dto.BuffzResponse;
import one.dfy.cafe24.ygplus.buffz.dto.UserInfoDto;
import one.dfy.cafe24.ygplus.buffz.mapper.BuffzMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BuffzService {

    @Autowired
    BuffzMapper buffzMapper;


    public BuffzResponse getTotalList(Map<String, Object> params) {
        List<UserInfoDto> userList = buffzMapper.userList(params);
        return new BuffzResponse(userList);
    }

}
