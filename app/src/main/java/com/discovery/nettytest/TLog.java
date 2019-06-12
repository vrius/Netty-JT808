package com.discovery.nettytest;


import android.util.Log;

/**
 * Manages logging for the entire class.
 */
public class TLog {

    // Generic tag for all In Call logging
    private static final String TAG = "NettyTest";

    public static final boolean DEBUG = true;//android.util.Log.isLoggable(TAG, android.util.Log.DEBUG);
    public static final boolean VERBOSE =true;/* android.util.Log.isLoggable(TAG,android.util.Log.VERBOSE);*/
    public static final String TAG_DELIMETER = " - ";

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(TAG, delimit(tag) + msg);
        }
    }

    public static void d(Object obj, String msg) {
        if (DEBUG) {
            Log.d(TAG, getPrefix(obj) + msg);
        }
    }

    public static void d(Object obj, String str1, Object str2) {
        if (DEBUG) {
            Log.d(TAG, getPrefix(obj) + str1 + str2);
        }
    }

    public static void v(Object obj, String msg) {
        if (VERBOSE) {
            Log.v(TAG, getPrefix(obj) + msg);
        }
    }

    public static void v(Object obj, String str1, Object str2) {
        if (VERBOSE) {
            Log.d(TAG, getPrefix(obj) + str1 + str2);
        }
    }

    public static void e(String tag, String msg, Exception e) {
        Log.e(TAG, delimit(tag) + msg, e);
    }

    public static void e(String tag, String msg) {
        Log.e(TAG, delimit(tag) + msg);
    }

    public static void e(Object obj, String msg, Exception e) {
        Log.e(TAG, getPrefix(obj) + msg, e);
    }

    public static void e(Object obj, String msg) {
        Log.e(TAG, getPrefix(obj) + msg);
    }

    public static void i(String tag, String msg) {
        Log.i(TAG, delimit(tag) + msg);
    }

    public static void i(Object obj, String msg) {
        Log.i(TAG, getPrefix(obj) + msg);
    }

    public static void w(Object obj, String msg) {
        Log.w(TAG, getPrefix(obj) + msg);
    }

    public static void wtf(Object obj, String msg) {
        Log.wtf(TAG, getPrefix(obj) + msg);
    }

    private static String getPrefix(Object obj) {
        return (obj == null ? "" : (obj.getClass().getSimpleName() + TAG_DELIMETER));
    }

    private static String delimit(String tag) {
        return tag + TAG_DELIMETER;
    }
}
