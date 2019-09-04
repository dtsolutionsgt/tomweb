package com.dts.tomweb;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.RestrictionEntry;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dts.base.clsClasses;
import com.dts.classes.clsArticuloObj;
import com.dts.classes.clsInventario_ciegoObj;
import com.dts.classes.clsInventario_detalleObj;
import com.dts.classes.clsInventario_teoricoObj;
import com.dts.classes.clsRegistro_handheldObj;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Conteo extends PBase {

    private EditText Barra, Cantidad, Ubicacion, Codigo;
    private TextView Codigo2, Cant2, Desc;

    private String Ubic, Cod, tipoArt, barra, desc,ccod;
    private Double canti;
    private Integer result=0, resta;

    private ImageView eliminar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conteo);

        super.InitBase(savedInstanceState);

        addlog("Conteo",""+du.getActDateTime(),gl.nombreusuario);

        Codigo = (EditText) findViewById(R.id.txtCodigo);
        Barra = (EditText) findViewById(R.id.txtBarra);
        Cantidad = (EditText) findViewById(R.id.txtCantidad);
        Ubicacion = (EditText) findViewById(R.id.txtUbicacion);

        Codigo2 = (TextView) findViewById(R.id.txtCodigo2);
        Cant2 = (TextView) findViewById(R.id.txtCantidad2);
        Desc = (TextView) findViewById(R.id.txtDesc);
        eliminar = (ImageView) findViewById(R.id.imageView17);

        //eliminar.setVisibility(View.INVISIBLE);

        Ubicacion.requestFocus();
        Barra.setFocusable(false);

        limpiaCampos();
        setHandlers();
    }


    //region Events

    private void setHandlers() {

        Ubicacion.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (arg1) {
                        case KeyEvent.KEYCODE_ENTER:
                            gl.ubicacion = Ubicacion.getText().toString().trim();
                            return true;
                    }
                }
                return false;
            }
        });

        Codigo.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (arg1) {
                        case KeyEvent.KEYCODE_ENTER:
                            gl.ubicacion = Ubicacion.getText().toString().trim();
                            Barra.setText("");
                            Cod = Codigo.getText().toString().trim();

                            if(Cod.isEmpty()) return true;

                            if (gl.tipoInv==1){
                                barra = Codigo.getText().toString().trim();
                                Barra.setText(barra);

                            }else {
                                if(!existencia()) return false;
                            }


                            mostrarConteo();

                            if(gl.tipoInv==2 || gl.tipoInv==3){

                                if(tipoArt.equals("S")) {
                                    Cantidad.setFocusable(false);
                                    insertaConteo();
                                    mostrarConteo();
                                    Codigo.clearFocus();
                                    Codigo.requestFocus();
                                    return true;
                                } else {
                                    Cantidad.setFocusableInTouchMode(true);
                                    Cantidad.setFocusable(true);
                                }


                            }

                            mostrarConteo();
                            //Cantidad.requestFocus();

                            return true;
                    }
                }
                return false;
            }
        });


        Cantidad.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (arg1) {
                        case KeyEvent.KEYCODE_ENTER:
                            gl.ubicacion = Ubicacion.getText().toString().trim();
                            if (gl.tipoInv==1){
                                barra = Codigo.getText().toString().trim();
                                Barra.setText(barra);
                            }else {
                                barra = Barra.getText().toString().trim();
                            }

                            if(barra.isEmpty()) {
                                if (!existencia()) return false; else Barra.setText(barra);
                            }

                            mostrarConteo();

                            if(gl.tipoInv!=1){
                                if(tipoArt.equals("S")) {
                                    Cantidad.setFocusable(false);
                                    insertaConteo();
                                    mostrarConteo();
                                    Ubicacion.requestFocus();
                                    return true;
                                } else {
                                    Cantidad.setFocusableInTouchMode(true);
                                    Cantidad.setFocusable(true);
                                }
                            }


                            insertaConteo();
                            mostrarConteo();

                            return true;
                    }
                }
                return false;
            }
        });

    }

    public boolean getBarra(){
        clsInventario_ciegoObj InvCiego = new clsInventario_ciegoObj(this, Con, db);

        try {

            InvCiego.fill(" WHERE ID = '"+ Cod +"'");

            if(InvCiego.count==0) return false;
            barra = InvCiego.first().codigo_barra.trim();

        }catch (Exception e){  }
        return true;
    }

    public void doExit(View view) {
        finish();
    }

    public void doListaConteos(View view){
        startActivity(new Intent(this, ListaConteos.class));
    }

    public void doListaProductos(View view){
        startActivity(new Intent(this, Productos.class));
    }

    public void doHelp(View view) {
        String tx;

        try{

            tx="-Ubicación: Si se deja el campo vacío tomará por defecto valor 1.\n\n" +
                    "-Barra: Este campo se llenará automáticamente al dar enter en 'Codigo'.\n\n" +
                    "-Cantidad: Si el producto es serializado se inhabilita el campo y se agregará de  uno en uno al dar enter en la casilla 'Código'.\n\n" +
                    "En el cuadro, 'Cantidad' mostrará el total de conteo según código y ubicación.";

            PopUp(tx);

        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }


    }

    public void doDelete (View view){
        Cod = Codigo.getText().toString().trim();
        Ubic = Ubicacion.getText().toString().trim();

        if(Ubic.isEmpty()){
            msgbox("Ingrese la ubicación del producto a eliminar");return;
        }else if(Cod.isEmpty()){
            msgbox("Ingrese el código del producto a eliminar, y presione enter para actualizar la barra");return;
        }

        msgAskDelete("Seguro que desea eliminar el conteo de el producto: '"+ desc +" - "+ Cod +"' En la ubicación: '"+Ubic+"' y código de barra: '"+ barra +"'");
    }

    public void doNext(View view) {
        gl.validaLicDB=10;

        getCampos();

        /*if(result==0){
            ComWS();
        }else if(result==1){
            insertaConteo();
            mostrarConteo();
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(gl.cbck==1){
            gl.cbck=0;

            Cantidad.setFocusableInTouchMode(true);
            Cantidad.setFocusable(true);
            Cantidad.requestFocus();

            Codigo.setText(gl.codBarra);
            Ubicacion.setText(gl.ubicacion);
            Barra.setText(gl.codBarra);

        }else if(gl.cbck==2){

            Codigo.requestFocus();
            Codigo.setText("");
            Ubicacion.setText(gl.ubicacion);
            Barra.setText("");

        }else if(gl.cbck==3){
            gl.cbck=0;

            Cantidad.setFocusable(false);
            Codigo.requestFocus();

            Codigo.setText(gl.codBarra);
            Ubicacion.setText(gl.ubicacion);
            Barra.setText(gl.codBarra);

        }
    }

    //endregion

    //region Main

    public void insertaConteo(){
        clsInventario_ciegoObj InvCiego = new clsInventario_ciegoObj(this, Con, db);
        clsClasses.clsInventario_ciego item=clsCls.new clsInventario_ciego();
        clsInventario_detalleObj InvDet = new clsInventario_detalleObj(this, Con, db);
        clsClasses.clsInventario_detalle itemDeta=clsCls.new clsInventario_detalle();

        clsRegistro_handheldObj regHH = new clsRegistro_handheldObj(this, Con, db);

        Long sfecha;
        String  ff,ffe;
        Integer fecha;

        try{

            regHH.fill();
            gl.IDregistro = regHH.first().id_registro;
            Cod = Codigo.getText().toString().trim();


            if(Ubicacion.getText().toString().isEmpty()){
                Ubic = "1";
            }else {
                Ubic = Ubicacion.getText().toString().trim();
            }
            sfecha=du.getActDate();
            ff = "20"+sfecha;
            ffe= ff.substring(0,8);

            if(gl.tipoInv!=1){
                if(tipoArt.equals("S")){
                    canti = 1.0;
                }else {
                    if(Cantidad.getText().toString().isEmpty()){
                        msgbox("Ingrese la cantidad");
                        return;
                    }else {
                        canti = Double.parseDouble(Cantidad.getText().toString());
                    }
                }
            }else {
                if(Cantidad.getText().toString().isEmpty()){
                    msgbox("Ingrese la cantidad");
                    return;
                }else {
                    canti = Double.parseDouble(Cantidad.getText().toString());
                }
            }

            if(Codigo.getText().toString().isEmpty()){
                msgbox("Ingrese el codigo");
                return;
            }

            if(canti==0){
                msgbox("Ingrese una cantidad mayor a 0");
                return;
            }


            if(gl.tipoInv==1) {

                Barra.setText(Codigo.getText().toString());
                barra = Barra.getText().toString().trim();

                item.id_inventario_enc =  gl.idInvEnc;
                item.codigo_barra = barra;
                item.cantidad = canti;
                item.comunicado = "N";
                item.ubicacion = Ubic;
                item.id_operador = gl.userid;
                item.fecha = ffe;
                item.id_registro = gl.IDregistro;
                item.eliminado = 0;

                InvCiego.add(item);

            }else if(gl.tipoInv==2 || gl.tipoInv==3){

                itemDeta.id_inventario_enc= gl.idInvEnc;
                itemDeta.id_articulo = Cod;
                itemDeta.ubicacion = Ubic;
                itemDeta.cantidad = canti;
                itemDeta.codigo_barra = barra;
                itemDeta.comunicado = "N";
                itemDeta.id_operador = gl.userid;
                itemDeta.fecha = ffe;
                itemDeta.id_registro = gl.IDregistro;
                item.eliminado = 0;

                InvDet.add(itemDeta);

            }

            Toast.makeText(this, "Agregado Correctamente", Toast.LENGTH_LONG).show();

            if(gl.tipoInv==1) limpiaCampos2(); else if (tipoArt.equals("F")) limpiaCampos2();

        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox("Error: "+e);
        }
    }

    public void Eliminar(){
        clsInventario_ciegoObj InvCiego = new clsInventario_ciegoObj(this, Con, db);
        clsInventario_detalleObj InvDetalle = new clsInventario_detalleObj(this, Con, db);
        String tabla="";
        Integer cc=0;

        try{
            if(barra == null){
                if(!getBarra()) {
                    msgbox("Verifique que el código corresponda a la barra, si no presione enter en el código ya escrito para actualizar");
                    return;
                }
            }

            if(gl.tipoInv==1){
                tabla = "INVENTARIO_CIEGO";

                InvCiego.fill(" WHERE CODIGO_BARRA = '"+ barra + "' AND UBICACION = '"+ Ubic +"' AND ID_INVENTARIO_ENC = '"+ gl.idInvEnc +"'");
                cc = InvCiego.count;
            } else if(gl.tipoInv==2 || gl.tipoInv==3) {
                tabla = "INVENTARIO_DETALLE";

                InvDetalle.fill(" WHERE CODIGO_BARRA = '"+ barra + "' AND UBICACION = '"+ Ubic +"' AND ID_INVENTARIO_ENC = '"+ gl.idInvEnc +"'");
                cc = InvDetalle.count;
            }

            if(cc==0){
                msgbox("Este producto no existe en la ubicación descrita, o no existe el producto.\nVerifique que el código corresponda a la barra, si no presione enter en el código ya escrito para actualizar.");
                return;
            }

            sql = "UPDATE "+ tabla +" SET ELIMINADO = 1, COMUNICADO = 'N' WHERE CODIGO_BARRA = '"+ barra + "' AND UBICACION = '"+ Ubic +"' AND ID_INVENTARIO_ENC = '"+ gl.idInvEnc +"'";
            db.execSQL(sql);
            limpiaCampos3();

            Toast.makeText(this, "Producto Eliminado Correctamente", Toast.LENGTH_LONG).show();
        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
            msgbox("Error Eliminar" + e);
        }
    }

    public boolean existencia() {
        clsInventario_teoricoObj teorico = new clsInventario_teoricoObj(this, Con, db);
        clsArticuloObj articulo = new clsArticuloObj(this, Con, db);

        try {
            if(gl.tipoInv==1){
                return true;
            }else if(gl.tipoInv==2){
                articulo.fill( " WHERE ID_ARTICULO = '"+ Cod +"'");

                if(articulo.count==0){
                    gl.codBarra = Cod;
                    msgAskArt("Agregar como no encontrado");
                    return false;
                }

            }else if(gl.tipoInv==3){
                teorico.fill( " WHERE ID_ARTICULO = '"+ Cod +"'");

                if(teorico.count==0){
                    gl.codBarra = Cod;
                    msgAskArt("Agregar como no encontrado");
                    return false;
                }
            }


        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox("Error: "+e);
        }

        return true;
    }

    //endregion

    //region Aux

    public void showToast(View view){
        try{
            if(!Barra.getText().toString().isEmpty()){
                Toast.makeText(this, Cod, Toast.LENGTH_LONG).show();
            }else {
                return;
            }

        }catch (Exception e){
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
        }
    }

    public void limpiaCampos(){
        Ubicacion.setText("");
        Codigo.setText("");
        Barra.setText("");
        Cantidad.setText("");
    }

    public void limpiaCampos2(){
        //Ubicacion.setText(Ubic);
        Codigo.setText("");
        Barra.setText("");
        Cantidad.setText("");
    }

    public void limpiaCampos3(){
        Codigo2.setText("");
        Cant2.setText("");
        Desc.setText("");
    }

    public void mostrarConteo(){
        clsInventario_teoricoObj teorico = new clsInventario_teoricoObj(this, Con, db);
        clsArticuloObj articulo = new clsArticuloObj(this, Con, db);
        Cursor dt;
        Double cant2;
        String Tabla="", codBck;

        try{
            codBck = Cod;

            Ubic = Ubicacion.getText().toString().trim();
            Cod = Codigo.getText().toString().trim();
            if(Cod.isEmpty()) Cod=codBck;
            if(gl.tipoInv==1){
                Tabla = "INVENTARIO_CIEGO";
                desc = "NO ENCONTRADO";
                Desc.setText(desc);
            }else if(gl.tipoInv==2){
                Tabla = "INVENTARIO_DETALLE";

                articulo.fill(" WHERE ID_ARTICULO ='"+ Cod +"'");
                desc = articulo.first().descripcion;
                tipoArt =  articulo.first().tipo_conteo;
                barra = articulo.first().codigo_barra;

                if(desc.isEmpty()){
                    msgAskArt("Agregar como no encontrado");
                    return;
                }else{
                    Desc.setText(desc);
                    Barra.setText(barra);
                }

            }else if(gl.tipoInv==3){
                Tabla = "INVENTARIO_DETALLE";

                teorico.fill(" WHERE ID_ARTICULO ='"+ Cod +"'");
                desc = teorico.first().descripcion;
                tipoArt =  teorico.first().tipo_conteo;
                barra = teorico.first().codigo_barra;

                if(desc.isEmpty()){
                    msgAskArt("Agregar como no encontrado");
                    return;
                }else{
                    Desc.setText(desc);
                    Barra.setText(barra);
                }

            }

            if(!Cod.isEmpty()){
                if(!Ubic.isEmpty()){
                    sql="SELECT SUM(CANTIDAD) FROM "+ Tabla +" WHERE UBICACION ="+ Ubic +" AND CODIGO_BARRA ='"+ barra +"' AND ELIMINADO = 0";
                }else {
                    sql="SELECT SUM(CANTIDAD) FROM "+ Tabla +" WHERE CODIGO_BARRA ='"+ barra +"' AND ELIMINADO = 0";
                }
            }else return;
            dt = Con.OpenDT(sql);
            dt.moveToFirst();

            cant2 = dt.getDouble(0);

            if (dt!=null) dt.close();

            if(Cod.length()>8){

                ccod = Cod.substring(0,7)+"...";
                Codigo2.setText(ccod);

            }else {
                Codigo2.setText(Cod);
            }

            Cant2.setText(Double.toString(cant2));

            if(result==1){
                msgAskContinue("Comunicar los datos?");
            }

        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);

        }
    }

    public void getCampos(){
        String c1, c2, c3;

        try{
            c1 = Codigo.getText().toString().trim();
            c2 = Ubicacion.getText().toString().trim();
            c3 = Cantidad.getText().toString().trim();

            if(!c1.isEmpty() || !c2.isEmpty() || !c3.isEmpty()){
                msgAskContinue("A dejado algunos campos con valores, ¿Seguro que desea continuar?");
                result = 1; return;
            }else{
                ComWS();
            }

            if(c1.isEmpty()){
                if(c2.isEmpty() && c3.isEmpty()){
                    result = 0;return;
                }else {
                    result = 2;return;
                }
            }else{
                result = 1;return;
            }

        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox("Error getCampos: "+e);
        }
    }

    //endregion

    //region Dialogs

    private void msgAskExit(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setCancelable(false);
        dialog.setTitle("Tom");
        dialog.setMessage("¿" + msg + "?");

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                TipoConteo();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialog.show();

    }

    private void msgAskArt(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setCancelable(false);
        dialog.setTitle("Tom");
        dialog.setMessage("¿" + msg + "?");

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                TipoConteo();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                limpiaCampos2();
                Codigo.requestFocus();
            }
        });

        dialog.show();

    }

    private void msgAskContinue(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setCancelable(false);
        dialog.setTitle("Tom");
        dialog.setMessage(msg);

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ComWS();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialog.show();

    }

    private void msgAskDelete(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setCancelable(false);
        dialog.setTitle("Tom");
        dialog.setMessage("¿"+msg+"?");

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Eliminar();
                limpiaCampos();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialog.show();

    }

    //endregion

    //region Activity Events

    public void TipoConteo(){
        startActivity(new Intent(this, TipoConteo.class));
    }

    public void ComWS(){
        startActivity(new Intent(this, ComWS.class));
    }

    //endregion

}
