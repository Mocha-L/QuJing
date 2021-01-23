package leon.qujing.api;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

import leon.qujing.QuJingServer;
import leon.qujing.XposedEntry;
import leon.qujing.handler.ClassHandler;

//类详情查看页面
public class ClassView implements QuJingServer.Operation {
    @Override
    public String handle(String url, Map<String, String> parms, Map<String, String> headers, Map<String, String> files) {
        try {
            HashMap<String, Object> detail = ClassHandler.getClassDetail(
                    ClassHandler.findClassbyName(parms.get("class"), XposedEntry.classLoader));
            return JSON.toJSONString(detail);
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }
}
