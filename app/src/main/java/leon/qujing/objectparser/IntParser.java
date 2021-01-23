package leon.qujing.objectparser;

import leon.qujing.QuJingServer;

public class IntParser implements QuJingServer.ObjectParser {
    @Override
    public java.lang.Object parse(java.lang.String data) {
        return Integer.parseInt(data);
    }

    @Override
    public String generate(Object obj) {
        return obj.toString();
    }
}