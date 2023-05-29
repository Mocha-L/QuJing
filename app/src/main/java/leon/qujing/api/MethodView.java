package leon.qujing.api;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XposedBridge;
import leon.qujing.QuJingServer;
import leon.qujing.XposedEntry;
import leon.qujing.handler.ClassHandler;
import leon.qujing.handler.MethodHandler;
import leon.qujing.handler.ObjectHandler;
import leon.qujing.utils.Utils;

//查看方法详情页面
public class MethodView implements QuJingServer.Operation {
    @Override
    public String handle(String url, Map<String, String> parms, Map<String, String> headers, Map<String, String> files) {
        try {
            Context context = AndroidAppHelper.currentApplication().getApplicationContext();
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            Method method = null;
            if(parms.get("javaname")!=null) {
                method = MethodHandler.getMethodbyJavaName(parms.get("javaname"));
                for (int i = 0; i < method.getDeclaringClass().getDeclaredMethods().length; i++) {
                    if(Utils.getJavaName(method.getDeclaringClass().getDeclaredMethods()[i]).equals(parms.get("javaname"))){
                        parms.put("method",""+i);
                    }
                }
            }
            if(method==null)method=ClassHandler.findClassbyName(parms.get("class"),XposedEntry.classLoader).getDeclaredMethods()[Integer.parseInt(parms.get("method"))];
            HashMap<String, Object> map = MethodHandler.getMethodDetail(method);
            map.put("method", parms.get("method"));
            map.put("objList", ObjectHandler.objects.keySet());
            map.put("DisplayName", packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString());
            map.put("IconBase64", Utils.drawableToByte(packageInfo.applicationInfo.loadIcon(context.getPackageManager())));
            map.put("ProcessID", String.valueOf(Process.myPid()));
            return QuJingServer.render(map, "pages/methodview.html");
        } catch (Exception e) {
            XposedBridge.log(e);
            return e.getLocalizedMessage();
        }
    }
}
