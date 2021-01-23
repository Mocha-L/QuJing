package leon.qujing.objectparser;

import leon.qujing.QuJingServer;

public class FloatParser implements QuJingServer.ObjectParser {
    @Override
    public Object parse(String data) {
        return Float.parseFloat(data);
    }

    @Override
    public String generate(Object obj) {
        return obj.toString();
    }
}