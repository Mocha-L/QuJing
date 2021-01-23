package leon.qujing.objectparser;

import leon.qujing.QuJingServer;

public class ShortParser implements QuJingServer.ObjectParser {
    @Override
    public Object parse(String data) {
        return Short.parseShort(data);
    }

    @Override
    public String generate(Object obj) {
        return obj.toString();
    }
}