package com.dts.tomweb;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.dts.listadapt.LA_Tablas;
import com.dts.listadapt.LA_Tablas2;

import java.util.ArrayList;

public class ListaConteos extends PBase {

    private GridView grid,dgrid;
    private Spinner spin,spinf;
    private ProgressBar pbar;
    private EditText txtBarra;
    private EditText txtUbic;
    private TextView regs;
    private CheckBox cb;

    private ArrayList<String> spinlist = new ArrayList<String>();
    private ArrayList<String> values=new ArrayList<String>();
    private ArrayList<String> dvalues=new ArrayList<String>();
    private LA_Tablas adapter;
    private LA_Tablas2 dadapter;

    private int cw;
    private String scod;
    private boolean consol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_conteos);

        super.InitBase(savedInstanceState);

        addlog("ListaConteos",""+du.getActDateTime(),gl.nombreusuario);

        grid = (GridView) findViewById(R.id.gridview1);
        dgrid = (GridView) findViewById(R.id.gridview2);
        pbar=(ProgressBar) findViewById(R.id.progressBar);pbar.setVisibility(View.INVISIBLE);
        txtBarra = (EditText) findViewById(R.id.txtBarra);
        txtUbic = (EditText) findViewById(R.id.txtNombre);
        regs = (TextView) findViewById(R.id.txtRegs);
        cb = (CheckBox) findViewById(R.id.cbConsolidar);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        cw = (int) ((displayMetrics.widthPixels-22)/5)-1;

        cb.setChecked(false);
        consol=false;

        if(gl.tipoInv==1) scod = " INVENTARIO_CIEGO";
        if(gl.tipoInv==2 || gl.tipoInv==3) scod = " INVENTARIO_DETALLE";

        setHandlers();

        showData(scod);

    }

    //region Events

    public void doExit(View view) {
        finish();
    }

    public void doHelp(View view) {
        String tx;

        try{

            tx="-Busqueda: La busqueda se puede hacer por código de barra, ubicación, o ambos.\n\n" +
                    "-Regs: Muestra la cantidad de registros, o de conteos realizados.\n\n" +
                    "-Consolidar: Consolida y muestra los registros según código de barra y ubicación.";

            PopUp(tx);

        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }


    }

    private void setHandlers(){

        try{

            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (cb.isChecked()==true) consol = true; showData(scod);
                if (cb.isChecked()==false) consol = false; showData(scod);
            });

            grid.setOnItemClickListener((parent, view, position, id) -> {

                try {
                    Object lvObj = grid.getItemAtPosition(position);
                    String item = (String) lvObj;

                    adapter.setSelectedIndex(position);
                    toast(item);
                } catch (Exception e) {
                    addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                    msgbox("Error setHandler: "+e);
                }
            });

            dgrid.setOnItemClickListener((parent, view, position, id) -> {

                try {
                    Object lvObj = dgrid.getItemAtPosition(position);
                    String item = (String) lvObj;

                    dadapter.setSelectedIndex(position);
                    toast(item);
                } catch (Exception e) {
                    addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                    msgbox("Error setHandler: "+e);
                }
            });

            dgrid.setOnItemLongClickListener((parent, view, position, id) -> {

                try {
                    Object lvObj = dgrid.getItemAtPosition(position);
                    String item = (String) lvObj;

                    adapter.setSelectedIndex(position);
                    msgbox(item);
                } catch (Exception e) {
                    addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                    msgbox("Error setHandler: "+e);
                }
                return true;
            });

            txtBarra.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start,int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start,int before, int count) {
                    showData(scod);
                }

            });

            txtUbic.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start,int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start,int before, int count) {
                    showData(scod);
                }

            });
        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox("Error setHandler: "+e);
        }
    }

    //endregion

    //region Main

    private void showData(String tn) {

        Cursor dt;
        String ss = "",barra,ubic;
        int cc,rg;

        try {

            dvalues.clear();
            values.clear();

            barra = txtBarra.getText().toString().trim();
            ubic = txtUbic.getText().toString().trim();

            if(!ubic.isEmpty() && !barra.isEmpty()){
                tn = tn +" WHERE CODIGO_BARRA = '"+ barra + "' AND UBICACION = '" + ubic + "' AND ID_INVENTARIO_ENC="+ gl.idInvEnc +" AND ELIMINADO = 0";
            }else if(!barra.isEmpty()){
                tn = tn +" WHERE CODIGO_BARRA = '"+ barra + "' AND ID_INVENTARIO_ENC="+ gl.idInvEnc +" AND ELIMINADO = 0";
            }else if(!ubic.isEmpty()) {
                tn = tn+ " WHERE UBICACION = '" + ubic + "' AND ID_INVENTARIO_ENC="+ gl.idInvEnc +" AND ELIMINADO = 0";
            }else {
                tn = tn + " WHERE ID_INVENTARIO_ENC="+ gl.idInvEnc +" AND ELIMINADO = 0";
            }

            if(consol==true){
                ss="SELECT CODIGO_BARRA, UBICACION, SUM(CANTIDAD) FROM "+ tn +" GROUP BY CODIGO_BARRA, UBICACION";
            }else {
                ss="SELECT CODIGO_BARRA, UBICACION, CANTIDAD FROM "+ tn;
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
                        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                        ss="?";
                    }
                    dvalues.add(ss);
                }
                dt.moveToNext();
            }
            if (dt!=null) dt.close();

            values.add("CODIGO");
            values.add("UBICACION");
            values.add("CANTIDAD");

            ViewGroup.LayoutParams dlayoutParams = dgrid.getLayoutParams();
            dlayoutParams.width =((int) (cw*cc))+25;
            dgrid.setLayoutParams(dlayoutParams);
            dgrid.setNumColumns(3);
            dadapter=new LA_Tablas2(this,dvalues);
            dgrid.setAdapter(dadapter);

            ViewGroup.LayoutParams layoutParams = grid.getLayoutParams();
            layoutParams.width =((int) (cw*cc))+25;
            grid.setLayoutParams(layoutParams);
            grid.setNumColumns(3);

            adapter=new LA_Tablas(this,values);
            grid.setAdapter(adapter);

            pbar.setVisibility(View.INVISIBLE);

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
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
