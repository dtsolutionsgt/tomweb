package com.dts.classes;

import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dts.base.BaseDatos;
import com.dts.base.clsClasses;


public class clsInventario_teoricoObj {

    public int count;

    private Context cont;
    private BaseDatos Con;
    private SQLiteDatabase db;
    public BaseDatos.Insert ins;
    public BaseDatos.Update upd;
    private clsClasses clsCls = new clsClasses();

    private String sel="SELECT * FROM Inventario_teorico";
    private String sql;
    public ArrayList<clsClasses.clsInventario_teorico> items= new ArrayList<clsClasses.clsInventario_teorico>();

    public clsInventario_teoricoObj(Context context, BaseDatos dbconnection, SQLiteDatabase dbase) {
        cont=context;
        Con=dbconnection;
        ins=Con.Ins;upd=Con.Upd;
        db = dbase;
        count = 0;
    }

    public void reconnect(BaseDatos dbconnection, SQLiteDatabase dbase) {
        Con=dbconnection;
        ins=Con.Ins;upd=Con.Upd;
        db = dbase;
    }

    public void add(clsClasses.clsInventario_teorico item) {
        addItem(item);
    }

    public void update(clsClasses.clsInventario_teorico item) {
        updateItem(item);
    }

    public void delete(clsClasses.clsInventario_teorico item) {
        deleteItem(item);
    }

    public void delete(int id) {
        deleteItem(id);
    }

    public void fill() {
        fillItems(sel);
    }

    public void fill(String specstr) {
        fillItems(sel+ " "+specstr);
    }

    public void fillSelect(String sq) {
        fillItems(sq);
    }

    public clsClasses.clsInventario_teorico first() {
        return items.get(0);
    }


    // Private

    private void addItem(clsClasses.clsInventario_teorico item) {

        ins.init("Inventario_teorico");

        ins.add("id_empresa",item.id_empresa);
        ins.add("id_articulo",item.id_articulo);
        ins.add("descripcion",item.descripcion);
        ins.add("cantidad",item.cantidad);
        ins.add("codigo_barra",item.codigo_barra);
        ins.add("costo",item.costo);
        ins.add("tipo_conteo",item.tipo_conteo);
        ins.add("id_inventario_enc",item.id_inventario_enc);

        db.execSQL(ins.sql());

    }

    private void updateItem(clsClasses.clsInventario_teorico item) {

        upd.init("Inventario_teorico");

        upd.add("descripcion",item.descripcion);
        upd.add("cantidad",item.cantidad);
        upd.add("costo",item.costo);
        upd.add("tipo_conteo",item.tipo_conteo);
        upd.add("id_inventario_enc",item.id_inventario_enc);

        upd.Where("(id_empresa="+item.id_empresa+") AND (id_articulo='"+item.id_articulo+"') AND (codigo_barra='"+item.codigo_barra+"')");

        db.execSQL(upd.sql());

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

    private void deleteItem(clsClasses.clsInventario_teorico item) {
        sql="DELETE FROM Inventario_teorico WHERE (id_empresa="+item.id_empresa+") AND (id_articulo='"+item.id_articulo+"') AND (codigo_barra='"+item.codigo_barra+"')";
        db.execSQL(sql);
    }

    private void deleteItem(int id) {
        sql="DELETE FROM Inventario_teorico WHERE id=" + id;
        db.execSQL(sql);
    }

    private void fillItems(String sq) {
        Cursor dt;
        clsClasses.clsInventario_teorico item;

        items.clear();

        dt=Con.OpenDT(sq);
        count =dt.getCount();
        if (dt.getCount()>0) dt.moveToFirst();

        while (!dt.isAfterLast()) {

            item = clsCls.new clsInventario_teorico();

            item.id_empresa=dt.getInt(0);
            item.id_articulo=dt.getString(1);
            item.descripcion=dt.getString(2);
            item.cantidad=dt.getDouble(3);
            item.codigo_barra=dt.getString(4);
            item.costo=dt.getDouble(5);
            item.tipo_conteo=dt.getString(6);
            item.id_inventario_enc=dt.getInt(7);

            items.add(item);

            dt.moveToNext();
        }

    }

    public int newID(String idsql) {
        Cursor dt;
        int nid;

        try {
            dt=Con.OpenDT(idsql);
            dt.moveToFirst();
            nid=dt.getInt(0)+1;
        } catch (Exception e) {
            nid=1;
        }

        return nid;
    }

    public String addItemSql(clsClasses.clsInventario_teorico item) {

        ins.init("Inventario_teorico");

        ins.add("id_empresa",item.id_empresa);
        ins.add("id_articulo",item.id_articulo);
        ins.add("descripcion",item.descripcion);
        ins.add("cantidad",item.cantidad);
        ins.add("codigo_barra",item.codigo_barra);
        ins.add("costo",item.costo);
        ins.add("tipo_conteo",item.tipo_conteo);
        ins.add("id_inventario_enc",item.id_inventario_enc);

        return ins.sql();

    }

    public String updateItemSql(clsClasses.clsInventario_teorico item) {

        upd.init("Inventario_teorico");

        upd.add("descripcion",item.descripcion);
        upd.add("cantidad",item.cantidad);
        upd.add("costo",item.costo);
        upd.add("tipo_conteo",item.tipo_conteo);
        upd.add("id_inventario_enc",item.id_inventario_enc);

        upd.Where("(id_empresa="+item.id_empresa+") AND (id_articulo='"+item.id_articulo+"') AND (codigo_barra='"+item.codigo_barra+"')");

        return upd.sql();

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

}

