package com.dts.proyectovacio;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class Usuario extends PBase {

    private EditText txtName,txtLogin,txtPass;
    private TextView lblCode;
    private Spinner spinRol;

    private clsUsuarioObj users;
    private clsRolObj roles;
    private clsClasses.clsUsuario item=clsCls.new clsUsuario();

    private int userid,rolid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);

        super.InitBase(savedInstanceState);

        lblCode = (TextView) findViewById(R.id.textView9);
        txtName = (EditText) findViewById(R.id.editText6);txtName.requestFocus();
        txtLogin = (EditText) findViewById(R.id.editText7);
        txtPass = (EditText) findViewById(R.id.editText8);
        spinRol = (Spinner) findViewById(R.id.spinner2);

        setHandlers();

        users =new clsUsuarioObj(this,Con,db);
        roles =new clsRolObj(this,Con,db);

        userid=gl.itemid;
        if (userid==0) newItem(); else loadItem();
        fillSpinner();

    }


    // Events

    public void doSave(View view) {
        if (gl.itemid!=0) {
            updateItem();
         } else {
            addItem();
        }
    }

    public void doExit(View view) {
        finish();
    }

    private void setHandlers() {

        spinRol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                TextView spinlabel;

                try {
                    spinlabel=(TextView)parentView.getChildAt(0);
                    spinlabel.setTextColor(Color.BLACK);
                    spinlabel.setPadding(5, 0, 0, 0);
                    spinlabel.setTextSize(18);

                    rolid=roles.items.get(position).id;
                } catch (Exception e) {
                    gl.userid=0;
                    //msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" .isel. "+e.getMessage());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                return;
            }
        });
    }

    // Main

    private void loadItem() {
        try {
            users.fill("WHERE ID="+userid);
            item=users.first();

            lblCode.setText(""+item.id);
            txtName.setText(item.nombre);txtName.requestFocus();
            txtLogin.setText(item.login);
            txtPass.setText(item.clave);

            rolid=item.rol;

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }
    }

    private void newItem() {
        clsUsuarioObj users =new clsUsuarioObj(this,Con,db);

        userid=users.newID("SELECT MAX(ID) FROM Usuario");
        rolid=0;

        lblCode.setText(""+userid);
        txtName.setText("");txtName.requestFocus();
        txtLogin.setText("");
        txtPass.setText("");
    }

    private void addItem() {
        if (!checkValues()) return;

        item.id=userid;
        item.nombre=txtName.getText().toString();
        item.login=txtLogin.getText().toString();
        item.clave=txtPass.getText().toString();
        item.activo=1;
        item.rol=rolid;

        try {
            users.add(item);
            toast("Registro agregado");finish();
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }
    }

    private void updateItem() {
        if (!checkValues()) return;

        item.nombre=txtName.getText().toString();
        item.login=txtLogin.getText().toString();
        item.clave=txtPass.getText().toString();
        item.rol=rolid;

        try {
            users.update(item);
            toast("Registro actualizado");finish();
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+" . "+e.getMessage());
        }

    }


    // Aux

    private boolean checkValues() {
        String val,login;

        val=txtName.getText().toString();
        if (emptystr(val)) {
            msgbox("Falta nombre.");txtName.requestFocus();return false;
        }

        val=txtLogin.getText().toString();login=val;
        if (emptystr(val)) {
            msgbox("Falta login.");txtName.requestFocus();return false;
        }

        val=txtPass.getText().toString();
        if (emptystr(val)) {
            msgbox("Falta clave.");txtName.requestFocus();return false;
        }

        users.fill("WHERE ID<>"+userid);
        for (int i = 0; i <users.count; i++) {
            if (users.items.get(i).login.equalsIgnoreCase(login)) {
                msgbox("El login ya existe para usuario : " +users.items.get(i).nombre);
                txtLogin.requestFocus();return false;
            }
        }

        if (rolid==0) {
            msgbox("Falta definir un rol.");return false;
        }

        return true;
    }

    private void fillSpinner() {
        ArrayList<String> spinlist = new ArrayList<String>();
        int pos=0;

        try {
            roles.fill("ORDER BY Nombre");

            for (int i = 0; i <roles.items.size(); i++) {
                spinlist.add(roles.items.get(i).nombre);
                if (roles.items.get(i).id==rolid) pos=i;
            }
        } catch (Exception e) {
            msgbox(new Object(){}.getClass().getEnclosingMethod().getName()+ " . "+e.getMessage());
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, spinlist);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinRol.setAdapter(dataAdapter);

        try {
            spinRol.setSelection(pos);
        } catch (Exception e) {
        }

        users.items.clear();
    }

    // Activity Events

    @Override
    protected void onResume() {
        super.onResume();

        try {
            users.reconnect(Con,db);
        } catch (Exception e) {
            msgbox(e.getMessage());
        }

        try {
            roles.reconnect(Con,db);
        } catch (Exception e) {
            msgbox(e.getMessage());
        }
    }

}
