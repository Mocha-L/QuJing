package leon.qujing.api;

import android.annotation.SuppressLint;
import android.text.Html;
import android.util.Base64;

import com.alibaba.fastjson.JSON;

import org.json.JSONObject;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
import leon.qujing.utils.Utils;


import static leon.qujing.utils.Utils.md5Hash;


//单方法监控的WebSocket处理
public class wsPacketView implements QuJingServer.wsOperation {
    @Override
    public NanoWSD.WebSocket handle(NanoHTTPD.IHTTPSession handshake) {
        return new ws(handshake);
    }

    public class ws extends NanoWSD.WebSocket {
        List<XC_MethodHook.Unhook> unhookList = new ArrayList<>();
        public XC_MethodHook sslWriteCallback = new XC_MethodHook() {
            @SuppressLint("DefaultLocale")
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                byte[] body = (byte[])param.args[0];
                int byteCount = (int)param.args[2];
                Object this_0 = XposedHelpers.getObjectField(param.thisObject, "this$0");
                Socket socket = (Socket)XposedHelpers.getObjectField(this_0, "socket");
                collectAndPack(socket, body, byteCount, "write", 1);
            }
        };
        public XC_MethodHook sslReadCallback = new XC_MethodHook() {
            @SuppressLint("DefaultLocale")
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                byte[] body = (byte[])param.args[0];
                int byteCount = (int)param.args[2];
                Object this_0 = XposedHelpers.getObjectField(param.thisObject, "this$0");
                Socket socket = (Socket)XposedHelpers.getObjectField(this_0, "socket");
                collectAndPack(socket, body, byteCount, "read", 1);
            }
        };

        public ws(NanoHTTPD.IHTTPSession handshakeRequest) {
            super(handshakeRequest);
        }

        @SuppressLint("DefaultLocale")
        private void collectAndPack(Socket socket, byte[] body, int byteCount, String flowType, int is_ssl) throws Throwable {
            InetAddress localAddr = socket.getLocalAddress();
            InetAddress inetAddr = socket.getInetAddress();
            HashMap<String, Object> msg = new HashMap<String, Object>();
            Calendar calendar = Calendar.getInstance();
            msg.put("flow_type", flowType);
            msg.put("flow_id", md5Hash(socket.toString()).substring(8, 16));
            msg.put("is_ssl", 1);
            msg.put("time", String.format("%02d:%02d:%02d.%03d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), calendar.get(Calendar.MILLISECOND)));
            msg.put("local_ip", localAddr.getHostAddress());
            msg.put("local_host", localAddr.getHostName());
            msg.put("local_port", socket.getLocalPort()+"");
            msg.put("remote_ip", inetAddr.getHostAddress());
            msg.put("remote_host", inetAddr.getHostName());
            msg.put("remote_port", socket.getPort()+"");
            msg.put("body_plain", Html.escapeHtml(new String(body, 0, byteCount,"UTF-8")));
            msg.put("body_hexdump", Utils.formatHexDump(body, 0, byteCount));;
            msg.put("body_base64", Base64.encodeToString(body, 0, byteCount, Base64.NO_WRAP));
            msg.put("body_length", byteCount+"");
            sendMsg("add", msg);
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
