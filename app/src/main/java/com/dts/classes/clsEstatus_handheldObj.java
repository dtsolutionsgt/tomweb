package com.dts.classes;

import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dts.base.BaseDatos;
import com.dts.base.clsClasses;

public class clsEstatus_handheldObj {

    public int count;

    private Context cont;
    private BaseDatos Con;
    private SQLiteDatabase db;
    public BaseDatos.Insert ins;
    public BaseDatos.Update upd;
    private clsClasses clsCls = new clsClasses();

    private String sel = "SELECT * FROM Estatus_handheld";
    private String sql;
    public ArrayList<clsClasses.clsEstatus_handheld> items = new ArrayList<clsClasses.clsEstatus_handheld>();

    public clsEstatus_handheldObj(Context context, BaseDatos dbconnection, SQLiteDatabase dbase) {
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

    public void add(clsClasses.clsEstatus_handheld item) {
        addItem(item);
    }

    public void update(clsClasses.clsEstatus_handheld item) {
        updateItem(item);
    }

    public void delete(clsClasses.clsEstatus_handheld item) {
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

    public clsClasses.clsEstatus_handheld first() {
        return items.get(0);
    }


    // Private

    private void addItem(clsClasses.clsEstatus_handheld item) {

        ins.init("Estatus_handheld");

        ins.add("id", item.id);
        ins.add("nombre", item.nombre);

        db.execSQL(ins.sql());

    }

    private void updateItem(clsClasses.clsEstatus_handheld item) {

        upd.init("Estatus_handheld");

        upd.add("nombre", item.nombre);

        upd.Where("(id=" + item.id + ")");

        db.execSQL(upd.sql());

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

    private void deleteItem(clsClasses.clsEstatus_handheld item) {
        sql = "DELETE FROM Estatus_handheld WHERE (id=" + item.id + ")";
        db.execSQL(sql);
    }

    private void deleteItem(int id) {
        sql = "DELETE FROM Estatus_handheld WHERE id=" + id;
        db.execSQL(sql);
    }

    private void fillItems(String sq) {
        Cursor dt;
        clsClasses.clsEstatus_handheld item;

        items.clear();

        dt = Con.OpenDT(sq);
        count = dt.getCount();
        if (dt.getCount() > 0) dt.moveToFirst();

        while (!dt.isAfterLast()) {

            item = clsCls.new clsEstatus_handheld();

            item.id = dt.getInt(0);
            item.nombre = dt.getString(1);

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

    public String addItemSql(clsClasses.clsEstatus_handheld item) {

        ins.init("Estatus_handheld");

        ins.add("id", item.id);
        ins.add("nombre", item.nombre);

        return ins.sql();

    }

    public String updateItemSql(clsClasses.clsEstatus_handheld item) {

        upd.init("Estatus_handheld");

        upd.add("nombre", item.nombre);

        upd.Where("(id=" + item.id + ")");

        return upd.sql();

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

}

