package com.dts.tomweb;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dts.base.clsClasses;
import com.dts.classes.clsArticuloObj;
import com.dts.classes.clsInventario_ciegoObj;
import com.dts.classes.clsRegistro_handheldObj;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Conteo extends PBase {

    private EditText Barra, Cantidad, Ubicacion, Codigo;
    private TextView Codigo2, Cant2, Desc;
    private String Ubic, Cod, tipoArt, barra, desc;
    private Double canti;
    private Integer result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conteo);

        super.InitBase(savedInstanceState);

        Codigo = (EditText) findViewById(R.id.txtCodigo);
        Barra = (EditText) findViewById(R.id.txtBarra);
        Cantidad = (EditText) findViewById(R.id.txtCantidad);
        Ubicacion = (EditText) findViewById(R.id.txtUbicacion);

        Codigo2 = (TextView) findViewById(R.id.txtCodigo2);
        Cant2 = (TextView) findViewById(R.id.txtCantidad2);
        Desc = (TextView) findViewById(R.id.txtDesc);

        Barra.setFocusable(false);

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
                            Codigo.requestFocus();
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

                            insertaConteo();
                            limpiaCampos();
                            mostrarConteo();
                            //inventario();

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

                            insertaConteo();
                            limpiaCampos();
                            mostrarConteo();
                            //inventario();

                            return true;
                    }
                }
                return false;
            }
        });

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

    }

    public void doNext(View view) {
        gl.validaLicDB=10;

        getCampos();

        if(result==0){
            ComWS();
        }else if(result==1){
            insertaConteo();
            mostrarConteo();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(callback==1){
            callback=0;

            Codigo.setText(gl.codBarra);
            Ubicacion.setText(gl.ubicacion);
            Barra.setText(gl.codBarra);

            //inventario();
        }
    }

    //endregion

    //region Main

    private boolean guardaTrans() {
        clsInventario_ciegoObj InvCiego = new clsInventario_ciegoObj(this, Con, db);
        clsRegistro_handheldObj regHH = new clsRegistro_handheldObj(this, Con, db);
        clsClasses.clsInventario_ciego item=clsCls.new clsInventario_ciego();
        Long sfecha;
        String codBarra, ubicacion;
        Integer cantidad,fecha;

        try {
            regHH.fill();
            gl.IDregistro = regHH.first().id_empresa;

            codBarra = Barra.getText().toString();
            sfecha=du.getActDate();
            fecha = sfecha.intValue();

            item.id_inventario_enc = gl.idInvEnc;
            item.codigo_barra = codBarra;
            ubicacion = Ubicacion.getText().toString();

            if(tipoArt.equals("F")){
                cantidad = Integer.parseInt(Cantidad.getText().toString());
                item.cantidad = cantidad;
            }else if(tipoArt.equals("S")){
                item.cantidad = 1;
            }

            item.comunicado = "";
            item.ubicacion = ubicacion;
            item.id_operador = gl.userid;
            item.id_registro = gl.IDregistro;

            InvCiego.add(item);

            db.execSQL(sql);

            return true;

        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
            return false;
        }
    }

    public boolean inventario(){
        clsInventario_ciegoObj invCiego = new clsInventario_ciegoObj(this, Con, db);
        clsClasses.clsInventario_ciego item=clsCls.new clsInventario_ciego();
        clsArticuloObj art = new clsArticuloObj(this, Con, db);

        try{
            Ubic = Ubicacion.getText().toString();
            Cod = Codigo.getText().toString();

            if(!Cod.isEmpty()) {

                art.fill("WHERE CODIGO_BARRA ='" + Cod + "'");

                if (art.count == 0) {
                    gl.codBarra = Cod;
                    msgAskExit("Agregar como no encontrado");
                    return false;
                } else {

                    if (Ubic.isEmpty() && !Cod.isEmpty()) {
                        invCiego.fill("WHERE CODIGO_BARRA ='" + Cod + "'");
                        Ubicacion.setText("1");
                    } else if (!Cod.isEmpty() && !Ubic.isEmpty()) {
                        invCiego.fill("WHERE CODIGO_BARRA ='" + Cod + "' AND UBICACION ='" + Ubic + "'");
                    }

                    barra = art.first().codigo_barra;
                    desc = art.first().descripcion;

                    Barra.setText(barra);
                    Codigo2.setText(Cod);
                    Desc.setText(desc);

                    if (invCiego.count == 0) {

                        tipoArt = art.first().tipo_conteo;
                        Toast.makeText(this, "articulo sin incluir en el inventario", Toast.LENGTH_LONG).show();

                        //guardaTrans();

                    } else {
                        canti = invCiego.first().cantidad;
                        Cant2.setText(Double.toString(canti));
                        Toast.makeText(this, "articulo ya incluido en el inventario", Toast.LENGTH_LONG).show();
                        //guardaTrans();
                    }

                }

            }


        }catch (Exception e){
            msgbox(""+e);
        }

        return true;
    }

    public void insertaConteo(){
        clsInventario_ciegoObj InvCiego = new clsInventario_ciegoObj(this, Con, db);
        clsClasses.clsInventario_ciego item=clsCls.new clsInventario_ciego();
        clsRegistro_handheldObj regHH = new clsRegistro_handheldObj(this, Con, db);

        Long sfecha;
        String  ff,ffe;
        Integer fecha;

        try{

            regHH.fill();
            gl.IDregistro = regHH.first().id_registro;

            if(Ubicacion.getText().toString().isEmpty()){
                Ubic = "1";
            }else {
                Ubic = Ubicacion.getText().toString();
            }


            sfecha=du.getActDate();
            ff = "20"+sfecha;
            ffe= ff.substring(0,8);

            if(Cantidad.getText().toString().isEmpty()){
                msgbox("Ingrese la cantidad");
                return;
            }

            canti = Double.parseDouble(Cantidad.getText().toString());

            if(gl.tipoInv==1) {
                Barra.setText(Codigo.getText().toString());
                barra = Barra.getText().toString();

                item.id_inventario_enc =  gl.idInvEnc;
                item.codigo_barra = barra;
                item.cantidad = canti;
                item.comunicado = "";
                item.ubicacion = Ubic;
                item.id_operador = gl.userid;
                item.fecha = ffe;
                item.id_registro = gl.IDregistro;
                item.eliminado = 0;

                InvCiego.add(item);

                Toast.makeText(this, "Agregado Correctamente", Toast.LENGTH_LONG).show();

            }else if(gl.tipoInv==2){

            }else if(gl.tipoInv==3){

            }

        }catch (Exception e){
            msgbox("Error: "+e);
        }
    }

    public void Eliminar(){
        try{

            if(!Codigo.getText().toString().isEmpty()){
                msgbox("");
            }else{
                msgbox("Ingrese el codigo del producto a eliminar");
            }

        }catch (Exception e){
            msgbox("Error Eliminar" + e);
        }
    }

    //endregion

    //region Aux

    public void limpiaCampos(){
        Ubicacion.setText("");
        Codigo.setText("");
        Barra.setText("");
        Cantidad.setText("");
    }

    public void mostrarConteo(){
        Cursor dt;
        Double cant2;

        try{

            sql="SELECT SUM(CANTIDAD) FROM INVENTARIO_CIEGO WHERE ID_INVENTARIO_ENC ="+ gl.idInvEnc +" AND CODIGO_BARRA ='"+ barra +"'";
            dt = Con.OpenDT(sql);
            dt.moveToFirst();

            cant2 = dt.getDouble(0);

            Codigo2.setText(barra);
            Cant2.setText(Double.toString(cant2));
            Desc.setText("NO ENCONTRADO");

            if(result==1){
                msgAskContinue("Comunicar los datos?");
            }

        }catch (Exception e){

        }
    }

    public void getCampos(){
        String c1, c2, c3;

        try{
            c1 = Codigo.getText().toString();
            c2 = Ubicacion.getText().toString();
            c3 = Cantidad.getText().toString();

            if(!c1.isEmpty() && !c2.isEmpty() && !c3.isEmpty()){
                result = 1; return;
            }

            if(c1.isEmpty()){
                if(c2.isEmpty() && c3.isEmpty()){
                    result = 0;return;
                }else {
                    msgAskContinue("A dejado algunos campos con valores, ¿Seguro que desea continuar?");
                    result = 2;return;
                }
            }else{
                result = 1;return;
            }

        }catch (Exception e){
            msgbox("Error getCampos: "+e);
        }
    }

    //endregion

    //region Dialogs

    private void msgAskExit(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

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

    private void msgAskContinue(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

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
