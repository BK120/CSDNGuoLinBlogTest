package com.bk120.pulltorefreshtest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {
    private RefreshableView refreshableView;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    String[] items={"A","B","C","D","E","F","G","H","I","J","K","101","SS","BK120","ZZ","MN","DV","AX"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getActionBar().hide();
        init();
    }

    private void init() {
        refreshableView= (RefreshableView) findViewById(R.id.refreshable_view);
        listView= (ListView) findViewById(R.id.list_view);
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,items);
        listView.setAdapter(adapter);
        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                refreshableView.finishRefreshing();
            }
        },0);
    }
}
