package leon.qujing.objectparser;

import android.app.AndroidAppHelper;

import leon.qujing.QuJingServer;

public class ContextParser implements QuJingServer.ObjectParser {
    @Override
    public Object parse(String data) {
        return AndroidAppHelper.currentApplication().getApplicationContext();
    }

    @Override
    public String generate(Object obj) {
        return "Context[INST]";
    }
}
