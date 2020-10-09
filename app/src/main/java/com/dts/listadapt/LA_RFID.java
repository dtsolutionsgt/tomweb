package com.dts.listadapt;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dts.base.AppMethods;
import com.dts.base.DateUtils;
import com.dts.base.MiscUtils;
import com.dts.base.clsClasses;
import com.dts.tomweb.PBase;
import com.dts.tomweb.R;

import java.util.ArrayList;

public class LA_RFID extends BaseAdapter {

    private static ArrayList<clsClasses.clsInventario_ciego_rfid> items;
    private int selectedIndex;
    private LayoutInflater l_Inflater;

    public LA_RFID(Context applicationContext, ArrayList<clsClasses.clsInventario_ciego_rfid> dvalues) {
        items = dvalues;
        l_Inflater = LayoutInflater.from(applicationContext);
        selectedIndex = -1;
    }

    public void setSelectedIndex(int ind) {
        selectedIndex = ind;
        notifyDataSetChanged();
    }

    public int getCount() {
        return items.size();
    }

    public Object getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {

            convertView = l_Inflater.inflate(R.layout.grid_cell, null);
            holder = new ViewHolder();

            holder.lblDesc = (TextView) convertView.findViewById(R.id.textView13);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.lblDesc.setText(items.get(position).codigo_barra);

        if(selectedIndex!= -1 && position == selectedIndex) {
            convertView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView lblDesc;
        TextView lbl1, lbl2;
        ImageView img1;
    }

}