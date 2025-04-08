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

        lblTitle = findViewById(R.id.Productos);lblTitle.setText(gl.nombreusuario);

    }

    //region Events

    public void doInventario(View view) {
        try{
            startActivity(new Intent(this,Inventario.class));
        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }
    }

    public void doCom(View view) {

        try{
            gl.validaLicDB=10;
            startActivity(new Intent(this,ComWS.class));
        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }
    }

    public void doExit(View view) {
        msgAskExit("Salir de aplicación");
    }

    //endregion

    //region Dialogs

    private void msgAskExit(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle("Tom");
        dialog.setMessage("¿" + msg + "?");
        dialog.setPositiveButton("Si", (dialog1, which) -> {
            gl.exitapp = true;
            finito();
        });
        dialog.setNegativeButton("No", (dialog2, which) -> {
        });
        dialog.show();
    }

    //endregion


    //region Activity Events

    @Override
    public void onBackPressed() {
        msgAskExit("Salir de aplicación");
    }

    public void finito(){
        //#GT16062022_2234: finish cierra, super.finish reabre la app
        finish();
        //super.finish();
    }

    //endregion
}
