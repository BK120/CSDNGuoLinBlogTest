package com.bk120.contactsdemo;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Selection;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AlphabetIndexer;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 实现联系人分组导航及吸顶效果
 * 如微信，手机联系人，坐标地址的界面效果
 * ListView具有快速滚动按钮
 */
public class MainActivity extends Activity {
    //分组的布局
    private LinearLayout titleLayout;
    //弹出式分组布局
    private RelativeLayout sectionToastLayout;
    //右侧可滑动字母表
    private Button alphabetButton;
    //弹出式分组上的文字
    private TextView sectionToastText;
    //分组上显示的字母
    private TextView title;
    //联系人ListView
    private ListView contactsListView;
    //联系人列表适配器
    private ContactAdapter adapter;
    //用于进行字母表分组
    private AlphabetIndexer indexer;
    //存储所有手机中的联系人
    private List<Contact> contacts=new ArrayList<Contact>();
    //定义字母表的排序规则
    private String alphabet="#ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    //上次第一个可见元素，用于滚动时记录标识
    private int lastFirstVisibleItem=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        adapter=new ContactAdapter(this,R.layout.contact_item,contacts);
        titleLayout= (LinearLayout) this.findViewById(R.id.title_layout);
        title= (TextView) this.findViewById(R.id.title);
        contactsListView= (ListView) this.findViewById(R.id.mainactivity_listview);
        sectionToastLayout= (RelativeLayout) this.findViewById(R.id.section_toast_layout);
        sectionToastText= (TextView) this.findViewById(R.id.section_toast_text);
        alphabetButton= (Button) this.findViewById(R.id.alphabetButton);
        //获取手机联系人
        Uri uri= ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor=getContentResolver().query(uri,new String[]{"display_name","sort_key"},null,null,"sort_key");
        if (cursor.moveToFirst()){
            //封装联系人加入集合
            do {
                String name=cursor.getString(0);
                String sortKey=getSortKey(cursor.getString(1));
                Contact contact=new Contact();
                contact.setName(name);
                contact.setSortKey(sortKey);
                contacts.add(contact);
            }while (cursor.moveToNext());
        }
        startManagingCursor(cursor);
        indexer=new AlphabetIndexer(cursor,1,alphabet);
        adapter.setIndexer(indexer);
        if(contacts.size()>0){
            setupContactsListView();
            setAlphabetListener();
        }
    }
    //滑动按钮设置监听,根据触摸位置结合字母表高度，计算当前触摸的字母，
    //按上时展示弹出式分组，松开即隐藏
    private void setAlphabetListener() {
        alphabetButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int alphabetHeight = alphabetButton.getHeight();
                float y = event.getY();
                int sectionPosition= (int) ((y/alphabetHeight)/(1f/27f));
                if(sectionPosition<0){
                    sectionPosition=0;
                }else if(sectionPosition>26){
                    sectionPosition=26;
                }
                String sectionLetter=String.valueOf(alphabet.charAt(sectionPosition));
                int position=indexer.getPositionForSection(sectionPosition);
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        sectionToastLayout.setVisibility(View.VISIBLE);
                        sectionToastText.setText(sectionLetter);
                        contactsListView.setSelection(position);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        sectionToastText.setText(sectionLetter);
                        contactsListView.setSelection(position);
                        break;
                    default:
                        //alphabetButton.setBackgroundResource();
                        sectionToastLayout.setVisibility(View.GONE);
                }
                return true;
            }
        });
    }

    //为ListView设置监听事件，根据当前滑动状态来改变分组的显示位置，从而实现吸顶--挤压效果的动画
    private void setupContactsListView() {
        contactsListView.setAdapter(adapter);
        contactsListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int section=indexer.getSectionForPosition(firstVisibleItem);
                int nextSecPosition=indexer.getPositionForSection(section+1);
                if(firstVisibleItem!=lastFirstVisibleItem){
                    ViewGroup.MarginLayoutParams params= (ViewGroup.MarginLayoutParams) titleLayout.getLayoutParams();
                    params.topMargin=0;
                    titleLayout.setLayoutParams(params);
                    title.setText(String.valueOf(alphabet.charAt(section)));
                }
                if (nextSecPosition==firstVisibleItem+1){
                    View childView=view.getChildAt(0);
                    if(childView!=null){
                        int titleHeight=titleLayout.getHeight();
                        int bottom=childView.getBottom();
                        ViewGroup.MarginLayoutParams params= (ViewGroup.MarginLayoutParams) titleLayout.getLayoutParams();
                        if(bottom<titleHeight){
                            float pushedDistance=bottom-titleHeight;
                            params.topMargin= (int) pushedDistance;
                            titleLayout.setLayoutParams(params);
                        }else {
                            if (params.topMargin!=0){
                                params.topMargin=0;
                                titleLayout.setLayoutParams(params);
                            }
                        }
                    }
                }
                lastFirstVisibleItem=firstVisibleItem;
            }
        });
    }

    /**
     *获取sort_Key的首个字母，如果是英文直接返回，否则返回#
     * @param sortKeyString 数据库中读取的sort key
     * @return 英文字母 或则#
     */
    private String getSortKey(String sortKeyString){
        String key=sortKeyString.substring(0,1).toUpperCase();
        if(key.matches("[A-Z]")){
            return key;
        }
        return "#";
    }
}
