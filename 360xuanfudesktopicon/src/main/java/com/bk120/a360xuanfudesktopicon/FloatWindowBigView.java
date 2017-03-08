package com.bk120.a360xuanfudesktopicon;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by bk120 on 2017/1/22.
 */

public class FloatWindowBigView extends LinearLayout {
    //记录大悬浮窗的宽度
    public static int viewWidth;
    //记录大悬浮窗的高度
    public static int viewHeight;
    private Context mContext;
    public FloatWindowBigView(final Context context) {
        super(context);
        mContext=context;
        LayoutInflater.from(context).inflate(R.layout.float_window_big,this);
        View view=findViewById(R.id.big_window_layout);
        viewWidth=view.getLayoutParams().width;
        viewHeight=view.getLayoutParams().height;
        Log.i("width;paramsWidth",view.getWidth()+";"+view.getLayoutParams().width);
        final Button close= (Button) findViewById(R.id.close);
        Button back= (Button) findViewById(R.id.back);
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击关闭悬浮窗的时候，移除所有悬浮窗，并停止Service
                MyWindowManager.removeBigWindow(context);
                MyWindowManager.removeSmallWindow(context);
                Intent i=new Intent(getContext(),FloatWindowService.class);
                context.stopService(i);
            }
        });
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击返回，移除大悬浮窗，创建小悬浮窗
                MyWindowManager.removeBigWindow(context);
                MyWindowManager.createSmallWindow(context);
            }
        });
        //
        Button music= (Button) findViewById(R.id.music);
        music.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"木有音乐！",Toast.LENGTH_SHORT).show();
            }
        });
        Button moive= (Button) findViewById(R.id.moive);
        moive.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"木有音乐！",Toast.LENGTH_SHORT).show();
            }
        });


    }
}
