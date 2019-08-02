package com.dts.classes;

import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dts.base.BaseDatos;
import com.dts.base.clsClasses;


public class clsInventario_operadorObj {

    public int count;

    private Context cont;
    private BaseDatos Con;
    private SQLiteDatabase db;
    public BaseDatos.Insert ins;
    public BaseDatos.Update upd;
    private clsClasses clsCls = new clsClasses();

    private String sel="SELECT * FROM Inventario_operador";
    private String sql;
    public ArrayList<clsClasses.clsInventario_operador> items= new ArrayList<clsClasses.clsInventario_operador>();

    public clsInventario_operadorObj(Context context, BaseDatos dbconnection, SQLiteDatabase dbase) {
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

    public void add(clsClasses.clsInventario_operador item) {
        addItem(item);
    }

    public void update(clsClasses.clsInventario_operador item) {
        updateItem(item);
    }

    public void delete(clsClasses.clsInventario_operador item) {
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

    public clsClasses.clsInventario_operador first() {
        return items.get(0);
    }


    // Private

    private void addItem(clsClasses.clsInventario_operador item) {

        ins.init("Inventario_operador");

        ins.add("id_inventario_enc",item.id_inventario_enc);
        ins.add("id_operador",item.id_operador);

        db.execSQL(ins.sql());

    }

    private void updateItem(clsClasses.clsInventario_operador item) {

        upd.init("Inventario_operador");


        upd.Where("(id_inventario_enc="+item.id_inventario_enc+") AND (id_operador="+item.id_operador+")");

        db.execSQL(upd.sql());

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

    private void deleteItem(clsClasses.clsInventario_operador item) {
        sql="DELETE FROM Inventario_operador WHERE (id_inventario_enc="+item.id_inventario_enc+") AND (id_operador="+item.id_operador+")";
        db.execSQL(sql);
    }

    private void deleteItem(int id) {
        sql="DELETE FROM Inventario_operador WHERE id=" + id;
        db.execSQL(sql);
    }

    private void fillItems(String sq) {
        Cursor dt;
        clsClasses.clsInventario_operador item;

        items.clear();

        dt=Con.OpenDT(sq);
        count =dt.getCount();
        if (dt.getCount()>0) dt.moveToFirst();

        while (!dt.isAfterLast()) {

            item = clsCls.new clsInventario_operador();

            item.id_inventario_enc=dt.getInt(0);
            item.id_operador=dt.getInt(1);

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

    public String addItemSql(clsClasses.clsInventario_operador item) {

        ins.init("Inventario_operador");

        ins.add("id_inventario_enc",item.id_inventario_enc);
        ins.add("id_operador",item.id_operador);

        return ins.sql();

    }

    public String updateItemSql(clsClasses.clsInventario_operador item) {

        upd.init("Inventario_operador");


        upd.Where("(id_inventario_enc="+item.id_inventario_enc+") AND (id_operador="+item.id_operador+")");

        return upd.sql();

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

}

