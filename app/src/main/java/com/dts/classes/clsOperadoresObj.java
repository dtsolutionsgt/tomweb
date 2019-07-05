package com.dts.classes;

import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dts.base.BaseDatos;
import com.dts.base.clsClasses;


public class clsOperadoresObj {

    public int count;

    private Context cont;
    private BaseDatos Con;
    private SQLiteDatabase db;
    public BaseDatos.Insert ins;
    public BaseDatos.Update upd;
    private clsClasses clsCls = new clsClasses();

    private String sel = "SELECT * FROM Operadores";
    private String sql;
    public ArrayList<clsClasses.clsOperadores> items = new ArrayList<clsClasses.clsOperadores>();

    public clsOperadoresObj(Context context, BaseDatos dbconnection, SQLiteDatabase dbase) {
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

    public void add(clsClasses.clsOperadores item) {
        addItem(item);
    }

    public void update(clsClasses.clsOperadores item) {
        updateItem(item);
    }

    public void delete(clsClasses.clsOperadores item) {
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

    public clsClasses.clsOperadores first() {
        return items.get(0);
    }


    // Private

    private void addItem(clsClasses.clsOperadores item) {

        ins.init("Operadores");

        ins.add("id_operador", item.id_operador);
        ins.add("id_empresa", item.id_empresa);
        ins.add("codigo", item.codigo);
        ins.add("clave", item.clave);
        ins.add("nombre", item.nombre);

        db.execSQL(ins.sql());

    }

    private void updateItem(clsClasses.clsOperadores item) {

        upd.init("Operadores");

        upd.add("id_empresa", item.id_empresa);
        upd.add("codigo", item.codigo);
        upd.add("clave", item.clave);
        upd.add("nombre", item.nombre);

        upd.Where("(id_operador=" + item.id_operador + ")");

        db.execSQL(upd.sql());

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

    private void deleteItem(clsClasses.clsOperadores item) {
        sql = "DELETE FROM Operadores WHERE (id_operador=" + item.id_operador + ")";
        db.execSQL(sql);
    }

    private void deleteItem(int id) {
        sql = "DELETE FROM Operadores WHERE id=" + id;
        db.execSQL(sql);
    }

    private void fillItems(String sq) {
        Cursor dt;
        clsClasses.clsOperadores item;

        items.clear();

        dt = Con.OpenDT(sq);
        count = dt.getCount();
        if (dt.getCount() > 0) dt.moveToFirst();

        while (!dt.isAfterLast()) {

            item = clsCls.new clsOperadores();

            item.id_operador = dt.getInt(0);
            item.id_empresa = dt.getInt(1);
            item.codigo = dt.getString(2);
            item.clave = dt.getString(3);
            item.nombre = dt.getString(4);

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

    public String addItemSql(clsClasses.clsOperadores item) {

        ins.init("Operadores");

        ins.add("id_operador", item.id_operador);
        ins.add("id_empresa", item.id_empresa);
        ins.add("codigo", item.codigo);
        ins.add("clave", item.clave);
        ins.add("nombre", item.nombre);

        return ins.sql();

    }

    public String updateItemSql(clsClasses.clsOperadores item) {

        upd.init("Operadores");

        upd.add("id_empresa", item.id_empresa);
        upd.add("codigo", item.codigo);
        upd.add("clave", item.clave);
        upd.add("nombre", item.nombre);

        upd.Where("(id_operador=" + item.id_operador + ")");

        return upd.sql();

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

}


