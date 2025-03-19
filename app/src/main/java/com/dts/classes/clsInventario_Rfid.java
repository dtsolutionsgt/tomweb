package com.dts.classes;

public class clsInventario_Rfid {

    public String tag;
    public String descripcion;
    public int cantidad;
    public String ubicacion;
    public String producto;

    public clsInventario_Rfid() {
        this.tag = tag;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.ubicacion = ubicacion;
        this.producto= producto;

    }

    public String getTag() {
        return tag;
    }

    public String getDescripcion() {
        return tag;
    }

    public int getCantidad() {
        return cantidad;
    }

    public String getUbicacion() {
        return ubicacion;
    }
}
