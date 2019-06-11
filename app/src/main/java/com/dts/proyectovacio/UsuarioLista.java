package com.dts.proyectovacio;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

public class UsuarioLista extends PBase {

    private ListView listView;
    private LA_Usuario adapter;
    private clsUsuarioObj users;

    private String itemtext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario_lista);

        super.InitBase(savedInstanceState);

        listView = (ListView) findViewById(R.id.listView1);

        users =new clsUsuarioObj(this,Con,db);

        setHandlers();

        listItems();

    }



    // Events

    public void doEdit(View view) {
        editItem();
    }

    public void doAdd(View view) {
        addItem();
    }

    public void doDel(View view) {
        msgAskDel("Eliminar registro");
    }

    public void doExit(View view) {
        finish();
    }

    private void setHandlers() {

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
                Object lvObj = listView.getItemAtPosition(position);
                clsClasses.clsUsuario item = (clsClasses.clsUsuario)lvObj;

                adapter.setSelectedIndex(position);

                itemtext=item.nombre;
                selid=item.id;selidx=position;
                editItem();
              };
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    Object lvObj = listView.getItemAtPosition(position);
                    clsClasses.clsUsuario item = (clsClasses.clsUsuario) lvObj;

                    adapter.setSelectedIndex(position);

                    itemtext=item.nombre;
                    selid=item.id;selidx=position;

                    showItemMenu();

                } catch (Exception e) {
                    toast(e.getMessage());
                }
                return true;
            }
        });

    }


    // Main

    private void listItems() {

        selid=0;

        try {
            users.fill();

            adapter=new LA_Usuario(this,this, users.items);
            listView.setAdapter(adapter);
        } catch (Exception e) {
            mu.msgbox(e.getMessage());
        }
    }

    private void addItem() {
        gl.itemid=0;
        callback =1;
        startActivity(new Intent(this,Usuario.class));
    }

    private void editItem() {
        callback =1;
        gl.itemid=selid;
        startActivity(new Intent(this,Usuario.class));
    }

    private void delItem() {
        try {
            users.delete(selid);
            listItems();
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }


    // Popup Menu

    private void showItemMenu() {
        final AlertDialog Dialog;
        final String[] selitems = {"Editar","Borrar"};

        AlertDialog.Builder menudlg = new AlertDialog.Builder(this);
        menudlg.setTitle("Usuario");

        menudlg.setItems(selitems , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        editItem();break;
                    case 1:
                        msgAskDel("Eliminar registro");;break;
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


    // Dialogs

    private void msgAskDel(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Usuario");
        dialog.setMessage("¿" + msg + "?");

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                delItem();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });

        dialog.show();

    }


    // Aux



    // Activity Events

    @Override
    protected void onResume() {
        super.onResume();

        try {
            users.reconnect(Con,db);
        } catch (Exception e) {
            msgbox(e.getMessage());
        }

        if (callback ==1) {
            callback =0;
            listItems();return;
        }

    }

}
