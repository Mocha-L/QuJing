package leon.qujing.objectparser;

import com.alibaba.fastjson.JSONObject;
import java.util.List;

import leon.qujing.QuJingServer;

public class StringArrayListParser implements QuJingServer.ObjectParser {
    @Override
    public Object parse(String data) {
        List<String> list = JSONObject.parseArray(data, String.class);
        return list;
    }

    @Override
    public String generate(Object obj) {
        return obj.toString();
    }
}