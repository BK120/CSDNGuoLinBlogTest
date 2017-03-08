package com.bk120.a360xuanfudesktopicon;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;

import android.os.*;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 对悬浮窗的控制及对手机内存的定时控制显示刷新
 */
public class FloatWindowService extends Service {
    private static final String TAG=FloatWindowService.class.getSimpleName();
    /**
       * 用于线程中创建和移除悬浮窗
    */
    private Handler handler=new Handler();

    /**
     * 定时器，定时检测当前是该创建还是移除悬浮窗
     */
    private Timer timer;
    public FloatWindowService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //开启定时器，每隔0.5秒刷新一次
        if(timer==null){
            timer=new Timer();
            timer.schedule(new RefreshTask(),0,500);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Service停止，定时器也停止
        timer.cancel();
        timer=null;
    }

    class RefreshTask extends TimerTask{

        @Override
        public void run() {
            //当前界面是桌面且没有悬浮窗显示，则创建悬浮窗
            if(isHome()&&!MyWindowManager.isWindowShowing()){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.createSmallWindow(getApplicationContext());
                    }
                });
            }
            //当前界面不是桌面且有悬浮窗则移除悬浮窗
            if (!isHome()&&MyWindowManager.isWindowShowing()){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.removeSmallWindow(getApplicationContext());
                        MyWindowManager.removeBigWindow(getApplicationContext());
                    }
                });
            }
            //当前界面是桌面，且有悬浮窗显示，则更新内存数据
            if (!isHome()&&MyWindowManager.isWindowShowing()){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.updateUsedPercent(getApplicationContext());
                    }
                });
            }
        }
    }
    /**
     * 判断当前界面是否是桌面
     */
    public boolean isHome(){
        ActivityManager mActivityManager= (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        //获取最近运行的几个应用
        List<ActivityManager.RunningTaskInfo> rti=mActivityManager.getRunningTasks(1);
        //当前正在交互的Activity，包名
        return getHomes().contains(rti.get(0).topActivity.getPackageName());
    }
    /**
     * 获取属于桌面的应用的应用包名即所有应用的包名
     */
    private List<String> getHomes(){
        List<String> names=new ArrayList<String>();
        PackageManager packageManager = this.getPackageManager();
        Intent i=new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME );
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri:resolveInfos){
            names.add(ri.activityInfo.packageName);
        }
        Log.i(TAG,names.toString());
        return names;
    }
}
