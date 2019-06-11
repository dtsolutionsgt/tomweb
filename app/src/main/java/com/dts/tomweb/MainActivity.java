package com.dts.tomweb;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dts.classes.clsUsuarioObj;

public class MainActivity extends PBase {

    private EditText txtUser,txtPass;
    private TextView lblTitle;

    private clsUsuarioObj usr;

    private int rolid;

    private Bundle instanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instanceState=savedInstanceState;

        startApplication();
        //grantPermissions();
    }

    // Manejo de permisos de la aplicacion - solo en MainActivity

    private void startApplication() {

        try {
            super.InitBase(instanceState);

            txtUser = (EditText) findViewById(R.id.editText2);txtUser.requestFocus();
            txtPass = (EditText) findViewById(R.id.editText3);
            lblTitle = (TextView) findViewById(R.id.textView2);

            lblTitle.setText("Nombre de aplicación");
            txtUser.setText("1");txtPass.setText("1");

            setHandlers();

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }

    }

    private void grantPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= 23) {

                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                        && checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    startApplication();
                } else {
                    /*ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.CALL_PHONE,
                                    Manifest.permission.CAMERA}, 1);*/
                }
            }

        } catch (Exception e) {
            msgbox(new Object() {
            }.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permisos aplicados.", Toast.LENGTH_SHORT).show();
            startApplication();
        } else {
            Toast.makeText(this, "Permisos incompletos.", Toast.LENGTH_LONG).show();
            super.finish();
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
        clsUsuarioObj usr=new clsUsuarioObj(this,Con,db);
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

            txtUser.setText("");txtPass.setText("");txtUser.requestFocus();

            callback =1;gl.exitapp=false;
            startActivity(new Intent(this,MenuPrincipal.class));

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }

    }


    // Menu

    private void showMenu() {

        final AlertDialog Dialog;
        final String[] selitems = {"Toast centralizado","Ingreso de valor","Lista en dialogo"};

        AlertDialog.Builder menudlg = new AlertDialog.Builder(this);
        menudlg.setTitle("Menu pagina principal");

        menudlg.setItems(selitems, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        doMenuItem1();break;
                    case 1:
                        doMenuItem2();break;
                    case 2:
                        doMenuItem3();break;
                }

                dialog.cancel();
            }
        });

        menudlg.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        Dialog = menudlg.create();
        Dialog.show();

    }

    private void doMenuItem1() {
        toast("Texto en centro de pagina");
    }

    private void doMenuItem2() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Valor numerico decimal");

        final EditText input = new EditText(this);
        alert.setView(input);

        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setText("");
        input.setHint("valor decimal");
        input.requestFocus();

        alert.setPositiveButton("Aplicar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                    Double val=mu.CDbl(input.getText().toString());
                    toast("Valor ingresago : "+input.getText().toString());
                } catch (Exception e) {
                    toast("¡Valor incorrecto!");return;
                }
            }
        });

        alert.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });

        alert.show();
    }

    private void doMenuItem3() {
        final AlertDialog Dialog;

        usr=new clsUsuarioObj(this,Con,db);
        usr.fill(" WHERE Activo=1 ORDER BY Nombre");
        if (usr.count == 0) {
            msgbox("Catálogo de usuarios vacio.");return;
        }

        final String[] selitems = new String[usr.count];
        for (int i = 0; i < usr.count; i++) {
            selitems[i] = usr.items.get(i).nombre;
        }

        AlertDialog.Builder mMenuDlg = new AlertDialog.Builder(this);
        mMenuDlg.setTitle("Lista de usuarios");

        mMenuDlg.setItems(selitems, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                try {
                    msgbox("Código : " + usr.items.get(item).id + "\nNombre : " + usr.items.get(item).nombre);
                } catch (Exception e) {
                }
            }
        });

        mMenuDlg.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        Dialog = mMenuDlg.create();
        Dialog.show();

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
