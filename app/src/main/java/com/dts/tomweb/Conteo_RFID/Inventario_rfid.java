package com.dts.tomweb.Conteo_RFID;
/*
import static com.dts.application.Application.TOTAL_TAGS;
import static com.dts.application.Application.UNIQUE_TAGS;
import static com.dts.application.Application.inventoryList;
import static com.dts.application.Application.inventoryMode;
import static com.dts.application.Application.tagsReadInventory;
import static com.dts.rfid.RFIDController.channelIndex;
import static com.dts.rfid.RFIDController.pc;
import static com.dts.rfid.RFIDController.phase;
import static com.dts.rfid.RFIDController.rssi;
import static com.dts.rfid.RFIDController.toneGenerator;*/

import static com.dts.rfid.RFIDController.toneGenerator;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dts.adapters.inventario_rfid_adapter;
import com.dts.base.clsClasses;
import com.dts.classes.clsArticuloObj;
import com.dts.classes.clsInventario_Rfid;
import com.dts.classes.clsInventario_ciegoObj;
import com.dts.classes.clsInventario_detalleObj;
import com.dts.tomweb.ComWS;
import com.dts.tomweb.PBase;
import com.dts.tomweb.Productos;
import com.dts.tomweb.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.zebra.rfid.api3.*;

public class Inventario_rfid extends PBase{

    /*********elementos de RFID ***********************/
    public static Readers readers;
    private static ArrayList<ReaderDevice> availableRFIDReaderList;
    private static ReaderDevice readerDevice;
    private static RFIDReader reader;
    private static String TAG = "DTS";
    //Beeper
    public static BEEPER_VOLUME beeperVolume = BEEPER_VOLUME.HIGH_BEEP;
    public static BEEPER_VOLUME sledBeeperVolume = BEEPER_VOLUME.HIGH_BEEP;
    public Timer tbeep;
    /**
     * method to start a timer task to beep for locate functionality and configure the ON OFF duration.
     */
    public Timer locatebeep;
    private boolean beepON = false;
    private boolean beepONLocate = false;


    private EventHandler eventHandler;

    /*****************************************************/

    String currentTime;
    private TextView lblLecturas,lblTotal;
    private RecyclerView rcListaLecturasRfid;
     private Button btnEnviarConteo, btnRegresar, btnListarConteo, btnLimpiarConteo;


    //***************** inventario demo de lecturas preexistentes ******//
    //clsClasses.clsInventario_detalle itemDeta=clsCls.new clsInventario_detalle();
    clsInventario_detalleObj InvDet;
    private final ArrayList<clsInventario_Rfid> Lista_Registros = new ArrayList<clsInventario_Rfid>();



    //********* lecturas RFID ********************//
    clsClasses.clsInventario_detalle inv_detalle_rfid;
    clsInventario_Rfid Registro_Rfid = new clsInventario_Rfid();
    ArrayList<clsInventario_Rfid> Lista_Registros_rfid;
    inventario_rfid_adapter adapter_rfid;

    clsClasses.clsInventario_ciego item_ciego_rfid;
    clsInventario_ciegoObj InvCiego_rfid;

    private String tipoArt;

    @SuppressLint({"StaticFieldLeak", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario_rfid);
        super.InitBase(savedInstanceState);

        //**objetos para insertar en las tablas de sqlite ********************//
        InvDet = new clsInventario_detalleObj(getApplicationContext(), Con, db);
        InvCiego_rfid = new clsInventario_ciegoObj(this, Con, db);

        currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        //*********** validaciones del scanner ****************************//
        if (readers == null) {
            readers = new Readers(this, ENUM_TRANSPORT.SERVICE_SERIAL);
        }

        //********** proceso en segundo plano para validar comunicacion ***//
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    if (readers != null) {
                        if (readers.GetAvailableRFIDReaderList() != null) {
                            availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                            if (availableRFIDReaderList.size() != 0) {
                                // get first reader from list
                                readerDevice = (ReaderDevice) availableRFIDReaderList.get(0);
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
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);

                if (result) {
                    // Connection successful
                    Log.d(TAG, "Conexión exitosa con el lector RFID.");
                } else {
                    // Connection failed
                    Log.d(TAG, "No se pudo conectar con el lector RFID.");
                }
            }
        }.execute();


        // Inicializar el ToneGenerator solo una vez
        if (toneGenerator == null) {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);  // Tipo de tono y volumen (100)
        }

        //********* campos de una lectura fictica **********************//
        lblLecturas = findViewById(R.id.txtLecturas);
        lblTotal = findViewById(R.id.txtTotal);
        rcListaLecturasRfid = findViewById(R.id.rcListaLecturasRFID);
        int encontrados = 0;
        int esperados = 0;

        btnEnviarConteo = findViewById(R.id.IdEnviarConteo);
        btnRegresar = findViewById(R.id.btnRegresar);
        btnListarConteo = findViewById(R.id.btnListarConteo);
        btnLimpiarConteo = findViewById(R.id.btnLimpiar);
        rcListaLecturasRfid.setLayoutManager(new LinearLayoutManager(this));

        //*** lectura demo rfid ********************************************************//
        //**#GT02042025****
       /* for (int i=1;i<encontrados+1;i=i+1){
            clsInventario_Rfid Registro_Rfid_Demo = new clsInventario_Rfid();
            Registro_Rfid_Demo.tag = "tag: " + "aaaaaaaaa" + i;
            Registro_Rfid_Demo.ubicacion = "bodega";
            Registro_Rfid_Demo.cantidad = 1;
            Registro_Rfid_Demo.producto="producto: Playera" + i;
            Lista_Registros.add(Registro_Rfid_Demo);
        }

        inventario_rfid_adapter adapter = new inventario_rfid_adapter(getApplicationContext(), Lista_Registros);
        rcListaLecturasRfid.setAdapter(adapter);*/
        lblLecturas.setText("Conteo: " + encontrados);
        lblTotal.setText("Esperado: " + esperados);

        //**************** objetos para lectura real de RFID ****************************************//
        Lista_Registros_rfid = new ArrayList<clsInventario_Rfid>();
        adapter_rfid=new inventario_rfid_adapter(getApplicationContext(), Lista_Registros_rfid);
        rcListaLecturasRfid.setAdapter(adapter_rfid);

        setHandlers();
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
                // HH event
                reader.Events.setHandheldEvent(true);
                // tag event with tag data
                reader.Events.setTagReadEvent(true);
                // application will collect tag using getReadTags API
                reader.Events.setAttachTagDataWithReadEvent(false);
                // set trigger mode as rfid so scanner beam will not come
                reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true);
                // set start and stop triggers
                reader.Config.setStartTrigger(triggerInfo.StartTrigger);
                reader.Config.setStopTrigger(triggerInfo.StopTrigger);
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
            }
        }
    }

    public class EventHandler implements RfidEventsListener {
        // Read Event Notification
        @SuppressLint("StaticFieldLeak")
        public void eventReadNotify(RfidReadEvents e) {
            // Recommended to use new method getReadTagsEx for better performance in case of large tag population
            TagData[] myTags = reader.Actions.getReadTags(1);
            if (myTags != null) {
                for (int index = 0; index < myTags.length; index++) {
                    Log.d(TAG, "Tag ID " + myTags[index].getTagID());

                    // Aquí puedes activar el beep cuando la lectura sea exitosa
                    playBeep(); // Llamada al método para activar el beep

                    if (myTags[index].getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
                            myTags[index].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS) {
                        if (myTags[index].getMemoryBankData().length() > 0) {
                            Log.d(TAG, " Mem Bank Data " + myTags[index].getMemoryBankData());
                        }
                    }

                    final String tagId = myTags[index].getTagID();
                    //final String tagId =tagId_.substring(tagId_.length() - 4);

                    new AsyncTask<String, Void, Boolean>() {
                        @Override
                        protected Boolean doInBackground(String... tags) {

                         //** GT: a futuro, tomar el tag y validar existencia en un inventario con detalle **//
                         //return simulateSQLiteQuery(tags[0]);
                            return true;
                        }

                        @SuppressLint("StaticFieldLeak")
                        @Override
                        protected void
                        onPostExecute(Boolean result) {
                            if (result) {

                                //GT14032025: valida que el tag no exista en la lista para evitar duplicados.
                                if(!tagExistsInInvDet(tagId)) {
                                    long sfecha;
                                    String  ff,ffe;
                                    Integer fecha;
                                    sfecha=du.getActDate();
                                    ff = "20"+sfecha;
                                    ffe= ff.substring(0,8);

                                    String Tag_Final = tagId.substring(tagId.length() - 4);

                                    //si es inventario con maestro guardar en la lista
                                    if(gl.tipoInv==2){
                                        tipoArt = "F";
                                        //*** inserta datos en tabla que se enviara al portal web ********************//
                                        inv_detalle_rfid=clsCls.new clsInventario_detalle();
                                        inv_detalle_rfid.id_inventario_enc= gl.idInvEnc;
                                        inv_detalle_rfid.id_articulo = Tag_Final;
                                        inv_detalle_rfid.ubicacion = "1";
                                        inv_detalle_rfid.cantidad = 1.0;
                                        inv_detalle_rfid.codigo_barra = Tag_Final;
                                        inv_detalle_rfid.comunicado = "N";
                                        inv_detalle_rfid.id_operador = gl.userid;
                                        inv_detalle_rfid.fecha = ffe;
                                        inv_detalle_rfid.id_registro = gl.IDregistro;
                                        inv_detalle_rfid.eliminado = 0;
                                        InvDet.add(inv_detalle_rfid);

                                        //String Tag_Final = tagId.substring(tagId.length() - 4);
                                        Registro_Rfid = new clsInventario_Rfid();
                                        Registro_Rfid.tag = Tag_Final;
                                        Registro_Rfid.descripcion= "tag "+ Tag_Final;
                                        Registro_Rfid.ubicacion = "bodega";
                                        Registro_Rfid.cantidad = 1;
                                        Registro_Rfid.producto="producto" +Tag_Final ;
                                        Lista_Registros_rfid.add(Registro_Rfid);

                                    } else if (gl.tipoInv==1) {

                                        //GT14032025: llenar objeto para enviarlo al portal
                                        item_ciego_rfid= new clsClasses.clsInventario_ciego();
                                        item_ciego_rfid.id_inventario_enc =  gl.idInvEnc;
                                        item_ciego_rfid.codigo_barra = Tag_Final;
                                        item_ciego_rfid.cantidad = 1.0;
                                        item_ciego_rfid.comunicado = "N";
                                        item_ciego_rfid.ubicacion = "1";
                                        item_ciego_rfid.id_operador = gl.userid;
                                        item_ciego_rfid.fecha = ffe;
                                        item_ciego_rfid.id_registro = gl.IDregistro;
                                        item_ciego_rfid.eliminado = 0;
                                        InvCiego_rfid.add(item_ciego_rfid);

                                        //String Tag_Final = tagId.substring(tagId.length() - 4);
                                        Registro_Rfid = new clsInventario_Rfid();
                                        Registro_Rfid.tag = Tag_Final;
                                        Registro_Rfid.descripcion= "tag "+ Tag_Final;
                                        Registro_Rfid.ubicacion = "bodega";
                                        Registro_Rfid.cantidad = 1;
                                        Registro_Rfid.producto="producto" +Tag_Final ;
                                        Lista_Registros_rfid.add(Registro_Rfid);

                                    }

                                    adapter_rfid.notifyItemInserted(Lista_Registros_rfid.size()-1);
                                    int Lectures = adapter_rfid.getItemCount();
                                    String msgLectures = "Conteo: " + Lectures;
                                    lblLecturas.setText(msgLectures);
                                }

                            } else {
                                Log.d(TAG, "Tag " + tagId + " does not exist in database.");
                            }

                        }
                    }.execute(tagId);

                }
            }
        }

        // Status Event Notification
        @SuppressLint("StaticFieldLeak")
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            Log.d(TAG, "Status Notification: " + rfidStatusEvents.StatusEventData.getStatusEventType());
            if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                    new AsyncTask<String, Void, Boolean>() {

                        @Override
                        protected Boolean doInBackground(String... strings) {
                            try {
                                reader.Actions.Inventory.perform();
                            } catch (InvalidUsageException e) {
                                e.printStackTrace();
                            } catch (OperationFailureException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.execute();
                }
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                    new AsyncTask<String, Void, Boolean>() {

                        @Override
                        protected Boolean doInBackground(String... strings) {
                            try {
                                reader.Actions.Inventory.stop();
                                //Log.d(TAG, "La lectura ha sido detenida correctamente.");
                            } catch (InvalidUsageException e) {
                                e.printStackTrace();
                            } catch (OperationFailureException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.execute();
                }
            }
        }

    }

    // Método para reproducir el beep en el lector
    private void playBeep() {

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




    //GT a futuro implementar las consultas a la base sqlite
    private boolean simulateSQLiteQuery(String tagId) {
        try {

            //Log.d(TAG, "Tag database " + tagId);
            String Tag_Final = tagId.substring(tagId.length() - 4);

            boolean exists = false;
            clsArticuloObj articulo = new clsArticuloObj(getApplicationContext(), Con, db);
            if(gl.tipoInv==2){

                //String Tag = tagId.substring(tagId.length() - 4);
                articulo.fill( " WHERE ID_ARTICULO = '"+ Tag_Final +"'");
                if(articulo.count==1){
                    gl.codBarra = Tag_Final;
                    exists = true;
                }
            }
            return exists; // Assume the tag was found in the database for simulation
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    private void setHandlers() {

        btnEnviarConteo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //gl.validaLicDB=10;
                //ComWS();
                toastlong("DEMO: Inventario enviado hacia wms.");
            }
        });

        btnRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //finish();
                msgAskExit("Salir de RFID?");
            }
        });

        btnListarConteo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getApplicationContext(), Productos.class));
            }
        });

        btnLimpiarConteo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearRecyclerView();
            }
        });

    }

    // Metodo para enviar los datos leidos hacia el portal
    public void ComWS(){
        startActivity(new Intent(this, ComWS.class));
    }

    // Método para limpiar el RecyclerView
    private void clearRecyclerView() {
        Lista_Registros_rfid.clear(); // Limpiar la lista de datos
        adapter_rfid.notifyDataSetChanged(); // Notificar al adapter que los datos han cambiado (se ha limpiado la lista)
        String msgLectures = "Conteo: 0";
        lblLecturas.setText(msgLectures);
        InvDet.DeleteAll();
        Log.d(TAG, "RecyclerView limpiado y conteo reiniciado.");
    }

    // Metodo para validar que un tag no exista en la lista
    private boolean tagExistsInInvDet(@NonNull String tagId) {

        String Tag_Final = tagId.substring(tagId.length() - 4);
        for (clsInventario_Rfid item : Lista_Registros_rfid) {
            if (item.tag.equals(Tag_Final)) {
                Log.d(TAG, " tag existe " + Tag_Final);
                return true;
            }
        }
        Log.d(TAG, " tag nuevo " + Tag_Final);
        return false;
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

    @Override
    public void onBackPressed() {
       // msgAskExit("Salir de RFID?");
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        CerrarRFIF();
    }

    public void CerrarRFIF(){
        try {
            if (reader != null) {
                reader.Events.removeEventsListener(eventHandler);
                reader.disconnect();
                //Toast.makeText(getApplicationContext(), "Disconnecting reader", Toast.LENGTH_LONG).show();
                reader = null;
                readers.Dispose();
                readers = null;
            }
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Help(View view) {
        String tx;

        try{

            tx="-Inventario: El conteo se puede hacer con un inventario ciego o con uno registrado previamente en el portal.\n\n" +
                    "-Conteo: Muestra la cantidad de registros leidos por el lector RFID.\n\n" +
                    "-Esperados: Sino es un inventario ciego, indica cuantos registros se deben encontrar.";

            PopUp(tx);

        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }


    }

}