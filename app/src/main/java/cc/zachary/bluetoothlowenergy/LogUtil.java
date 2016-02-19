package cc.zachary.bluetoothlowenergy;

import android.util.Log;

/**
 * Log便捷工具类
 * Created by Zachary on 2016/2/17.
 */
public class LogUtil {
    private static final boolean SHOW_LOG = true;
    private static final String TAG = "zachary";

    public static void i(String msg) {
        if (SHOW_LOG) {
            Log.i(TAG, msg);
        }
    }

    public static void d(String msg) {
        if (SHOW_LOG) {
            Log.d(TAG, msg);
        }
    }

    public static void v(String msg) {
        if (SHOW_LOG) {
            Log.v(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (SHOW_LOG) {
            Log.e(TAG, msg);
        }
    }
}
