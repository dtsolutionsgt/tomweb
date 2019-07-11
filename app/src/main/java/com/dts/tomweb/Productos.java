package com.dts.tomweb;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dts.listadapt.LA_Tablas;
import com.dts.listadapt.LA_Tablas2;

import java.util.ArrayList;

public class Productos extends PBase {

    private GridView grid,dgrid;
    private ProgressBar pbar;
    private EditText txtBarra;
    private EditText txtNombre;
    private TextView regs;
    private CheckBox cb;

    private ArrayList<String> values=new ArrayList<String>();
    private ArrayList<String> dvalues=new ArrayList<String>();
    private LA_Tablas adapter;
    private LA_Tablas2 dadapter;

    private int cw;
    private String scod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productos);

        super.InitBase(savedInstanceState);

        grid = (GridView) findViewById(R.id.gridview1);
        dgrid = (GridView) findViewById(R.id.gridview2);
        pbar=(ProgressBar) findViewById(R.id.progressBar);pbar.setVisibility(View.INVISIBLE);
        txtBarra = (EditText) findViewById(R.id.txtBarra);
        txtNombre = (EditText) findViewById(R.id.txtNombre);
        regs = (TextView) findViewById(R.id.txtRegs);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        cw = (int) ((displayMetrics.widthPixels-22)/5)-1;

        if(gl.tipoInv==1) scod = " INVENTARIO_CIEGO";
        if(gl.tipoInv==2 || gl.tipoInv==3) scod = " ARTICULO";

        setHandlers();

        showData(scod);

    }

    //region Events

    public void doExit(View view) {
        finish();
    }

    public void doHelp(View view) {

    }

    private void setHandlers(){

        try{
            grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    try {
                        Object lvObj = grid.getItemAtPosition(position);
                        String item = (String) lvObj;

                        adapter.setSelectedIndex(position);
                        toast(item);
                    } catch (Exception e) {
                        //addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                        msgbox("Error setHandler: "+e);
                        //mu.msgbox(e.getMessage());
                    }
                }

                ;
            });

            dgrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    try {
                        Object lvObj = dgrid.getItemAtPosition(position);
                        String item = (String) lvObj;

                        dadapter.setSelectedIndex(position);
                        toast(item);
                    } catch (Exception e) {
                        //addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                        msgbox("Error setHandler: "+e);
                        //mu.msgbox(e.getMessage());
                    }
                }

                ;
            });

            dgrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                    try {
                        Object lvObj = dgrid.getItemAtPosition(position);
                        String item = (String) lvObj;

                        adapter.setSelectedIndex(position);
                        msgbox(item);
                    } catch (Exception e) {
                        //addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                        msgbox("Error setHandler: "+e);
                    }
                    return true;
                }
            });

            txtBarra.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                    if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                        switch (arg1) {
                            case KeyEvent.KEYCODE_ENTER:
                                showData(scod);
                                return true;
                        }
                    }
                    return false;
                }
            });

            txtNombre.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                    if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                        switch (arg1) {
                            case KeyEvent.KEYCODE_ENTER:
                                showData(scod);
                                return true;
                        }
                    }
                    return false;
                }
            });
        }catch (Exception e){
            //addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox("Error setHandler: "+e);
        }

    }

    //endregion

    //region Main

    private void showData(String tn) {
        Cursor dt;
        String ss = "",barra,nomb;
        int cc,rg;

        try {

            barra = txtBarra.getText().toString();
            nomb = txtNombre.getText().toString();

            if(gl.tipoInv==1) {

                if (!nomb.isEmpty() && !barra.isEmpty()) {
                    tn = tn + " WHERE CODIGO_BARRA = " + barra + " AND UBICACION =" + nomb + " AND ELIMINADO = 0";
                } else if (!barra.isEmpty()) {
                    tn = tn + " WHERE CODIGO_BARRA = " + barra + " AND ELIMINADO = 0";
                } else if (!nomb.isEmpty()) {
                    tn = tn + " WHERE UBICACION = " + nomb + " AND ELIMINADO = 0";
                }else{
                    tn = tn + " WHERE ELIMINADO = 0";
                }

                ss = "SELECT CODIGO_BARRA, UBICACION, CANTIDAD FROM " + tn;

            } else {

                if (!nomb.isEmpty() && !barra.isEmpty()) {
                    tn = tn + " WHERE CODIGO_BARRA = " + barra + " AND DESCRIPCION =" + nomb;
                } else if (!barra.isEmpty()) {
                    tn = tn + " WHERE CODIGO_BARRA = " + barra;
                } else if (!nomb.isEmpty()) {
                    tn = tn + " WHERE DESCRIPCION = " + nomb;
                }

                ss = "SELECT ID_ARTICULO, DESCRIPCION, CODIGO_BARRA, TIPO_CONTEO FROM " + tn;
            }

            dt=Con.OpenDT(ss);
            if (dt.getCount()==0) {
                pbar.setVisibility(View.INVISIBLE);return;
            }

            cc = dt.getColumnCount();
            rg = dt.getCount();
            if(rg>0){
                regs.setText(""+rg);
            }

            dt.moveToFirst();
            while (!dt.isAfterLast()) {

                for (int i = 0; i < cc; i++) {
                    try {
                        ss=dt.getString(i);
                    } catch (Exception e) {
                        ss="?";
                    }
                    dvalues.add(ss);
                }
                dt.moveToNext();
            }

            values.add("CODIGO");
            if(gl.tipoInv==1){
                values.add("UBICACION");
                values.add("CANTIDAD");
            } else{
                values.add("NOMBRE");
                values.add("BARRA");
                values.add("TIPO CONTEO");
            }

            ViewGroup.LayoutParams dlayoutParams = dgrid.getLayoutParams();
            dlayoutParams.width =((int) (cw*cc))+25;
            dgrid.setLayoutParams(dlayoutParams);

            dgrid.setColumnWidth(cw);
            dgrid.setStretchMode(GridView.NO_STRETCH);
            dgrid.setNumColumns(cc);

            dadapter=new LA_Tablas2(this,dvalues);
            dgrid.setAdapter(dadapter);


            ViewGroup.LayoutParams layoutParams = grid.getLayoutParams();
            layoutParams.width =((int) (cw*cc))+25;
            grid.setLayoutParams(layoutParams);

            grid.setColumnWidth(cw);
            grid.setStretchMode(GridView.NO_STRETCH);
            grid.setNumColumns(cc);

            adapter=new LA_Tablas(this,values);
            grid.setAdapter(adapter);

            pbar.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            msgbox("showData2: "+e);
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
