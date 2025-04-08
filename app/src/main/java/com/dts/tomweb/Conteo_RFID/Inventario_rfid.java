package com.dts.tomweb.Conteo_RFID;

import static com.dts.rfid.RFIDController.toneGenerator;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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
import com.dts.classes.InventarioHelper;
import com.dts.classes.clsArticuloObj;
import com.dts.classes.clsInventario_Rfid;
import com.dts.classes.clsInventario_ciegoObj;
import com.dts.classes.clsInventario_detalleObj;
import com.dts.tomweb.ComWS;
import com.dts.tomweb.PBase;
import com.dts.tomweb.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private List<clsInventario_Rfid> Lista_Registros_rfid = Collections.synchronizedList(new ArrayList<>());
    inventario_rfid_adapter adapter_rfid;

    clsClasses.clsInventario_ciego item_ciego_rfid;
    clsInventario_ciegoObj InvCiego_rfid;

    private String tipoArt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario_rfid);
        super.InitBase(savedInstanceState);

        // Objetos para insertar en las tablas SQLite
        InvDet = new clsInventario_detalleObj(getApplicationContext(), Con, db);
        InvCiego_rfid = new clsInventario_ciegoObj(this, Con, db);

        currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        // Inicializa el toneGenerator si no existe
        if (toneGenerator == null) {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        }

        // Configura campos visuales
        lblLecturas = findViewById(R.id.txtLecturas);
        lblTotal = findViewById(R.id.txtTotal);
        rcListaLecturasRfid = findViewById(R.id.rcListaLecturasRFID);
        rcListaLecturasRfid.setLayoutManager(new LinearLayoutManager(this));

        btnEnviarConteo = findViewById(R.id.IdEnviarConteo);
        btnRegresar = findViewById(R.id.btnRegresar);
        btnListarConteo = findViewById(R.id.btnListarConteo);
        btnLimpiarConteo = findViewById(R.id.btnLimpiar);

        lblLecturas.setText("Conteo: 0");
        lblTotal.setText("Esperado: 0");

        // Inicializa adaptador y lista
        Lista_Registros_rfid = Collections.synchronizedList(new ArrayList<>());
        adapter_rfid = new inventario_rfid_adapter(getApplicationContext(), Lista_Registros_rfid);
        rcListaLecturasRfid.setAdapter(adapter_rfid);

        // Validación del lector RFID usando Executor (no AsyncTask)
        if (readers == null) {
            readers = new Readers(this, ENUM_TRANSPORT.SERVICE_SERIAL);
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            boolean conectado = false;

            try {
                List<ReaderDevice> lista = readers.GetAvailableRFIDReaderList();
                if (lista != null && !lista.isEmpty()) {
                    availableRFIDReaderList = new ArrayList<>(lista);
                    readerDevice = availableRFIDReaderList.get(0);
                    reader = readerDevice.getRFIDReader();

                    if (!reader.isConnected()) {
                        reader.connect();
                        ConfigureReader();
                        conectado = true;
                    }
                }
            } catch (InvalidUsageException | OperationFailureException e) {
                e.printStackTrace();
                Log.e(TAG, "Error conectando con lector RFID: " + e.getMessage());
            }

            boolean finalConectado = conectado;
            handler.post(() -> {
                if (finalConectado) {
                    Log.d(TAG, "Conexión exitosa con el lector RFID.");
                } else {
                    Log.d(TAG, "No se pudo conectar con el lector RFID.");
                }
            });
        });

        cargarRegistrosPrevios();

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

        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final Handler handler = new Handler(Looper.getMainLooper());

        private final List<clsInventario_Rfid> listaRegistrosSync = Collections.synchronizedList(Lista_Registros_rfid);

        @Override
        public void eventReadNotify(RfidReadEvents e) {
            TagData[] myTags = reader.Actions.getReadTags(30);
            if (myTags == null) return;

            for (TagData tag : myTags) {
                String tagId = tag.getTagID();
                String tagFinal = tagId.substring(tagId.length() - 4);

                Log.d(TAG, "Tag ID: " + tagId);
                playBeep();

                if (tag.getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
                        tag.getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS &&
                        !tag.getMemoryBankData().isEmpty()) {
                    Log.d(TAG, "Mem Bank Data: " + tag.getMemoryBankData());
                }

                executor.execute(() -> procesarTag(tagId,tagFinal));
            }
        }

        private void procesarTag(String tagId, String tagFinal) {
            synchronized (listaRegistrosSync) {
                if (existeVisualmente(tagId) || InventarioHelper.tagExists(tagId, gl.tipoInv, InvDet, InvCiego_rfid)) return;

                guardarEnBD(tagId, tagFinal);
                agregarAlAdapter(tagId, tagFinal);
            }
        }

        private boolean existeVisualmente(String tagId) {
            for (clsInventario_Rfid item : listaRegistrosSync) {
                if (item.tag.equals(tagId)) return true;
            }
            return false;
        }

        private void guardarEnBD(String tagId, String tagFinal) {
            long sfecha = du.getActDate();
            String ff = "20" + sfecha;
            String ffe = ff.substring(0, 8);

            if (gl.tipoInv == 2) {
                tipoArt = "F";
                clsClasses.clsInventario_detalle item = clsCls.new clsInventario_detalle();
                item.id_inventario_enc = gl.idInvEnc;
                item.id_articulo = tagFinal;
                item.ubicacion = "1";
                item.cantidad = 1.0;
                item.codigo_barra = tagId;
                item.comunicado = "N";
                item.id_operador = gl.userid;
                item.fecha = ffe;
                item.id_registro = gl.IDregistro;
                item.eliminado = 0;
                InvDet.add(item);
            } else if (gl.tipoInv == 1) {
                clsClasses.clsInventario_ciego item = new clsClasses.clsInventario_ciego();
                item.id_inventario_enc = gl.idInvEnc;
                item.codigo_barra = tagId;
                item.cantidad = 1.0;
                item.comunicado = "N";
                item.ubicacion = "1";
                item.id_operador = gl.userid;
                item.fecha = ffe;
                item.id_registro = gl.IDregistro;
                item.eliminado = 0;
                InvCiego_rfid.add(item);
            }
        }

        private void agregarAlAdapter(String tagId, String tagFinal) {
            clsInventario_Rfid registro = new clsInventario_Rfid();
            registro.tag = tagFinal;
            registro.descripcion = "tag: " + tagFinal;
            registro.ubicacion = "bodega";
            registro.cantidad = 1;
            registro.producto = "producto: " + tagId;

            handler.post(() -> {
                synchronized (listaRegistrosSync) {
                    listaRegistrosSync.add(registro);
                    adapter_rfid.notifyItemInserted(listaRegistrosSync.size() - 1);
                    lblLecturas.setText("Conteo: " + adapter_rfid.getItemCount());
                    rcListaLecturasRfid.scrollToPosition(adapter_rfid.getItemCount() - 1);
                }
            });
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
                        }
                    });
                }

                if (tipo == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                    executor.execute(() -> {
                        try {
                            reader.Actions.Inventory.stop();
                            Log.d(TAG, "Lectura detenida.");

                            // Validar duplicados al finalizar
                            //handler.post(() -> buscarDuplicadosPorCodigoBarra());
                        } catch (InvalidUsageException | OperationFailureException ex) {
                            ex.printStackTrace();
                        }
                    });
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


    private void setHandlers() {

        btnEnviarConteo.setOnClickListener(v -> {
            //gl.validaLicDB=10;
            //ComWS();
            toastlong("DEMO: Inventario enviado hacia wms.");
        });

        btnRegresar.setOnClickListener(v -> {
           //finish();
            msgAskExit("Salir de RFID?");
        });

        btnListarConteo.setOnClickListener(v -> {
            //startActivity(new Intent(getApplicationContext(), Productos.class));
        });

        btnLimpiarConteo.setOnClickListener(v -> clearRecyclerView());

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

    private void msgAskExit(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle("Tom");
        dialog.setMessage("¿" + msg + "?");
        dialog.setPositiveButton("Si", (dialog1, which) -> {
            CerrarRFIF();
            finish();
        });
        dialog.setNegativeButton("No", (dialog2, which) -> {
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
                Toast.makeText(getApplicationContext(), "Disconnecting reader", Toast.LENGTH_LONG).show();
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

    private void cargarRegistrosPrevios() {

        Lista_Registros_rfid.clear();

        if (gl.tipoInv == 2) {

            InvDet.fill(); // Carga desde la tabla Inventario_detalle

            for (clsClasses.clsInventario_detalle item : InvDet.items) {
                clsInventario_Rfid reg = new clsInventario_Rfid();
                String tagFinal = item.codigo_barra.substring(item.codigo_barra.length() - 4);
                reg.tag =tagFinal;
                reg.descripcion = "tag " + tagFinal;
                reg.ubicacion = item.ubicacion;
                reg.cantidad = (int) item.cantidad;
                reg.producto = "producto: " + item.codigo_barra;
                Lista_Registros_rfid.add(reg);
            }

        } else if (gl.tipoInv == 1) {

            InvCiego_rfid.fill(); // Carga desde la tabla Inventario_ciego

            for (clsClasses.clsInventario_ciego item : InvCiego_rfid.items) {
                clsInventario_Rfid reg = new clsInventario_Rfid();
                String tagFinal = item.codigo_barra.substring(item.codigo_barra.length() - 4);
                reg.tag = tagFinal;
                reg.descripcion = "tag " + tagFinal;
                reg.ubicacion = item.ubicacion;
                reg.cantidad = (int) item.cantidad;
                reg.producto = "producto: " + item.codigo_barra;
                Lista_Registros_rfid.add(reg);
            }
        }

        // Notificar al adaptador que los datos cambiaron
        adapter_rfid.notifyDataSetChanged();

        // Actualizar el conteo
        lblLecturas.setText("Conteo: " + adapter_rfid.getItemCount());
    }

    private final Handler handler = new Handler(Looper.getMainLooper());

    private void buscarDuplicadosPorCodigoBarra() {
        Map<String, Integer> conteoCodigos = new HashMap<>();

        for (clsClasses.clsInventario_detalle item : InvDet.items) {
            String codigo = item.codigo_barra;
            if (codigo != null && !codigo.isEmpty()) {
                conteoCodigos.put(codigo, conteoCodigos.getOrDefault(codigo, 0) + 1);
            }
        }

        List<String> duplicados = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : conteoCodigos.entrySet()) {
            if (entry.getValue() > 1) {
                duplicados.add(entry.getKey() + " (" + entry.getValue() + ")");
            }
        }

        if (!duplicados.isEmpty()) {
            String mensaje = "Duplicados encontrados:\n" + TextUtils.join("\n", duplicados);
            handler.post(() -> Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG).show());
        } else {
            handler.post(() -> Toast.makeText(getApplicationContext(), "No hay duplicados en código de barra.", Toast.LENGTH_SHORT).show());
        }
    }

}