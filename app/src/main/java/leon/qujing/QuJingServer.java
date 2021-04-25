package leon.qujing;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import leon.qujing.api.ClassView;
import leon.qujing.api.Invoke;
import leon.qujing.api.MethodView;
import leon.qujing.api.PacketView;
import leon.qujing.api.wsMethodView;
import leon.qujing.api.wsPacketView;
import leon.qujing.objectparser.BooleanParser;
import leon.qujing.objectparser.ByteArrayParser;
import leon.qujing.objectparser.ContextParser;
import leon.qujing.objectparser.DoubleParser;
import leon.qujing.objectparser.FloatParser;
import leon.qujing.objectparser.GenericParser;
import leon.qujing.objectparser.IntParser;
import leon.qujing.objectparser.LongParser;
import leon.qujing.objectparser.ShortParser;
import leon.qujing.objectparser.StoredObjectParser;
import leon.qujing.objectparser.StringArrayListParser;
import leon.qujing.objectparser.StringArrayParser;
import leon.qujing.objectparser.StringMapParser;
import leon.qujing.objectparser.StringParser;
import leon.qujing.utils.NanoHTTPD;
import leon.qujing.utils.NanoWSD;
import leon.qujing.utils.Utils;


public class QuJingServer extends NanoWSD {
    public static HashMap<String, ObjectParser> parsers = new HashMap<String, ObjectParser>();
    static Hashtable<String, Operation> route = new Hashtable<String, Operation>();
    static Hashtable<String, wsOperation> wsroute = new Hashtable<String, wsOperation>();
    static String g_StrTargetAPP = "";

    public QuJingServer(int port) {
        this(port, null);
    }

    public QuJingServer(int port, Hashtable<String, Operation> route) {
        super(port);
        if(port == 61000) {
            QuJingServer.route.put("/", new config());
            QuJingServer.route.put("/settargetapp", new setTargetApp());
            QuJingServer.route.put("/manualguid", new manualGuid());
            QuJingServer.route.put("/querytargetapp", new queryTargetApp());
        }
        else {
            //注册对象序列化/反序列化处理器

            //通用序列化
            parsers.put("store", new StoredObjectParser());
            parsers.put("generic", new GenericParser());

            //常见类型序列化web
            parsers.put("string", new StringParser());
            parsers.put("Context", new ContextParser());
            parsers.put("int", new IntParser());
            parsers.put("short", new ShortParser());
            parsers.put("long", new LongParser());
            parsers.put("float", new FloatParser());
            parsers.put("double", new DoubleParser());
            parsers.put("boolean", new BooleanParser());
            parsers.put("byte", new ByteArrayParser());
            parsers.put("StringArray", new StringArrayParser());
            parsers.put("StringArrayList", new StringArrayListParser());
            parsers.put("StringMap", new StringMapParser());

            //常见类型序列化android
            parsers.put("Ljava.lang.Integer;", new IntParser());
            parsers.put("Ljava.lang.Boolean;", new BooleanParser());
            parsers.put("Ljava.lang.String;", new StringParser());
            parsers.put("I", new IntParser());
            parsers.put("S", new ShortParser());
            parsers.put("J", new LongParser());
            parsers.put("F", new FloatParser());
            parsers.put("D", new DoubleParser());
            parsers.put("Z", new BooleanParser());
            parsers.put("[B", new ByteArrayParser());

            //注册WebSocket路由
//            wsroute.put("/", new wsTracer());
            wsroute.put("/methodview", new wsMethodView());
            wsroute.put("/packetview", new wsPacketView());
//            wsroute.put("/wsTraceNew", new wsTracerNew());
            //注册HTTP请求路由
            if (route != null) QuJingServer.route = route;
            QuJingServer.route.put("/", new index());
            QuJingServer.route.put("/config", new index());
            QuJingServer.route.put("/status", new status());
            QuJingServer.route.put("/classview", new ClassView());
            QuJingServer.route.put("/methodview", new MethodView());
            QuJingServer.route.put("/packetview", new PacketView());
//            QuJingServer.route.put("/tracer", new Tracer());
            QuJingServer.route.put("/invoke", new Invoke());
//            QuJingServer.route.put("/invoke2", new Invoke_New());
//            QuJingServer.route.put("/memory", new MemoryView());
        }
        try {
            //启动监听
            start(0, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        //处理WebSocket路由
        wsOperation wsop = wsroute.get(handshake.getUri());
        if (wsop != null) return wsop.handle(handshake);
        else return wsroute.get("/").handle(handshake);
    }

    @Override
    public Response serveHttp(IHTTPSession session) {
        //处理HTTP请求路由
        //先做一下基本解析
        Map<String, String> files = new HashMap<String, String>();
        Map<String, String> headers = null;
        Response resp = null;
        try {
            headers = session.getHeaders();
            session.parseBody(files);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String uri = session.getUri();
        //处理路由
        Operation operation = route.get(uri.toLowerCase());
        if (operation == null) {
            try {
                XposedEntry.res.getAssets().open(uri.substring(1));
                operation = new assets();
                String msg = operation.handle(uri, session.getParms(), headers, files);
                resp = newFixedLengthResponse(Response.Status.OK, null, msg);
                resp.addHeader("Access-Control-Allow-Origin", "*");
                return resp;
            } catch (IOException e) {
                operation = route.get("/");
            }
        }
        resp = newFixedLengthResponse(operation.handle(uri, session.getParms(), headers, files));
        resp.addHeader("Access-Control-Allow-Origin", "*");
        return resp;
    }
    //供动态注册路由使用
    public void Register(String uri, Operation op) {
        route.put(uri, op);
    }
    public void Register(String uri, wsOperation op) {
        wsroute.put(uri, op);
    }

    //简单的模板引擎
    public static String render(Map<String, Object> model, String page) throws IOException, TemplateException {
        Template tmp = new Template(page, new InputStreamReader(XposedEntry.res.getAssets().open(page)), null);
        StringWriter sw = new StringWriter();
        tmp.process(model, sw);
        return sw.toString();
    }
    public static String file(String page) throws IOException, TemplateException {
        InputStreamReader reader = new InputStreamReader(XposedEntry.res.getAssets().open(page));
        int ch;
        StringWriter sw = new StringWriter();
        while ((ch = reader.read())!=-1){
            sw.write(ch);
        }
        return sw.toString();
    }

    //定义序列化/反序列化器
    public interface ObjectParser {
        Object parse(String data);
        String generate(Object obj);
    }
    //定义HTTP请求处理器
    public interface Operation {
        String handle(String url, Map<String, String> parms, Map<String, String> headers, Map<String, String> files);
    }
    //定义WebSocket处理器
    public interface wsOperation {
        WebSocket handle(IHTTPSession handshake);
    }
    //默认主页（以及调用模板引擎的示例）
    public class index implements QuJingServer.Operation {
        @Override
        public String handle(String url, Map<String, String> parms, Map<String, String> headers, Map<String, String> files) {
            try {
                Context context = AndroidAppHelper.currentApplication().getApplicationContext();
                PackageManager pm = context.getPackageManager();
                PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);

                Map<String, Object> map = new HashMap<String, Object>();
                map.put("DisplayName", packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString());
                map.put("IconBase64", Utils.drawableToByte(packageInfo.applicationInfo.loadIcon(context.getPackageManager())));
                map.put("ProcessID", String.valueOf(Process.myPid()));

                return render(map, "pages/index.html");
            } catch (Exception e) {
                e.printStackTrace();
                return e.getLocalizedMessage();
            }
        }
    }
    //状态页面
    public class status implements QuJingServer.Operation {
        @Override
        public String handle(String url, Map<String, String> parms, Map<String, String> headers, Map<String, String> files) {
            try {
                return "OK";
            } catch (Exception e) {
                e.printStackTrace();
                return e.getLocalizedMessage();
            }
        }
    }
    //配置主页（以及调用模板引擎的示例）
    public class config implements QuJingServer.Operation {
        @Override
        public String handle(String url, Map<String, String> parms, Map<String, String> headers, Map<String, String> files) {
            try {
                Map<String, Object> map = new HashMap<String, Object>();
                Context context = AndroidAppHelper.currentApplication().getApplicationContext();
                List<PackageInfo> packageInfo = context.getPackageManager().getInstalledPackages(0);
                List<Map<String, String>> packages = new ArrayList<>();

                String TargetAppsStr = g_StrTargetAPP;

                for (PackageInfo pk: packageInfo) {
                    boolean bIsCheck = TargetAppsStr.contains(pk.packageName + ";");
                    Map<String, String> pkinfo = new HashMap<String, String>();
                    pkinfo.put("PackageName", pk.packageName);
                    pkinfo.put("Check", bIsCheck?" checked=\"checked\" ":"");
                    pkinfo.put("DisplayName", pk.applicationInfo.loadLabel(context.getPackageManager()).toString());
                    pkinfo.put("IconBase64", Utils.drawableToByte(pk.applicationInfo.loadIcon(context.getPackageManager())));
                    if(bIsCheck){
                        packages.add(0, pkinfo);
                    }
                    else{
                        packages.add(pkinfo);
                    }

                }
                map.put("packages", packages);
                return render(map, "pages/config.html");
            } catch (Exception e) {
                e.printStackTrace();
                return e.getLocalizedMessage();
            }
        }
    }

    //提交配置
    public class setTargetApp implements QuJingServer.Operation {
        @Override
        public String handle(String url, Map<String, String> parms, Map<String, String> headers, Map<String, String> files) {
            try {
                Log.i("NANOHTTPD", "setTargetApp");

                StringBuilder sb = new StringBuilder();
                for(String key: parms.keySet()){
                    sb.append(key).append(";");
                }
                String TargetAppsStr = sb.toString();
                g_StrTargetAPP = TargetAppsStr;
                return "OK";
            } catch (Exception e) {
                e.printStackTrace();
                return e.getLocalizedMessage();
            }
        }
    }

    //手动操作指导
    public class manualGuid implements QuJingServer.Operation {
        @Override
        public String handle(String url, Map<String, String> parms, Map<String, String> headers, Map<String, String> files) {
            try {
                Map<String, Object> map = new HashMap<String, Object>();
                Context context = AndroidAppHelper.currentApplication().getApplicationContext();
                List<PackageInfo> packageInfo = context.getPackageManager().getInstalledPackages(0);
                List<Map<String, String>> packages = new ArrayList<>();

                String TargetAppsStr = g_StrTargetAPP;

                for (PackageInfo pk: packageInfo) {
                    boolean bIsCheck = TargetAppsStr.contains(pk.packageName + ";");
                    if(bIsCheck){
                        String dispalyName = pk.applicationInfo.loadLabel(context.getPackageManager()).toString();
                        Map<String, String> pkinfo = new HashMap<String, String>();
                        pkinfo.put("PackageName", pk.packageName);
                        pkinfo.put("DisplayName", dispalyName);
                        pkinfo.put("IconBase64", Utils.drawableToByte(pk.applicationInfo.loadIcon(context.getPackageManager())));
                        int pid = Utils.getAppPid(context, pk.packageName);
                        if(pid == 0){
                            pkinfo.put("ProcessID", "");
                            pkinfo.put("nextStep", String.format("APP未打开，请手动打开应用(%s)", dispalyName));
                        }else {
                            pkinfo.put("ProcessID", "" + pid);
                            pkinfo.put("nextStep", "");
                        }
                        packages.add(0, pkinfo);
                    }
                }
                map.put("packages", packages);
                return render(map, "pages/manualguid.html");
            } catch (Exception e) {
                e.printStackTrace();
                return e.getLocalizedMessage();
            }
        }
    }

    //手动操作指导
    public class queryTargetApp implements QuJingServer.Operation {
        @Override
        public String handle(String url, Map<String, String> parms, Map<String, String> headers, Map<String, String> files) {
            try {
                return g_StrTargetAPP;
            } catch (Exception e) {
                e.printStackTrace();
                return e.getLocalizedMessage();
            }
        }
    }

    // 资源文件
    public class assets implements QuJingServer.Operation {
        @Override
        public String handle(String url, Map<String, String> parms, Map<String, String> headers, Map<String, String> files) {
            try {
                return file(url.substring(1));
            } catch (Exception e) {
                e.printStackTrace();
                return e.getLocalizedMessage();
            }
        }
    }
}