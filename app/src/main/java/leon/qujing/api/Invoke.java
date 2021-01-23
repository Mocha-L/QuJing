package leon.qujing.api;

import java.lang.reflect.Method;
import java.util.Map;

import leon.qujing.QuJingServer;
import leon.qujing.XposedEntry;
import leon.qujing.handler.ObjectHandler;

//处理反射调用
public class Invoke implements QuJingServer.Operation {
    @Override
    public String handle(String url, Map<String, String> parms, Map<String, String> headers, Map<String, String> files) {
        StringBuilder sb = new StringBuilder();
        Object thisobj;
        Object[] params;
        thisobj = parms.get("thisobj").equals("null") ? null : ObjectHandler.objects.get(parms.get("thisobj"));

        try {
            Method[] methods = Class.forName(parms.get("class"), false, XposedEntry.classLoader).getDeclaredMethods();
            Method m = methods[Integer.parseInt(parms.get("method"))];
            params = new Object[m.getParameterTypes().length];
            for (int i = 0; i < m.getParameterTypes().length; i++) {
                params[i] = QuJingServer.parsers.get(parms.get("parser" + i)).parse(parms.get("param" + i));
            }
            m.setAccessible(true);
            sb.append(ObjectHandler.saveObject(m.invoke(thisobj, params)));
        } catch (Exception e) {
            sb.append("执行异常，报错信息: " + e.getLocalizedMessage());
        }
        return sb.toString();
    }
    public static boolean isMe(){
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        for (int i = 4; i <stacks.length ; i++) {
            if(stacks[i].getClassName().equals(Invoke.class.getName()))return true;
        }
        return false;
    }

}
