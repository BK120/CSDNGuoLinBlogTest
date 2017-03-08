package com.bk120.pulltorefreshtest;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by bk120 on 2017/2/18.
 * 下拉刷新头部
 */

public class RefreshableView extends LinearLayout implements View.OnTouchListener{
    //下拉状态
    public static final int STATUS_PULL_TO_REFRESH=0;
    //释放立即刷新状态
    public static final int STATUS_REFRESH_TO_REFRESH=1;
    //正在刷新状态
    public static final int STATUS_REFRESHING=2;
    //刷新完成或未刷新状态
    public static final int STATUS_REFRESH_FINISHED=3;
    //下拉头部回滚的速度
    public static final int SCROLL_SPEED=-20;
    //一分钟的毫秒值,用于判断上次更新时间
    public static final long ONE_MINUTE=60*1000;
    //一小时的毫秒值，用于判断上次更新的时间
    public static final long ONE_HOUR=60*ONE_MINUTE;
    //一天的毫秒值，用于判断上次更新时间
    public static final long ONE_DAY=24*ONE_HOUR;
    //一个月的毫秒值，用于判断上次更新时间
    public static final long ONE_MONTH=30*ONE_DAY;
    //一年的毫秒值，
    public static final long ONE_YEAR=12*ONE_MONTH;
    //上次更新的字符串常量，用于SharePrefernce的键值
    public static final String UPDATED_AT="updated_at";
    //下拉刷新的回调接口
    public PullToRefreshListener mListener;
    //下拉头View
    private View header;
    //下拉的ListView
    private ListView listView;
    //刷新时显示的进度条
    private ProgressBar progressBar;
    //指示下拉和刷新的箭头
    private ImageView arrow;
    //指示下拉和刷新的文字描述
    private TextView desciption;
    //上次更新时间的文字描述
    private TextView updateAt;
    //下拉头的布局参数
    private MarginLayoutParams headerLayoutParams;
    //上次更新时间的毫秒值
    private long lastUpdateTime;
    //防止不同界面下拉刷新和上次更新时间有相互冲突，使用id来区分
    private int mId=-1;
    //下拉头的高度
    private int hideHeaderHeight;
    //当前的状态，可选值有上面四个
    private int currentStatus=STATUS_REFRESH_FINISHED;
    //记录上一次的操作，避免重复操作
    private int lastStatus=currentStatus;
    //手指按下时屏幕纵坐标
    private float yDown;
    //在被判定滚动前手指可移动最大值
    private int touchSlop;
    //是否加载过一次Layout，这里的onLayout中的初始化只需要加载一次
    private boolean loadOnce;
    //当前是否可以下拉，只有ListView滚动到头在允许才允许下拉
    private boolean ableToPull;

    //下拉刷新构造函数，运行时动态添加一个下拉头的布局
    public RefreshableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        header= LayoutInflater.from(context).inflate(R.layout.pull_to_refresh,null,false);
        progressBar= (ProgressBar) header.findViewById(R.id.progress_bar);
        arrow= (ImageView) header.findViewById(R.id.arrow);
        desciption= (TextView) header.findViewById(R.id.description);
        updateAt= (TextView) header.findViewById(R.id.updated_at);
        touchSlop= ViewConfiguration.get(context).getScaledTouchSlop();
        refreshUpdatedValue();
        setOrientation(VERTICAL);
        addView(header,0);
    }
    //进行关键化初始操作，比如讲下拉头向上偏移进行隐藏，给ListView注册touch事件
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(changed&&!loadOnce){
            hideHeaderHeight=-header.getHeight();
            headerLayoutParams= (MarginLayoutParams) header.getLayoutParams();
            headerLayoutParams.topMargin=hideHeaderHeight;
            listView= (ListView) getChildAt(1);
            listView.setOnTouchListener(this);
            loadOnce=true;
        }
    }
    //刷新下拉箭头中更新时间的文字描述
    private void refreshUpdatedValue() {
        lastUpdateTime=SharePreferencesUtils.getLong(getContext(),UPDATED_AT+mId,-1l);
        long currentTime=System.currentTimeMillis();
        long timePassed=currentTime-lastUpdateTime;
        long timeIntoFormat;
        String updateAtValue;
        if(lastUpdateTime==-1){
            updateAtValue=getResources().getString(R.string.not_update_yet);
        }else if(timePassed<0){
            updateAtValue=getResources().getString(R.string.time_error);
        }else if(timePassed<ONE_MINUTE){
            updateAtValue=getResources().getString(R.string.update_just_now);
        }else if(timePassed<ONE_HOUR){
            timeIntoFormat=timePassed/ONE_MINUTE;
            String value=timeIntoFormat+"分钟";
            updateAtValue=String.format(getResources().getString(R.string.update_at),value);
        }else if(timePassed<ONE_DAY){
            timeIntoFormat=timePassed/ONE_HOUR;
            String value=timeIntoFormat+"小时";
            updateAtValue=String.format(getResources().getString(R.string.update_at),value);
        }else if(timePassed<ONE_MONTH){
            timeIntoFormat=timePassed/ONE_DAY;
            String value=timeIntoFormat+"天";
            updateAtValue=String.format(getResources().getString(R.string.update_at),value);
        }else if(timePassed<ONE_YEAR){
            timeIntoFormat=timePassed/ONE_MONTH;
            String value=timeIntoFormat+"个月";
            updateAtValue=String.format(getResources().getString(R.string.update_at),value);
        }else {
            timeIntoFormat=timePassed/ONE_YEAR;
            String value=timeIntoFormat+"年";
            updateAtValue=String.format(getResources().getString(R.string.update_at),value);
        }
        updateAt.setText(updateAtValue);
    }
    //listView被触摸时候调用，处理各种下拉刷新操作
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        setIsAbleToPull(event);
        if (ableToPull){
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    yDown=event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float yMove=event.getRawY();
                    int distance= (int) (yMove-yDown);
                    //如果手指是下滑状态，并且下拉头是完全隐藏，就屏蔽下拉事件
                    if(distance<=0&&headerLayoutParams.topMargin<=hideHeaderHeight){
                        return false;
                    }
                    if (distance<touchSlop){
                        return false;
                    }
                    if (currentStatus!=STATUS_REFRESHING){
                        if (headerLayoutParams.topMargin>0){
                            currentStatus=STATUS_REFRESH_TO_REFRESH;
                        }else {
                            currentStatus=STATUS_PULL_TO_REFRESH;
                        }
                        //通过偏移下拉头的topMargin值，实现下拉效果
                        headerLayoutParams.topMargin=(distance/2)+hideHeaderHeight;
                        header.setLayoutParams(headerLayoutParams);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (currentStatus==STATUS_REFRESH_TO_REFRESH){
                        //松手时如果是释放立即刷新状态，就去调用正在刷新的任务
                        new RefreshingTask().execute();
                    }else if(currentStatus==STATUS_REFRESH_TO_REFRESH){
                        //松手如果是下拉状态，就去调用隐藏下拉头的任务
                        new HideHeaderTask().execute();
                    }
                    break;
                default:
                    //时刻记得更新下拉头的信息
                    if (currentStatus==STATUS_REFRESH_TO_REFRESH||currentStatus==STATUS_REFRESH_TO_REFRESH){
                        updateHeaderView();
                        //下拉过程中让ListView失去焦点
                        listView.setPressed(false);
                        listView.setFocusable(false);
                        listView.setFocusableInTouchMode(false);
                        lastStatus=currentStatus;
                        //当前正处于下拉或释放状态，返回true屏蔽ListView滚动事件
                        return true;
                    }
            }
        }
        return false;
    }
    //更新下拉头中的信息
    private void updateHeaderView() {
        if (lastStatus!=currentStatus){
            if (currentStatus==STATUS_PULL_TO_REFRESH){
                desciption.setText(getResources().getString(R.string.pull_to_refresh));
                arrow.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                rotateArrow();
            }else if(currentStatus==STATUS_REFRESH_TO_REFRESH){
                desciption.setText(getResources().getString(R.string.refresh_to_refresh));
                arrow.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                rotateArrow();
            }else if(currentStatus==STATUS_REFRESHING){
                desciption.setText(getResources().getString(R.string.refreshing));
                arrow.clearAnimation();
                arrow.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }
            refreshUpdatedValue();
        }
    }
    //根据当前状态旋转箭头
    private void rotateArrow() {
        float pivotX=arrow.getWidth()/2f;
        float pivotY=arrow.getHeight()/2f;
        float fromDegrees=0f;
        float toDegrees=0f;
        if (currentStatus==STATUS_PULL_TO_REFRESH){
            fromDegrees=180f;
            toDegrees=360f;
        }else if(currentStatus==STATUS_REFRESH_TO_REFRESH){
            fromDegrees=0f;
            toDegrees=180f;
        }
        RotateAnimation animation=new RotateAnimation(fromDegrees,toDegrees,pivotX,pivotY);
        animation.setDuration(100);
        animation.setFillAfter(true);
        arrow.startAnimation(animation);
    }

    /**
     * 给下拉刷新控件注册一个监听
     * @param l 监听实现
     * @param id 处理不同界面在下拉刷新和处理上次更新时间相互冲突，不同界面传入刷新控件传入不同id
     */
    public void setOnRefreshListener(PullToRefreshListener l,int id){
        this.mListener=l;
        this.mId=id;
    }
    //结束listView正在刷新状态
    public void finishRefreshing(){
        currentStatus=STATUS_REFRESH_FINISHED;
        SharePreferencesUtils.putLong(getContext(),UPDATED_AT+mId,System.currentTimeMillis());
        new HideHeaderTask().execute();
    }
    //根据当前ListView的滚动状态来设定值，每次都需要在onTouch中第一个执行，判断出当前应该是滚动ListView还是进行下拉
    private void setIsAbleToPull(MotionEvent event) {
        View firstChild = listView.getChildAt(0);
        if(firstChild!=null){
            int firstVisblePos=listView.getFirstVisiblePosition();
            if(firstVisblePos==0&&firstChild.getTop()==0){
                if (!ableToPull){
                    yDown=event.getRawY();
                }
                ableToPull=true;
            }else {
                if (headerLayoutParams.topMargin!=hideHeaderHeight){
                    headerLayoutParams.topMargin=hideHeaderHeight;
                    header.setLayoutParams(headerLayoutParams);
                }
                ableToPull=false;
            }
        }else {
            //如果ListView中没有元素，也允许下拉刷新
            ableToPull=true;
        }
    }
    //正在刷新的任务，任务中会调用注册进来的下拉刷新监听器
    class RefreshingTask extends AsyncTask<Void,Integer,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            int topMargin=headerLayoutParams.topMargin;
            while (true){
                topMargin=topMargin+SCROLL_SPEED;
                if(topMargin<=0){
                    topMargin=0;
                    break;
                }
                publishProgress(topMargin);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            currentStatus=STATUS_REFRESHING;
            publishProgress(0);
            if (mListener!=null){
                mListener.onRefresh();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            updateHeaderView();
            headerLayoutParams.topMargin=values[0];
            header.setLayoutParams(headerLayoutParams);
        }
    }
    //隐藏下拉头的任务，当未进行下拉刷新或下拉刷新完成后，此任务将会使下拉头重新隐藏
    class HideHeaderTask extends AsyncTask<Void,Integer,Integer>{
        @Override
        protected Integer doInBackground(Void... params) {
            int topMargin=headerLayoutParams.topMargin;
            while (true){
                topMargin=topMargin+SCROLL_SPEED;
                if(topMargin<=hideHeaderHeight){
                    topMargin=hideHeaderHeight;
                    break;
                }
                publishProgress(topMargin);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return topMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            headerLayoutParams.topMargin=values[0];
            header.setLayoutParams(headerLayoutParams);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            headerLayoutParams.topMargin=integer;
            header.setLayoutParams(headerLayoutParams);
            currentStatus=STATUS_REFRESH_FINISHED;
        }
    }
    //下拉刷新监听器，使用下拉刷新的地方注册此监听器获取刷新回调
    public interface PullToRefreshListener{
        //刷新时回调此方法，在子线程中调用
        void onRefresh();
    }
}
