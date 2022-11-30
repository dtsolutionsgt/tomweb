package com.dts.classes;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dts.base.BaseDatos;
import com.dts.base.clsClasses;

import java.util.ArrayList;


public class clsArticulo_codigo_barraObj {

    public int count;

    private Context cont;
    private BaseDatos Con;
    private SQLiteDatabase db;
    public BaseDatos.Insert ins;
    public BaseDatos.Update upd;
    private clsClasses clsCls = new clsClasses();

    private String sel = "SELECT * FROM Articulo_codigo_barra";
    private String sql;
    public ArrayList<clsClasses.clsArticulo_codigo_barra> items = new ArrayList<clsClasses.clsArticulo_codigo_barra>();

    public clsArticulo_codigo_barraObj(Context context, BaseDatos dbconnection, SQLiteDatabase dbase) {
        cont = context;
        Con = dbconnection;
        ins = Con.Ins;
        upd = Con.Upd;
        db = dbase;
        count = 0;
    }

    public void reconnect(BaseDatos dbconnection, SQLiteDatabase dbase) {
        Con = dbconnection;
        ins = Con.Ins;
        upd = Con.Upd;
        db = dbase;
    }

    public void add(clsClasses.clsArticulo_codigo_barra item) {
        addItem(item);
    }

    public void update(clsClasses.clsArticulo_codigo_barra item) {
        updateItem(item);
    }

    public void delete(clsClasses.clsArticulo_codigo_barra item) {
        deleteItem(item);
    }

    public void delete(int id) {
        deleteItem(id);
    }

    public void fill() {
        fillItems(sel);
    }

    public void fill(String specstr) {
        fillItems(sel + " " + specstr);
    }

    public void fillSelect(String sq) {
        fillItems(sq);
    }

    public clsClasses.clsArticulo_codigo_barra first() {
        return items.get(0);
    }


    // Private

    private void addItem(clsClasses.clsArticulo_codigo_barra item) {

        ins.init("Articulo_codigo_barra");

        ins.add("id_articulo", item.id_articulo);
        ins.add("codigo_barra", item.codigo_barra);
        ins.add("id_empresa", item.id_empresa);

        db.execSQL(ins.sql());

    }

    private void updateItem(clsClasses.clsArticulo_codigo_barra item) {

        upd.init("Articulo_codigo_barra");

        upd.add("id_articulo", item.id_articulo);
        upd.add("codigo_barra", item.codigo_barra);

        upd.Where("(id_empresa=" + item.id_empresa + ")");

        db.execSQL(upd.sql());

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

    private void deleteItem(clsClasses.clsArticulo_codigo_barra item) {
        sql = "DELETE FROM Articulo_codigo_barra WHERE (id_empresa=" + item.id_empresa + ")";
        db.execSQL(sql);
    }

    private void deleteItem(int id) {
        sql = "DELETE FROM Articulo_codigo_barra WHERE id=" + id;
        db.execSQL(sql);
    }

    private void fillItems(String sq) {
        Cursor dt;
        clsClasses.clsArticulo_codigo_barra item;

        items.clear();

        dt = Con.OpenDT(sq);
        count = dt.getCount();
        if (dt.getCount() > 0) dt.moveToFirst();

        while (!dt.isAfterLast()) {

            item = clsCls.new clsArticulo_codigo_barra();

            item.id_empresa = dt.getInt(0);
            item.id_articulo = dt.getString(1);
            item.codigo_barra = dt.getString(2);

            items.add(item);

            dt.moveToNext();
        }

        if (dt!=null) dt.close();

    }

    public int newID(String idsql) {
        Cursor dt;
        int nid;

        try {
            dt = Con.OpenDT(idsql);
            dt.moveToFirst();
            nid = dt.getInt(0) + 1;
            if (dt!=null) dt.close();
        } catch (Exception e) {
            nid = 1;
        }


        return nid;
    }

    public String addItemSql(clsClasses.clsArticulo_codigo_barra item) {

        ins.init("Articulo_codigo_barra");

        ins.add("id_empresa", item.id_empresa);
        ins.add("id_articulo", item.id_articulo);
        ins.add("codigo_barra", item.codigo_barra);

        return ins.sql();

    }

    public String updateItemSql(clsClasses.clsArticulo_codigo_barra item) {

        upd.init("Articulo_codigo_barra");

        upd.add("id_articulo", item.id_articulo);
        upd.add("codigo_barra", item.codigo_barra);

        upd.Where("(id_empresa=" + item.id_empresa + ")");

        return upd.sql();

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

}

