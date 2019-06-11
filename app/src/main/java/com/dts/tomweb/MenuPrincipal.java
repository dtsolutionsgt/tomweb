package com.dts.tomweb;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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
        lblTitle = (TextView) findViewById(R.id.textView3);lblTitle.setText(gl.nombreusuario);

        rolid=gl.rolid;

        setHandlers();

        buildMenuItems();
        listItems();

    }


    // Events

    private void setHandlers() {

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object lvObj = listView.getItemAtPosition(position);
                clsClasses.clsMenu item = (clsClasses.clsMenu)lvObj;

                adapter.setSelectedIndex(position);

                menuOption(position);
            };
        });
    }

    // Main

    private void listItems() {
        try {
             adapter=new LA_Menu(this,this, menuitems);
            listView.setAdapter(adapter);
        } catch (Exception e) {
            mu.msgbox(e.getMessage());
        }
    }

    private void menuOption(int midx) {
        switch (midx) {
            case 0:
                callback =1;
                startActivity(new Intent(this,UsuarioLista.class));break;
            case 1:
                startActivity(new Intent(this,ComWS.class));break;
            case 2:
                msgAskExit("Salir de aplicación");break;
        }

    }


    // Dialogs

    private void msgAskExit(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Proyecto Vacío");
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


    // Aux

    private void buildMenuItems() {
        clsClasses.clsMenu item;

        menuitems.clear();

        item=clsCls.new clsMenu();
        item.id=1;item.nombre="Usuarios";
        menuitems.add(item);

        item=clsCls.new clsMenu();
        item.id=2;item.nombre="Comunicación";
        menuitems.add(item);

        item=clsCls.new clsMenu();
        item.id=3;item.nombre="Salir";
        menuitems.add(item);

    }


    // Activity Events

    @Override
    public void onBackPressed() {
        msgAskExit("Salir de aplicación");
    }


}
