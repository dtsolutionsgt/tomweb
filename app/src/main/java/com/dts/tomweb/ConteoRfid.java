package com.dts.tomweb;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dts.base.clsClasses;
import com.dts.classes.clsInventario_ciego_RfidObj;
import com.dts.classes.clsInventario_detalleObj;
import com.dts.classes.clsRegistro_handheldObj;
import com.dts.listadapt.LA_RFID;
import com.dts.listadapt.LA_Tablas;

import com.dts.listadapt.LA_Tablas2;
import com.zebra.rfid.api3.ACCESS_OPERATION_CODE;
import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.api3.TriggerInfo;

import java.util.ArrayList;

public class ConteoRfid extends PBase {

    private GridView grid,dgrid;
    //private Spinner spin,spinf;
    private ProgressBar pbar;
    private EditText txtBarra;
    private EditText txtUbic;
    private TextView regs;
    private CheckBox cb;

    private LA_Tablas adapter;
    private LA_Tablas2 dadapter;
    private LA_RFID dadapter_rfid;

    private int cw;
    private String scod;
    private boolean consol;

    /*********elementos de RFID ************/
    public static Readers readers;
    private static ArrayList<ReaderDevice> availableRFIDReaderList;
    private static ReaderDevice readerDevice;
    private static RFIDReader reader;
    private static String TAG = "DEMO";
    TextView textView;
    private EventHandler eventHandler;

    private ArrayList<String> spinlist = new ArrayList<String>();
    private ArrayList<String> values=new ArrayList<String>();
    private ArrayList<String> dvalues=new ArrayList<String>();
    private ArrayList<clsClasses.clsInventario_ciego_rfid> datos_rfid = new ArrayList<clsClasses.clsInventario_ciego_rfid>();
    public clsClasses.clsInventario_ciego_rfid listaTag;

    /*********************************************/
    /*********  elementos para guardar ***********/
    private String Ubic, Cod, tipoArt, barra, desc,ccod;
    private Double canti;
    private Integer result=0, resta;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conteo_rfid);
        textView = findViewById(R.id.TagText);


        /************************************************/
        /******** variables y constantes rfid **********/

        if (readers == null) {
            readers = new Readers(this, ENUM_TRANSPORT.SERVICE_SERIAL);
        }

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    if (readers != null) {
                        if (readers.GetAvailableRFIDReaderList() != null) {
                            availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                            if (availableRFIDReaderList.size() != 0) {
                                // get first reader from list
                                readerDevice = availableRFIDReaderList.get(0);
                                reader = readerDevice.getRFIDReader();
                                if (!reader.isConnected()) {
                                    // Establish connection to the RFID Reader
                                    reader.connect();
                                    ConfigureReader();
                                    return true;
                                }
                            }
                        }
                    }
                } catch (InvalidUsageException e) {
                    e.printStackTrace();
                } catch (OperationFailureException e) {
                    e.printStackTrace();
                    Log.d(TAG, "OperationFailureException " + e.getVendorMessage());
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean)
            {
                super.onPostExecute(aBoolean);
                if (aBoolean) {
                    //Toast.makeText(getApplicationContext(), "Reader Connected", Toast.LENGTH_LONG).show();
                    textView.setText("RFID listo.");
                }
            }
        }.execute();


        /*************************************************************/
        /********** variables y constantes de grid *******************/
        super.InitBase(savedInstanceState);
        addlog("ConteoRfid",""+du.getActDateTime(),gl.nombreusuario);

        grid = (GridView) findViewById(R.id.gridview1);
        dgrid = (GridView) findViewById(R.id.gridview2);
        pbar=(ProgressBar) findViewById(R.id.progressBar);pbar.setVisibility(View.INVISIBLE);
        txtBarra = (EditText) findViewById(R.id.txtBarra);
        txtUbic = (EditText) findViewById(R.id.txtNombre);
        regs = (TextView) findViewById(R.id.txtRegs);
        cb = (CheckBox) findViewById(R.id.cbConsolidar);

        if(gl.tipoInv==1) scod = " INVENTARIO_CIEGO_RFID";
        if(gl.tipoInv==2 || gl.tipoInv==3) scod = " INVENTARIO_DETALLE";

        setHandlers();

        showData(scod);
    }


    /*********************************************************************/
    /************** configuración RFID  **********************************/

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        //Toast.makeText(getApplicationContext(),"Cerando RFID",Toast.LENGTH_LONG).show();
        super.onDestroy();
        try {
            if (reader != null)
            {
                reader.Events.removeEventsListener(eventHandler);
                reader.disconnect();
                Toast.makeText(getApplicationContext(), "RFID Desconectado.", Toast.LENGTH_LONG).show();
                reader = null;
                readers.Dispose();
                readers = null;
            }
        }
        catch (InvalidUsageException e)
        {
            e.printStackTrace();
        }
        catch (OperationFailureException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //return;
    }

    private void ConfigureReader() {
        if (reader.isConnected()) {

            TriggerInfo triggerInfo = new TriggerInfo();
            triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
            triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
            try {
                // receive events from reader
                if (eventHandler == null)
                    eventHandler = new EventHandler();
                reader.Events.addEventsListener(eventHandler);
                reader.Events.setHandheldEvent(true);
                reader.Events.setTagReadEvent(true);
                reader.Events.setAttachTagDataWithReadEvent(false);
                reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true);
                reader.Config.setStartTrigger(triggerInfo.StartTrigger);
                reader.Config.setStopTrigger(triggerInfo.StopTrigger);
                System.out.println("\nReader ID: "+ reader.ReaderCapabilities.ReaderID.getID());
                //textView.setText("\nReader ID: "+ reader.ReaderCapabilities.ReaderID.getID());

            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (reader != null)
            {
                reader.Events.removeEventsListener(eventHandler);
                reader.disconnect();
                Toast.makeText(getApplicationContext(), "RFID Desconectado.", Toast.LENGTH_LONG).show();
                reader = null;
                readers.Dispose();
                readers = null;
            }
        }
        catch (InvalidUsageException e)
        {
            e.printStackTrace();
        }
        catch (OperationFailureException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void guardar(View view) {

        ArrayList<String> arrlist = new ArrayList<String>();
        int cantidad =0;
        String etiqueta ="";
        int ubicacion = 0;

        for(int i=0;i<dadapter.getCount();i++){
            if(i % 3 == 0){
                etiqueta =(String) dadapter.getItem(i);
                Log.d(TAG, "valor " + etiqueta);
            }
        }

    }

    public class EventHandler implements RfidEventsListener {

        @SuppressLint("WrongViewCast")
        @Override
        public void eventReadNotify(RfidReadEvents e) {

            //datos_rfid.clear();


            TagData[] myTags = reader.Actions.getReadTags(30);

            if (myTags != null) {

                for (int index = 0; index < myTags.length; index++) {
                    Log.d(TAG, "Tag ID " + myTags[index].getTagID());
                    //Log.d(TAG, "Tag evento " + e);
                   final String tag = myTags[index].getTagID();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            /*listaTag = new clsClasses.clsInventario_ciego_rfid();
                            listaTag.codigo_barra = tag;
                            listaTag.cantidad= 1;
                            listaTag.ubicacion = "1";
                            datos_rfid.add(listaTag);
                            dadapter_rfid = new LA_RFID(getApplicationContext(),datos_rfid);
                            dgrid.setAdapter(dadapter_rfid);
                            dadapter_rfid.notifyDataSetChanged();*/

                            insertaConteo(tag);

                            dvalues.add(tag);
                            dvalues.add("1");
                            dvalues.add("1");
                            dadapter= new LA_Tablas2(getApplicationContext(),dvalues);
                            dgrid.setAdapter(dadapter);
                            dadapter.notifyDataSetChanged();


                        }
                    });

                    if (myTags[index].getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ && myTags[index].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS)
                    {
                        if (myTags[index].getMemoryBankData().length() > 0) {
                            Log.d(TAG, " Mem Bank Data " + myTags[index].getMemoryBankData());

                        }
                    }
                }
            }
        }

        @Override
        public void eventStatusNotify(RfidStatusEvents e) {

            Log.d(TAG, "Status Notification: " + e.StatusEventData.getStatusEventType());
            if (e.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT)
            {
                if (e.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED)
                {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                reader.Actions.Inventory.perform();
                            } catch (InvalidUsageException e) {
                                e.printStackTrace();
                            } catch (OperationFailureException e)
                            {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.execute();
                }

                if (e.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED)
                {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                reader.Actions.Inventory.stop();
                            }
                            catch (InvalidUsageException e)
                            {
                                e.printStackTrace();
                            }
                            catch (OperationFailureException e)
                            {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.execute();
                }

            }
        }

    }

    public void insertaConteo(String tag){
        clsInventario_ciego_RfidObj InvCiego = new clsInventario_ciego_RfidObj(this, Con, db);
        clsClasses.clsInventario_ciego_rfid item= new clsClasses.clsInventario_ciego_rfid();
        clsInventario_detalleObj InvDet = new clsInventario_detalleObj(this, Con, db);
        clsClasses.clsInventario_detalle itemDeta= clsCls.new clsInventario_detalle();
        clsRegistro_handheldObj regHH = new clsRegistro_handheldObj(this, Con, db);

        Long sfecha;
        String  ff,ffe;
        Integer fecha;
        String codigo_tag = tag;
        Integer cant = 1;


        try{

            regHH.fill();
            gl.IDregistro = regHH.first().id_registro;
            //Cod = Codigo.getText().toString().trim();
            Cod = codigo_tag;

            Ubic = "1";

            /*if(Ubicacion.getText().toString().isEmpty()){
                Ubic = "1";
            }else {
                Ubic = Ubicacion.getText().toString().trim();
            }*/

            sfecha=du.getActDate();
            ff = "20"+sfecha;
            ffe= ff.substring(0,8);

            if(gl.tipoInv!=1){
                if(tipoArt.equals("S")){
                    canti = 1.0;
                }else {
                   /* if(Cantidad.getText().toString().isEmpty()){
                        msgbox("Ingrese la cantidad");
                        return;
                    }else {
                        canti = Double.parseDouble(Cantidad.getText().toString());
                    }*/
                   canti = Double.parseDouble(String.valueOf(cant));
                }
            }else {
                /*if(Cantidad.getText().toString().isEmpty()){
                    msgbox("Ingrese la cantidad");
                    return;
                }else {
                    canti = Double.parseDouble(Cantidad.getText().toString());
                }*/
                canti = Double.parseDouble(String.valueOf(cant));
            }

          /*  if(Codigo.getText().toString().isEmpty()){
                msgbox("Ingrese el codigo");
                return;
            }*/

            if(canti==0){
                msgbox("Ingrese una cantidad mayor a 0");
                return;
            }


            if(gl.tipoInv==1) {

            /*    Barra.setText(Codigo.getText().toString());
                barra = Barra.getText().toString().trim();*/
            //barra = listaTag.codigo_barra;
                barra = codigo_tag;

                item.id_inventario_enc =  gl.idInvEnc;
                item.codigo_barra = barra;
                item.cantidad = canti;
                item.comunicado = "N";
                item.ubicacion = Ubic;
                item.id_operador = gl.userid;
                item.fecha = ffe;
                item.id_registro = gl.IDregistro;
                item.eliminado = 0;

                InvCiego.add(item);

            }else if(gl.tipoInv==2 || gl.tipoInv==3){

                itemDeta.id_inventario_enc= gl.idInvEnc;
                itemDeta.id_articulo = Cod;
                itemDeta.ubicacion = Ubic;
                itemDeta.cantidad = canti;
                itemDeta.codigo_barra = barra;
                itemDeta.comunicado = "N";
                itemDeta.id_operador = gl.userid;
                itemDeta.fecha = ffe;
                itemDeta.id_registro = gl.IDregistro;
                item.eliminado = 0;

                InvDet.add(itemDeta);
            }
            Toast.makeText(this, "Agregado Correctamente", Toast.LENGTH_LONG).show();
        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox("Error: "+e);
        }
    }


    /*************************************************/
    /********** configuración del grid ***************/

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

            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (cb.isChecked()==true) consol = true; showData(scod);
                    if (cb.isChecked()==false) consol = false; showData(scod);
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
                        Object lvObj = dgrid.getItemAtPosition(position);
                        String item = (String) lvObj;

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

    private void showData(String tn) {
        Cursor dt;
        String ss = "",barra,ubic;
        int cc,rg;

        try {
            //dvalues.clear();
            values.clear();

           /* barra = txtBarra.getText().toString().trim();
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
            }*/


          /*  dt=Con.OpenDT(ss);
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
            if (dt!=null) dt.close();*/

            values.add("CODIGO");
            values.add("UBICACION");
            values.add("CANTIDAD");

            ViewGroup.LayoutParams layoutParams = grid.getLayoutParams();
            //layoutParams.width =((int) (cw*cc))+25;
            layoutParams.width =((int) (cw*3))+25;
            grid.setLayoutParams(layoutParams);
            grid.setNumColumns(3);

            adapter=new LA_Tablas(this,values);
            grid.setAdapter(adapter);


            ViewGroup.LayoutParams dlayoutParams = dgrid.getLayoutParams();
            //dlayoutParams.width =((int) (cw*cc))+25;
            dlayoutParams.width =((int) (cw*3))+25;
            dgrid.setLayoutParams(dlayoutParams);
            dgrid.setNumColumns(3);

            dadapter=new LA_Tablas2(this,dvalues);
            dgrid.setAdapter(dadapter);

            //listaTag = new clsClasses.clsInventario_ciego_rfid();
            //datos_rfid.add(listaTag);

            //dadapter= new LA_RFID(this,datos_rfid);
            //dgrid.setAdapter(dadapter);





            pbar.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            msgbox("showData2: "+e);
        }


    }
}
