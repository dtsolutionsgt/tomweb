package com.dts.tomweb;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.dts.classes.clsInventario_encabezadoObj;

import com.dts.base.clsClasses;

public class Inventario extends PBase {

    private TextView Correlativo, Empresa, Estado, FechaInv, Descrip, TipoInv;
    private String estado, tipoinv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario);

        super.InitBase(savedInstanceState);

        addlog("Inventario",""+du.getActDateTime(),gl.nombreusuario);

        Correlativo = (TextView) findViewById(R.id.txtCorrel);
        Empresa = (TextView) findViewById(R.id.txtEmpresa);
        Estado = (TextView) findViewById(R.id.txtEstado);
        FechaInv = (TextView) findViewById(R.id.txtFecha);
        Descrip = (TextView) findViewById(R.id.txtDescripcion);
        TipoInv = (TextView) findViewById(R.id.txtTipoInv);

        Filltxt();

    }

    //region Events

    public void doExit(View view) {
        finish();
    }

    public void doHelp(View view) {
        String tx;

        try{

            tx="-Correlativo: Este campo muestra el numero de inventario que se está utilizando.\n\n" +
               "-Empresa: Muestra el identificador(id) de la empresa.\n\n" +
               "-Fecha Inv: Fecha en la que se empezó el inventario.\n\n" +
               "-Descripción: Nombre del inventario.\n\n" +
               "-Tipo Inv: El tipo del inventario con el que se está trabajando.";

            PopUp(tx);

        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }


    }

    public void doNext(View view) {
        startActivity(new Intent(this, Conteo.class));
    }

    private void setHandlers(){
        try{

        }catch (Exception e){

        }
    }

    //endregion

    //region Main

    public void Filltxt(){
        clsInventario_encabezadoObj invEnc = new clsInventario_encabezadoObj(this, Con, db);
        clsClasses.clsInventario_encabezado item=clsCls.new clsInventario_encabezado();

        String sfecha;

        try{
            invEnc.fill();

            if(invEnc.count == 0) return;

            item = invEnc.first();

            if(item.id_estado.equals("3")){

                estado = "Terminado";

            }else if(item.id_estado.equals("2")){

                estado = "Proceso";

            }else if(item.id_estado.equals("1")){

                estado = "Nuevo";

            }

            sfecha = item.fecha_inicio;

            if(item.tipo_inventario == 4){

                gl.tipoInv=4;
                tipoinv = "Inventario Comparativo";

            }else if(item.tipo_inventario == 3){

                gl.tipoInv=3;
                tipoinv = "Inventario Teorico";

            }else if(item.tipo_inventario == 2){

                gl.tipoInv=2;
                tipoinv = "Inventario con Maestro";

            }else if(item.tipo_inventario == 1){

                gl.tipoInv=1;
                tipoinv = "Inventario Ciego";

            }

            gl.idInvEnc=item.id_inventario_enc;
            gl.empresa=item.id_empresa;

            Correlativo.setText(Integer.toString(gl.idInvEnc));
            Empresa.setText(Integer.toString(gl.empresa));
            Estado.setText(estado);
            FechaInv.setText(sfecha);
            Descrip.setText(item.nombre);
            TipoInv.setText(tipoinv);


        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox(""+e);
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
