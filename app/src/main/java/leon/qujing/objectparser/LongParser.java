package leon.qujing.objectparser;

import leon.qujing.QuJingServer;

public class LongParser implements QuJingServer.ObjectParser {
    @Override
    public Object parse(String data) {
        return Long.parseLong(data);
    }

    @Override
    public String generate(Object obj) {
        return obj.toString();
    }
}