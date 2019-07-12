package com.dts.tomweb;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
                            //Codigo.requestFocus();
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

                            Cantidad.requestFocus();
                            mostrarConteo();

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
                            mostrarConteo();
                            limpiaCampos2();
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

    public void doHelp(View view) {  }

    public void doDelete (View view){
        barra = Codigo.getText().toString();
        Ubic = Ubicacion.getText().toString();
        msgAskDelete("Seguro que desea eliminar el conteo de el producto: "+ barra);
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

            if(Codigo.getText().toString().isEmpty()){
                msgbox("Ingrese el codigo");
                return;
            }

            canti = Double.parseDouble(Cantidad.getText().toString());

            if(canti==0){
                msgbox("Ingrese una cantidad mayor a 0");
                return;
                            }

            if(gl.tipoInv==1) {
                Barra.setText(Codigo.getText().toString());
                barra = Barra.getText().toString();

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

                Toast.makeText(this, "Agregado Correctamente", Toast.LENGTH_LONG).show();

                Codigo.requestFocus();

            }else if(gl.tipoInv==2){

            }else if(gl.tipoInv==3){

            }

        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox("Error: "+e);
        }
    }

    public void Eliminar(){
        clsInventario_ciegoObj InvCiego = new clsInventario_ciegoObj(this, Con, db);

        try{

            if(barra.isEmpty()){
                msgbox("Ingrese el código del producto a eliminar");return;
            }

            if(Ubic.isEmpty()){
                msgbox("Ingrese la ubicación del producto a eliminar");return;
            }

            InvCiego.fill("WHERE CODIGO_BARRA = "+ barra + " AND UBICACION = "+ Ubic);

            if(InvCiego.count==0){
                msgbox("Este producto no exite en la ubicación descrita, o algunos de los dos no exite");
                return;
            }

            sql = "UPDATE INVENTARIO_CIEGO SET ELIMINADO = 1 WHERE CODIGO_BARRA = "+ barra + " AND UBICACION = "+ Ubic;
            db.execSQL(sql);

            gl.eliminar=true;

        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
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

    public void limpiaCampos2(){
        Ubicacion.setText(Ubic);
        Codigo.setText("");
        Barra.setText("");
        Cantidad.setText("");
    }

    public void mostrarConteo(){
        Cursor dt;
        Double cant2;

        try{

            Cod = Codigo.getText().toString();
            Ubic = Ubicacion.getText().toString();

            if(!Cod.isEmpty()){
                if(!Ubic.isEmpty()){
                    sql="SELECT SUM(CANTIDAD) FROM INVENTARIO_CIEGO WHERE UBICACION ="+ Ubic +" AND CODIGO_BARRA ='"+ Cod +"' AND ELIMINADO = 0";
                }else {
                    sql="SELECT SUM(CANTIDAD) FROM INVENTARIO_CIEGO WHERE CODIGO_BARRA ='"+ Cod +"' AND ELIMINADO = 0";
                }
            }else return;
            dt = Con.OpenDT(sql);
            dt.moveToFirst();

            cant2 = dt.getDouble(0);

            Codigo2.setText(Cod);
            Cant2.setText(Double.toString(cant2));
            if(gl.tipoInv==1){
                Desc.setText("NO ENCONTRADO");
            }

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
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
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

    private void msgAskDelete(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

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
