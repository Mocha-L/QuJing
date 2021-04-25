package leon.qujing.api;

import android.util.Base64;

import com.alibaba.fastjson.JSON;

import org.json.JSONObject;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import leon.qujing.QuJingServer;
import leon.qujing.XposedEntry;
import leon.qujing.utils.NanoHTTPD;
import leon.qujing.utils.NanoWSD;


//单方法监控的WebSocket处理
public class wsPacketView implements QuJingServer.wsOperation {
    @Override
    public NanoWSD.WebSocket handle(NanoHTTPD.IHTTPSession handshake) {
        return new ws(handshake);
    }

    public class ws extends NanoWSD.WebSocket {
        List<XC_MethodHook.Unhook> unhookList = new ArrayList<>();
        public XC_MethodHook sslWriteCallback = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                byte[] body = (byte[])param.args[0];
                int byteCount = (int)param.args[2];
                byte[] bodyNew = new byte[byteCount];
                System.arraycopy(body, 0, bodyNew, 0, byteCount);
                String bodyBase64 = Base64.encodeToString(bodyNew, Base64.NO_WRAP);
                Object this_0 = XposedHelpers.getObjectField(param.thisObject, "this$0");
                Socket socket = (Socket)XposedHelpers.getObjectField(this_0, "socket");
                InetAddress localAddr = socket.getLocalAddress();
                InetAddress inetAddr = socket.getInetAddress();
                HashMap<String, Object> msg = new HashMap<String, Object>();
                msg.put("flow_type", "write");
                msg.put("local_ip", localAddr.getHostAddress());
                msg.put("local_host", localAddr.getHostName());
                msg.put("local_port", socket.getLocalPort()+"");
                msg.put("remote_ip", inetAddr.getHostAddress());
                msg.put("remote_host", inetAddr.getHostName());
                msg.put("remote_port", socket.getPort()+"");
                if(Arrays.equals((byte[])bodyNew,new String((byte[])bodyNew).getBytes())){
                    msg.put("body_plain", new String(bodyNew).trim());
                }
                msg.put("body_base64", bodyBase64);
                msg.put("body_length", byteCount+"");
                sendMsg("add", msg);
            }
        };
        public XC_MethodHook sslReadCallback = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                byte[] body = (byte[])param.args[0];
                int byteCount = (int)param.args[2];
                byte[] bodyNew = new byte[byteCount];
                System.arraycopy(body, 0, bodyNew, 0, byteCount);
                String bodyBase64 = Base64.encodeToString(bodyNew, Base64.NO_WRAP);
                Object this_0 = XposedHelpers.getObjectField(param.thisObject, "this$0");
                Socket socket = (Socket)XposedHelpers.getObjectField(this_0, "socket");
                InetAddress localAddr = socket.getLocalAddress();
                InetAddress inetAddr = socket.getInetAddress();
                HashMap<String, Object> msg = new HashMap<String, Object>();
                msg.put("flow_type", "read");
                msg.put("local_ip", localAddr.getHostAddress());
                msg.put("local_host", localAddr.getHostName());
                msg.put("local_port", socket.getLocalPort()+"");
                msg.put("remote_ip", inetAddr.getHostAddress());
                msg.put("remote_host", inetAddr.getHostName());
                msg.put("remote_port", socket.getPort()+"");
                if(Arrays.equals((byte[])body,new String((byte[])body).getBytes())){
                    msg.put("body_plain", new String(body).trim());
                }
                msg.put("body_base64", bodyBase64);
                msg.put("body_length", byteCount+"");
                sendMsg("add", msg);
            }
        };

        public ws(NanoHTTPD.IHTTPSession handshakeRequest) {
            super(handshakeRequest);
        }

        @Override
        protected void onOpen() {
            try {
                HashMap<String, Object> msg = new HashMap<String, Object>();
                msg.put("body_plain", "capture packet now.");
                sendMsg("add", msg);
                makeHook();
            } catch (Exception e) {
                XposedBridge.log("onOpen error. e:" + e.toString());
            }
        }

        @Override
        protected void onClose(NanoWSD.WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
            for (XC_MethodHook.Unhook unhook: unhookList) {
                unhook.unhook();
            }
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

        void sendMsg(String cmd, HashMap<String, Object> msg) throws IOException {
            msg.put("resource", "flows");
            msg.put("cmd", cmd);;
            SendThread st = new SendThread();
            st.setObj(msg, this);
            new Thread(st).start();
        }

        public void makeHook(){
            XposedBridge.log("curr sdk:"+android.os.Build.VERSION.SDK_INT);
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.O){

                unhookList.add(XposedHelpers.findAndHookMethod(
                        "com.android.org.conscrypt.ConscryptFileDescriptorSocket$SSLOutputStream",
                        XposedEntry.classLoader, "write", byte[].class, int.class, int.class, sslWriteCallback));

                unhookList.add(XposedHelpers.findAndHookMethod(
                        "com.android.org.conscrypt.ConscryptFileDescriptorSocket$SSLInputStream",
                        XposedEntry.classLoader, "read", byte[].class, int.class, int.class, sslReadCallback));
            }
            else {
                unhookList.add(XposedHelpers.findAndHookMethod(
                        "com.android.org.conscrypt.OpenSSLSocketImpl$SSLOutputStream",
                        XposedEntry.classLoader, "write", byte[].class, int.class, int.class, sslWriteCallback));
                unhookList.add(XposedHelpers.findAndHookMethod(
                        "com.android.org.conscrypt.OpenSSLSocketImpl$SSLInputStream",
                        XposedEntry.classLoader, "read", byte[].class, int.class, int.class, sslReadCallback));
            }
            XposedBridge.log("hook success");
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
                myws.send(JSON.toJSONString(mObject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
