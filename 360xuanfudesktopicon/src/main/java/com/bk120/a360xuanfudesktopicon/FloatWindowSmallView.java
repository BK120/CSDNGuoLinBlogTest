package com.bk120.a360xuanfudesktopicon;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * Created by bk120 on 2017/1/22.
 * 小悬浮窗视图类
 */

public class FloatWindowSmallView extends LinearLayout{
    /**
     * 小悬浮窗的宽度
     */
    public static int viewWidth;
    /**
     * 小悬浮窗的高度
     */
    public static int viewHeight;
    /**
     * 系统状态栏的高度
     */
    private static int statusBarHeight;
    /**
     * 更新小悬浮窗的位置
     */
    private WindowManager windowManager;
    /**
     * 小悬浮窗的参数
     */
    private WindowManager.LayoutParams mParams;
    //手指在桌面的横坐标位置
    private float xInScreen;
    //手指在桌面的纵坐标位置
    private float yInScreen;
    //手指按下在屏幕上的横坐标值
    private float xDownInScreen;
    //手指按下在屏幕上的纵坐标值
    private float yDownInScreen;
    //手指按下View悬浮窗上的横坐标值
    private float xInView;
    //手指按下在View悬浮窗的纵坐标值
    private float yInView;
    public FloatWindowSmallView(Context context) {
        super(context);
        windowManager= (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //悬浮窗小视图布局与当前类结合关联
        LayoutInflater.from(context).inflate(R.layout.float_window_small,this);
        View view=findViewById(R.id.small_window_layout);
        viewWidth=view.getLayoutParams().width;
        viewHeight=view.getLayoutParams().height;
        TextView percentView= (TextView) findViewById(R.id.percent);
        percentView.setText(MyWindowManager.getUsedPercentValue(context));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                /**
                 *getX和getRowX区别：
                 * getX：获取触摸点在屏幕上的横坐标位置
                 * getRowX:获取触摸点在当前视图中的横向位置
                 */
                xInView=event.getX();
                yInView=event.getY();
                xDownInScreen=event.getRawX();
                yDownInScreen=event.getRawY()-getStatusBarHeight();
                xInView=event.getRawX();
                yInView=event.getRawY()-getStatusBarHeight();
                break;
            case MotionEvent.ACTION_MOVE:
                xInScreen=event.getRawX();
                yInScreen=event.getRawY()-getStatusBarHeight();
                //手指移动更新悬浮窗位置
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:
                //手指离开屏幕时，xDownInScreen和xInScreen相等，并且yDownInScreen和yInScreen相等，则视为触发了单击事件
                if (xDownInScreen==xInScreen&&yDownInScreen==yInScreen){
                    openBigWindow();
                }
                break;
            default:
                break;
        }
        return true;
    }
    //将小悬浮窗参数传入，更新悬浮窗位置
    public void setmParams(WindowManager.LayoutParams Params) {
        this.mParams = Params;
    }

    //打开大悬浮窗，关闭小悬浮窗
    private void openBigWindow() {
        MyWindowManager.createBigWindow(getContext());
        MyWindowManager.removeSmallWindow(getContext());
    }

    //更新悬浮窗位置
    private void updateViewPosition() {
       /* mParams.x= (int) (xInScreen-xInView);
        mParams.y= (int) (yInScreen-yInView);*/
        mParams.x= (int) xInScreen;
        mParams.y= (int) yInScreen;
        windowManager.updateViewLayout(this,mParams);
    }
    //获取状态栏高度
    private float getStatusBarHeight() {
        if (statusBarHeight==0){
            try {
                //反射获取状态栏高度
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (int) field.get(o);
                statusBarHeight=getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }
}
