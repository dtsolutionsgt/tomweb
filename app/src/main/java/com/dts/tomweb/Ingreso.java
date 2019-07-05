package com.dts.tomweb;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dts.classes.clsInventario_encabezadoObj;
import com.dts.classes.clsRegistro_handheldObj;
import com.dts.classes.clsOperadoresObj;

public class Ingreso extends PBase {

    private EditText txtUser,txtPass;
    private TextView lblTitle,lblVer;

    private String version="Ver: 1.0.0 - 12/06/19";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingreso);

        try {
            super.InitBase(savedInstanceState);

            txtUser = (EditText) findViewById(R.id.editText2);txtUser.requestFocus();
            txtPass = (EditText) findViewById(R.id.editText3);
            lblTitle = (TextView) findViewById(R.id.textView2);
            lblVer = (TextView) findViewById(R.id.textView3);lblVer.setText(version);

            txtUser.setText("1");txtPass.setText("1");txtPass.requestFocus();

            setHandlers();

            getDB();

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }

    }

    // Events

    public void doEnter(View view) {
        processLogIn();
    }

    public void doShowMenu(View view) {
        showMenu();
    }

    private void setHandlers() {

        txtUser.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (arg1) {
                        case KeyEvent.KEYCODE_ENTER:
                            txtPass.requestFocus();
                            return true;
                    }
                }
                return false;
            }
        });

        txtPass.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (arg1) {
                        case KeyEvent.KEYCODE_ENTER:
                            processLogIn();
                            return true;
                    }
                }
                return false;
            }
        });

    }


    // Main

    public void getDB(){
        clsInventario_encabezadoObj invEnc = new clsInventario_encabezadoObj(this, Con, db);
        try{
            invEnc.fill();

            if(invEnc.count == 0) {
                gl.validaLicDB=0;
                startActivity(new Intent(this, ComWS.class));
            }else{
                getLic();
            }


        }catch (Exception e){

        }
    }

    private void getLic(){
        clsRegistro_handheldObj LicHH = new clsRegistro_handheldObj(this, Con, db);

        try {
            LicHH.fill();

            if(LicHH.count==0){
                gl.validaLicDB=1;
                startActivity(new Intent(this, Licencia.class));
            }else{
                LicHH.fill("WHERE id_estatus = 1");

                if(LicHH.count==0) {
                    startActivity(new Intent(this, Licencia.class));
                }
            }
        }catch (Exception e){

        }
    }

    private void processLogIn() {
        String user,pass,su,sp;
        clsOperadoresObj opr =new clsOperadoresObj(this,Con,db);
        boolean flag;

        try {
            user=txtUser.getText().toString();
            if (emptystr(user)) {
                toast("Falta usuario.");txtUser.requestFocus();return;
            }

            pass=txtPass.getText().toString();
            if (emptystr(pass)) {
                toast("Falta clave.");txtUser.requestFocus();return;
            }
            user = user;pass = pass;


            opr.fill();
            if (opr.count == 0) {
                msgbox("Catálogo de usuarios vacio.");return;
            }

            flag=false;
            for (int i = 0; i <opr.count; i++) {

                su=opr.items.get(i).codigo; sp=opr.items.get(i).clave;

                if (su.equalsIgnoreCase(user) && sp.equalsIgnoreCase(pass)) {
                    gl.userid=opr.items.get(i).id_operador;
                    gl.nombreusuario=opr.items.get(i).nombre;

                    flag=true;break;
                }
            }

            if (!flag) {
                msgbox("¡El usuario no existe o contraseña incorrecta!");txtUser.requestFocus();return;
            }


            txtUser.setText("");txtPass.setText("");txtUser.requestFocus();

            callback =1;gl.exitapp=false;
            startActivity(new Intent(this,MenuPrincipal.class));

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }

    }


    // Menu

    private void showMenu() {

    }

    // Aux


    // Activity Events

    @Override
    protected void onResume() {
        super.onResume();
        if (callback ==1) {
            callback =0;
            if (gl.exitapp) finish();
        }

        if(gl.validaLicDB==2){
            super.finish();
        }

        if(gl.validaLicDB==4){
            getDB();
        }
    }

}

