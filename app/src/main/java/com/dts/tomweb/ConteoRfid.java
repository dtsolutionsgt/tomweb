package com.dts.tomweb;

import static com.dts.application.Application.inventoryList;
import static com.dts.application.Application.tagsReadInventory;
import static com.dts.application.Application.UNIQUE_TAGS;
import static com.dts.rfid.RFIDController.channelIndex;
import static com.dts.rfid.RFIDController.pc;
import static com.dts.rfid.RFIDController.phase;
import static com.dts.rfid.RFIDController.rssi;
import static com.dts.rfid.RFIDController.toneGenerator;
import static com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED;
import static com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED;


import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dts.base.clsClasses;
import com.dts.classes.clsInventario_ciegoObj;
import com.dts.classes.clsInventario_detalleObj;
import com.dts.inventory.InventoryListItem;
import com.dts.listadapt.LA_RFID;
import com.zebra.rfid.api3.ACCESS_OPERATION_CODE;
import com.zebra.rfid.api3.ACCESS_OPERATION_STATUS;
import com.zebra.rfid.api3.BEEPER_VOLUME;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.MEMORY_BANK;
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
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConteoRfid extends PBase  {

    private ListView lvConteoRFID;
    private ProgressBar pbar;
    private TextView regs;

    private LA_RFID dadapter_rfid;

    /*********elementos de RFID ************/
    public Readers readers;
    private static ArrayList<ReaderDevice> availableRFIDReaderList;
    private static ReaderDevice readerDevice;
    private static RFIDReader reader;
    private static final String TAG = "DEMO";
    TextView textView;
    private EventHandler eventHandler;

    public ArrayList<clsClasses.clsInventario_ciego_rfid> dvalues_rfid = new ArrayList<>();
    ArrayList<String> codigos = new ArrayList<>();
    ArrayList<String> lista_limpia = new ArrayList<>();

    String currentTime;

    Integer contador = 0;

    //Beeper
    public static BEEPER_VOLUME beeperVolume = BEEPER_VOLUME.HIGH_BEEP;

    public Timer tbeep;

    //for beep and LED
    private boolean beepON = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conteo_rfid);

        inicializarUI();
        inicializarVariables();
        inicializarReader();
    }

    private void inicializarUI() {
        textView = findViewById(R.id.TagText);
        pbar = findViewById(R.id.progressBar);
        textView.setText(R.string.rfid_iniciando);
        lvConteoRFID = findViewById(R.id.lvConteoRFID);
        regs = findViewById(R.id.txtRegs);
    }

    private void inicializarVariables() {
        codigos.clear();
        dvalues_rfid.clear();

        int streamType = AudioManager.STREAM_DTMF;
        toneGenerator = new ToneGenerator(streamType, 90);

        if (readers == null) {
            readers = new Readers(this, ENUM_TRANSPORT.SERVICE_SERIAL);
        }
    }

    private void inicializarReader() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            boolean conectado = false;

            try {
                List<ReaderDevice> dispositivos = readers.GetAvailableRFIDReaderList();

                if (dispositivos == null || dispositivos.isEmpty()) {
                    Log.w(TAG, "No se encontraron lectores RFID.");
                } else {
                    availableRFIDReaderList = new ArrayList<>(dispositivos);

                    // Logging de los lectores disponibles
                    for (ReaderDevice dev : availableRFIDReaderList) {
                        Log.d(TAG, "Lector disponible: " + dev.getName());
                    }

                    readerDevice = availableRFIDReaderList.get(0);
                    reader = readerDevice.getRFIDReader();

                    if (reader != null && !reader.isConnected()) {
                        reader.connect();
                        ConfigureReader();
                        conectado = true;
                    }
                }

            } catch (InvalidUsageException ex) {
                logRfidError("Uso inválido al conectar lector RFID", ex);
            } catch (OperationFailureException ex) {
                logRfidError("Fallo en operación al conectar lector RFID", ex);
            } catch (Exception ex) {
                logRfidError("Error inesperado al conectar lector RFID", ex);
            }

            final boolean estadoConexion = conectado;
            handler.post(() -> textView.setText(estadoConexion ? R.string.lectura_rfid_lista : R.string.con_perdida_rfid));
        });
    }


    private void logRfidError(String message, Exception e) {
        Log.e(getString(R.string.rfid_conteo), message + " [" + e.getClass().getSimpleName() + "]: " + e.getMessage(), e);
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show()
        );
    }


    @Override
    public void onBackPressed() {
        msgAskExit(getString(R.string.salir_de_rfid));
    }

    private void ConfigureReader() {
        if (reader == null || !reader.isConnected()) {
            Log.w("RFID_CONTEO", "No se puede configurar: lector no conectado.");
            return;
        }

        TriggerInfo triggerInfo = new TriggerInfo();
        triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
        triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);

        try {
            if (eventHandler == null) {
                eventHandler = new EventHandler();
            }

            reader.Events.addEventsListener(eventHandler);
            reader.Events.setHandheldEvent(true);
            reader.Events.setTagReadEvent(true);
            reader.Events.setAttachTagDataWithReadEvent(false);
            reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true);
            reader.Config.setStartTrigger(triggerInfo.StartTrigger);
            reader.Config.setStopTrigger(triggerInfo.StopTrigger);

            Log.d( "RFID_CONTEO", getString(R.string.lector_rfid_configurado_correctamente_id) + reader.ReaderCapabilities.ReaderID.getID());

        } catch (InvalidUsageException exInvalid) {
            logRfidError(getString(R.string.uso_inv_lido_al_configurar_lector_rfid), exInvalid);
        } catch (OperationFailureException exFailure) {
            logRfidError(getString(R.string.fallo_en_operaci_n_al_configurar_lector_rfid), exFailure);
        } catch (Exception exGeneric) {
            logRfidError(getString(R.string.error_inesperado_al_configurar_lector_rfid), exGeneric);
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
            TagData[] tags = reader.Actions.getReadTags(30);

            if (tags == null) return;

            for (TagData tag : tags) {
                Log.d(TAG, "Tag ID: " + tag.getTagID());

                if (tag.getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
                        tag.getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS &&
                        !tag.getMemoryBankData().isEmpty()) {

                    Log.d(TAG, "Mem Bank Data: " + tag.getMemoryBankData());
                }

                executor.execute(() -> procesarLectura(tag));
            }
        }

        private void procesarLectura(TagData tag) {
            try {
                insertaConteo(tag.getTagID());
                procesarTag(tag);
            } catch (Exception ex) {
                logError("procesarLectura", ex, "procesarTag error");
            }
        }

        @Override
        public void eventStatusNotify(RfidStatusEvents e) {
            STATUS_EVENT_TYPE tipoEvento = e.StatusEventData.getStatusEventType();

            Log.d(TAG, "Status Notification: " + tipoEvento);

            if (tipoEvento == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                HANDHELD_TRIGGER_EVENT_TYPE tipo = e.StatusEventData.HandheldTriggerEventData.getHandheldEvent();

                if (tipo.equals(HANDHELD_TRIGGER_PRESSED)) {
                    executor.execute(this::iniciarLectura);
                } else if (tipo.equals(HANDHELD_TRIGGER_RELEASED)) {
                    executor.execute(this::detenerLectura);
                }

                handler.post(() -> {
                    GuardarLista();
                    showData();
                });
            }
        }

        private void iniciarLectura() {
            try {
                reader.Actions.Inventory.perform();
            } catch (InvalidUsageException | OperationFailureException ex) {
                logError(getString(R.string.iniciarlectura), ex, getString(R.string.failed_trigger_rfid) + currentTime);
            }
        }

        private void detenerLectura() {
            try {
                reader.Actions.Inventory.stop();
                Log.d(TAG, getString(R.string.lectura_rfid_detenida));
            } catch (InvalidUsageException | OperationFailureException ex) {
                logError(getString(R.string.detenerlectura), ex, getString(R.string.stop_trigger_error));
            }
        }

        private void logError(String origen, Exception ex, String contexto) {
            Log.e(TAG, "[" + origen + "] " + contexto + ": " + ex.getMessage(), ex);
            addlog(origen, ex.getMessage(), contexto);
        }
    }

    private void procesarTag(TagData tagData) {
        if (tagData == null || tagData.getTagID() == null) {
            Log.w(TAG, getString(R.string.tagdata_nulo_o_sin_id_ignorado));
            return;
        }

        try {
            String tagID = tagData.getTagID();
            int seenCount = Math.max(tagData.getTagSeenCount(), 1);
            com.dts.application.Application.TOTAL_TAGS.addAndGet(seenCount);

            boolean isExisting = inventoryList.containsKey(tagID);
            InventoryListItem item;

            if (isExisting) {
                Integer indexObj = inventoryList.get(tagID);
                if (indexObj == null || indexObj < 0 || indexObj >= tagsReadInventory.size()) {
                    Log.w(TAG, getString(R.string.ndice_inv_lido_o_nulo_en_tagsreadinventory_para_tag) + tagID);
                    return;
                }
                int index = indexObj;

                tagsReadInventory.size();

                item = tagsReadInventory.get(index);
                item.incrementCountWithTagSeenCount(seenCount);

            } else {
                item = new InventoryListItem(tagID, seenCount, null, null, null, null, null, null);
                if (!tagsReadInventory.add(item)) {
                    Log.w(TAG, getString(R.string.no_se_pudo_agregar_nuevo_tag) + tagID);
                    return;
                }

                inventoryList.put(tagID, UNIQUE_TAGS.incrementAndGet());
            }

            if (tagData.getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ) {
                item.setMemoryBankData(safeString(tagData.getMemoryBankData()));
                item.setMemoryBank(safeEnum(tagData.getMemoryBank()));
            }

            if (pc) item.setPC(Integer.toHexString(tagData.getPC()));
            if (phase) item.setPhase(Integer.toString(tagData.getPhase()));
            if (channelIndex) item.setChannelIndex(Integer.toString(tagData.getChannelIndex()));
            if (rssi) item.setRSSI(Integer.toString(tagData.getPeakRSSI()));

            startbeepingTimer();

        } catch (Exception ex) {
            Log.e(TAG, getString(R.string.error_procesando_tag) + ex.getMessage(), ex);
            addlog(getString(R.string.procesartag), ex.getMessage(), getString(R.string.tagid) + tagData.getTagID());
        }
    }

    private String safeString(String value) {
        return value != null ? value : "";
    }

    private String safeEnum(MEMORY_BANK value) {
        return value != null ? value.toString() : "";
    }

    public void insertaConteo(String tagId) {
        if (tagId == null || tagId.trim().isEmpty()) {
            Log.w(TAG, getString(R.string.id_de_tag_nulo_o_vac_o_no_se_inserta_conteo));
            return;
        }

        final String ubicacion = "1";
        double cantidadReal;

        currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        clsInventario_detalleObj invDetalleObj = new clsInventario_detalleObj(this, Con, db);
        clsInventario_ciegoObj invCiegoObj = new clsInventario_ciegoObj(this, Con, db);
        clsClasses.clsInventario_detalle detalle = new clsClasses.clsInventario_detalle();
        clsClasses.clsInventario_ciego ciego = new clsClasses.clsInventario_ciego();

        try {
            String sqlConsulta = "SELECT CODIGO_BARRA FROM INVENTARIO_CIEGO WHERE CODIGO_BARRA = '" + tagId + "'";
            Cursor cursor = Con.OpenDT(sqlConsulta);
            int existe = (cursor != null) ? cursor.getCount() : 0;

            cantidadReal = 1;

            String fechaCorta = obtenerFechaCorta();

            if (gl.tipoInv == 0) {
                // Inventario Ciego
                ciego.id_inventario_enc = gl.idInvEnc;
                ciego.codigo_barra = tagId;
                ciego.cantidad = cantidadReal;
                ciego.comunicado = "N";
                ciego.ubicacion = ubicacion;
                ciego.id_operador = gl.userid;
                ciego.fecha = fechaCorta;
                ciego.id_registro = gl.IDregistro;
                ciego.eliminado = 0;

                try {
                    if (existe == 0) {
                        invCiegoObj.add(ciego);
                    } else {
                        String sqlUpdate = "UPDATE Inventario_ciego SET cantidad = cantidad + 1 WHERE CODIGO_BARRA = '" + tagId + "'";
                        db.execSQL(sqlUpdate);
                    }
                } catch (Exception e) {
                    logError(getString(R.string.insertaconteo), e, getString(R.string.error_insertando_actualizando_inventario_ciego));
                    msgbox(getString(R.string.error_al_registrar_el_conteo) + e.getMessage());
                }

            } else if (gl.tipoInv == 2 || gl.tipoInv == 3) {
                // Inventario Detalle
                detalle.id_inventario_enc = gl.idInvEnc;
                detalle.id_articulo = tagId;
                detalle.codigo_barra = tagId;
                detalle.ubicacion = ubicacion;
                detalle.cantidad = cantidadReal;
                detalle.comunicado = "N";
                detalle.id_operador = gl.userid;
                detalle.fecha = fechaCorta;
                detalle.id_registro = gl.IDregistro;
                detalle.eliminado = 0;

                invDetalleObj.add(detalle);
            }

        } catch (Exception e) {
            logError(getString(R.string.insertaconteo), e, getString(R.string.error_general_durante_inserci_n_de_conteo));
            msgbox(getString(R.string.error_inesperado) + e.getMessage());
        }
    }

    private String obtenerFechaCorta() {
        String fechaRaw = "20" + du.getActDate();
        return fechaRaw.length() >= 8 ? fechaRaw.substring(0, 8) : fechaRaw;
    }

    private void logError(String origen, Exception e, String contexto) {
        Log.e(TAG, "[" + origen + "] " + contexto + ": " + e.getMessage(), e);
        addlog(origen, e.getMessage(), contexto + " - " + du.getActDate());
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

    public void CerrarRFIF() {
        try {
            if (reader != null) {
                reader.Events.removeEventsListener(eventHandler);
                reader.disconnect();

                mostrarToastSeguro();

                reader = null;

                if (readers != null) {
                    readers.Dispose();
                    readers = null;
                }

                Log.d(TAG, "Lector RFID desconectado correctamente.");
            }
        } catch (InvalidUsageException ex) {
            logError("CerrarRFIF", ex, "ERROR_RFID_DISCONNECT_1");
        } catch (OperationFailureException ex) {
            logError("CerrarRFIF", ex, "ERROR_RFID_DISCONNECT_2");
        } catch (Exception ex) {
            logError("CerrarRFIF", ex, "ERROR_RFID_DISCONNECT_3");
        }
    }


    /********** configuración del grid ***************/
    public void doHelp(View view) {
        String tx;

        try{

            tx="-Busqueda: La busqueda se puede hacer por código de barra, ubicación, o ambos.\n\n" +
                    "-Regs: Muestra la cantidad de registros, o de conteos realizados.\n\n" +
                    "-Consolidar: Consolida y muestra los registros según código de barra y ubicación.";

            PopUp(tx);

        }catch (Exception e){
            addlog(Objects.requireNonNull(new Object() {
            }.getClass().getEnclosingMethod()).getName(), e.getMessage(), "");
        }


    }

    private void showData() {
        if (contador > 0) return;

        try {
            dvalues_rfid.clear();
            lvConteoRFID.setAdapter(null);

            String tabla = "INVENTARIO_CIEGO";
            String consulta = "SELECT CODIGO_BARRA, UBICACION, CANTIDAD FROM " + tabla +
                    " WHERE ID_INVENTARIO_ENC=" + gl.idInvEnc + " AND ELIMINADO = 0";

            Cursor cursor = Con.OpenDT(consulta);

            if (cursor != null) {
                int totalRegistros = cursor.getCount();
                contador = totalRegistros;

                if (totalRegistros > 0) {
                    regs.setText(String.valueOf(totalRegistros));

                    while (cursor.moveToNext()) {
                        clsClasses.clsInventario_ciego_rfid item = new clsClasses.clsInventario_ciego_rfid();
                        item.codigo_barra = cursor.getString(0);
                        item.ubicacion = cursor.getString(1);
                        item.cantidad = Double.parseDouble(cursor.getString(2));
                        dvalues_rfid.add(item);
                    }
                }

                cursor.close();
            }

            dadapter_rfid = new LA_RFID(this, dvalues_rfid);
            lvConteoRFID.setAdapter(dadapter_rfid);
            pbar.setVisibility(View.INVISIBLE);

        } catch (Exception ex) {
            logError("showData", ex, "ERROR_SHOWDATA_RFID");
            msgbox("Error en showData: " + ex.getMessage());
        }
    }


    private void GuardarLista() {
        if (gl.tipoInv != 1) return;

        clsInventario_ciegoObj invCiego = new clsInventario_ciegoObj(this, Con, db);
        String ubicacion = "1";
        String fechaFormateada = obtenerFechaFormateada();

        try {
            List<Map.Entry<String, Integer>> entradaLista = new ArrayList<>(inventoryList.entrySet());

            for (Map.Entry<String, Integer> entrada : entradaLista) {
                String codigoBarra = entrada.getKey();

                if (lista_limpia.contains(codigoBarra)) continue;
                lista_limpia.add(codigoBarra);

                if (!existeEnInventarioCiego(codigoBarra)) {
                    clsClasses.clsInventario_ciego itemCiego = construirItemInventarioCiego(codigoBarra, ubicacion, fechaFormateada);
                    clsClasses.clsInventario_ciego_rfid itemRfid = new clsClasses.clsInventario_ciego_rfid();
                    itemRfid.codigo_barra = codigoBarra;
                    dvalues_rfid.add(itemRfid);

                    try {
                        invCiego.add(itemCiego);
                    } catch (Exception ex) {
                        logError("guardarLista/add", ex, "error_insert_inv_ciego");
                        msgbox("Error al insertar: " + ex.getMessage());
                    }
                }
            }

        } catch (Exception ex) {
            logError("guardarLista", ex, "error_insert_inv_ciego");
        }
    }

    private boolean existeEnInventarioCiego(String codigoBarra) {
        String consulta = "SELECT CODIGO_BARRA FROM INVENTARIO_CIEGO WHERE CODIGO_BARRA = '" + codigoBarra + "'";
        Cursor cursor = Con.OpenDT(consulta);
        return cursor != null && cursor.getCount() > 0;
    }

    private clsClasses.clsInventario_ciego construirItemInventarioCiego(String codigoBarra, String ubicacion, String fecha) {
        clsClasses.clsInventario_ciego item = new clsClasses.clsInventario_ciego();
        item.id_inventario_enc = gl.idInvEnc;
        item.codigo_barra = codigoBarra;
        item.cantidad = 1.0;
        item.comunicado = "N";
        item.ubicacion = ubicacion;
        item.id_operador = gl.userid;
        item.fecha = fecha;
        item.id_registro = gl.IDregistro;
        item.eliminado = 0;
        return item;
    }

    private String obtenerFechaFormateada() {
        long fechaNum = du.getActDate();
        String fecha = "20" + fechaNum;
        return fecha.length() >= 8 ? fecha.substring(0, 8) : fecha;
    }

    public void getCampos(){
        try{

            if(dadapter_rfid != null){

                int registros = dadapter_rfid.getCount();

                if(registros <=0){
                    msgAskContinue();
                }else{
                    CerrarRFIF();
                    ComWS();
                }
            }else{
                msgbox("No hay data que sincronizar");
            }
        }catch (Exception e){
            addlog(Objects.requireNonNull(new Object() {
            }.getClass().getEnclosingMethod()).getName(), e.getMessage(), "");
            msgbox("Error getCampos: "+e);
        }
    }

    public void ComWS(){
        startActivity(new Intent(this, ComWS.class));
    }

    private void msgAskContinue() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle("Tom");
        dialog.setMessage("No hay data con rfid registrada, ¿Continuar?");
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

            conectarLectorRfid();

        } catch (Exception ex) {
            logError("onResume", ex, "Error general en onResume");
        }
    }
    private void conectarLectorRfid() {
        executor.execute(() -> {
            boolean conectado = false;

            try {
                List<ReaderDevice> lista = readers.GetAvailableRFIDReaderList();

                if (lista != null && !lista.isEmpty()) {
                    availableRFIDReaderList = new ArrayList<>(lista);
                    readerDevice = lista.get(0);
                    reader = readerDevice.getRFIDReader();

                    if (reader != null && !reader.isConnected() && gl != null) {
                        reader.connect();
                        ConfigureReader();
                        conectado = true;
                    }
                } else {
                    Log.w(TAG, "No hay lectores RFID disponibles.");
                }

            } catch (InvalidUsageException | OperationFailureException ex) {
                logError("conectarLectorRfid", ex, "Error conectando lector RFID");
            } catch (Exception ex) {
                logError("conectarLectorRfid", ex, "Error inesperado");
            }

            boolean estadoFinal = conectado;
            handler.post(() -> textView.setText(estadoFinal
                    ? R.string.lectura_rfid_lista
                    : R.string.se_ha_perdido_la_comunicaci_n_al_rfid));
        });
    }
    private void mostrarToastSeguro() {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(this, R.string.rfid_desconectado, Toast.LENGTH_LONG).show()
        );
    }
}
