package com.dts.base;

public class clsClasses {

    public class clsArticulo {
        public String id_articulo;
        public int id_empresa;
        public String codigo_barra;
        public String descripcion;
        public double costo;
        public String tipo_conteo;
    }

    public class clsArticulo_codigo_barra {
        public int id_empresa;
        public String id_articulo;
        public String codigo_barra;
    }

    public class clsEstado_inventario {
        public int id_estado;
        public String nombre;
    }

    public class clsEstatus_handheld {
        public int id;
        public String nombre;
    }

    public class clsOperadores {
        public int id_operador;
        public int id_empresa;
        public String codigo;
        public String clave;
        public double nombre;
    }

    public class clsRegistro_handheld {
        public int id_registro;
        public int id_empresa;
        public int fecha_registro;
        public String serie_dispositivo;
        public String id_estatus;
        public String id_pais;
        public String descripcion;
    }






    //************************************

    public class clsUsuario {
        public int id;
        public String nombre;
        public int activo;
        public String login;
        public String clave;
        public int rol;
    }

    public class clsRol {
        public int  id;
        public String nombre;
    }

    public class clsMenu {
        public int  id;
        public String nombre;
    }

}
