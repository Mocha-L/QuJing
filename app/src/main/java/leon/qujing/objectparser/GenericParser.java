package leon.qujing.objectparser;

import leon.qujing.QuJingServer;

public class GenericParser implements QuJingServer.ObjectParser {
    @Override
    public java.lang.Object parse(java.lang.String data) {
        //无法还原
        return null;
    }

    @Override
    public String generate(Object obj) {
        return obj.toString();
    }
}