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

import com.dts.classes.clsUsuarioObj;

public class Ingreso extends PBase {

    private EditText txtUser,txtPass;
    private TextView lblTitle,lblVer;

    private clsUsuarioObj usr;

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

    private void processLogIn() {
        String user,pass,su,sp;
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

            /*
            usr.fill(" WHERE Activo=1");
            if (usr.count == 0) {
                msgbox("Catálogo de usuarios vacio.");return;
            }

            flag=false;
            for (int i = 0; i <usr.count; i++) {

                su=usr.items.get(i).login; sp=usr.items.get(i).clave;

                if (su.equalsIgnoreCase(user) && sp.equalsIgnoreCase(pass)) {
                    gl.userid=usr.items.get(i).id;
                    gl.nombreusuario=usr.items.get(i).nombre;
                    gl.rolid=usr.items.get(i).rol;

                    flag=true;break;
                }
            }

            if (!flag) {
                msgbox("¡El usuario no existe o contraseña incorrecta!");txtUser.requestFocus();return;
            }
            */

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
    }

}

