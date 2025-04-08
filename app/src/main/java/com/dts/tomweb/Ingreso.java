package com.dts.tomweb;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import com.zebra.rfid.api3.*;

import com.dts.classes.clsInventario_encabezadoObj;
import com.dts.classes.clsRegistro_handheldObj;
import com.dts.classes.clsOperadoresObj;

import java.util.ArrayList;

public class Ingreso extends PBase {

    private EditText txtUser,txtPass;
    private TextView lblTitle,lblVer, tituloRFID;

    private String version="Ver: 1.0.1 - 08/05/23";

    /************************************************************************/
    /******** variables para validar la existencia de lector rfid **********/

    private static Readers readers;
    private static ArrayList availableRFIDReaderList;
    private static ReaderDevice readerDevice;
    private static RFIDReader reader;
    private static String TAG = "DEMO";

    private EventHandler eventHandler;

     //TextView textView;


    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingreso);
        tituloRFID = findViewById(R.id.TagText);


        if (readers == null) {
            readers = new Readers(this, ENUM_TRANSPORT.SERVICE_SERIAL);
        }

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
                                    gl.rfid_activo=true;
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
                    //Toast.makeText(getApplicationContext(), "Lector RFID conectado exitosamente.", Toast.LENGTH_SHORT).show();
                    tituloRFID.setText("Lector RFID conectado.");
                    Log.d(TAG, "Conexión exitosa con el lector RFID.");
                } else {
                    // Connection failed
                    //Toast.makeText(getApplicationContext(), "No se pudo conectar con el lector RFID.", Toast.LENGTH_SHORT).show();
                    tituloRFID.setText("Lector RFID no conectado.");
                    Log.d(TAG, "No se pudo conectar con el lector RFID.");
                }
            }
        }.execute();


        try {
            super.InitBase(savedInstanceState);

            if(gl!=null){
                addlog("Ingreso",""+du.getActDateTime(),gl.nombreusuario);
            }

            txtUser = findViewById(R.id.editText2);txtUser.requestFocus();
            txtPass = findViewById(R.id.editText3);
            lblTitle = findViewById(R.id.textView2);
            lblVer = findViewById(R.id.Productos);lblVer.setText(version);
            //txtUser.setText("2");txtPass.setText("gustav");txtPass.requestFocus();

            setHandlers();

            getDB();

            //CerrarRFIF();


        } catch (Exception e) {
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

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
    public void doEnter(View view) {
        processLogIn();
    }

    public void doHelp(View view) {
        String tx;

        try{

            tx="-Usuario: Ingrese el usuario que esté asignado al inventario que se está trabajando en la empresa.\n\n" +
                    "-Clave: Ingrese la clave(contraseña) asignada al usuario anterior.\n\n" +
                    "Para ingresar presione enter.";

            PopUp(tx);

        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }


    }
    private void setHandlers() {

        txtUser.setOnKeyListener((arg0, arg1, arg2) -> {
            if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                switch (arg1) {
                    case KeyEvent.KEYCODE_ENTER:
                        txtPass.requestFocus();
                        return true;
                }
            }
            return false;
        });

        txtPass.setOnKeyListener((arg0, arg1, arg2) -> {
            if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                switch (arg1) {
                    case KeyEvent.KEYCODE_ENTER:
                        processLogIn();
                        return true;
                }
            }
            return false;
        });

    }

    // Main
    public void getDB(){
        clsInventario_encabezadoObj invEnc = new clsInventario_encabezadoObj(this, Con, db);
        try{
            invEnc.fill();

            if(invEnc.count == 0) {
                gl.validaLicDB=0;
                startActivity(new Intent(this, ComWS.class));
            }else{
                getLic();
            }


        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            //startActivity(new Intent(this, Licencia.class));
        }
    }

    private void getLic(){
        clsRegistro_handheldObj LicHH = new clsRegistro_handheldObj(this, Con, db);

        try {
            LicHH.fill();

            if(LicHH.count==0){
                gl.validaLicDB=1;
                startActivity(new Intent(this, Licencia.class));
            }else{
                LicHH.fill("WHERE id_estatus = 1");

                if(LicHH.count==0) {
                    startActivity(new Intent(this, Licencia.class));
                }
            }
        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");

        }
    }

    private void processLogIn() {
        String user,pass,su,sp;
        clsOperadoresObj opr =new clsOperadoresObj(this,Con,db);
        boolean flag;

        try {
            user=txtUser.getText().toString();
            if (emptystr(user)) {
                toast("Falta usuario.");txtUser.requestFocus();return;
            }

            pass=txtPass.getText().toString();
            if (emptystr(pass)) {
                toast("Falta clave.");txtUser.requestFocus();return;
            }
            user = user;pass = pass;


            opr.fill();
            if (opr.count == 0) {
                msgbox("Catálogo de usuarios vacio.");return;
            }

            flag=false;
            for (int i = 0; i <opr.count; i++) {

                su=opr.items.get(i).codigo; sp=opr.items.get(i).clave;

                if (su.equalsIgnoreCase(user) && sp.equalsIgnoreCase(pass)) {
                    gl.userid=opr.items.get(i).id_operador;
                    gl.nombreusuario=opr.items.get(i).nombre;

                    flag=true;break;
                }
            }

            if (!flag) {
                msgbox("¡El usuario no existe o contraseña incorrecta!");txtUser.requestFocus();return;
            }

            txtUser.setText("");txtPass.setText("");txtUser.requestFocus();
            callback =1;gl.exitapp=false;

            //GT 11082021: se cierra la conexión porque solo se abrio para validar la existencia del dispositivo
            CerrarRFIF();
            startActivity(new Intent(this,MenuPrincipal.class));

        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
        }

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

    // Activity Events

    @Override
    protected void onResume() {
        super.onResume();

        try{

            CerrarRFIF();

            if (callback ==1) {
                callback =0;
                if (gl.exitapp) finish();
            }

            if(gl.validaLicDB==2){
                super.finish();
            }

            if(gl.validaLicDB==4){
                getDB();
            }

        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
       CerrarRFIF();
    }

    public class EventHandler implements RfidEventsListener {
        // Read Event Notification
        public void eventReadNotify(RfidReadEvents e) {
            // Recommended to use new method getReadTagsEx for better performance in case of large tag population
            TagData[] myTags = reader.Actions.getReadTags(100);
            if (myTags != null) {
                for (int index = 0; index < myTags.length; index++) {
                    Log.d(TAG, "Tag ID " + myTags[index].getTagID());
                    if (myTags[index].getOpCode() == ACCESS_OPERATION_CODE.ACCESS_OPERATION_READ &&
                            myTags[index].getOpStatus() == ACCESS_OPERATION_STATUS.ACCESS_SUCCESS) {
                        if (myTags[index].getMemoryBankData().length() > 0) {
                            Log.d(TAG, " Mem Bank Data " + myTags[index].getMemoryBankData());
                        }
                    }
                }
            }
        }

        // Status Event Notification
        @SuppressLint("StaticFieldLeak")
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            Log.d(TAG, "Status Notification: " + rfidStatusEvents.StatusEventData.getStatusEventType());
            if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                    new AsyncTask() {
                        @Override
                        protected Void doInBackground(Object[] objects) {
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
                    new AsyncTask() {
                        @Override
                        protected Void doInBackground(Object[] objects) {
                            try {
                                reader.Actions.Inventory.stop();
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


}