package com.bk120.a360xuanfudesktopicon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * 模仿360的悬浮桌面操作图标指示器
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    //开启悬浮窗
    public void startFloatWindow(View view){
        Intent i=new Intent(this,FloatWindowService.class);
        startService(i);
        this.finish();
    }
}
