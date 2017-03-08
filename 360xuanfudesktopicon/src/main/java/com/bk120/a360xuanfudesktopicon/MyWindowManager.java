package com.bk120.a360xuanfudesktopicon;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by bk120 on 2017/1/21.
 */
public class MyWindowManager {
    /**
     * 小悬浮窗的实例
     */
    private static FloatWindowSmallView smallWindow;
    /**
     * 大悬浮窗的实例
     */
    private static FloatWindowBigView bigWindow;
    /**
     * 小悬浮窗View的参数
     */
    private static WindowManager.LayoutParams smallWindowParams;
    /**
     * 大悬浮窗的View参数
     */
    private static WindowManager.LayoutParams bigWindowParams;
    /**
     * 控制屏幕上添加或移除悬浮窗
     */
    private static WindowManager mWindowManager;
    /**
     * 获取手机可用内存
     */
    private static ActivityManager mActivityManager;
    /**
     * 悬浮窗是否显示
     * @return
     */
    public static boolean isWindowShowing() {
        return smallWindow!=null||bigWindow!=null;
    }

    /**
     * 创建桌面小悬浮窗,初始位置为屏幕的垂直部分2/3位置
     * @param applicationContext 此处必须为应用程序的Context对象
     */
    public static void createSmallWindow(Context applicationContext) {
        WindowManager windowManager = getWindowManager(applicationContext);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if (smallWindow==null){
            smallWindow=new FloatWindowSmallView(applicationContext);
            smallWindowParams=new WindowManager.LayoutParams();
            smallWindowParams.type= WindowManager.LayoutParams.TYPE_PHONE;
            smallWindowParams.format= PixelFormat.RGBA_8888;
            smallWindowParams.flags= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            smallWindowParams.gravity= Gravity.LEFT|Gravity.TOP;
            smallWindowParams.width=FloatWindowSmallView.viewWidth;
            smallWindowParams.height=FloatWindowSmallView.viewHeight;
            smallWindowParams.x=screenWidth;
            smallWindowParams.y=screenHeight*2/3;
        }
        smallWindow.setmParams(smallWindowParams);
        windowManager.addView(smallWindow,smallWindowParams);
    }

    /**
     * 
     * @param applicationContext
     * @return
     */
    private static WindowManager getWindowManager(Context applicationContext) {
        if (mWindowManager==null){
            mWindowManager= (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    /**
     * 移除桌面小悬浮窗
     * @param applicationContext
     */
    public static void removeSmallWindow(Context applicationContext) {
        if (smallWindow!=null){
            WindowManager windowManager = getWindowManager(applicationContext);
            windowManager.removeView(smallWindow);
            smallWindow=null;
        }
    }

    /**
     * 移除桌面大悬浮窗
     * @param applicationContext
     */
    public static void removeBigWindow(Context applicationContext) {
        if (bigWindow!=null){
            WindowManager windowManager = getWindowManager(applicationContext);
            windowManager.removeView(bigWindow);
            bigWindow=null;
        }
    }

    /**
     * 更新内存数据
     * @param applicationContext
     */
    public static void updateUsedPercent(Context applicationContext) {
    }

    /**
     * 获取内存使用百分比
     * @param context
     * @return
     */
    public static String getUsedPercentValue(Context context) {
        String dir="/proc/meminfo";
        try {
            FileReader fr=new FileReader(dir);
            BufferedReader br=new BufferedReader(fr,2048);
            String memoryLine=br.readLine();
            String subMemoryLine=memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            br.close();
            long totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll("\\D+", ""));
            long avaliableSize=getAvaliableMemory(context)/1024;
            int percent= (int) ((totalMemorySize-avaliableSize)/(float)totalMemorySize*100);
            return percent+"%";

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "悬浮窗";
    }

    /**
     * 返回当前可用内存，以字节为单位
     * @param context
     * @return
     */
    private static long getAvaliableMemory(Context context) {
        ActivityManager.MemoryInfo mi=new ActivityManager.MemoryInfo();
        getActivityManager(context).getMemoryInfo(mi);
        return mi.availMem;
    }

    /**
     * 获取ActivityManager
     * @param context
     * @return
     */
    private static ActivityManager getActivityManager(Context context) {
        if (mActivityManager==null){
            mActivityManager= (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        return mActivityManager;
    }

    /**
     * 创建桌面大悬浮窗,位置位于屏幕中间
     * @param context
     */
    public static void createBigWindow(Context context) {
        WindowManager windowManager = getWindowManager(context);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if (bigWindow==null){
            bigWindow=new FloatWindowBigView(context);
            bigWindowParams=new WindowManager.LayoutParams();
            bigWindowParams.x=screenWidth/2-FloatWindowBigView.viewWidth/2;
            bigWindowParams.y=screenHeight/2-FloatWindowBigView.viewHeight/2;
            bigWindowParams.type= WindowManager.LayoutParams.TYPE_PHONE;
            bigWindowParams.format=PixelFormat.RGBA_8888;
            bigWindowParams.gravity=Gravity.LEFT|Gravity.TOP;
            bigWindowParams.width=FloatWindowBigView.viewWidth;
            bigWindowParams.height=FloatWindowBigView.viewHeight;
        }
        windowManager.addView(bigWindow,bigWindowParams);
    }
}
