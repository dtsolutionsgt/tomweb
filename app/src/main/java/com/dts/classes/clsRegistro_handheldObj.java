package com.dts.classes;

import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dts.base.BaseDatos;
import com.dts.base.clsClasses;

public class clsRegistro_handheldObj {

    public int count;

    private Context cont;
    private BaseDatos Con;
    private SQLiteDatabase db;
    public BaseDatos.Insert ins;
    public BaseDatos.Update upd;
    private clsClasses clsCls = new clsClasses();

    private String sel = "SELECT * FROM Registro_handheld";
    private String sql;
    public ArrayList<clsClasses.clsRegistro_handheld> items = new ArrayList<clsClasses.clsRegistro_handheld>();

    public clsRegistro_handheldObj(Context context, BaseDatos dbconnection, SQLiteDatabase dbase) {
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

    public void add(clsClasses.clsRegistro_handheld item) {
        addItem(item);
    }

    public void update(clsClasses.clsRegistro_handheld item) {
        updateItem(item);
    }

    public void delete(clsClasses.clsRegistro_handheld item) {
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

    public clsClasses.clsRegistro_handheld first() {
        return items.get(0);
    }


    // Private

    private void addItem(clsClasses.clsRegistro_handheld item) {

        ins.init("Registro_handheld");

        ins.add("id_registro", item.id_registro);
        ins.add("id_empresa", item.id_empresa);
        ins.add("fecha_registro", item.fecha_registro);
        ins.add("serie_dispositivo", item.serie_dispositivo);
        ins.add("id_estatus", item.id_estatus);
        ins.add("id_pais", item.id_pais);
        ins.add("descripcion", item.descripcion);

        db.execSQL(ins.sql());

    }

    private void updateItem(clsClasses.clsRegistro_handheld item) {

        upd.init("Registro_handheld");

        upd.add("id_registro", item.id_registro);
        upd.add("fecha_registro", item.fecha_registro);
        upd.add("id_estatus", item.id_estatus);
        upd.add("id_pais", item.id_pais);
        upd.add("descripcion", item.descripcion);

        upd.Where("(id_empresa=" + item.id_empresa + ") AND (serie_dispositivo='" + item.serie_dispositivo + "')");

        db.execSQL(upd.sql());

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

    private void deleteItem(clsClasses.clsRegistro_handheld item) {
        sql = "DELETE FROM Registro_handheld WHERE (id_empresa=" + item.id_empresa + ") AND (serie_dispositivo='" + item.serie_dispositivo + "')";
        db.execSQL(sql);
    }

    private void deleteItem(int id) {
        sql = "DELETE FROM Registro_handheld WHERE id=" + id;
        db.execSQL(sql);
    }

    private void fillItems(String sq) {
        Cursor dt;
        clsClasses.clsRegistro_handheld item;

        items.clear();

        dt = Con.OpenDT(sq);
        count = dt.getCount();
        if (dt.getCount() > 0) dt.moveToFirst();

        while (!dt.isAfterLast()) {

            item = clsCls.new clsRegistro_handheld();

            item.id_registro = dt.getInt(0);
            item.id_empresa = dt.getInt(1);
            item.fecha_registro = dt.getString(2);
            item.serie_dispositivo = dt.getString(3);
            item.id_estatus = dt.getString(4);
            item.id_pais = dt.getString(5);
            item.descripcion = dt.getString(6);

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

    public String addItemSql(clsClasses.clsRegistro_handheld item) {

        ins.init("Registro_handheld");

        ins.add("id_registro", item.id_registro);
        ins.add("id_empresa", item.id_empresa);
        ins.add("fecha_registro", item.fecha_registro);
        ins.add("serie_dispositivo", item.serie_dispositivo);
        ins.add("id_estatus", item.id_estatus);
        ins.add("id_pais", item.id_pais);
        ins.add("descripcion", item.descripcion);

        return ins.sql();

    }

    public String updateItemSql(clsClasses.clsRegistro_handheld item) {

        upd.init("Registro_handheld");

        upd.add("id_registro", item.id_registro);
        upd.add("fecha_registro", item.fecha_registro);
        upd.add("id_estatus", item.id_estatus);
        upd.add("id_pais", item.id_pais);
        upd.add("descripcion", item.descripcion);

        upd.Where("(id_empresa=" + item.id_empresa + ") AND (serie_dispositivo='" + item.serie_dispositivo + "')");

        return upd.sql();

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

}

