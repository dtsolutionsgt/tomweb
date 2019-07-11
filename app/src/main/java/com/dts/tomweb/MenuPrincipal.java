package com.dts.tomweb;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.dts.base.clsClasses;
import com.dts.listadapt.LA_Menu;

import java.util.ArrayList;

public class MenuPrincipal extends PBase {

    private TextView lblTitle;

    private ArrayList<clsClasses.clsMenu> menuitems = new ArrayList<clsClasses.clsMenu>();
    private ListView listView;
    private LA_Menu adapter;

    private int rolid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);

        super.InitBase(savedInstanceState);

        listView = (ListView) findViewById(R.id.listView1);
        lblTitle = (TextView) findViewById(R.id.Productos);lblTitle.setText(gl.nombreusuario);

    }

    //region Events

    public void doInventario(View view) {
        startActivity(new Intent(this,Inventario.class));
    }

    public void doCom(View view) {
        gl.validaLicDB=10;
        startActivity(new Intent(this,ComWS.class));
    }

    public void doExit(View view) {
        msgAskExit("Salir de aplicación");
    }

    //endregion

    //region Dialogs

    private void msgAskExit(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Tom");
        dialog.setMessage("¿" + msg + "?");

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                gl.exitapp=true;
                finish();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });

        dialog.show();

    }

    //endregion


    //region Activity Events

    @Override
    public void onBackPressed() {
        msgAskExit("Salir de aplicación");
    }

    //endregion
}
