package com.dts.tomweb;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.FileOutputStream;

public class Licencia extends PBase {

    private EditText Lic;
    private TextView clave;
    private Integer serieL;
    private String serie, lic;

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
        clave = (TextView) findViewById(R.id.txtClave);

        clave.setText(gl.NoSerieHH);

    }



    //region Events

    public void doHelp(View view){

    }

    public void doActivate(View view) {
        serieL = gl.NoSerieHH.length();
        serie = gl.NoSerieHH;
        lic = Lic.getText().toString();

        try{

            if(lic.isEmpty()){
                msgbox("Debe ingresar la clave de activacion");
            } else{

                if(lic.length()>=serieL){

                    //msgbox("El usuario ingresó: "+ lic +"\n No serie es: "+ serie);

                    if(lic.equals(gl.NoSerieHH)){
                        //crear archivo
                        Crear_Registro_Licencia();
                        msgbox("Se ha regitrado correctamente la licencia del dispositivo");
                        //return a comws
                    }else if(!lic.equals(gl.NoSerieHH)){
                        msgbox("Clave de activacion incorrecta");
                    }

                }else {
                   // msgbox("El usuario ingresó: "+ lic +" long: "+ lic.length() +"\n No serie es: "+ serie + " long: "+ serieL);
                }


            }


        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

    }

    public void doExit(View view) {
        super.finish();
        gl.validaLicDB=4;
        finish();
    }

    //endregion

    //region Main

    public boolean Crear_Registro_Licencia(){
        String fecha;

        try {
            fecha = du.getActDates();

            String FILENAME = "credentialsTOM";
            String string = gl.NoSerieHH;

            FileOutputStream fileLic= openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fileLic.write(string.getBytes());
            fileLic.close();

        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            return false;
        }

        return true;
    }

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
