package leon.qujing.api;

import android.os.Process;

import com.alibaba.fastjson.JSON;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import leon.qujing.QuJingServer;
import leon.qujing.XposedEntry;
import leon.qujing.handler.MethodHandler;
import leon.qujing.handler.ObjectHandler;
import leon.qujing.utils.NanoHTTPD;
import leon.qujing.utils.NanoWSD;
import leon.qujing.utils.Utils;

import static leon.qujing.QuJingServer.parsers;

//单方法监控的WebSocket处理
public class wsMethodView implements QuJingServer.wsOperation {
    @Override
    public NanoWSD.WebSocket handle(NanoHTTPD.IHTTPSession handshake) {
        return new ws(handshake);
    }

    public class ws extends NanoWSD.WebSocket {
        public Method m = null;
//        public String server="http://127.0.0.1:8000";//TODO +Process.myPid();
        XC_MethodHook.Unhook unhook = null;
        MethodHook myHook = new MethodHook(this);
        boolean modify = true;
        HashMap<String, Object> objs = new HashMap<>();

        public ws(NanoHTTPD.IHTTPSession handshakeRequest) {
            super(handshakeRequest);
            try {
                Map<String, String> args = handshakeRequest.getParms();
                if(args.get("javaname")!=null) {
                    m = MethodHandler.getMethodbyJavaName(args.get("javaname"));
                }
                if(m==null)m = Class.forName(args.get("class"), false, XposedEntry.classLoader)
                        .getDeclaredMethods()[Integer.parseInt(args.get("method"))];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onOpen() {
            unhook = myHook.hook(m);
            if (unhook != null) try {
                sendLog("<p>开始监控函数: "+ m.getName() +"</p>");
                sendUpdateObj();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onClose(NanoWSD.WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
            unhook.unhook();
        }

        @Override
        protected void onMessage(NanoWSD.WebSocketFrame message) {

        }

        @Override
        protected void onPong(NanoWSD.WebSocketFrame pong) {

        }

        @Override
        protected void onException(IOException exception) {

        }

        void sendLog(String Msg) throws IOException {
            Map<String, Object> object = new HashMap<String, Object>();
            object.put("op", "msg");
            object.put("msg", Msg);
            SendThread st = new SendThread();
            st.setObj(object, this);
            new Thread(st).start();
        }

        void sendUpdateObj() throws IOException {
            Map<String, Object> object = new HashMap<String, Object>();
            object.put("op", "updatethis");
            object.put("data", new JSONArray(ObjectHandler.objects.keySet()));
            send(new JSONObject(object).toString());
        }
    }

    public class SendThread implements Runnable {
        Map<String, Object> mObject = null;
        ws myws = null;
        public void setObj(Map<String, Object> object, ws ws_){
            mObject = object;
            myws = ws_;
        }

        @Override
        public void run() {
            try  {
                myws.send(new JSONObject(mObject).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public class MethodHook extends XC_MethodHook {
        public ws myws;
        public Member method;            //被Hook的方法
        public Object thisObject;        //方法被调用时的this对象
        public Object[] args;            //方法被调用时的参数
        private Object result = null;    //方法被调用后的返回结果
        private int pid = 0;

        MethodHook(ws ws) {
            myws = ws;
        }

        public void setPid(int pid) {
            this.pid = pid;
        }

        private void gatherInfo(MethodHookParam param) {
            method = param.method;
            thisObject = param.thisObject;
            args = param.args;
        }

        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);
            if (pid > 0 && pid != Process.myPid()) return;
            gatherInfo(param);
            if(Invoke.isMe()){
                StringBuilder sb=new StringBuilder();
                StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
                sb.append("<div style=\"border: 1px dashed red; margin: 40px auto 10px auto; background-color: #fdd; padding: 10px;\"><details closed>");
                sb.append("<summary>远程反射调用执行</summary>");
                for (int i = 0; i <stacks.length ; i++) {
                    sb.append("<p>"+stacks[i].getClassName()+"."+stacks[i].getMethodName()+"</p>");
                }
                sb.append("/<details></div>");
                myws.sendLog(sb.toString());
                return;
            }
            if (thisObject != null) {
                if(!ObjectHandler.objects.containsValue(thisObject)){
                    ObjectHandler.objects.put(thisObject.getClass().getName() + "@" + Integer.toHexString(new Random().nextInt()), thisObject);
                    myws.sendUpdateObj();
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("<div style=\"border: 1px dashed red; margin: 40px auto 10px auto; background-color: #fdd; padding: 10px;\"><details closed>");
            sb.append("<summary>[" + Utils.getPrecisionStandardTime() + "] " + method.getDeclaringClass().getName() + "." + MethodDescription(param) + " 被调用</summary>");
            sb.append("<details>");
            sb.append("<summary>查看调用堆栈</summary>");
            StackTraceElement[] strace = Thread.currentThread().getStackTrace();
            for (int i = 4; i < strace.length; i++) {
                sb.append("<span style=\"padding-left: 40px;\"> at " + strace[i].getClassName() + "." + strace[i].getMethodName() + " : " + strace[i].getLineNumber() + "</span><br>");
            }
            sb.append("</details></details>");
            sb.append("<dl>");
            try {
                if (args != null) for (int i = 0; i < args.length; i++) {
                    if(args[i] == null){
                        sb.append("<dt>参数" + i + " " + "null </dt>");
                    }
                    else {
                        sb.append("<dt>参数" + i + " " + args[i].getClass().getName() + "</dt>");
                        sb.append("<dd>" + translate(args[i]) + "</dd>");
                    }
                }
            } catch (Exception e) {
                sb.append("<p>" + e.getLocalizedMessage() + "</p>");
            } finally {
                sb.append("</dl></div>");
                log(sb.toString());
            }
//            if (myws.modify) {
//                HashMap json=new HashMap();
//                json.put("method", Utils.getJavaName(myws.m));
//                if(thisObject!=null)json.put("this",ObjectHandler.saveObject(thisObject));
//                ArrayList params=new ArrayList();
//                if(args!=null)for (Object arg:args) {
//                    params.add(ObjectHandler.saveObject(arg));
//                }
//                json.put("params",params.toArray());
//                ArrayList stacks=new ArrayList();
//                for (StackTraceElement element: Thread.currentThread().getStackTrace()) {
//                    stacks.add(element.getClassName() + "." + element.getMethodName() + " : " + element.getLineNumber());
//                }
//                json.put("stack",stacks);
//                param.setResult(
//                        ObjectHandler.parseObject(
//                                new netUtil(myws.server + "/invoke2", new JSONObject(json).toString(2)).getRet()
//                        )
//                );
//            }
        }

        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);
            if (pid > 0 && pid != Process.myPid()) return;
            gatherInfo(param);
            if(Invoke.isMe())return;
            result = param.getResult();
            //Write your code here.
            if (myws.modify) {
                //param.setResult(requestResult(result));
            }
            StringBuilder sb = new StringBuilder();
            //sb.append("<details open>");
            //sb.append("<summary>["+Process.myPid() +"]"+ method.getDeclaringClass().getName() + "." + MethodDescription(param).toString() +" Returned</summary>");
            sb.append("<div style=\"border: 1px dashed blue; margin: 10px auto 40px auto; background-color: #ddf; padding: 10px;\"><dl>");
            try {
                if (param.getThrowable() == null) {
                    sb.append("<dt>调用返回</dt><dd>" + translate(result) + "</dd>");
                } else {
                    sb.append("<dt>Throw</dt><dd>" + translate(param.getThrowable()) + "</dd>");
                }
            } catch (Throwable e) {
                sb.append("<p>" + e.getLocalizedMessage() + "</p>");
            } finally {
                //log("</" + method.getDeclaringClass() + " method=" + MethodDescription(param).toString() +" pid="+Process.myPid()+ ">");
                sb.append("</dl></div>");
                log(sb.toString());
            }
        }

        private void log(String log) {
            //You can add your own logger here.
            //e.g filelogger like Xlog.log(log);
            try {
                myws.sendLog(log);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String translate(Object obj) {
            //Write your translator here.
            if (obj == null) return "null";
            if (obj.getClass().getName().equals("java.lang.String")) return obj.toString();
            else if(Utils.getTypeSignature(obj.getClass()).equals("[B"))return parsers.get("[B").generate(obj);
            else return JSON.toJSONString(obj);
        }

        private String MethodDescription(MethodHookParam param) {
            StringBuilder sb = new StringBuilder();
            sb.append(method.getName().toString());
            sb.append("(");
            if (args != null) for (Object arg : args) {
                if (arg == null) sb.append("UnknownType");
                else if (arg.getClass().isPrimitive()) sb.append(arg.getClass().getSimpleName());
                else sb.append(arg.getClass().getName());
                sb.append(",");
            }
            sb.append(")");
            return sb.toString();
        }

        public Unhook hook(Member method) {
            return XposedBridge.hookMethod(method, this);
        }

        public void hook(String clzn, String methodRegEx) throws ClassNotFoundException {
            Pattern pattern = Pattern.compile(methodRegEx);
            for (Member method : Class.forName(clzn, false, XposedEntry.classLoader).getDeclaredMethods()) {
                if (pattern.matcher(method.getName()).matches() && !method.isSynthetic())
                    this.hook(method);
            }
        }
    }
}
