package com.dts.tomweb;

import static com.dts.application.Application.UNIQUE_TAGS_CSV;
import static com.dts.application.Application.inventoryList;
import static com.dts.application.Application.inventoryMode;
import static com.dts.application.Application.matchingTagsList;
import static com.dts.application.Application.memoryBankId;
import static com.dts.application.Application.missingTagsList;
import static com.dts.application.Application.tagListMap;
import static com.dts.application.Application.tagsReadForSearch;
import static com.dts.application.Application.tagsReadInventory;
import static com.dts.application.Application.unknownTagsList;
import static com.dts.application.Application.UNIQUE_TAGS;
import static com.dts.application.Application.TOTAL_TAGS;
import static com.dts.application.Application.missedTags;
import static com.dts.application.Application.matchingTags;
import static com.dts.rfid.RFIDController.channelIndex;
import static com.dts.rfid.RFIDController.pc;
import static com.dts.rfid.RFIDController.phase;
import static com.dts.rfid.RFIDController.rssi;
import static com.dts.rfid.RFIDController.toneGenerator;



import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dts.base.clsClasses;
import com.dts.classes.clsInventario_ciegoObj;
import com.dts.classes.clsInventario_detalleObj;
import com.dts.classes.clsRegistro_handheldObj;
import com.dts.inventory.InventoryListItem;
import com.dts.listadapt.LA_RFID;
import com.dts.listadapt.LA_Tablas;
import com.dts.listadapt.LA_Tablas2;
import com.zebra.rfid.api3.ACCESS_OPERATION_CODE;
import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS;
import com.zebra.rfid.api3.BEEPER_VOLUME;
import com.zebra.rfid.api3.Constants;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class ConteoRfid extends PBase  {

    private ListView lvConteoRFID;
    private ProgressBar pbar;
    private EditText txtBarra;
    private EditText txtUbic;
    private TextView regs;
    private CheckBox cb;

    private LA_Tablas adapter;
    private LA_Tablas2 dadapter;
    private LA_RFID dadapter_rfid;
    private LA_RFID dadapter_rfid2;

    private int cw;
    private String scod;
    private boolean consol;
    private Integer result=0;

    /*********elementos de RFID ************/
    public static Readers readers;
    private static ArrayList<ReaderDevice> availableRFIDReaderList;
    private static ReaderDevice readerDevice;
    private static RFIDReader reader;
    private static String TAG = "DEMO";
    TextView textView;
    private EventHandler eventHandler;

    public ArrayList<clsClasses.clsInventario_ciego_rfid> dvalues_rfid = new ArrayList<clsClasses.clsInventario_ciego_rfid>();
    ArrayList<String> codigos = new ArrayList<String>();
    ArrayList<String> lista_limpia = new ArrayList<String>();
    //ArrayList<Map.Entry<String, Integer>> lista_limpia2 = new ArrayList<Map.Entry<String, Integer>>();


    /*********************************************/
    /*********  elementos para guardar ***********/
    private String Ubic, Cod, tipoArt, barra;
    private Double canti;
    String currentTime;
    clsRegistro_handheldObj regHH;

    Integer contador = 0;

    //Beeper
    public static BEEPER_VOLUME beeperVolume = BEEPER_VOLUME.HIGH_BEEP;
    public static BEEPER_VOLUME sledBeeperVolume = BEEPER_VOLUME.HIGH_BEEP;



    public Timer tbeep;
    /**
     * method to start a timer task to beep for locate functionality and configure the ON OFF duration.
     */
    public Timer locatebeep;


    //for beep and LED
    private boolean beepON = false;
    private boolean beepONLocate = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_conteo_rfid);
        textView = findViewById(R.id.TagText);
        pbar = findViewById(R.id.progressBar);

        codigos.clear();
        dvalues_rfid.clear();


        /************************************************/
        /******** variables y constantes rfid **********/

        if (readers == null) {
            readers = new Readers(this, ENUM_TRANSPORT.SERVICE_SERIAL);
        }


        /*********************************************************/
        /*********** variables para tono y volumne del scanner ***/
        int streamType = AudioManager.STREAM_DTMF;
        toneGenerator = new ToneGenerator(streamType, 90);


        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    if (readers != null ) {
                        if (readers.GetAvailableRFIDReaderList() != null) {
                            availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                            if (availableRFIDReaderList.size() != 0) {
                                // get first reader from list
                                readerDevice = availableRFIDReaderList.get(0);
                                reader = readerDevice.getRFIDReader();
                                if (!reader.isConnected()  && gl != null) {
                                    // Establish connection to the RFID Reader
                                    reader.connect();
                                    ConfigureReader();
                                    return true;
                                }
                                else
                                {
                                    return false;
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
                    textView.setText("Lectura RFID lista.");
                }
                else{
                    textView.setText("Se ha perdido la comunicación al RFID.");
                }
            }
        }.execute();


        /*************************************************************/
        /********** variables y constantes de grid *******************/

        try{

            super.InitBase(savedInstanceState);
            addlog("ConteoRfid",""+du.getActDateTime(),gl.nombreusuario);

            lvConteoRFID = (ListView) findViewById(R.id.lvConteoRFID);
            txtBarra = (EditText) findViewById(R.id.txtBarra);
            txtUbic = (EditText) findViewById(R.id.txtNombre);
            regs = (TextView) findViewById(R.id.txtRegs);
            cb = (CheckBox) findViewById(R.id.cbConsolidar);

            /*************************************************/
            /**** obtengo la data del registro handheld ******/
            regHH = new clsRegistro_handheldObj(this, Con, db);
            regHH.fill();
            gl.IDregistro = regHH.first().id_registro;


            if(gl.tipoInv==1) scod = " INVENTARIO_CIEGO";
            if(gl.tipoInv==2 || gl.tipoInv==3) scod = " INVENTARIO_DETALLE";
            if(gl.tipoInv==5) scod = " INVENTARIO_CIEGO_RFID";


            if(scod != null ){
                //showData(scod);
                pbar.setVisibility(View.INVISIBLE);
            }
            else{
                pbar.setVisibility(View.INVISIBLE);
            }

            lvConteoRFID.setFocusable(true);

            //scod ="INVENTARIO_CIEGO";
            //setHandlers();


        }catch (Exception e){
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }
    }

    /*********************************************************************/
    /************** configuración RFID  **********************************/

    @Override
    public void onBackPressed() {
        msgAskExit("Salir de RFID?");
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
        CerrarRFIF();
    }

    public void doNext(View view) {

        gl.validaLicDB=10;
        //Cerrar la comunicación
        //#GT16062022: CerrarRFIF se aplica dentro de getCampos, con data para sincronizar
        //sin nada que sincronizar, se queda abierto el RFID porque aun estamos en el layout
        //CerrarRFIF();
        getCampos();
        //ComWS();
    }

    public class EventHandler implements RfidEventsListener {

        @SuppressLint("WrongViewCast")
        @Override
        public void eventReadNotify(RfidReadEvents e) {

            TagData[] myTags = reader.Actions.getReadTags(30);
            //TagDataArray myTags = reader.Actions.getReadTagsEx(30);

            if (myTags != null) {

                for (int index = 0; index < myTags.length; index++) {
                    Log.d(TAG, "Tag ID " + myTags[index].getTagID());
                    //Log.d(TAG, "Tag evento " + e);
                   final String tag = myTags[index].getTagID();
                   final int tag_ = index;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //FUNCION QUE GUARDA TAG EN TIEMPO REAL A LA MEMORIA INTERNA
                              //insertaConteo(tag);

                            //new MatchingTagsResponseHandlerTask(myTags[tag_]).execute();

                            new ResponseHandlerTask(myTags[tag_]).execute();

                            /*if(!codigos.contains(tag)){

                                clsClasses.clsInventario_ciego_rfid items_nuevo= new clsClasses.clsInventario_ciego_rfid();
                                items_nuevo.codigo_barra = tag;
                                items_nuevo.ubicacion = "1";
                                items_nuevo.cantidad = 1;
                                //items_nuevo.id_inventario_enc=1;
                                //items_nuevo.eliminado = 0;
                                //items_nuevo.comunicado = "";
                                codigos.add(items_nuevo.codigo_barra);
                                dvalues_rfid.add(items_nuevo);
                                dadapter_rfid = new LA_RFID(getApplicationContext(), dvalues_rfid);
                                lvConteoRFID.setAdapter(dadapter_rfid);
                            }*/
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
                                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"failed_triger_rfid"+ currentTime );
                            } catch (OperationFailureException e)
                            {
                                e.printStackTrace();
                                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"failed_triger_rfid"+ currentTime );
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
                                Log.d(TAG, "termina lectura: ");
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

                   runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        GuardarLista(scod);
                        showData(scod);
                    }
                });

            }

        }

    }

    public void insertaConteo(String tag){

        //clsInventario_ciego_RfidObj InvCiegoRfid = new clsInventario_ciego_RfidObj(this, Con, db);
        //clsRegistro_handheldObj regHH = new clsRegistro_handheldObj(this, Con, db);
        //clsClasses.clsInventario_ciego_rfid item= new clsClasses.clsInventario_ciego_rfid();

        /********** data de un inventario que no es ciego *************************************/
        clsInventario_detalleObj InvDet = new clsInventario_detalleObj(this, Con, db);
        clsClasses.clsInventario_detalle itemDeta= clsCls.new clsInventario_detalle();

        /********** objetos de un inventario que es ciego o usa rfid *************************/
        clsInventario_ciegoObj InvCiego = new clsInventario_ciegoObj(this, Con, db);
        clsClasses.clsInventario_ciego items;

        Long sfecha;
        String  ff,ffe, ss;
        Integer rg;
        Cod = tag;
        Integer cant = 1;
        Cursor dt;

        currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        try{

            /******lo valido en el oncreate, aca se repetiria n veces y lo manejara lento -- ****/
            //regHH.fill();
            //gl.IDregistro = regHH.first().id_registro;
            Ubic = "1";
            items = new clsClasses.clsInventario_ciego();

            //ss="SELECT CODIGO_BARRA FROM INVENTARIO_CIEGO WHERE ID_INVENTARIO_ENC="+ gl.idInvEnc +" AND ELIMINADO = 0 AND CODIGO_BARRA=" + '"+ barra + "';
            ss= "SELECT CODIGO_BARRA FROM INVENTARIO_CIEGO WHERE CODIGO_BARRA = '"+ Cod + "' ";

            dt=Con.OpenDT(ss);
            rg = dt.getCount();

            sfecha=du.getActDate();
            ff = "20"+sfecha;
            ffe= ff.substring(0,8);

            if(gl.tipoInv!=1){
                if(tipoArt.equals("S")){
                    canti = 1.0;
                }else {
                   canti = Double.parseDouble(String.valueOf(cant));
                }
            }else {

                canti = Double.parseDouble(String.valueOf(cant));
            }

            if(canti==0){
                msgbox("Ingrese una cantidad mayor a 0");
                return;
            }

            if(gl.tipoInv==0) {

                barra = Cod;
                items.id_inventario_enc = gl.idInvEnc;
                items.codigo_barra = barra;
                items.cantidad= canti;
                items.comunicado = "N";
                items.ubicacion = Ubic;
                items.id_operador = gl.userid;
                //items.fecha = ffe + " " + currentTime;
                items.fecha = ffe;
                items.id_registro = gl.IDregistro;
                items.eliminado = 0;

                try {

                    if(rg == 0){
                        //InvCiegoRfid.add(item);
                        InvCiego.add(items);
                    }
                    else {
                        /*sql="update Inventario_ciego_rfid set cantidad = cantidad + 1 WHERE CODIGO_BARRA = '"+ barra + "' ";*/
                        sql="update Inventario_ciego set cantidad = cantidad + 1 WHERE CODIGO_BARRA = '"+ barra + "' ";
                        db.execSQL(sql);
                    }

                } catch (Exception e) {
                    addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "error_insert_inv_ciego " + du.getActDate());
                    msgbox("Error: "+e.getMessage());
                }

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
                itemDeta.eliminado =0;
                InvDet.add(itemDeta);
            }

        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "error_insert_conteo " + du.getActDate());
            msgbox("Error: "+e);
        }
    }

    private void msgAskExit(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setCancelable(false);
        dialog.setTitle("Tom");
        dialog.setMessage("¿"+msg+"?");

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                CerrarRFIF();
                finish();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialog.show();

    }

    public void doExit(View view) {
        CerrarRFIF();
        finish();
    }

    public void CerrarRFIF(){
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
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"ERROR_RFID_DISCONNECT_1 " + du.getActDate() );
        }
        catch (OperationFailureException e)
        {
            e.printStackTrace();
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"ERROR_RFID_DISCONNECT_2 " + du.getActDate() );
        }
        catch (Exception e)
        {
            e.printStackTrace();
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"ERROR_RFID_DISCONNECT_3"+ du.getActDate() );
        }

        //finish();
    }


    /*************************************************/
    /********** configuración del grid ***************/

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

            lvConteoRFID.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    try {
                        Object lvObj = lvConteoRFID.getItemAtPosition(position);
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

            lvConteoRFID.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                    try {
                        Object lvObj = lvConteoRFID.getItemAtPosition(position);
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
        String ss = "";
        int cc,rg;
        //currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        new clsClasses.clsInventario_ciego_rfid();
        clsClasses.clsInventario_ciego_rfid items;

        try {

            if (contador == 0){

                dvalues_rfid.clear();
                lvConteoRFID.setAdapter(null);


            tn ="INVENTARIO_CIEGO";

            tn = tn +" WHERE ID_INVENTARIO_ENC="+ gl.idInvEnc +" AND ELIMINADO = 0";
            ss="SELECT CODIGO_BARRA, UBICACION, CANTIDAD FROM "+ tn;

            dt=Con.OpenDT(ss);
            rg = dt.getCount();
            contador = dt.getCount();
            if(rg>0){
                regs.setText(""+rg);
            }

            dt.moveToFirst();
            while (!dt.isAfterLast()) {
                items= new clsClasses.clsInventario_ciego_rfid();
                items.codigo_barra = dt.getString(0);
                items.ubicacion = dt.getString(1);
                items.cantidad = Double.parseDouble(dt.getString(2));
                dvalues_rfid.add(items);
                dt.moveToNext();
            }

            if (dt!=null) dt.close();

            dadapter_rfid= new LA_RFID(this,dvalues_rfid);
            lvConteoRFID.setAdapter(dadapter_rfid);
            pbar.setVisibility(View.INVISIBLE);


            }


        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"ERROR_SHOWDATA_RFID " + du.getActDate());
            msgbox("showData: "+e.getMessage());
        }


    }

    private void GuardarLista(String tn){


            /********** data de un inventario que no es ciego *************************************/
            //clsInventario_detalleObj InvDet = new clsInventario_detalleObj(this, Con, db);
            //clsClasses.clsInventario_detalle itemDeta= clsCls.new clsInventario_detalle();

            /********** objetos de un inventario que es ciego o usa rfid *************************/
            clsInventario_ciegoObj InvCiego = new clsInventario_ciegoObj(this, Con, db);
            clsClasses.clsInventario_ciego items;
            clsClasses.clsInventario_ciego_rfid item_rfid;

            Long sfecha;
            String  ff,ffe, ss;
            Integer rg;
            //Cod = "dsd";  //TAG
            Integer cant = 1;
            Cursor dt;

            currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            sfecha=du.getActDate();
            ff = "20"+sfecha;
            ffe= ff.substring(0,8);

            try{

                /******lo valido en el oncreate, aca se repetiria n veces y lo manejara lento ****/
                //regHH.fill();
                //gl.IDregistro = regHH.first().id_registro;
                Ubic = "1";
                items = new clsClasses.clsInventario_ciego();

                if(gl.tipoInv!=1){
                    /*if(tipoArt.equals("S")){
                        canti = 1.0;
                    }else {
                        canti = Double.parseDouble(String.valueOf(cant));
                    }*/
                    canti = 1.0;

                }else {

                    canti = Double.parseDouble(String.valueOf(cant));
                }


                if(gl.tipoInv==1) {

                    Set<Map.Entry<String, Integer>> entrySet
                            = inventoryList.entrySet();

                    Map.Entry<Integer, String>[] entryArray
                            = entrySet.toArray(
                            new Map.Entry[entrySet.size()]);


                    //GT16052022: itero la lista final que se usó en memoria
                    for (int x = 0; x < inventoryList.size(); x++) {

                        String p_codigo_barra ="";
                        p_codigo_barra = String.valueOf(entryArray[x].getKey());

                        if (!lista_limpia.contains(p_codigo_barra)){

                            lista_limpia.add(p_codigo_barra);

                            ss= "SELECT CODIGO_BARRA FROM INVENTARIO_CIEGO WHERE CODIGO_BARRA = '"+ p_codigo_barra + "' ";
                            dt=Con.OpenDT(ss);
                            rg = dt.getCount();

                            barra = p_codigo_barra;
                            items.id_inventario_enc = gl.idInvEnc;
                            items.codigo_barra = barra;
                            //items.cantidad = canti;
                            //items.cantidad = p.cantidad;
                            items.cantidad = 1;
                            items.comunicado = "N";
                            items.ubicacion = Ubic;
                            items.id_operador = gl.userid;
                            //items.fecha = ffe + " " + currentTime;
                            items.fecha = ffe;
                            items.id_registro = gl.IDregistro;
                            items.eliminado = 0;


                            item_rfid = new clsClasses.clsInventario_ciego_rfid();
                            item_rfid.codigo_barra =  p_codigo_barra;
                            dvalues_rfid.add(item_rfid);

                            try {

                                if (rg == 0) {
                                    InvCiego.add(items);
                                } else {
                                    /*sql="update Inventario_ciego_rfid set cantidad = cantidad + 1 WHERE CODIGO_BARRA = '"+ barra + "' ";*/
                                    //sql = "update Inventario_ciego set cantidad = cantidad + 1 WHERE CODIGO_BARRA = '" + barra + "' ";
                                    //db.execSQL(sql);

                                }

                            } catch (Exception e) {
                                addlog(new Object() {
                                }.getClass().getEnclosingMethod().getName(), e.getMessage(), "error_insert_inv_ciego " + du.getActDate());
                                msgbox("Error: " + e.getMessage());
                            }
                        }

                    }
                }

        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "error_insert_inv_ciego " + du.getActDate());
            //msgbox("Error: "+e.getMessage());
        }
    }

    public void getCampos(){
        try{

            if(dadapter_rfid != null){

                Integer registros = dadapter_rfid.getCount();

                if(registros <=0){
                    msgAskContinue("No hay data con rfid registrada, ¿Seguro que desea continuar?");
                    result = 1; return;
                }else{

                    CerrarRFIF();
                    ComWS();
                }
            }else{
                msgbox("No hay data que sincronizar");
            }

        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox("Error getCampos: "+e);
        }
    }

    public void ComWS(){
        startActivity(new Intent(this, ComWS.class));
    }

    private void msgAskContinue(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setCancelable(false);
        dialog.setTitle("Tom");
        dialog.setMessage(msg);

        dialog.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ComWS();
            }
        });

        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialog.show();

    }

    private class MatchingTagsResponseHandlerTask extends AsyncTask<Void, Void, Boolean> {

        private TagData tagData;
        private InventoryListItem inventoryItem;
        private InventoryListItem oldObject;
        private String memoryBank;
        private String memoryBankData;

        public MatchingTagsResponseHandlerTask(TagData tagData) {
            this.tagData = tagData;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            runOnUiThread(new Runnable() {
                @SuppressLint("WrongConstant")
                @Override
                public void run() {
                    boolean added = false;

                    try {
                        if (inventoryList.containsKey(tagData.getTagID())) {
                            inventoryItem = new InventoryListItem(tagData.getTagID(), 1, null, null, null, null, null, null);
                            int index = inventoryList.get(tagData.getTagID());
                            if (index >= 0) {
                                if (tagListMap.containsKey(tagData.getTagID()))
                                    tagsReadInventory.get(index).setTagStatus("MATCH");
                                else
                                    tagsReadInventory.get(index).setTagStatus("UNKNOWN");
                                TOTAL_TAGS++;
                                //Tag is already present. Update the fields and increment the count
                                if (tagData.getOpCode() != null)
                                    if (tagData.getOpCode().toString().equalsIgnoreCase("ACCESS_OPERATION_READ")) {
                                        memoryBank = tagData.getMemoryBank().toString();
                                        memoryBankData = tagData.getMemoryBankData().toString();
                                    }
                                if (memoryBankId == 1) {  //matching tags
                                    if (tagListMap.containsKey(tagData.getTagID()) && !matchingTagsList.contains(tagsReadInventory.get(index))) {
                                        matchingTagsList.add(tagsReadInventory.get(index));
                                        tagsReadForSearch.add(tagsReadInventory.get(index));
                                        added = true;
                                    }
                                } else if (memoryBankId == 2 && tagListMap.containsKey(tagData.getTagID())) {
                                    if (missingTagsList.contains(tagsReadInventory.get(index))) {
                                        missingTagsList.remove(tagsReadInventory.get(index));
                                        tagsReadForSearch.remove(tagsReadInventory.get(index));
                                        added = true;
                                    }
                                }
                                oldObject = tagsReadInventory.get(index);
                                if (oldObject.getCount() == 0) {
                                    missedTags--;
                                    matchingTags++;
                                    UNIQUE_TAGS++;
                                }
                                oldObject.incrementCount();
                                if (oldObject.getMemoryBankData() != null && !oldObject.getMemoryBankData().equalsIgnoreCase(memoryBankData))
                                    oldObject.setMemoryBankData(memoryBankData);
                                //oldObject.setEPCId(inventoryItem.getEPCId());
                                oldObject.setPC(Integer.toString(tagData.getPC()));
                                oldObject.setPhase(Integer.toString(tagData.getPhase()));
                                oldObject.setChannelIndex(Integer.toString(tagData.getChannelIndex()));
                                oldObject.setRSSI(Integer.toString(tagData.getPeakRSSI()));
                            }

                        } else {
                            //Tag is encountered for the first time. Add it.
                            if (inventoryMode == 0 || (inventoryMode == 1 && UNIQUE_TAGS_CSV <= Constants.UNIQUE_TAG_LIMIT)) {
                                int tagSeenCount = tagData.getTagSeenCount();
                                if (tagSeenCount != 0) {
                                    TOTAL_TAGS += tagSeenCount;
                                    inventoryItem = new InventoryListItem(tagData.getTagID(), tagSeenCount, null, null, null, null, null, null);
                                } else {
                                    TOTAL_TAGS++;
                                    inventoryItem = new InventoryListItem(tagData.getTagID(), 1, null, null, null, null, null, null);
                                }
                                if (tagListMap.containsKey(tagData.getTagID()))
                                    inventoryItem.setTagStatus("MATCH");
                                else
                                    inventoryItem.setTagStatus("UNKNOWN");
                                if (memoryBankId == 1)
                                    tagsReadInventory.add(inventoryItem);
                                else if (memoryBankId == 3) {
                                    inventoryItem.setTagDetails("unknown");
                                    added = tagsReadInventory.add(inventoryItem);
                                    unknownTagsList.add(inventoryItem);
                                    tagsReadForSearch.add(inventoryItem);
                                } else {
                                    if (inventoryItem.getTagDetails() == null) {
                                        inventoryItem.setTagDetails("unknown");
                                    }
                                    added = tagsReadInventory.add(inventoryItem);
                                    if (memoryBankId != 2)
                                        tagsReadForSearch.add(inventoryItem);
                                }
                                if (added || memoryBankId == 1) {
                                    inventoryList.put(tagData.getTagID(), UNIQUE_TAGS_CSV);
                                    if (tagData.getOpCode() != null)
                                        if (tagData.getOpCode().toString().equalsIgnoreCase("ACCESS_OPERATION_READ")) {
                                            memoryBank = tagData.getMemoryBank().toString();
                                            memoryBankData = tagData.getMemoryBankData().toString();
                                        }
                                    oldObject = tagsReadInventory.get(UNIQUE_TAGS_CSV);
                                    oldObject.setMemoryBankData(memoryBankData);
                                    oldObject.setMemoryBank(memoryBank);
                                    oldObject.setPC(Integer.toString(tagData.getPC()));
                                    oldObject.setPhase(Integer.toString(tagData.getPhase()));
                                    oldObject.setChannelIndex(Integer.toString(tagData.getChannelIndex()));
                                    oldObject.setRSSI(Integer.toString(tagData.getPeakRSSI()));
                                    UNIQUE_TAGS++;
                                    UNIQUE_TAGS_CSV++;
                                }
                            }
                        }

                        // beep on each tag read
                        //startbeepingTimer();

                    } catch (IndexOutOfBoundsException e) {
                        addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(),"error_lectura " +TAG);
                        oldObject = null;
                        added = false;
                    } catch (Exception e) {
                        addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(),"error_lectura " +TAG);
                        oldObject = null;
                        added = false;
                    }
                    tagData = null;
                    inventoryItem = null;
                    memoryBank = null;
                    memoryBankData = null;
                }
            });
            return true;

        }
    }


    /**
     * Async Task, which will handle tag data response from reader. This task is used to check whether tag is in inventory list or not.
     * If tag is not in the list then it will add the tag data to inventory list. If tag is there in inventory list then it will update the tag details in inventory list.
     */
    @SuppressLint("StaticFieldLeak")
    public class ResponseHandlerTask extends AsyncTask<Void, Void, Boolean> {
        private TagData tagData;
        private InventoryListItem inventoryItem;
        private InventoryListItem oldObject;
        //private Fragment fragment;
        private String memoryBank;
        private String memoryBankData;

        ResponseHandlerTask(TagData tagData) {
            this.tagData = tagData;
            //this.fragment = fragment;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean added = false;
            try {
                if (inventoryList.containsKey(tagData.getTagID())) {
                    inventoryItem = new InventoryListItem(tagData.getTagID(), 1, null, null, null, null, null, null);
                    int index = inventoryList.get(tagData.getTagID());
                    if (index >= 0) {
                        //Tag is already present. Update the fields and increment the count
                        if (tagData.getOpCode() != null)
                            if (tagData.getOpCode().toString().equalsIgnoreCase("ACCESS_OPERATION_READ")) {
                                memoryBank = tagData.getMemoryBank().toString();
                                memoryBankData = tagData.getMemoryBankData().toString();
                            }
                        oldObject = tagsReadInventory.get(index);
                        int tagSeenCount = 0;
                        if (Integer.toString(tagData.getTagSeenCount()) != null)
                            tagSeenCount = tagData.getTagSeenCount();
                        if (tagSeenCount != 0) {
                            TOTAL_TAGS += tagSeenCount;
                            oldObject.incrementCountWithTagSeenCount(tagSeenCount);
                        } else {
                            TOTAL_TAGS++;
                            oldObject.incrementCount();
                        }
                        if (oldObject.getMemoryBankData() != null && !oldObject.getMemoryBankData().equalsIgnoreCase(memoryBankData))
                            oldObject.setMemoryBankData(memoryBankData);
                        if (pc)
                            oldObject.setPC(Integer.toHexString(tagData.getPC()));
                        if (phase)
                            oldObject.setPhase(Integer.toString(tagData.getPhase()));
                        if (channelIndex)
                            oldObject.setChannelIndex(Integer.toString(tagData.getChannelIndex()));
                        if (rssi)
                            oldObject.setRSSI(Integer.toString(tagData.getPeakRSSI()));

                    }
                } else {
                    //Tag is encountered for the first time. Add it.
                    if (inventoryMode == 0 || (inventoryMode == 1 && UNIQUE_TAGS <= Constants.UNIQUE_TAG_LIMIT)) {
                        int tagSeenCount = 0;
                        if (Integer.toString(tagData.getTagSeenCount()) != null)
                            tagSeenCount = tagData.getTagSeenCount();
                        if (tagSeenCount != 0) {
                            TOTAL_TAGS += tagSeenCount;
                            inventoryItem = new InventoryListItem(tagData.getTagID(), tagSeenCount, null, null, null, null, null, null);
                        } else {
                            TOTAL_TAGS++;
                            inventoryItem = new InventoryListItem(tagData.getTagID(), 1, null, null, null, null, null, null);
                        }
                        added = tagsReadInventory.add(inventoryItem);
                        if (added) {
                            inventoryList.put(tagData.getTagID(), UNIQUE_TAGS);
                            if (tagData.getOpCode() != null)
                                if (tagData.getOpCode().toString().equalsIgnoreCase("ACCESS_OPERATION_READ")) {
                                    memoryBank = tagData.getMemoryBank().toString();
                                    memoryBankData = tagData.getMemoryBankData().toString();

                                }
                            oldObject = tagsReadInventory.get(UNIQUE_TAGS);
                            oldObject.setMemoryBankData(memoryBankData);
                            oldObject.setMemoryBank(memoryBank);
                            if (pc)
                                oldObject.setPC(Integer.toHexString(tagData.getPC()));
                            if (phase)
                                oldObject.setPhase(Integer.toString(tagData.getPhase()));
                            if (channelIndex)
                                oldObject.setChannelIndex(Integer.toString(tagData.getChannelIndex()));
                            if (rssi)
                                oldObject.setRSSI(Integer.toString(tagData.getPeakRSSI()));
                            UNIQUE_TAGS++;

                        }
                    }
                }
                // beep on each tag read
                startbeepingTimer();
            } catch (IndexOutOfBoundsException e) {
                //logAsMessage(TYPE_ERROR, TAG, e.getMessage());
                oldObject = null;
                added = false;
            } catch (Exception e) {
                // logAsMessage(TYPE_ERROR, TAG, e.getMessage());
                oldObject = null;
                added = false;
            }
            inventoryItem = null;
            memoryBank = null;
            memoryBankData = null;
            return added;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            cancel(true);
/*            if (oldObject != null && fragment instanceof ResponseHandlerInterfaces.ResponseTagHandler)
                ((ResponseHandlerInterfaces.ResponseTagHandler) fragment).handleTagResponse(oldObject, result);*/
            oldObject = null;
        }
    }


    public void startbeepingTimer() {

        if (beeperVolume != BEEPER_VOLUME.QUIET_BEEP) {
            if (!beepON) {
                beepON = true;
                beep();
                if (tbeep == null) {
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            stopbeepingTimer();
                            beepON = false;
                        }
                    };
                    tbeep = new Timer();
                    tbeep.schedule(task, 10);
                }
            }
        }
    }

    /**
     * method to stop timer
     */
    public void stopbeepingTimer() {
        if (tbeep != null && toneGenerator != null) {
            toneGenerator.stopTone();
            tbeep.cancel();
            tbeep.purge();
        }
        tbeep = null;
    }

    public void beep() {
        if (toneGenerator != null) {
            int toneType = ToneGenerator.TONE_PROP_BEEP;
            toneGenerator.startTone(toneType);
        }
    }

    @Override
    protected void onResume() {
        //super.onRestart();
        super.onResume();

        try {

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
                                    if (!reader.isConnected() && gl != null) {
                                        // Establish connection to the RFID Reader
                                        reader.connect();
                                        ConfigureReader();
                                        return true;
                                    } else {
                                        return false;
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
                protected void onPostExecute(Boolean aBoolean) {
                    super.onPostExecute(aBoolean);
                    if (aBoolean) {
                        //Toast.makeText(getApplicationContext(), "Reader Connected", Toast.LENGTH_LONG).show();
                        textView.setText("Lectura RFID lista.");
                    } else {
                        textView.setText("Se ha perdido la comunicación al RFID.");
                    }
                }
            }.execute();


        } catch (Exception e) {
            addlog(new Object() {
            }.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

    }



}
