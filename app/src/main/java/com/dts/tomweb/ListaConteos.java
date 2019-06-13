package com.dts.tomweb;

import android.os.Bundle;
import android.view.View;

public class ListaConteos extends PBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_conteos);

        super.InitBase(savedInstanceState);
    }

    //region Events

    public void doExit(View view) {
        finish();
    }

    public void doHelp(View view) {

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
