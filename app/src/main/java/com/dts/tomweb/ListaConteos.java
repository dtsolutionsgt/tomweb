package com.dts.tomweb;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.ImageView;
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
    private ImageView imgEliminar;

    private ArrayList<String> spinlist = new ArrayList<String>();
    private ArrayList<String> values=new ArrayList<String>();
    private ArrayList<String> dvalues=new ArrayList<String>();
    private LA_Tablas adapter;
    private LA_Tablas2 dadapter;

    private int cw;
    private String scod;
    private boolean consol;
    private int idConteo = 0;

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
        imgEliminar = (ImageView) findViewById(R.id.imgEliminar);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        cw = (int) ((displayMetrics.widthPixels-22)/5)-1;

        cb.setChecked(false);
        imgEliminar.setVisibility(View.VISIBLE);
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

            tx="-Búsqueda: La búsqueda se puede hacer por código de barra, ubicación, o ambos.\n\n" +
                    "-Regs: Muestra la cantidad de registros, o de conteos realizados.\n\n" +
                    "-Consolidar: Consolida y muestra los registros según código de barra y ubicación.";

            PopUp(tx);

        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }


    }

    private void setHandlers(){

        try{

            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (cb.isChecked()==true)  {
                        consol = true;
                        showData(scod);
                        imgEliminar.setVisibility(View.INVISIBLE);
                    }
                    if (cb.isChecked()==false){
                        consol = false;
                        showData(scod);
                        imgEliminar.setVisibility(View.VISIBLE);
                    }
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
                        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                        msgbox("Error setHandler: "+e);
                    }
                }

                ;
            });

            dgrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    try {
                        idConteo = 0;
                        Object lvObj = dgrid.getItemAtPosition(position - (position % 4));
                        String item = (String) lvObj;
                        idConteo = Integer.valueOf(item);

                        dadapter.setSelectedIndex(position);
                        toast(item);
                    } catch (Exception e) {
                        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                        msgbox("Error setHandler: "+e);
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
                        addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
                        msgbox("Error setHandler: "+e);
                    }
                    return true;
                }
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
                tn = tn +" WHERE CODIGO_BARRA = '"+ barra + "' " +
                        " AND UBICACION = '" + ubic + "' " +
                        " AND ID_INVENTARIO_ENC="+ gl.idInvEnc +"" +
                        " AND ELIMINADO = 0 " +
                        " AND COMUNICADO = 'N' ";
            }else if(!barra.isEmpty()){
                tn = tn +" WHERE CODIGO_BARRA = '"+ barra + "' " +
                        " AND ID_INVENTARIO_ENC="+ gl.idInvEnc +" " +
                        " AND ELIMINADO = 0 " +
                        " AND COMUNICADO = 'N' ";
            }else if(!ubic.isEmpty()) {
                tn = tn+ " WHERE UBICACION = '" + ubic + "' " +
                         " AND ID_INVENTARIO_ENC="+ gl.idInvEnc +" " +
                         " AND ELIMINADO = 0" +
                         " AND COMUNICADO = 'N' ";
            }else {
                tn = tn + " WHERE ID_INVENTARIO_ENC="+ gl.idInvEnc +"" +
                          " AND ELIMINADO = 0" +
                          " AND COMUNICADO = 'N' ";;
            }

            if(consol==true){
                ss="SELECT 0, CODIGO_BARRA, UBICACION, SUM(CANTIDAD) FROM "+ tn +" GROUP BY CODIGO_BARRA, UBICACION";
            }else {
                if (gl.tipoInv==1){
                    ss="SELECT ID_REGISTRO, CODIGO_BARRA, UBICACION, CANTIDAD FROM "+ tn;
                }else if (gl.tipoInv==2){
                    ss="SELECT ID_INVENTARIO_DET, CODIGO_BARRA, UBICACION, CANTIDAD FROM "+ tn;
                }
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

            values.add("ID");
            values.add("CODIGO");
            values.add("UBICACION");
            values.add("CANTIDAD");

            ViewGroup.LayoutParams dlayoutParams = dgrid.getLayoutParams();
            dlayoutParams.width =((int) (cw*cc))+25;
            dgrid.setLayoutParams(dlayoutParams);

            //dgrid.setColumnWidth(cw);
            //dgrid.setStretchMode(GridView.NO_STRETCH);
            dgrid.setNumColumns(4);

            dadapter=new LA_Tablas2(this,dvalues);
            dgrid.setAdapter(dadapter);


            ViewGroup.LayoutParams layoutParams = grid.getLayoutParams();
            layoutParams.width =((int) (cw*cc))+25;
            grid.setLayoutParams(layoutParams);

            //grid.setColumnWidth(cw);
            //grid.setStretchMode(GridView.NO_STRETCH);
            grid.setNumColumns(4);

            adapter=new LA_Tablas(this,values);
            grid.setAdapter(adapter);

            pbar.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox("showData2: "+e);
        }


    }

    public void doDelete (View view){
        Cursor dt;
        String ss = "",codigo="",ubic="";
        int cc,rg;
        double cantidad = 0;

        try{

            if (idConteo!=0){
                if (gl.tipoInv==1){
                    ss=" SELECT CODIGO_BARRA, UBICACION, CANTIDAD FROM Inventario_ciego " +
                       " WHERE ID_REGISTRO = '"+ idConteo + "' ";
                }else if (gl.tipoInv==2){
                    ss=" SELECT CODIGO_BARRA, UBICACION, CANTIDAD FROM Inventario_detalle" +
                       " WHERE ID_INVENTARIO_DET = '"+ idConteo + "' " ;
                }

                dt=Con.OpenDT(ss);

                if (dt.getCount()!=0) {

                    if(dt.getCount()>0){
                        dt.moveToFirst();

                        ubic = dt.getString(1);
                        codigo = dt.getString(0);
                        cantidad = dt.getDouble(2);

                    }

                    if (dt!=null) dt.close();

                    if(!ubic.isEmpty() && !codigo.isEmpty()){

                        msgAskDelete("Seguro que desea eliminar el conteo del producto "+  codigo +
                                " en la ubicación "+ubic+" y con cantidad "+ cantidad +"");
                    }

                }else{
                    msgbox("No se pudo eliminar ningún conteo");
                }

            }else{
                msgbox("Debe seleccionar el conteo a eliminar");
            }


        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox("Error: " + new Object(){}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
        }
    }

    private void msgAskDelete(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setCancelable(false);
        dialog.setTitle("Tom");
        dialog.setMessage("¿"+msg+"?");

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
               eliminar();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialog.show();

    }

    private void eliminar(){
        try{

            if (gl.tipoInv==1){
                sql = " UPDATE INVENTARIO_CIEGO SET ELIMINADO = 1, COMUNICADO = 'N' " +
                        " WHERE id_inventario_det = " + idConteo ;
            }else{
                sql = " UPDATE INVENTARIO_DETALLE SET ELIMINADO = 1, COMUNICADO = 'N' " +
                        " WHERE id_inventario_det = " + idConteo ;
            }

            db.execSQL(sql);

        }catch (Exception e){
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox("Error: " + new Object(){}.getClass().getEnclosingMethod().getName() + " " + e.getMessage());
        }

        showData(scod);

    }

    //endregion

    //region Aux


    //endregion

    //region Dialogs


    //endregion

    //region Activity Events


    //endregion

}
