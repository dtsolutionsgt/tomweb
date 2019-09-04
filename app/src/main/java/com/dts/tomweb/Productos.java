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
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.dts.classes.clsInventario_detalleObj;
import com.dts.listadapt.LA_Tablas;
import com.dts.listadapt.LA_Tablas2;

import java.util.ArrayList;
import java.util.List;

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

    private Spinner spinnCon;

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
        spinnCon = (Spinner) findViewById(R.id.spinner3);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        cw = (int) ((displayMetrics.widthPixels-22)/5)-1;

        if(gl.tipoInv==1) scod = " INVENTARIO_CIEGO";  spinnCon.setVisibility(View.INVISIBLE);
        if(gl.tipoInv==2) scod = " ARTICULO";  spinnCon.setVisibility(View.VISIBLE);
        if(gl.tipoInv==3) scod = " INVENTARIO_TEORICO";  spinnCon.setVisibility(View.VISIBLE);

        setHandlers();
        spinner();

        //showData(scod);

    }

    //region Events

    public void doExit(View view) {
        finish();
    }

    public void doHelp(View view) {
        String tx;

        try{

            tx="-Busqueda: La busqueda se puede haccer por código de barra, nombre o ambos.\n\n" +
                    "-Regs: Muestra la cantidad de registros, o de conteos realizados.\n\n" +
                    "-Todos: Muestra todos los productos.\n\n" +
                    "-Contados: Muestra solo los productos que ya fueron agregados a un conteo.\n\n" +
                    "-No Contados: Muestra los productos que aún no han sido agregados a un conteo.";

            PopUp(tx);

        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }


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

            txtBarra.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start,int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start,int before, int count) {
                    showData(scod);
                }

            });

            txtNombre.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start,int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start,int before, int count) {
                    showData(scod);
                }

            });

            spinnCon.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    showData(scod);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    showData(scod);
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
        clsInventario_detalleObj InvDet = new clsInventario_detalleObj(this, Con, db);
        Cursor dt;
        String ss = "",barra,nomb,text,art="";
        int cc,rg,cnt,val=1;

        try {
            values.clear();
            dvalues.clear();

            barra = txtBarra.getText().toString().trim();
            nomb = txtNombre.getText().toString().trim();
            text = spinnCon.getSelectedItem().toString();

            if(gl.tipoInv==1) {

                if (!nomb.isEmpty() && !barra.isEmpty()) {
                    tn = tn + " WHERE CODIGO_BARRA = " + barra + " AND UBICACION =" + nomb + " AND ELIMINADO = 0 AND ID_INVENTARIO_ENC = "+gl.idInvEnc;
                } else if (!barra.isEmpty()) {
                    tn = tn + " WHERE CODIGO_BARRA = " + barra + " AND ELIMINADO = 0 AND ID_INVENTARIO_ENC = "+gl.idInvEnc;
                } else if (!nomb.isEmpty()) {
                    tn = tn + " WHERE UBICACION = " + nomb + " AND ELIMINADO = 0 AND ID_INVENTARIO_ENC = "+gl.idInvEnc;
                }else{
                    tn = tn + " WHERE ELIMINADO = 0 AND ID_INVENTARIO_ENC = "+gl.idInvEnc;
                }

                ss = "SELECT CODIGO_BARRA, UBICACION, CANTIDAD FROM " + tn;

            } else {

                art = tn;

                if(text.equals("Contados")){

                    tn = "select b.id_articulo, b.descripcion, b.codigo_barra, b.tipo_conteo from "+ art +" b inner join inventario_detalle a ON a.id_articulo = b.id_articulo AND a.codigo_barra = b.codigo_barra ";
                    val=2;

                }else if(text.equals("No Contados")){

                    tn = "select b.id_articulo, b.descripcion, b.codigo_barra, b.tipo_conteo from "+ art +" b where b.id_articulo not in (select id_articulo from inventario_detalle where id_inventario_enc = '"+ gl.idInvEnc +"') ";
                    val=3;

                }else if(text.equals("Todos")){

                    tn = tn + " WHERE id_empresa ="+ gl.empresa;
                    val=1;

                }

                if (!nomb.isEmpty() && !barra.isEmpty()) {
                    if(val==1){
                        tn = tn + " AND CODIGO_BARRA = '" + barra + "' AND DESCRIPCION ='" + nomb + "'";
                    }else {
                        if(val==2)tn = tn + " WHERE b.CODIGO_BARRA = '" + barra + "' AND b.DESCRIPCION ='" + nomb + "' AND a.ID_INVENTARIO_ENC = '"+ gl.idInvEnc +"' AND a.ELIMINADO=0 GROUP BY b.id_articulo";
                        if(val==3)tn = tn + " AND CODIGO_BARRA = '" + barra + "' AND DESCRIPCION ='" + nomb + "' GROUP BY b.id_articulo";
                    }

                } else if (!barra.isEmpty()) {
                    if(val==1){
                        tn = tn + " AND CODIGO_BARRA = '" + barra + "'";
                    }else {
                        if(val==2)tn = tn + " WHERE  b.CODIGO_BARRA = '" + barra + "' AND a.ID_INVENTARIO_ENC = '"+ gl.idInvEnc +"' AND a.ELIMINADO=0 GROUP BY b.id_articulo";
                        if(val==3)tn = tn + " AND  CODIGO_BARRA = '" + barra + "' GROUP BY b.id_articulo";
                    }

                } else if (!nomb.isEmpty()) {
                    if(val==1){
                        tn = tn + " AND DESCRIPCION = '" + nomb + "'";
                    }else {
                        if(val==2)tn = tn + " WHERE  b.DESCRIPCION = '" + nomb + "' AND a.ID_INVENTARIO_ENC = '"+ gl.idInvEnc +"' AND a.ELIMINADO=0 GROUP BY b.id_articulo";
                        if(val==3)tn = tn + " AND DESCRIPCION = '" + nomb + "' GROUP BY b.id_articulo";
                    }

                }else {
                    if(!text.equals("Todos")){

                        if(val==2)  tn = tn + " AND a.ID_INVENTARIO_ENC = '"+ gl.idInvEnc +"' GROUP BY b.id_articulo";
                        if(val==3)  tn = tn + " GROUP BY b.id_articulo";
                    }

                }

                if(val==1) ss = "SELECT ID_ARTICULO, DESCRIPCION, CODIGO_BARRA, TIPO_CONTEO FROM " + tn;
                else ss = tn;

            }

            dt=Con.OpenDT(ss);

            cc = dt.getColumnCount();
            rg = dt.getCount();
            regs.setText(""+rg);

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

            if (dt!=null) dt.close();

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
            msgbox("showData: "+e);
        }


    }

    //endregion

    //region Aux

    public void spinner(){
        try{
            List<String> list = new ArrayList<String>();
            list.add("Todos");
            list.add("Contados");
            list.add("No Contados");
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnCon.setAdapter(dataAdapter);

            spinnCon.setAdapter(dataAdapter);
        }catch (Exception e){}

    }

    //endregion

    //region Dialogs


    //endregion

    //region Activity Events


    //endregion

}
