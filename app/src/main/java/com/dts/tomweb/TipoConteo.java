package com.dts.tomweb;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.dts.base.clsClasses;
import com.dts.classes.clsArticuloObj;
import com.dts.classes.clsInventario_teoricoObj;

public class TipoConteo extends PBase {

    private TextView Barra;
    private RadioButton Fisico, Serial;

    private String tipo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tipo_conteo);

        super.InitBase(savedInstanceState);

        Barra = (TextView) findViewById(R.id.txtBarra);
        Fisico = (RadioButton) findViewById(R.id.rbFisico);
        Serial = (RadioButton) findViewById(R.id.rbSerial);

        Barra.setText(gl.codBarra);
    }


    //region Events

    public void doExit(View view) {
        gl.cbck=2;
        finish();
    }

    public void doHelp(View view) {

    }

    //endregion

    //region Main

    public void addProd(View view){
        clsInventario_teoricoObj teo = new clsInventario_teoricoObj(this, Con, db);
        clsClasses.clsInventario_teorico itemt=clsCls.new clsInventario_teorico();
        clsArticuloObj art = new clsArticuloObj(this, Con, db);
        clsClasses.clsArticulo item=clsCls.new clsArticulo();

        try{

            if(!Fisico.isChecked() && !Serial.isChecked()){
                msgbox("Debe escoger un tipo de conteo");
                return;
            }

            if(Fisico.isChecked()){ tipo = "F"; gl.cbck = 1; }
            if(Serial.isChecked()){ tipo = "S"; gl.cbck = 3; }

            if(gl.tipoInv==2){

                item.codigo_barra = gl.codBarra;
                item.costo = 0;
                item.descripcion = "NO ENCONTRADO";
                item.id_articulo = gl.codBarra;
                item.id_empresa = gl.empresa;
                item.tipo_conteo = tipo;

                art.add(item);

            }else if(gl.tipoInv==3){

                itemt.codigo_barra = gl.codBarra;
                itemt.costo = 0;
                itemt.cantidad = 0;
                itemt.descripcion = "NO ENCONTRADO";
                itemt.id_articulo = gl.codBarra;
                itemt.id_empresa = gl.empresa;
                itemt.tipo_conteo = tipo;
                itemt.id_inventario_enc = gl.idInvEnc;

                teo.add(itemt);

            }


            Toast.makeText(this, "Producto agregado correctamente", Toast.LENGTH_LONG).show();

            super.finish();

        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox("Error getCampos: "+e);
        }
    }

    //endregion

    //region Aux


    //endregion

    //region Dialogs


    //endregion

    //region Activity Events


    //endregion

}
