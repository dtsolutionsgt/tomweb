package com.dts.classes;

import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dts.base.BaseDatos;
import com.dts.base.clsClasses;

public class clsInventario_ciegoObj {

    public int count;

    private Context cont;
    private BaseDatos Con;
    private SQLiteDatabase db;
    public BaseDatos.Insert ins;
    public BaseDatos.Update upd;
    private clsClasses clsCls = new clsClasses();

    private String sel="SELECT * FROM Inventario_ciego";
    private String sql;
    public ArrayList<clsClasses.clsInventario_ciego> items= new ArrayList<clsClasses.clsInventario_ciego>();

    public clsInventario_ciegoObj(Context context, BaseDatos dbconnection, SQLiteDatabase dbase) {
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

    public void add(clsClasses.clsInventario_ciego item) {
        addItem(item);
    }

    public void update(clsClasses.clsInventario_ciego item) {
        updateItem(item);
    }

    public void delete(clsClasses.clsInventario_ciego item) {
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

    public clsClasses.clsInventario_ciego first() {
        return items.get(0);
    }


    // Private

    private void addItem(clsClasses.clsInventario_ciego item) {

        ins.init("Inventario_ciego");

        ins.add("id_inventario_enc",item.id_inventario_enc);
        ins.add("codigo_barra",item.codigo_barra);
        ins.add("cantidad",item.cantidad);
        //ins.add("id",item.id);
        ins.add("comunicado",item.comunicado);
        ins.add("ubicacion",item.ubicacion);
        ins.add("id_operador",item.id_operador);
        ins.add("fecha",item.fecha);
        ins.add("Id_registro",item.id_registro);
        ins.add("eliminado",item.eliminado);

        db.execSQL(ins.sql());

    }

    private void updateItem(clsClasses.clsInventario_ciego item) {

        upd.init("Inventario_ciego");

        upd.add("id_inventario_enc",item.id_inventario_enc);
        upd.add("codigo_barra",item.codigo_barra);
        upd.add("cantidad",item.cantidad);
        upd.add("comunicado",item.comunicado);
        upd.add("ubicacion",item.ubicacion);
        upd.add("id_operador",item.id_operador);
        upd.add("fecha",item.fecha);
        upd.add("Id_registro",item.id_registro);
        upd.add("eliminado",item.eliminado);

        upd.Where("(id="+item.id+")");

        db.execSQL(upd.sql());

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

    private void deleteItem(clsClasses.clsInventario_ciego item) {
        sql="DELETE FROM Inventario_ciego WHERE (id="+item.id+")";
        db.execSQL(sql);
    }

    private void deleteItem(int id) {
        sql="DELETE FROM Inventario_ciego WHERE id=" + id;
        db.execSQL(sql);
    }

    private void fillItems(String sq) {
        Cursor dt;
        clsClasses.clsInventario_ciego item;

        items.clear();

        dt=Con.OpenDT(sq);
        count =dt.getCount();
        if (dt.getCount()>0) dt.moveToFirst();

        while (!dt.isAfterLast()) {

            item = new clsClasses.clsInventario_ciego();

            item.id_inventario_enc=dt.getInt(0);
            item.codigo_barra=dt.getString(1);
            item.cantidad=dt.getDouble(2);
            item.id=dt.getInt(3);
            item.comunicado=dt.getString(4);
            item.ubicacion=dt.getString(5);
            item.id_operador=dt.getInt(6);
            item.fecha=dt.getString(7);
            item.id_registro=dt.getInt(8);
            item.eliminado=dt.getInt(9);

            items.add(item);

            dt.moveToNext();
        }
        if (dt!=null) dt.close();

    }

    public int newID(String idsql) {
        Cursor dt;
        int nid;

        try {
            dt=Con.OpenDT(idsql);
            dt.moveToFirst();
            nid=dt.getInt(0)+1;
            if (dt!=null) dt.close();
        } catch (Exception e) {
            nid=1;
        }

        return nid;
    }

    public String addItemSql(clsClasses.clsInventario_ciego item) {

        ins.init("Inventario_ciego");

        ins.add("id_inventario_enc",item.id_inventario_enc);
        ins.add("codigo_barra",item.codigo_barra);
        ins.add("cantidad",item.cantidad);
        ins.add("id",item.id);
        ins.add("comunicado",item.comunicado);
        ins.add("ubicacion",item.ubicacion);
        ins.add("id_operador",item.id_operador);
        ins.add("fecha",item.fecha);
        ins.add("Id_registro",item.id_registro);
        ins.add("eliminado",item.eliminado);

        return ins.sql();

    }

    public String updateItemSql(clsClasses.clsInventario_ciego item) {

        upd.init("Inventario_ciego");

        upd.add("id_inventario_enc",item.id_inventario_enc);
        upd.add("codigo_barra",item.codigo_barra);
        upd.add("cantidad",item.cantidad);
        upd.add("comunicado",item.comunicado);
        upd.add("ubicacion",item.ubicacion);
        upd.add("id_operador",item.id_operador);
        upd.add("fecha",item.fecha);
        upd.add("Id_registro",item.id_registro);
        upd.add("eliminado",item.eliminado);

        upd.Where("(id="+item.id+")");

        return upd.sql();

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

}
