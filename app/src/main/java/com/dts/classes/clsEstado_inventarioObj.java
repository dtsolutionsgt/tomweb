package com.dts.classes;

import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dts.base.BaseDatos;
import com.dts.base.clsClasses;

public class clsEstado_inventarioObj {

    public int count;

    private Context cont;
    private BaseDatos Con;
    private SQLiteDatabase db;
    public BaseDatos.Insert ins;
    public BaseDatos.Update upd;
    private clsClasses clsCls = new clsClasses();

    private String sel = "SELECT * FROM Estado_inventario";
    private String sql;
    public ArrayList<clsClasses.clsEstado_inventario> items = new ArrayList<clsClasses.clsEstado_inventario>();

    public clsEstado_inventarioObj(Context context, BaseDatos dbconnection, SQLiteDatabase dbase) {
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

    public void add(clsClasses.clsEstado_inventario item) {
        addItem(item);
    }

    public void update(clsClasses.clsEstado_inventario item) {
        updateItem(item);
    }

    public void delete(clsClasses.clsEstado_inventario item) {
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

    public clsClasses.clsEstado_inventario first() {
        return items.get(0);
    }


    // Private

    private void addItem(clsClasses.clsEstado_inventario item) {

        ins.init("Estado_inventario");

        ins.add("Id_estado", item.id_estado);
        ins.add("nombre", item.nombre);

        db.execSQL(ins.sql());

    }

    private void updateItem(clsClasses.clsEstado_inventario item) {

        upd.init("Estado_inventario");

        upd.add("nombre", item.nombre);

        upd.Where("(Id_estado=" + item.id_estado + ")");

        db.execSQL(upd.sql());

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

    private void deleteItem(clsClasses.clsEstado_inventario item) {
        sql = "DELETE FROM Estado_inventario WHERE (Id_estado=" + item.id_estado + ")";
        db.execSQL(sql);
    }

    private void deleteItem(int id) {
        sql = "DELETE FROM Estado_inventario WHERE id=" + id;
        db.execSQL(sql);
    }

    private void fillItems(String sq) {
        Cursor dt;
        clsClasses.clsEstado_inventario item;

        items.clear();

        dt = Con.OpenDT(sq);
        count = dt.getCount();
        if (dt.getCount() > 0) dt.moveToFirst();

        while (!dt.isAfterLast()) {

            item = clsCls.new clsEstado_inventario();

            item.id_estado = dt.getInt(0);
            item.nombre = dt.getString(1);

            items.add(item);

            dt.moveToNext();
        }

    }

    public int newID(String idsql) {
        Cursor dt;
        int nid;

        try {
            dt = Con.OpenDT(idsql);
            dt.moveToFirst();
            nid = dt.getInt(0) + 1;
        } catch (Exception e) {
            nid = 1;
        }

        return nid;
    }

    public String addItemSql(clsClasses.clsEstado_inventario item) {

        ins.init("Estado_inventario");

        ins.add("Id_estado", item.id_estado);
        ins.add("nombre", item.nombre);

        return ins.sql();

    }

    public String updateItemSql(clsClasses.clsEstado_inventario item) {

        upd.init("Estado_inventario");

        upd.add("nombre", item.nombre);

        upd.Where("(Id_estado=" + item.id_estado + ")");

        return upd.sql();

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

}

