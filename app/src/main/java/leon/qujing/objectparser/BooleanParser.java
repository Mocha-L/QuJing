package leon.qujing.objectparser;

import leon.qujing.QuJingServer;

public class BooleanParser implements QuJingServer.ObjectParser{

    @Override
    public Object parse(String data) {
        return Boolean.parseBoolean(data);
    }

    @Override
    public String generate(Object obj) {
        return obj.toString();
    }
}
