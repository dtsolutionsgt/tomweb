package com.dts.tomweb;

import static com.dts.application.Application.inventoryList;
import static com.dts.application.Application.tagsReadInventory;
import static com.dts.application.Application.UNIQUE_TAGS;
import static com.dts.application.Application.TOTAL_TAGS;
import static com.dts.rfid.RFIDController.channelIndex;
import static com.dts.rfid.RFIDController.pc;
import static com.dts.rfid.RFIDController.phase;
import static com.dts.rfid.RFIDController.rssi;
import static com.dts.rfid.RFIDController.toneGenerator;


import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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

    private String Ubic, Cod, tipoArt, barra;
    private Double canti;
    String currentTime;
    clsRegistro_handheldObj regHH;

    Integer contador = 0;

    //Beeper
    public static BEEPER_VOLUME beeperVolume = BEEPER_VOLUME.HIGH_BEEP;
    public static BEEPER_VOLUME sledBeeperVolume = BEEPER_VOLUME.HIGH_BEEP;

    public Timer tbeep;

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

        if (readers == null) {
            readers = new Readers(this, ENUM_TRANSPORT.SERVICE_SERIAL);
        }

        int streamType = AudioManager.STREAM_DTMF;
        toneGenerator = new ToneGenerator(streamType, 90);

        // Refactor moderno usando ExecutorService y Handler para evitar AsyncTask
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            boolean conectado = false;
            try {
                if (readers != null) {
                    List<ReaderDevice> lista = readers.GetAvailableRFIDReaderList();
                    if (lista != null && !lista.isEmpty()) {
                        availableRFIDReaderList = new ArrayList<>(lista);
                        readerDevice = lista.get(0);
                        reader = readerDevice.getRFIDReader();
                        if (!reader.isConnected() && gl != null) {
                            reader.connect();
                            ConfigureReader();
                            conectado = true;
                        }
                    }
                }
            } catch (InvalidUsageException | OperationFailureException e) {
                e.printStackTrace();
                Log.d(TAG, "Error de conexión: " + e.getMessage());
            }

            boolean finalConectado = conectado;
            handler.post(() -> {
                if (finalConectado) {
                    textView.setText("Lectura RFID lista.");
                } else {
                    textView.setText("Se ha perdido la comunicación al RFID.");
                }
            });
        });

        // Bloque UI y lógica
        try {
            super.InitBase(savedInstanceState);
            addlog("ConteoRfid", du.getActDateTime().toString(), gl.nombreusuario);

            lvConteoRFID = findViewById(R.id.lvConteoRFID);
            txtBarra = findViewById(R.id.txtBarra);
            txtUbic = findViewById(R.id.txtNombre);
            regs = findViewById(R.id.txtRegs);
            cb = findViewById(R.id.cbConsolidar);

            regHH = new clsRegistro_handheldObj(this, Con, db);
            regHH.fill();
            gl.IDregistro = regHH.first().id_registro;

            if (gl.tipoInv == 1) scod = " INVENTARIO_CIEGO";
            if (gl.tipoInv == 2 || gl.tipoInv == 3) scod = " INVENTARIO_DETALLE";
            if (gl.tipoInv == 5) scod = " INVENTARIO_CIEGO_RFID";

            pbar.setVisibility(View.INVISIBLE);
            lvConteoRFID.setFocusable(true);

        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }
    }

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

        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void eventReadNotify(RfidReadEvents e) {
            TagData[] myTags = reader.Actions.getReadTags(30);

            if (myTags != null) {
                for (TagData tag : myTags) {
                    Log.d(TAG, "Tag ID " + tag.getTagID());

                    if (tag.getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
                            tag.getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS &&
                            !tag.getMemoryBankData().isEmpty()) {
                        Log.d(TAG, "Mem Bank Data " + tag.getMemoryBankData());
                    }

                    // Procesar el tag en un hilo separado
                    executor.execute(() -> {
                        try {
                            insertaConteo(tag.getTagID());
                            procesarTag(tag);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            addlog("eventReadNotify", ex.getMessage(), "procesarTag error");
                        }
                    });
                }
            }
        }


        @Override
        public void eventStatusNotify(RfidStatusEvents e) {
            Log.d(TAG, "Status Notification: " + e.StatusEventData.getStatusEventType());

            if (e.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                HANDHELD_TRIGGER_EVENT_TYPE tipo = e.StatusEventData.HandheldTriggerEventData.getHandheldEvent();

                if (tipo == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                    executor.execute(() -> {
                        try {
                            reader.Actions.Inventory.perform();
                        } catch (InvalidUsageException | OperationFailureException ex) {
                            ex.printStackTrace();
                            addlog("HANDHELD_TRIGGER_PRESSED", ex.getMessage(), "failed_trigger_rfid " + currentTime);
                        }
                    });
                }

                if (tipo == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                    executor.execute(() -> {
                        try {
                            reader.Actions.Inventory.stop();
                            Log.d(TAG, "termina lectura: ");
                        } catch (InvalidUsageException | OperationFailureException ex) {
                            ex.printStackTrace();
                        }
                    });
                }

                // Actualización de UI después del trigger
                handler.post(() -> {
                    GuardarLista(scod);
                    showData(scod);
                });
            }
        }
    }

    private void procesarTag(TagData tagData) {
        InventoryListItem inventoryItem = null;
        InventoryListItem oldObject = null;
        String memoryBank = null;
        String memoryBankData = null;
        boolean added = false;

        try {
            String tagID = tagData.getTagID();

            if (inventoryList.containsKey(tagID)) {
                int index = inventoryList.get(tagID);
                if (index >= 0) {
                    oldObject = tagsReadInventory.get(index);
                    int tagSeenCount = tagData.getTagSeenCount();
                    TOTAL_TAGS += tagSeenCount > 0 ? tagSeenCount : 1;

                    if (tagSeenCount > 0) {
                        oldObject.incrementCountWithTagSeenCount(tagSeenCount);
                    } else {
                        oldObject.incrementCount();
                    }

                    if (tagData.getOpCode() != null &&
                            tagData.getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ) {
                        memoryBankData = tagData.getMemoryBankData();
                    }

                    if (oldObject.getMemoryBankData() == null ||
                            !oldObject.getMemoryBankData().equalsIgnoreCase(memoryBankData)) {
                        oldObject.setMemoryBankData(memoryBankData);
                    }

                    if (pc) oldObject.setPC(Integer.toHexString(tagData.getPC()));
                    if (phase) oldObject.setPhase(Integer.toString(tagData.getPhase()));
                    if (channelIndex) oldObject.setChannelIndex(Integer.toString(tagData.getChannelIndex()));
                    if (rssi) oldObject.setRSSI(Integer.toString(tagData.getPeakRSSI()));
                }
            } else {
                int tagSeenCount = tagData.getTagSeenCount();
                TOTAL_TAGS += tagSeenCount > 0 ? tagSeenCount : 1;

                inventoryItem = new InventoryListItem(tagID, tagSeenCount > 0 ? tagSeenCount : 1,
                        null, null, null, null, null, null);

                added = tagsReadInventory.add(inventoryItem);

                if (added) {
                    inventoryList.put(tagID, UNIQUE_TAGS);
                    if (tagData.getOpCode() != null &&
                            tagData.getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ) {
                        memoryBank = tagData.getMemoryBank().toString();
                        memoryBankData = tagData.getMemoryBankData();
                    }

                    oldObject = tagsReadInventory.get(UNIQUE_TAGS);
                    oldObject.setMemoryBankData(memoryBankData);
                    oldObject.setMemoryBank(memoryBank);

                    if (pc) oldObject.setPC(Integer.toHexString(tagData.getPC()));
                    if (phase) oldObject.setPhase(Integer.toString(tagData.getPhase()));
                    if (channelIndex) oldObject.setChannelIndex(Integer.toString(tagData.getChannelIndex()));
                    if (rssi) oldObject.setRSSI(Integer.toString(tagData.getPeakRSSI()));

                    UNIQUE_TAGS++;
                }
            }

            startbeepingTimer();

        } catch (Exception e) {
            e.printStackTrace();
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
        dialog.setPositiveButton("Si", (dialog1, which) -> {
            CerrarRFIF();
            finish();
        });
        dialog.setNegativeButton("No", (dialog2, which) -> {
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

            lvConteoRFID.setOnItemLongClickListener((parent, view, position, id) -> {

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
        dialog.setPositiveButton("Si", (dialog1, which) -> ComWS());
        dialog.setNegativeButton("No", (dialog2, which) -> {
        });
        dialog.show();
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

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onResume() {
        super.onResume();

        try {
            if (readers == null) {
                readers = new Readers(this, ENUM_TRANSPORT.SERVICE_SERIAL);
            }

            executor.execute(() -> {
                boolean conectado = false;
                try {
                    List<ReaderDevice> lista = readers.GetAvailableRFIDReaderList();
                    if (lista != null && !lista.isEmpty()) {
                        availableRFIDReaderList = new ArrayList<>(lista);
                        readerDevice = lista.get(0);
                        reader = readerDevice.getRFIDReader();
                        if (!reader.isConnected() && gl != null) {
                            reader.connect();
                            ConfigureReader();
                            conectado = true;
                        }
                    }
                } catch (InvalidUsageException | OperationFailureException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Error conexión RFID: " + e.getMessage());
                }

                boolean finalConectado = conectado;
                handler.post(() -> {
                    if (finalConectado) {
                        textView.setText("Lectura RFID lista.");
                    } else {
                        textView.setText("Se ha perdido la comunicación al RFID.");
                    }
                });
            });

        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "onResume error");
        }
    }

}
