package com.dts.tomweb;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

        grid = (GridView) findViewById(R.id.gridview1);
        dgrid = (GridView) findViewById(R.id.gridview2);
        pbar=(ProgressBar) findViewById(R.id.progressBar);pbar.setVisibility(View.INVISIBLE);
        txtBarra = (EditText) findViewById(R.id.txtBarra);
        txtUbic = (EditText) findViewById(R.id.txtUbic);
        regs = (TextView) findViewById(R.id.txtRegs);
        cb = (CheckBox) findViewById(R.id.cbConsolidar);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        cw = (int) ((displayMetrics.widthPixels-22)/5)-1;

        cb.setChecked(false);
        consol=false;

        setHandlers();

        processTable();

    }

    //region Events

    public void doExit(View view) {
        finish();
    }

    public void doHelp(View view) {

    }

    private void setHandlers(){

        try{

            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (cb.isChecked()==true) consol = true; processTable();
                    if (cb.isChecked()==false) consol = false; processTable();
                }
            });

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
                                processTable();
                                return true;
                        }
                    }
                    return false;
                }
            });

            txtUbic.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                    if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                        switch (arg1) {
                            case KeyEvent.KEYCODE_ENTER:
                                processTable();
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

    private void processTable() {
        try{
            values.clear();
            dvalues.clear();

            pbar.setVisibility(View.VISIBLE);

            Handler mmtimer = new Handler();
            Runnable mmrunner = new Runnable() {
                @Override
                public void run() {
                    pbar.setVisibility(View.VISIBLE);

                    values.clear();
                    dvalues.clear();

                    adapter = new LA_Tablas(ListaConteos.this, values);
                    grid.setAdapter(adapter);

                    dadapter = new LA_Tablas2(ListaConteos.this, dvalues);
                    dgrid.setAdapter(dadapter);
                }
            };
            mmtimer.postDelayed(mmrunner, 50);

            Handler mtimer = new Handler();
            Runnable mrunner = new Runnable() {
                @Override
                public void run() {
                    scod = "INVENTARIO_CIEGO";
                    showData(scod);
                }
            };
            mtimer.postDelayed(mrunner, 1000);
        }catch (Exception e){
            //addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox("processTable: "+e);
        }

    }

    private void showData(String tn) {
        Cursor dt;
        String ss = "",barra,ubic;
        int cc,rg;

        try {

            barra = txtBarra.getText().toString();
            ubic = txtUbic.getText().toString();

            if(!ubic.isEmpty() && !barra.isEmpty()){
                tn = tn +" WHERE CODIGO_BARRA = "+ barra + " AND UBICACION ="+ ubic;
            }else if(!barra.isEmpty()){
                tn = tn +" WHERE CODIGO_BARRA = "+barra;
            }else if(!ubic.isEmpty()) {
                tn = tn+ " WHERE UBICACION = "+ubic;
            }

            /*if(consol=true){
                ss="SELECT ID, UBICACION, SUM(CASE WHEN ID=ID THEN CANTIDAD) FROM "+tn ;
            }else {*/
                ss="SELECT CODIGO_BARRA, UBICACION, CANTIDAD FROM "+tn;
            //}


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
            values.add("UBICACION");
            values.add("CANTIDAD");

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
