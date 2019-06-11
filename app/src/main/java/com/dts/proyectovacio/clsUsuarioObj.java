package com.dts.proyectovacio;

import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class clsUsuarioObj {

    public int count;

    private Context cont;
    private BaseDatos Con;
    private SQLiteDatabase db;
    public BaseDatos.Insert ins;
    public BaseDatos.Update upd;
    private clsClasses clsCls = new clsClasses();

    private String sel="SELECT * FROM Usuario";
    private String sql;
    public ArrayList<clsClasses.clsUsuario> items= new ArrayList<clsClasses.clsUsuario>();

    public clsUsuarioObj(Context context, BaseDatos dbconnection, SQLiteDatabase dbase) {
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

    public void add(clsClasses.clsUsuario item) {
        addItem(item);
    }

    public void update(clsClasses.clsUsuario item) {
        updateItem(item);
    }

    public void delete(clsClasses.clsUsuario item) {
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

    public clsClasses.clsUsuario first() {
        return items.get(0);
    }


    // Private

    private void addItem(clsClasses.clsUsuario item) {

        ins.init("Usuario");

        ins.add("ID",item.id);
        ins.add("Nombre",item.nombre);
        ins.add("Activo",item.activo);
        ins.add("Login",item.login);
        ins.add("Clave",item.clave);
        ins.add("Rol",item.rol);

        db.execSQL(ins.sql());

    }

    private void updateItem(clsClasses.clsUsuario item) {

        upd.init("Usuario");

        upd.add("Nombre",item.nombre);
        upd.add("Activo",item.activo);
        upd.add("Login",item.login);
        upd.add("Clave",item.clave);
        upd.add("Rol",item.rol);

        upd.Where("(ID="+item.id+")");

        db.execSQL(upd.sql());

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

    private void deleteItem(clsClasses.clsUsuario item) {
        sql="DELETE FROM Usuario WHERE (ID="+item.id+")";
        db.execSQL(sql);
    }

    private void deleteItem(int id) {
        sql="DELETE FROM Usuario WHERE id=" + id;
        db.execSQL(sql);
    }

    private void fillItems(String sq) {
        Cursor dt;
        clsClasses.clsUsuario item;

        items.clear();

        dt=Con.OpenDT(sq);
        count =dt.getCount();
        if (dt.getCount()>0) dt.moveToFirst();

        while (!dt.isAfterLast()) {

            item = clsCls.new clsUsuario();

            item.id=dt.getInt(0);
            item.nombre=dt.getString(1);
            item.activo=dt.getInt(2);
            item.login=dt.getString(3);
            item.clave=dt.getString(4);
            item.rol=dt.getInt(5);

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


}

