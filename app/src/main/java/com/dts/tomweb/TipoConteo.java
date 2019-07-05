package com.dts.tomweb;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class TipoConteo extends PBase {

    private TextView Barra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tipo_conteo);

        super.InitBase(savedInstanceState);

        Barra = (TextView) findViewById(R.id.txtBarra);

        Barra.setText(gl.codBarra);
    }


    //region Events

    public void doExit(View view) {
        finish();
    }

    public void doHelp(View view) {

    }

    public void doNext(View view){

        callback = 1;
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


    //endregion

}
