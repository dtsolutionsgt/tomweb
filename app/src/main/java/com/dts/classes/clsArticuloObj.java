package com.dts.classes;
import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dts.base.BaseDatos;
import com.dts.base.clsClasses;


public class clsArticuloObj {

    public int count;

    private Context cont;
    private BaseDatos Con;
    private SQLiteDatabase db;
    public BaseDatos.Insert ins;
    public BaseDatos.Update upd;
    private clsClasses clsCls = new clsClasses();

    private String sel="SELECT * FROM Articulo";
    private String sql;
    public ArrayList<clsClasses.clsArticulo> items= new ArrayList<clsClasses.clsArticulo>();

    public clsArticuloObj(Context context, BaseDatos dbconnection, SQLiteDatabase dbase) {
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

    public void add(clsClasses.clsArticulo item) {
        addItem(item);
    }

    public void update(clsClasses.clsArticulo item) {
        updateItem(item);
    }

    public void delete(clsClasses.clsArticulo item) {
        deleteItem(item);
    }

    public void delete(String id) {
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

    public clsClasses.clsArticulo first() {
        return items.get(0);
    }


    // Private

    private void addItem(clsClasses.clsArticulo item) {

        ins.init("Articulo");

        ins.add("id_articulo",item.id_articulo);
        ins.add("id_empresa",item.id_empresa);
        ins.add("codigo_barra",item.codigo_barra);
        ins.add("descripcion",item.descripcion);
        ins.add("costo",item.costo);
        ins.add("tipo_conteo",item.tipo_conteo);

        db.execSQL(ins.sql());

    }

    private void updateItem(clsClasses.clsArticulo item) {

        upd.init("Articulo");

        upd.add("id_empresa",item.id_empresa);
        upd.add("codigo_barra",item.codigo_barra);
        upd.add("descripcion",item.descripcion);
        upd.add("costo",item.costo);
        upd.add("tipo_conteo",item.tipo_conteo);

        upd.Where("(id_articulo='"+item.id_articulo+"')");

        db.execSQL(upd.sql());

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

    private void deleteItem(clsClasses.clsArticulo item) {
        sql="DELETE FROM Articulo WHERE (id_articulo='"+item.id_articulo+"')";
        db.execSQL(sql);
    }

    private void deleteItem(String id) {
        sql="DELETE FROM Articulo WHERE id='" + id+"'";
        db.execSQL(sql);
    }

    private void fillItems(String sq) {
        Cursor dt;
        clsClasses.clsArticulo item;

        items.clear();

        dt=Con.OpenDT(sq);
        count =dt.getCount();
        if (dt.getCount()>0) dt.moveToFirst();

        while (!dt.isAfterLast()) {

            item = clsCls.new clsArticulo();

            item.id_articulo=dt.getString(0);
            item.id_empresa=dt.getInt(1);
            item.codigo_barra=dt.getString(2);
            item.descripcion=dt.getString(3);
            item.costo=dt.getDouble(4);
            item.tipo_conteo=dt.getString(5);

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

    public String addItemSql(clsClasses.clsArticulo item) {

        ins.init("Articulo");

        ins.add("id_articulo",item.id_articulo);
        ins.add("id_empresa",item.id_empresa);
        ins.add("codigo_barra",item.codigo_barra);
        ins.add("descripcion",item.descripcion);
        ins.add("costo",item.costo);
        ins.add("tipo_conteo",item.tipo_conteo);

        return ins.sql();

    }

    public String updateItemSql(clsClasses.clsArticulo item) {

        upd.init("Articulo");

        upd.add("id_empresa",item.id_empresa);
        upd.add("codigo_barra",item.codigo_barra);
        upd.add("descripcion",item.descripcion);
        upd.add("costo",item.costo);
        upd.add("tipo_conteo",item.tipo_conteo);

        upd.Where("(id_articulo='"+item.id_articulo+"')");

        return upd.sql();

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

}

