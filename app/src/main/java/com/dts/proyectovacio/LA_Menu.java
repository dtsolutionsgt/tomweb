package com.dts.proyectovacio;

import android.content.Context;

import java.util.ArrayList;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LA_Menu extends BaseAdapter {

    private MiscUtils mu;
    private DateUtils du;
    private AppMethods app;

    private ArrayList<clsClasses.clsMenu> items = new ArrayList<clsClasses.clsMenu>();
    private int selectedIndex;
    private LayoutInflater l_Inflater;

    public LA_Menu(Context context, PBase owner, ArrayList<clsClasses.clsMenu> results) {
        items = results;
        l_Inflater = LayoutInflater.from(context);
        selectedIndex = -1;

        mu = owner.mu;
        du = owner.du;
        app = owner.app;
    }

    public void setSelectedIndex(int ind) {
        selectedIndex = ind;
        notifyDataSetChanged();
    }

    public void refreshItems() {
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

            convertView = l_Inflater.inflate(R.layout.lv_menu, null);
            holder = new ViewHolder();

            holder.lbl1 = (TextView) convertView.findViewById(R.id.lblV1);
            holder.lbl2 = (TextView) convertView.findViewById(R.id.lblV2);
            holder.img1 = (ImageView) convertView.findViewById(R.id.imageView5);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.lbl1.setText("" + items.get(position).nombre);
        holder.lbl2.setText("" + items.get(position).id);

        switch (items.get(position).id) {
            case 1:
                holder.img1.setImageResource(R.drawable.users);break;
            case 2:
                holder.img1.setImageResource(R.drawable.update);break;
            case 3:
                holder.img1.setImageResource(R.drawable.btn_exit);break;
        }


        if (selectedIndex != -1 && position == selectedIndex) {
            convertView.setBackgroundColor(Color.rgb(26, 138, 198));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView lbl1, lbl2;
        ImageView img1;
    }

}

