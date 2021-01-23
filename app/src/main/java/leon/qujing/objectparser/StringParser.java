package leon.qujing.objectparser;

import leon.qujing.QuJingServer;

public class StringParser implements QuJingServer.ObjectParser {
    @Override
    public Object parse(java.lang.String data) {
        return data;
    }

    @Override
    public String generate(Object obj) {
        return obj.toString();
    }
}
