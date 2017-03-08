package com.bk120.contactsdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.List;

/**
 * Created by bk120 on 2017/2/17.
 */

public class ContactAdapter extends ArrayAdapter{
    //需要操作的Item布局
    private int resource;
    //字母表分组工具
    private SectionIndexer mIndexer;
    public ContactAdapter(Context context, int textViewResourceId, List<Contact> objects) {
        super(context, textViewResourceId,objects);
        resource=textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Contact contact= (Contact) getItem(position);
        LinearLayout layout=null;
        if(convertView==null){
            layout= (LinearLayout) LayoutInflater.from(getContext()).inflate(resource,null);
        }else {
            layout= (LinearLayout) convertView;
        }
        TextView name= (TextView) layout.findViewById(R.id.name);
        LinearLayout sortKeyLayout= (LinearLayout) layout.findViewById(R.id.sort_key_layout);
        TextView sortKey= (TextView) layout.findViewById(R.id.sort_key);
        name.setText(contact.getName());
        int section=mIndexer.getSectionForPosition(position);
        if(position==mIndexer.getPositionForSection(section)){
            sortKey.setText(contact.getSortKey());
            sortKeyLayout.setVisibility(View.VISIBLE);
        }else {
            sortKeyLayout.setVisibility(View.GONE);
        }
        return layout;
    }
    //给当前适配器传入分组工具
    public void setIndexer(SectionIndexer indexer){
        mIndexer=indexer;
    }
}
