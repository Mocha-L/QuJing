package leon.qujing.objectparser;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

import leon.qujing.QuJingServer;

public class StringMapParser implements QuJingServer.ObjectParser {
    @Override
    public Object parse(String data) {
        JSONObject jsonObject = JSONObject.parseObject(data);
        Map<String, Object> map = (Map<String, Object>)jsonObject;
        return map;
    }

    @Override
    public String generate(Object obj) {
        return obj.toString();
    }
}