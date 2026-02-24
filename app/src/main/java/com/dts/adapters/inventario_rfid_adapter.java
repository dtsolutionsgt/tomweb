package com.dts.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dts.classes.clsInventario_Rfid;
import com.dts.tomweb.R;

import java.util.List;

public class inventario_rfid_adapter extends RecyclerView.Adapter<inventario_rfid_adapter.ViewHolder> {

    private List<clsInventario_Rfid> items;
    private int selectedIndex = -1;
    private Context context;

    public inventario_rfid_adapter(Context context, List<clsInventario_Rfid> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_inventario_rfid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        clsInventario_Rfid item = items.get(position);

        holder.lblTag.setText(item.tag);
        holder.lblProducto.setText(item.producto);
        holder.lblCantidad.setText(String.valueOf(item.cantidad));

        if (selectedIndex != -1 && position == selectedIndex) {
            holder.itemView.setBackgroundColor(Color.rgb(26,138,198));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView lblTag, lblProducto, lblCantidad;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            lblTag = itemView.findViewById(R.id.txtTagRfid);
            lblProducto = itemView.findViewById(R.id.txtDescripcion);
            lblCantidad = itemView.findViewById(R.id.txtCantidad);
        }
    }
}
