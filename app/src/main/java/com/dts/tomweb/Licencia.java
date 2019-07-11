package com.dts.tomweb;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class Licencia extends PBase {

    private EditText Lic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licencia);

        super.InitBase(savedInstanceState);

        addlog("Licencia",""+du.getActDateTime(),gl.nombreusuario);

        if(gl.validaLicDB==1){
            mu.msgbox("Este dispositivo no tiene una licencia registrada");
        }else if (gl.validaLicDB==2) {
            mu.msgbox("Este dispositivo no tiene una licencia activa");
        }

        Lic = (EditText) findViewById(R.id.txtLic);

    }



    //region Events

    public void doHelp(View view){

    }

    public void doActivate(View view) {

        try{

            if(Lic.getText().toString().isEmpty()){
                msgbox("Debe ingresar la clave de activacion");
            } else if(Lic.getText().toString().equals(gl.NoSerieHH)){
                msgbox("Clave registrada correctamente");
            }else if(!Lic.getText().toString().equals(gl.NoSerieHH)){
                msgbox("Clave de activacion incorrecta");
            }

            super.finish();
            gl.validaLicDB=4;

        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

    }

    public void doExit(View view) {
        finish();
    }

    //endregion

    //region Main


    //endregion

    //region Aux


    //endregion

    //region Dialogs


    //endregion

    //region Activity Events

    @Override
    public void onBackPressed() {
        if(gl.validaLicDB==2) {
            super.finish();
        }else{
            super.onBackPressed();
        }
    }

    //endregion



}
