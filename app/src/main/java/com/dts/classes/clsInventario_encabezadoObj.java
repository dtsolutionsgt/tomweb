package com.dts.classes;

import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dts.base.BaseDatos;
import com.dts.base.clsClasses;

public class clsInventario_encabezadoObj {

    public int count;

    private Context cont;
    private BaseDatos Con;
    private SQLiteDatabase db;
    public BaseDatos.Insert ins;
    public BaseDatos.Update upd;
    private clsClasses clsCls = new clsClasses();

    private String sel="SELECT * FROM Inventario_encabezado";
    private String sql;
    public ArrayList<clsClasses.clsInventario_encabezado> items= new ArrayList<clsClasses.clsInventario_encabezado>();

    public clsInventario_encabezadoObj(Context context, BaseDatos dbconnection, SQLiteDatabase dbase) {
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

    public void add(clsClasses.clsInventario_encabezado item) {
        addItem(item);
    }

    public void update(clsClasses.clsInventario_encabezado item) {
        updateItem(item);
    }

    public void delete(clsClasses.clsInventario_encabezado item) {
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

    public clsClasses.clsInventario_encabezado first() {
        return items.get(0);
    }


    // Private

    private void addItem(clsClasses.clsInventario_encabezado item) {

        ins.init("Inventario_encabezado");

        ins.add("id_inventario_enc",item.id_inventario_enc);
        ins.add("id_estado",item.id_estado);
        ins.add("id_empresa",item.id_empresa);
        ins.add("fecha_inicio",item.fecha_inicio);
        ins.add("fecha_final",item.fecha_final);
        ins.add("nombre",item.nombre);
        ins.add("id_usuario",item.id_usuario);
        ins.add("tipo_inventario",item.tipo_inventario);

        db.execSQL(ins.sql());

    }

    private void updateItem(clsClasses.clsInventario_encabezado item) {

        upd.init("Inventario_encabezado");

        upd.add("id_estado",item.id_estado);
        upd.add("id_empresa",item.id_empresa);
        upd.add("fecha_inicio",item.fecha_inicio);
        upd.add("fecha_final",item.fecha_final);
        upd.add("nombre",item.nombre);
        upd.add("id_usuario",item.id_usuario);
        upd.add("tipo_inventario",item.tipo_inventario);

        upd.Where("(id_inventario_enc="+item.id_inventario_enc+")");

        db.execSQL(upd.sql());

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

    private void deleteItem(clsClasses.clsInventario_encabezado item) {
        sql="DELETE FROM Inventario_encabezado WHERE (id_inventario_enc="+item.id_inventario_enc+")";
        db.execSQL(sql);
    }

    private void deleteItem(int id) {
        sql="DELETE FROM Inventario_encabezado WHERE id=" + id;
        db.execSQL(sql);
    }

    private void fillItems(String sq) {
        Cursor dt;
        clsClasses.clsInventario_encabezado item;

        items.clear();

        dt=Con.OpenDT(sq);
        count =dt.getCount();
        if (dt.getCount()>0) dt.moveToFirst();

        while (!dt.isAfterLast()) {

            item = clsCls.new clsInventario_encabezado();

            item.id_inventario_enc=dt.getInt(0);
            item.id_estado=dt.getString(1);
            item.id_empresa=dt.getInt(2);
            item.fecha_inicio=dt.getInt(3);
            item.fecha_final=dt.getInt(4);
            item.nombre=dt.getString(5);
            item.id_usuario=dt.getInt(6);
            item.tipo_inventario=dt.getInt(7);

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

    public String addItemSql(clsClasses.clsInventario_encabezado item) {

        ins.init("Inventario_encabezado");

        ins.add("id_inventario_enc",item.id_inventario_enc);
        ins.add("id_estado",item.id_estado);
        ins.add("id_empresa",item.id_empresa);
        ins.add("fecha_inicio",item.fecha_inicio);
        ins.add("fecha_final",item.fecha_final);
        ins.add("nombre",item.nombre);
        ins.add("id_usuario",item.id_usuario);
        ins.add("tipo_inventario",item.tipo_inventario);

        return ins.sql();

    }

    public String updateItemSql(clsClasses.clsInventario_encabezado item) {

        upd.init("Inventario_encabezado");

        upd.add("id_estado",item.id_estado);
        upd.add("id_empresa",item.id_empresa);
        upd.add("fecha_inicio",item.fecha_inicio);
        upd.add("fecha_final",item.fecha_final);
        upd.add("nombre",item.nombre);
        upd.add("id_usuario",item.id_usuario);
        upd.add("tipo_inventario",item.tipo_inventario);

        upd.Where("(id_inventario_enc="+item.id_inventario_enc+")");

        return upd.sql();

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

}

