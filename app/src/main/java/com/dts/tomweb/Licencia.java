package com.dts.tomweb;

import android.os.Bundle;
import android.view.View;

public class Licencia extends PBase {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licencia);

        super.InitBase(savedInstanceState);

    }



    //region Events

    public void doActivate(View view) {

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


    //endregion



}
