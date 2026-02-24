package com.dts.tomweb.Conteo_RFID;

import static com.dts.rfid.RFIDController.toneGenerator;

import android.app.AlertDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dts.adapters.inventario_rfid_adapter;
import com.dts.base.clsClasses;
import com.dts.classes.InventarioHelper;
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
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.zebra.rfid.api3.*;

public class Inventario_rfid extends PBase{

    /*********elementos de RFID ***********************/
    public Readers readers;
    private static ArrayList<ReaderDevice> availableRFIDReaderList;
    private static ReaderDevice readerDevice;
    private static RFIDReader reader;
    private static final String TAG = "DTS";
    //Beeper
    public static BEEPER_VOLUME beeperVolume = BEEPER_VOLUME.HIGH_BEEP;

    public Timer tbeep;
    /**
     * method to start a timer task to beep for locate functionality and configure the ON OFF duration.
     */
    private boolean beepON = false;
    private EventHandler eventHandler;

    /*****************************************************/

    String currentTime;
    private TextView lblLecturas;
    private RecyclerView rcListaLecturasRfid;
    private Button btnEnviarConteo, btnRegresar, btnListarConteo, btnLimpiarConteo;

    //***************** inventario demo de lecturas preexistentes ******//
    clsInventario_detalleObj InvDet;

    private List<clsInventario_Rfid> Lista_Registros_rfid = Collections.synchronizedList(new ArrayList<>());
    inventario_rfid_adapter adapter_rfid;

    clsInventario_ciegoObj InvCiego_rfid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario_rfid);
        super.InitBase(savedInstanceState);

        inicializarBaseDeDatos();
        inicializarUI();
        inicializarAdaptador();
        inicializarReader();
        cargarRegistrosPrevios();
        setHandlers();
    }

    private void inicializarBaseDeDatos() {
        InvDet = new clsInventario_detalleObj(getApplicationContext(), Con, db);
        InvCiego_rfid = new clsInventario_ciegoObj(this, Con, db);
        currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private void inicializarUI() {
        if (toneGenerator == null) {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        }

        lblLecturas = findViewById(R.id.txtLecturas);
        TextView lblTotal = findViewById(R.id.txtTotal);
        rcListaLecturasRfid = findViewById(R.id.rcListaLecturasRFID);
        rcListaLecturasRfid.setLayoutManager(new LinearLayoutManager(this));

        btnEnviarConteo = findViewById(R.id.IdEnviarConteo);
        btnRegresar = findViewById(R.id.btnRegresar);
        btnListarConteo = findViewById(R.id.btnListarConteo);
        btnLimpiarConteo = findViewById(R.id.btnLimpiar);

        lblLecturas.setText(R.string.conteo_registros_cero);
        lblTotal.setText(R.string.Esperado_cero);
    }

    private void inicializarAdaptador() {
        Lista_Registros_rfid = Collections.synchronizedList(new ArrayList<>());
        adapter_rfid = new inventario_rfid_adapter(getApplicationContext(), Lista_Registros_rfid);
        rcListaLecturasRfid.setAdapter(adapter_rfid);
    }

    private void inicializarReader() {
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
                } else {
                    Log.w(TAG, "No hay lectores RFID disponibles.");
                }
            } catch (InvalidUsageException exInvalid) {
                logRfidError("Uso inválido al conectar lector RFID", exInvalid);
            } catch (OperationFailureException exFailure) {
                logRfidError("Fallo en operación al conectar lector RFID", exFailure);
            } catch (Exception exGeneric) {
                logRfidError("Error inesperado al conectar lector RFID", exGeneric);
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
    }


    private void ConfigureReader() {
        if (reader == null || !reader.isConnected()) {
            Log.w(TAG, "Reader no está conectado, no se puede configurar.");
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

            Log.d(TAG, "Configuración del lector RFID completada.");
        } catch (InvalidUsageException exInvalid) {
            logRfidError("Uso inválido al configurar lector RFID", exInvalid);
        } catch (OperationFailureException exFailure) {
            logRfidError("Fallo en operación al configurar lector RFID", exFailure);
        } catch (Exception exGeneric) {
            logRfidError("Error inesperado al configurar lector RFID", exGeneric);
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
                clsClasses.clsInventario_detalle item = new clsClasses.clsInventario_detalle();
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
                    int nuevoIndice = listaRegistrosSync.size() - 1;

                    adapter_rfid.notifyItemInserted(nuevoIndice);
                    lblLecturas.setText(getString(R.string.conteo_registros, adapter_rfid.getItemCount()));
                    rcListaLecturasRfid.scrollToPosition(nuevoIndice);
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
                        } catch (InvalidUsageException exInvalid) {
                            logRfidError("Uso inválido al iniciar inventario", exInvalid);
                        } catch (OperationFailureException exFailure) {
                            logRfidError("Fallo en operación al iniciar inventario", exFailure);
                        } catch (Exception exGeneric) {
                            logRfidError("Error inesperado al iniciar inventario", exGeneric);
                        }
                    });
                }

                if (tipo == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                    executor.execute(() -> {
                        try {
                            reader.Actions.Inventory.stop();
                            Log.d(TAG, "Lectura RFID detenida con éxito.");
                        } catch (InvalidUsageException exInvalid) {
                            logRfidError("Uso inválido al detener inventario", exInvalid);
                        } catch (OperationFailureException exFailure) {
                            logRfidError("Fallo en operación al detener inventario", exFailure);
                        } catch (Exception exGeneric) {
                            logRfidError("Error inesperado al detener inventario", exGeneric);
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
            gl.validaLicDB=10;
            ComWS();
            toastlong("Realice envío de datos");
        });

        btnRegresar.setOnClickListener(v -> {
           //finish();
            msgAskExit();
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

    // Limpia todos los datos del RecyclerView, UI y base de datos relacionada
    private void clearRecyclerView() {
        resetRfidList();
        updateLecturaCounter();
        clearInventoryData();
        logClearAction();
    }

    private void resetRfidList() {
        int itemCount = Lista_Registros_rfid.size();
        Lista_Registros_rfid.clear();
        if (itemCount > 0) {
            adapter_rfid.notifyItemRangeRemoved(0, itemCount);
        }
    }

    private void updateLecturaCounter() {
        lblLecturas.setText(getString(R.string.conteo_registros, adapter_rfid.getItemCount()));
    }

    private void clearInventoryData() {
        InvDet.DeleteAll();
    }

    private void logClearAction() {
        Log.d(TAG, "Datos del RecyclerView limpiados. Conteo reiniciado a cero.");
    }


    private void msgAskExit() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle("Tom");
        dialog.setMessage("¿" + "Salir de RFID?" + "?");
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

    private static final String TAGCerrarenrfid = "CerrarRFIF";

    public void CerrarRFIF() {
        try {
            if (reader != null) {
                reader.Events.removeEventsListener(eventHandler);
                reader.disconnect();
                Toast.makeText(getApplicationContext(), "Lector desconectado", Toast.LENGTH_SHORT).show();
                reader = null;
                readers.Dispose();
                readers = null;
            }
        } catch (InvalidUsageException e) {
            handleDisconnectionError("Uso inválido del lector", e);
        } catch (OperationFailureException e) {
            handleDisconnectionError("Fallo en operación del lector", e);
        } catch (Exception e) {
            handleDisconnectionError("Error desconocido al cerrar el lector", e);
        }
    }

    private void handleDisconnectionError(String userMessage, Exception e) {
        Log.e(TAGCerrarenrfid, userMessage + ": " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
        Toast.makeText(getApplicationContext(), userMessage, Toast.LENGTH_LONG).show();
    }

    public void Help(View view) {
        String tx;

        try{

            tx="-Inventario: El conteo se puede hacer con un inventario ciego o con uno registrado previamente en el portal.\n\n" +
                    "-Conteo: Muestra la cantidad de registros leidos por el lector RFID.\n\n" +
                    "-Esperados: Sino es un inventario ciego, indica cuantos registros se deben encontrar.";

            PopUp(tx);

        }catch (Exception e){
            addlog(Objects.requireNonNull(new Object() {
            }.getClass().getEnclosingMethod()).getName(), e.getMessage(), "");
        }

    }

    private void cargarRegistrosPrevios() {
        Lista_Registros_rfid.clear();

        List<clsInventario_Rfid> nuevosRegistros = new ArrayList<>();

        if (gl.tipoInv == 2) {
            InvDet.fill();
            for (clsClasses.clsInventario_detalle item : InvDet.items) {
                nuevosRegistros.add(mapearItemRfid(item.codigo_barra, item.ubicacion, item.cantidad));
            }
        } else if (gl.tipoInv == 1) {
            InvCiego_rfid.fill();
            for (clsClasses.clsInventario_ciego item : InvCiego_rfid.items) {
                nuevosRegistros.add(mapearItemRfid(item.codigo_barra, item.ubicacion, item.cantidad));
            }
        }

        Lista_Registros_rfid.addAll(nuevosRegistros);

        if (!nuevosRegistros.isEmpty()) {
            adapter_rfid.notifyItemRangeInserted(0, nuevosRegistros.size());
        }
        lblLecturas.setText(getString(R.string.conteo_registros, adapter_rfid.getItemCount()));

    }

    private clsInventario_Rfid mapearItemRfid(String codigoBarra, String ubicacion, double cantidad) {
        String tagFinal = codigoBarra.length() >= 4
                ? codigoBarra.substring(codigoBarra.length() - 4)
                : codigoBarra;

        clsInventario_Rfid reg = new clsInventario_Rfid();
        reg.tag = tagFinal;
        reg.descripcion = "tag " + tagFinal;
        reg.ubicacion = ubicacion;
        reg.cantidad = (int) cantidad;
        reg.producto = "producto: " + codigoBarra;

        return reg;
    }

    private void logRfidError(String message, Exception e) {
        Log.e("RFID_TRIGGER", message + " [" + e.getClass().getSimpleName() + "]: " + e.getMessage(), e);
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show()
        );
    }

}