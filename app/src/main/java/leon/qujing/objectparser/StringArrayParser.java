package leon.qujing.objectparser;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

import leon.qujing.QuJingServer;

public class StringArrayParser implements QuJingServer.ObjectParser {
    @Override
    public Object parse(String data) {
        List<String> list = JSONObject.parseArray(data, String.class);
        int size = list.size();
        String[] array = (String[])list.toArray(new String[size]);
        return array;
    }

    @Override
    public String generate(Object obj) {
        return obj.toString();
    }
}