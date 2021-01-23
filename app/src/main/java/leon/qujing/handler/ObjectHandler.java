package leon.qujing.handler;

import java.util.HashMap;
import java.util.Map;

import leon.qujing.QuJingServer;
import leon.qujing.utils.Utils;

import static leon.qujing.QuJingServer.parsers;

//处理对象相关内容
public class ObjectHandler {
    public static HashMap<String, Object> objects = new HashMap<String, Object>();

    public static Object storeObject(Object obj, String name) {
        return objects.put(name, obj);
    }

    public static String saveObject(Object obj){
        String typeSign = Utils.getTypeSignature(obj.getClass());
        if(obj==null)return "Null";
        QuJingServer.ObjectParser parser = parsers.get(typeSign);
        if(parser==null)parser=parsers.get("store");
        return typeSign+"#"+parser.generate(obj);
    }
    public static String briefObject(Object obj){
        String typeSign = Utils.getTypeSignature(obj.getClass());
        if(obj==null)return "Null";
        QuJingServer.ObjectParser parser = parsers.get(typeSign);
        if(parser==null)parser=parsers.get("generic");
        return typeSign+"#"+parser.generate(obj);
    }

    public static Object getObject(String name) {
        return objects.get(name);
    }

    public static Object parseObject(String Object){
        if(Object.equals("Null"))return null;
        if(Object==null)return null;
        if(Object.indexOf("#")<0)return null;
        String type=Object.substring(0,Object.indexOf("#"));
        String raw=Object.substring(Object.indexOf("#")+1);
        QuJingServer.ObjectParser parser = parsers.get(type);
        if(parser==null)parser=parsers.get("store");
        return parser.parse(raw);
    }

    public static Object[] getObjects(String name, String type) {
        return null;
    }

    public static Object removeObject(String name) {
        return objects.remove(name);
    }

    public static Object removeObject(Object object) {
        for (Map.Entry entry : objects.entrySet()) {
            if (entry.getValue().equals(object)) return objects.remove(entry.getKey());
        }
        return null;
    }
}
