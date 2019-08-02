package com.dts.classes;

import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dts.base.BaseDatos;
import com.dts.base.clsClasses;


public class clsInventario_detalleObj {

    public int count;

    private Context cont;
    private BaseDatos Con;
    private SQLiteDatabase db;
    public BaseDatos.Insert ins;
    public BaseDatos.Update upd;
    private clsClasses clsCls = new clsClasses();

    private String sel="SELECT * FROM Inventario_detalle";
    private String sql;
    public ArrayList<clsClasses.clsInventario_detalle> items= new ArrayList<clsClasses.clsInventario_detalle>();

    public clsInventario_detalleObj(Context context, BaseDatos dbconnection, SQLiteDatabase dbase) {
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

    public void add(clsClasses.clsInventario_detalle item) {
        addItem(item);
    }

    public void update(clsClasses.clsInventario_detalle item) {
        updateItem(item);
    }

    public void delete(clsClasses.clsInventario_detalle item) {
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

    public clsClasses.clsInventario_detalle first() {
        return items.get(0);
    }


    // Private

    private void addItem(clsClasses.clsInventario_detalle item) {

        ins.init("Inventario_detalle");

        //ins.add("id_inventario_det",item.id_inventario_det);
        ins.add("id_inventario_enc",item.id_inventario_enc);
        ins.add("id_articulo",item.id_articulo);
        ins.add("ubicacion",item.ubicacion);
        ins.add("cantidad",item.cantidad);
        ins.add("codigo_barra",item.codigo_barra);
        ins.add("comunicado",item.comunicado);
        ins.add("id_operador",item.id_operador);
        ins.add("fecha",item.fecha);
        ins.add("Id_registro",item.id_registro);
        ins.add("eliminado",item.eliminado);

        db.execSQL(ins.sql());

    }

    private void updateItem(clsClasses.clsInventario_detalle item) {

        upd.init("Inventario_detalle");

        upd.add("id_inventario_enc",item.id_inventario_enc);
        upd.add("id_articulo",item.id_articulo);
        upd.add("ubicacion",item.ubicacion);
        upd.add("cantidad",item.cantidad);
        upd.add("codigo_barra",item.codigo_barra);
        upd.add("comunicado",item.comunicado);
        upd.add("id_operador",item.id_operador);
        upd.add("fecha",item.fecha);
        upd.add("Id_registro",item.id_registro);
        upd.add("eliminado",item.eliminado);

        upd.Where("(id_inventario_det="+item.id_inventario_det+")");

        db.execSQL(upd.sql());

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

    private void deleteItem(clsClasses.clsInventario_detalle item) {
        sql="DELETE FROM Inventario_detalle WHERE (id_inventario_det="+item.id_inventario_det+")";
        db.execSQL(sql);
    }

    private void deleteItem(int id) {
        sql="DELETE FROM Inventario_detalle WHERE id=" + id;
        db.execSQL(sql);
    }

    private void fillItems(String sq) {
        Cursor dt;
        clsClasses.clsInventario_detalle item;

        items.clear();

        dt=Con.OpenDT(sq);
        count =dt.getCount();
        if (dt.getCount()>0) dt.moveToFirst();

        while (!dt.isAfterLast()) {

            item = clsCls.new clsInventario_detalle();

            item.id_inventario_det=dt.getInt(0);
            item.id_inventario_enc=dt.getInt(1);
            item.id_articulo=dt.getString(2);
            item.ubicacion=dt.getString(3);
            item.cantidad=dt.getDouble(4);
            item.codigo_barra=dt.getString(5);
            item.comunicado=dt.getString(6);
            item.id_operador=dt.getInt(7);
            item.fecha=dt.getString(8);
            item.id_registro=dt.getInt(9);
            item.eliminado=dt.getInt(10);

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

    public String addItemSql(clsClasses.clsInventario_detalle item) {

        ins.init("Inventario_detalle");

        ins.add("id_inventario_det",item.id_inventario_det);
        ins.add("id_inventario_enc",item.id_inventario_enc);
        ins.add("id_articulo",item.id_articulo);
        ins.add("ubicacion",item.ubicacion);
        ins.add("cantidad",item.cantidad);
        ins.add("codigo_barra",item.codigo_barra);
        ins.add("comunicado",item.comunicado);
        ins.add("id_operador",item.id_operador);
        ins.add("fecha",item.fecha);
        ins.add("Id_registro",item.id_registro);
        ins.add("eliminado",item.eliminado);

        return ins.sql();

    }

    public String updateItemSql(clsClasses.clsInventario_detalle item) {

        upd.init("Inventario_detalle");

        upd.add("id_inventario_enc",item.id_inventario_enc);
        upd.add("id_articulo",item.id_articulo);
        upd.add("ubicacion",item.ubicacion);
        upd.add("cantidad",item.cantidad);
        upd.add("codigo_barra",item.codigo_barra);
        upd.add("comunicado",item.comunicado);
        upd.add("id_operador",item.id_operador);
        upd.add("fecha",item.fecha);
        upd.add("Id_registro",item.id_registro);
        upd.add("eliminado",item.eliminado);

        upd.Where("(id_inventario_det="+item.id_inventario_det+")");

        return upd.sql();

        //Toast toast= Toast.makeText(cont,upd.sql(), Toast.LENGTH_LONG);toast.show();

    }

}

