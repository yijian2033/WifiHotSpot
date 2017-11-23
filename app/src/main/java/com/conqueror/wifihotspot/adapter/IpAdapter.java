package com.conqueror.wifihotspot.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.conqueror.wifihotspot.R;
import com.conqueror.wifihotspot.bean.IpBean;

import java.util.ArrayList;

/**
 * @author yijian2033
 * @date on 2017/11/20
 * @describe TODO
 */

public class IpAdapter extends BaseAdapter {

    private ArrayList<String> list;
    private Context context;

    public IpAdapter(Context context, ArrayList<String> list) {

        this.context = context;
        this.list = list;
    }


    @Override
    public int getCount() {

        if (list != null) {
            return list.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int i) {

        if (list != null) {
            return list.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {

        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHodler viewHodler;

        if (view == null) {
            viewHodler = new ViewHodler();
            view = View.inflate(context, R.layout.item_ip, null);
            viewHodler.tv = view.findViewById(R.id.tv_ip);
            view.setTag(viewHodler);
        } else {
            viewHodler = (ViewHodler) view.getTag();
        }
        String ip = list.get(i);

        viewHodler.tv.setText(ip);

        return view;
    }

    private class ViewHodler {

        TextView tv;
    }

}
