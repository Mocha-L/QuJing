package leon.qujing.objectparser;

import leon.qujing.QuJingServer;

public class DoubleParser implements QuJingServer.ObjectParser {
    @Override
    public Object parse(String data) {
        return Double.parseDouble(data);
    }

    @Override
    public String generate(Object obj) {
        return obj.toString();
    }
}