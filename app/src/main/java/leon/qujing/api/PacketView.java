package leon.qujing.api;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import leon.qujing.QuJingServer;
import leon.qujing.XposedEntry;
import leon.qujing.handler.ClassHandler;
import leon.qujing.handler.MethodHandler;
import leon.qujing.handler.ObjectHandler;
import leon.qujing.utils.Utils;

//查看方法详情页面
public class PacketView implements QuJingServer.Operation {
    @Override
    public String handle(String url, Map<String, String> parms, Map<String, String> headers, Map<String, String> files) {
        try {
            Context context = AndroidAppHelper.currentApplication().getApplicationContext();
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("DisplayName", packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString());
            map.put("ProcessID", String.valueOf(Process.myPid()));
            return QuJingServer.render(map, "pages/packetview.html");
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
    }
}
