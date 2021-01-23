package leon.qujing.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {
    private static final Map<Class<?>, String> PRIMITIVE_TO_SIGNATURE;

    static {
        PRIMITIVE_TO_SIGNATURE = new HashMap<Class<?>, String>(9);
        PRIMITIVE_TO_SIGNATURE.put(byte.class, "B");
        PRIMITIVE_TO_SIGNATURE.put(char.class, "C");
        PRIMITIVE_TO_SIGNATURE.put(short.class, "S");
        PRIMITIVE_TO_SIGNATURE.put(int.class, "I");
        PRIMITIVE_TO_SIGNATURE.put(long.class, "J");
        PRIMITIVE_TO_SIGNATURE.put(float.class, "F");
        PRIMITIVE_TO_SIGNATURE.put(double.class, "D");
        PRIMITIVE_TO_SIGNATURE.put(void.class, "V");
        PRIMITIVE_TO_SIGNATURE.put(boolean.class, "Z");
    }

    public static String MethodDescription(Method m) {
        StringBuilder sb = new StringBuilder();
        sb.append(Modifier.toString(m.getModifiers()));
        sb.append(" ");
        sb.append(m.getReturnType().getName());
        sb.append(" ");
        sb.append(m.getName());
        sb.append("(");
        for (int i = 0; i < m.getParameterTypes().length; i++) {
            if (i != 0) sb.append(",");
            sb.append(m.getParameterTypes()[i].getName());
            sb.append(" param" + i);
        }
        sb.append(")");
        if (m.getExceptionTypes().length > 0) {
            sb.append("throws ");
            boolean first = true;
            for (Class<?> type : m.getExceptionTypes()) {
                if (!first) sb.append(",");
                else first = false;
                sb.append(type.getName());
            }
        }
        return sb.toString();
    }

    public static String FieldDescription(Field field) {
        return Modifier.toString(field.getModifiers()) + " " + field.getType() + " " + field.getName();
    }

    public static String getJavaName(Method method) {
        StringBuilder result = new StringBuilder();
        result.append(getTypeSignature(method.getDeclaringClass()));
        result.append("->");
        result.append(method.getName());
        result.append(getMethodSignature(method));
        return result.toString();
    }

    public static String getMethodSignature(Method method) {
        StringBuilder result = new StringBuilder();

        result.append('(');
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> parameterType : parameterTypes) {
            result.append(getTypeSignature(parameterType));
        }
        result.append(')');
        result.append(getTypeSignature(method.getReturnType()));

        return result.toString();
    }

    public static String getTypeSignature(Class<?> clazz) {
        String primitiveSignature = PRIMITIVE_TO_SIGNATURE.get(clazz);
        if (primitiveSignature != null) {
            return primitiveSignature;
        } else if (clazz.isArray()) {
            return "[" + getTypeSignature(clazz.getComponentType());
        } else {
            // TODO: this separates packages with '.' rather than '/'
            return "L" + clazz.getName() + ";";
        }
    }

    public static String drawableToByte(Drawable drawable) throws Exception{
        if (drawable != null) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            int size = bitmap.getWidth() * bitmap.getHeight() * 4;
            // 创建一个字节数组输出流,流的大小为size
            ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
            // 设置位图的压缩格式，质量为100%，并放入字节数组输出流中
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            // 将字节数组输出流转化为字节数组byte[]
            byte[] imagedata = baos.toByteArray();
            return "data:image/png;base64," + Base64.encodeToString(imagedata, Base64.DEFAULT);
        }
        return "";
    }

    public static int getAppPid(Context ctx, String processName) {
        ActivityManager am = ((ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (processName.equals(info.processName)) {
                return info.pid;
            }
        }
        return 0;
    }

    public static String getPrecisionStandardTime(){
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    Date date = new Date(System.currentTimeMillis());
    String time = sdf.format(date);
    return time;
    }

    public static void chmod_file(File file){
        try {
            String command = "chmod 777 " + file.getAbsolutePath();
            Runtime runtime = Runtime.getRuntime();

            runtime.exec(command).waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if(sdCardExist)
        {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
            return sdDir.toString();
        }
        return null;
    }

    public static boolean RootCommand(String command)
    {
        Process process = null;
        DataOutputStream os = null;
        try
        {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e)
        {
            return false;
        } finally
        {
            try
            {
                if (os != null)
                {
                    os.close();
                }
                process.destroy();
            } catch (Exception e)
            {
            }
        }
        return true;
    }
}


