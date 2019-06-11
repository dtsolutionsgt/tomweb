package com.dts.proyectovacio;

import android.content.Context;

import java.util.ArrayList;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LA_Usuario extends BaseAdapter {

    private MiscUtils mu;
    private DateUtils du;
    private AppMethods app;

    private ArrayList<clsClasses.clsUsuario> items = new ArrayList<clsClasses.clsUsuario>();
    private int selectedIndex;
    private LayoutInflater l_Inflater;

    public LA_Usuario(Context context, PBase owner, ArrayList<clsClasses.clsUsuario> results) {
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

            convertView = l_Inflater.inflate(R.layout.lv_usuario, null);
            holder = new ViewHolder();

            holder.lbl1 = (TextView) convertView.findViewById(R.id.lblV1);
            holder.lbl2 = (TextView) convertView.findViewById(R.id.lblV4);
            holder.lbl3 = (TextView) convertView.findViewById(R.id.lblV5);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.lbl1.setText("" + items.get(position).nombre);
        holder.lbl2.setText("" + items.get(position).login);
        holder.lbl3.setText("" + items.get(position).clave);

        if (selectedIndex != -1 && position == selectedIndex) {
            convertView.setBackgroundColor(Color.rgb(26, 138, 198));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView lbl1, lbl2, lbl3;
    }

}

