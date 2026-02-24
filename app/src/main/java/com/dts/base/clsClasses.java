package com.dts.base;

public class clsClasses {

    public static class clsArticulo {
        public String id_articulo;
        public int id_empresa;
        public String codigo_barra;
        public String descripcion;
        public double costo;
        public String tipo_conteo;
    }

    public static class clsArticulo_codigo_barra {
        public int id_empresa;
        public String id_articulo;
        public String codigo_barra;
    }

    public static class clsEstado_inventario {
        public int id_estado;
        public String nombre;
    }

    public static class clsEstatus_handheld {
        public int id;
        public String nombre;
    }

    public static class clsInventario_ciego {
        public int id_inventario_enc;
        public String codigo_barra;
        public double cantidad;
        public int id;
        public String comunicado;
        public String ubicacion;
        public int id_operador;
        public String fecha;
        public int id_registro;
        public int eliminado;
    }

    public static class clsInventario_ciego_rfid {
        public int id_inventario_enc;
        public String codigo_barra;
        public double cantidad;
        public int id;
        public String comunicado;
        public String ubicacion;
        public int id_operador;
        public String fecha;
        public String hora;
        public int id_registro;
        public int eliminado;
    }

    public static class clsInventario_detalle {
        public int id_inventario_det;
        public int id_inventario_enc;
        public String id_articulo;
        public String ubicacion;
        public double cantidad;
        public String codigo_barra;
        public String comunicado;
        public int id_operador;
        public String fecha;
        public int id_registro;
        public int eliminado;
    }

    public static class clsInventario_encabezado {
        public int id_inventario_enc;
        public String id_estado;
        public int id_empresa;
        public String fecha_inicio;
        public String fecha_final;
        public String nombre;
        public int id_usuario;
        public int tipo_inventario;
    }

    public static class clsInventario_operador {
        public int id_inventario_enc;
        public int id_operador;
    }

    public static class clsInventario_teorico {
        public int id_empresa;
        public String id_articulo;
        public String descripcion;
        public double cantidad;
        public String codigo_barra;
        public double costo;
        public String tipo_conteo;
        public int id_inventario_enc;
    }

    public static class clsOperadores {
        public int id_operador;
        public int id_empresa;
        public String codigo;
        public String clave;
        public String nombre;
    }

    public static class clsRegistro_handheld {
        public int id_registro;
        public int id_empresa;
        public String fecha_registro;
        public String serie_dispositivo;
        public String id_estatus;
        public String id_pais;
        public String descripcion;
    }

    public static class clsUsuario {
        public int id;
        public String nombre;
        public int activo;
        public String login;
        public String clave;
        public int rol;
    }

    public static class clsRol {
        public int id;
        public String nombre;
    }

    public static class clsMenu {
        public int id;
        public String nombre;
    }
}