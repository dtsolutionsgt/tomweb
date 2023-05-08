package com.dts.tomweb;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dts.base.BaseDatos;
import com.dts.base.DateUtils;
import com.dts.base.clsDataBuilder;
import com.dts.classes.clsInventario_ciego_RfidObj;
import com.dts.classes.clsInventario_ciegoObj;
import com.dts.classes.clsInventario_detalleObj;
import com.dts.classes.clsRegistro_handheldObj;
import com.dts.classes.clsInventario_encabezadoObj;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ComWS extends PBase {


    private int isbusy,regHH;
    private String credential;
    private int Com_IdEmpresa,Com_Id_Inventario;
    private boolean errflag;

    private SQLiteDatabase dbT;
    private BaseDatos ConT;
    private BaseDatos.Insert insT;

    private ArrayList<String> listItems=new ArrayList<String>();
    private ArrayList<String> results=new ArrayList<String>();

    //private ArrayList<clsClasses.clsEnvio> items=new ArrayList<clsClasses.clsEnvio>();
    //private ListAdaptEnvio adapter;

    private clsDataBuilder dbld;
    private DateUtils DU;
    private String jsonWS;

    // Web Service -

    public AsyncCallRec wsRtask;
    public AsyncCallSend wsStask;

    private static String sstr,fstr,ferr,fterr,idbg,dbg,ftmsg,sprog;
    private int scon,stockflag,reccnt,count;
    private String senv,gEmpresa,ActRuta;
    private boolean ftflag,ret;

    private RelativeLayout relEnv, relRec;
    private TextView Prg,Prg2;
    private ProgressBar prgBar;

    private final String NAMESPACE ="http://tempuri.org/";
    private String METHOD_NAME,URL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_com_ws);

        super.InitBase(savedInstanceState);

        System.setProperty("line.separator","\r\n");
        addlog("ComWs",""+du.getActDateTime(),gl.nombreusuario);

        dbld=new clsDataBuilder(this);

        relEnv = (RelativeLayout) findViewById(R.id.relEnv);
        relRec = (RelativeLayout) findViewById(R.id.relRec);
        Prg = (TextView) findViewById(R.id.lblProgress);
        Prg2 = (TextView) findViewById(R.id.lblProgress2);
        prgBar = (ProgressBar) findViewById(R.id.progressBar2);

        isbusy=0;
        count=0;

        Prg.setText("");
        prgBar.setVisibility(View.INVISIBLE);

        URL="http://52.41.114.122/wsTomWeb/wstomwebws.asmx";



        envCompleto();
    }


    // Events

    public void askRec(View view) {

        if (isbusy==1) {
            toast("Por favor, espere que se termine la tarea actual.");return;
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setCancelable(false);
        dialog.setTitle("Recepción");
        dialog.setMessage("¿Recibir datos nuevos?");

        dialog.setPositiveButton("Recibir", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                runRecep();
                prgBar.setVisibility(View.VISIBLE);
            }
        });

        dialog.setNegativeButton("Cancelar", null);

        dialog.show();

    }

    public void askSend(View view) {

        if (isbusy==1) {
            toast("Por favor, espere que se termine la tarea actual.");return;
        }

        prgBar.setVisibility(View.VISIBLE);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setCancelable(false);
        dialog.setTitle("Envio");
        dialog.setMessage("¿Enviar datos?");
        dialog.setPositiveButton("Enviar", (dialog1, which) -> runSend());
        dialog.setNegativeButton("Cancelar", (dialog12, which) -> prgBar.setVisibility(View.INVISIBLE));
        dialog.show();

    }

    public void doHelp(View view) {
        String tx;

        try{

            tx="Si hay datos sin comunicar se mostrará el boton de enviar, " +
                    "si no hay nada que comunicar se mostrará el botón de recibir.\n\n" +
                    "-Recibir: Recibirá datos nuevos del BOF (verificar su conexión a internet antes de recibir).\n\n" +
                    "-Enviar: Enviará los datos del dispositivo al BOF (verificar su conexión a internet antes de enviar).\n\n" +
                    "-En la esquina superior izquierda hay botón que muestra las tablas con los respectivos datos.";

            PopUp(tx);

        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }


    }

    public void doExit(View view) {
        finish();
    }

    public void tablas(View view){
        startActivity(new Intent(this, Tablas.class));
    }


    // Main

    private void runRecep() {
        if (isbusy==1) return;
        isbusy=1;

        wsRtask = new AsyncCallRec();
        wsRtask.execute();
    }

    private void runSend() {

        try {

            if (isbusy==1) return;
            isbusy=1;

            wsStask = new AsyncCallSend();
            wsStask.execute();

        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "runSend");
        }

    }

    public void writeData(View view){
        dbld.clear();
        dbld.insert("USUARIO","WHERE 1=1");
        dbld.save();
    }

    // Web Service Methods
    public int fillTable(String value,String delcmd) {
        int rc;
        String s,ss;

        METHOD_NAME = "getIns";
        sstr="OK";

        try {

            idbg=idbg+" filltable ";

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("SQL");param.setValue(value);

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE+METHOD_NAME, envelope);

            SoapObject resSoap =(SoapObject) envelope.getResponse();
            SoapObject result = (SoapObject) envelope.bodyIn;

            rc=resSoap.getPropertyCount()-1;
            idbg=idbg+" rec " +rc +"  ";

            s="";

            for (int i = 0; i < rc; i++) {
                String str = "";
                try {
                    str = ((SoapObject) result.getProperty(0)).getPropertyAsString(i);

                }catch (Exception e){
                    mu.msgbox("error: " + e.getMessage());
                    addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
                }

                if (i==0) {

                    idbg=idbg+" ret " +str +"  ";

                    if (str.equalsIgnoreCase("#")) {
                        listItems.add(delcmd);
                    } else {
                        idbg=idbg+str;
                        ftmsg=ftmsg+"\n"+str;ftflag=true;
                        gl.licExist=1;
                        sstr=str;return 0;
                    }
                } else {
                    try {
                        sql=str;
                        listItems.add(sql);
                        sstr=str;
                    } catch (Exception e) {
                        sstr=e.getMessage();
                        addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
                    }
                }
            }

            return 1;
        } catch (Exception e) {

            idbg=idbg+" ERR "+e.getMessage();
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            return 0;
        }
    }

    public boolean Procesar_Inventario_Ciego(Integer Id_Inventario_Enc,Integer Id_Registro) {

        int rc;
        String s,ss;

        METHOD_NAME = "Procesar_Inventario_Ciego";
        sstr="OK";

        try {

            s="";
            int ii=dbld.size();
            for (int i = 0; i < dbld.size(); i++) {
                ss=dbld.items.get(i);
                s=s+ss+"\n";
            }

            idbg=idbg+" Procesar_Inventario_Ciego ";

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("pSQL");param.setValue(s);

            PropertyInfo param2 = new PropertyInfo();
            param2.setType(String.class);
            param2.setName("Id_Inventario_Enc");param2.setValue(Id_Inventario_Enc);

            PropertyInfo param3 = new PropertyInfo();
            param3.setType(String.class);
            param3.setName("Id_Registro");param3.setValue(Id_Registro);

            request.addProperty(param);
            request.addProperty(param2);
            request.addProperty(param3);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE+METHOD_NAME, envelope);

            SoapPrimitive resSoap =(SoapPrimitive) envelope.getResponse();
            s = resSoap.toString();

            sstr = "#";
            if (s.equalsIgnoreCase("#")) return true;

            sstr = s;

            return false;

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            sstr = e.getMessage();
            idbg=idbg+" ERR "+e.getMessage();
            return false;
        }
    }

    public boolean Procesar_Inventario_Ciego_Rfid(Integer Id_Inventario_Enc,Integer Id_Registro) {

        int rc;
        String s,ss;

        METHOD_NAME = "Procesar_Inventario_Ciego_Rfid";
        sstr="OK";

        try {

            s="";
            int ii=dbld.size();
            for (int i = 0; i < dbld.size(); i++) {
                ss=dbld.items.get(i);
                s=s+ss+"\n";
            }

            idbg=idbg+" Procesar_Inventario_Ciego_Rfid";

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("pSQL");param.setValue(s);

            PropertyInfo param2 = new PropertyInfo();
            param2.setType(String.class);
            param2.setName("Id_Inventario_Enc");param2.setValue(Id_Inventario_Enc);

            PropertyInfo param3 = new PropertyInfo();
            param3.setType(String.class);
            param3.setName("Id_Registro");param3.setValue(Id_Registro);

            request.addProperty(param);
            request.addProperty(param2);
            request.addProperty(param3);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE+METHOD_NAME, envelope);

            SoapPrimitive resSoap =(SoapPrimitive) envelope.getResponse();
            s = resSoap.toString();

            sstr = "#";
            if (s.equalsIgnoreCase("#")) return true;

            sstr = s;
            return false;

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            sstr = e.getMessage();
            idbg=idbg+" ERR "+e.getMessage();
            return false;
        }
    }



    public boolean Procesar_Inventario_Detalle(Integer Id_Inventario_Enc, Integer Id_Registro) {

        int rc;
        String s,ss;

        METHOD_NAME = "Procesar_Inventario_Detalle";
        sstr="OK";

        try {

            s="";
            for (int i = 0; i < dbld.size(); i++) {
                ss=dbld.items.get(i);
                s=s+ss+"\n";
            }

            idbg=idbg+" Procesar_Inventario_Detalle ";

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("pSQL");param.setValue(s);

            PropertyInfo param2 = new PropertyInfo();
            param2.setType(String.class);
            param2.setName("Id_Inventario_Enc");param2.setValue(Id_Inventario_Enc);


            PropertyInfo param3 = new PropertyInfo();
            param3.setType(String.class);
            param3.setName("Id_Registro");param3.setValue(Id_Registro);

            request.addProperty(param);
            request.addProperty(param2);
            request.addProperty(param3);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE+METHOD_NAME, envelope);

            SoapPrimitive resSoap =(SoapPrimitive) envelope.getResponse();
            s = resSoap.toString();

            sstr = "#";
            if (s.equalsIgnoreCase("#")) return true;

            sstr = s;
            return false;

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            sstr = e.getMessage();
            idbg=idbg+" ERR "+e.getMessage();
            return false;
        }
    }

    public int commitSQL() {

        int rc;
        String s,ss;

        METHOD_NAME = "Commit";
        sstr="OK";

        if (dbld.size()==0) return 1;

        s="";
        for (int i = 0; i < dbld.size(); i++) {
            ss=dbld.items.get(i);
            s=s+ss+"\n";
        }

        try {

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("pSQL");param.setValue(s);

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE+METHOD_NAME, envelope);

            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            s = response.toString();

            sstr = "#";
            if (s.equalsIgnoreCase("#")) return 1;

            errflag=true;
            sstr = s;

            return 0;

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            errflag=true;
            sstr=e.getMessage();
        }

        return 0;
    }

    public int OpenDTt(String sql) {

        int rc;
        METHOD_NAME = "OpenDT";
        results.clear();

        try {

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("SQL");param.setValue(sql);

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE+METHOD_NAME, envelope);

            SoapObject resSoap =(SoapObject) envelope.getResponse();
            SoapObject result = (SoapObject) envelope.bodyIn;

            rc=resSoap.getPropertyCount()-1;

            for (int i = 0; i < rc+1; i++)
            {
                String str = ((SoapObject)result.getProperty(0)).getPropertyAsString(i);

                if (i==0) {
                    sstr=str;
                    if (!str.equalsIgnoreCase("#")) {
                        sstr=str;
                        return 0;
                    }
                } else {
                    results.add(str);
                }
            }

            return 1;

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            sstr=e.getMessage();
        }

        return 0;

    }

    public int getTest() {

        METHOD_NAME = "TestWS";

        try {

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            PropertyInfo param = new PropertyInfo();
            param.setType(String.class);
            param.setName("Value");param.setValue("OK");

            request.addProperty(param);
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(URL);
            transport.call(NAMESPACE+METHOD_NAME, envelope);
            SoapPrimitive response = (SoapPrimitive) envelope.getResponse();

            sstr = response.toString()+"..";

            return 1;

        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            sstr=e.getMessage();
        }

        return 0;
    }


    // WEB SERVICE - RECEPCION

    private boolean getData(){

        listItems.clear();
        idbg="";stockflag=0;

        ftmsg="";ftflag=false;

        try {

            if (!AddTable("REGISTRO_HANDHELD")) return false;
            if (!saveData())return false;
            if(gl.licExist==0) return false;
            if (!AddTable("ESTADO_INVENTARIO")) return false;
            if (!AddTable("INVENTARIO_ENCABEZADO")) return false;
            if (!saveData())return false;
            if (!AddTable("OPERADORES")) return false;
            if (!saveData())return false;

            if(gl.tipoInv==1) {
                //Nada que recibir, inventario ciego
                listItems.add("DELETE FROM INVENTARIO_CIEGO");

            }else if(gl.tipoInv==2){
                while (count!=2){
                    count++;
                    if (!AddTable("INVENTARIO_MAESTRO")) return false;
                    listItems.add("DELETE FROM INVENTARIO_DETALLE");
                }

            }else if(gl.tipoInv==3){
                if (!AddTable("INVENTARIO_TEORICO")) return false;
                listItems.add("DELETE FROM INVENTARIO_DETALLE");
            }
            else if(gl.tipoInv==5){
                listItems.add("DELETE FROM INVENTARIO_CIEGO_RFID");
            }

            if (!saveData())return false;

         } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            return false;
        }

        try {
            ConT.close();
        } catch (Exception e) {
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            return false;
        }

        return true;
    }

    private boolean saveData(){

        int rc;
        ferr="";
        clsRegistro_handheldObj registroHH =new clsRegistro_handheldObj(this,Con,db);
        clsInventario_ciegoObj invC =new clsInventario_ciegoObj(this,Con,db);

        try {

            rc=listItems.size();reccnt=rc;
            if (rc==0) return true;

            ConT = new BaseDatos(this);
            dbT = ConT.getWritableDatabase();
            ConT.vDatabase =dbT;
            insT=ConT.Ins;
            dbT.beginTransaction();

            for (int i = 0; i < rc; i++) {
                try {
                    sql = listItems.get(i);
                    dbT.execSQL(sql);
                    if (i % 10==0) SystemClock.sleep(20);
                } catch (Exception e) {
                    addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), sql);
                    Log.e("z", e.getMessage());
                }
            }

            dbT.setTransactionSuccessful();
            dbT.endTransaction();

            if(regHH==1){

                registroHH.fill();

                gl.licExist=registroHH.count;

                if(gl.licExist>0){
                    gl.empresa = registroHH.first().id_empresa;
                }

                regHH=0;

            }else if(regHH==2){
                invC.fill();

                if(invC.count==0){
                    regHH=0;
                }

            }

            return true;

        } catch (Exception e) {
            Log.e("Error",e.getMessage());
            addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            try {
                ConT.close();
            } catch (Exception ee) {
                addlog(new Object(){}.getClass().getEnclosingMethod().getName(),e.getMessage(),"");
            }

            sstr=e.getMessage();
            ferr=sstr+"\n"+sql;
            return false;
        }
    }

    private boolean AddTable(String TN) {

        String SQL;

        try {

            SQL=getTableSQL(TN);

            sprog = TN;wsRtask.onProgressUpdate();

            if(count==1){ TN = "ARTICULO"; }
            else if(count==2){ TN = "ARTICULO_CODIGO_BARRA"; }

            if (fillTable(SQL,"DELETE FROM "+TN)==1) {
                idbg=idbg +SQL+"#"+"PASS OK";
                return true;
            } else {
                idbg=idbg +SQL+"#"+" PASS FAIL  ";
                fstr="Tab:"+TN+" "+sstr;
                return false;
            }

        } catch (Exception e) {
            fstr="Tab:"+TN+", "+ e.getMessage();idbg=idbg + e.getMessage();
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            return false;
        }
    }

    private String getTableSQL(String TN) {

        String SQL = "";
        clsRegistro_handheldObj registroHH =new clsRegistro_handheldObj(this,Con,db);
        clsInventario_encabezadoObj invEnc =new clsInventario_encabezadoObj(this,Con,db);

        if (TN.equalsIgnoreCase("REGISTRO_HANDHELD")) {
            regHH=1;
            SQL = "SELECT * FROM REGISTRO_HANDHELD WHERE SERIE_DISPOSITIVO= '"+ gl.NoSerieHH +"' AND ID_ESTATUS =1";
            return SQL;
        }

        if (TN.equalsIgnoreCase("ESTADO_INVENTARIO")) {
            SQL = "SELECT * FROM ESTADO_INVENTARIO";
            return SQL;
        }

        if (TN.equalsIgnoreCase("INVENTARIO_ENCABEZADO")) {
            registroHH.fill();
            Com_IdEmpresa = registroHH.first().id_empresa;

            SQL = "SELECT * FROM INVENTARIO_ENCABEZADO WHERE ID_EMPRESA = " + Com_IdEmpresa + " AND ID_ESTADO = 1";
            return SQL;
        }

        if (TN.equalsIgnoreCase("OPERADORES")) {

            invEnc.fill();
            Com_Id_Inventario = invEnc.first().id_inventario_enc;
            gl.tipoInv =  invEnc.first().tipo_inventario;

            SQL = "SELECT A.* FROM OPERADORES A, INVENTARIO_OPERADOR B  WHERE A.ID_OPERADOR = B.ID_OPERADOR AND A.ID_EMPRESA ='" + Com_IdEmpresa +"' AND B.ID_INVENTARIO_ENC='" + Com_Id_Inventario +"'";
            return SQL;
        }

        if (TN.equalsIgnoreCase("ARTICULO")) {
            SQL = "SELECT * FROM ARTICULO WHERE ID_EMPRESA ='" + Com_IdEmpresa +"'";
            return SQL;
        }

        if (TN.equalsIgnoreCase("INVENTARIO_MAESTRO")) {
            String ss;

            if(count==1){
                TN = "ARTICULO";
                SQL = "SELECT * FROM ARTICULO WHERE ID_EMPRESA ='" + Com_IdEmpresa + "'";
                return SQL;
            }else if(count==2){
                TN = "ARTICULO_CODIGO_BARRA";
                SQL = "SELECT * FROM ARTICULO_CODIGO_BARRA WHERE ID_EMPRESA ='" + Com_IdEmpresa + "'";
                return SQL;
            }

        }

        if (TN.equalsIgnoreCase("INVENTARIO_TEORICO")) {
            SQL = "SELECT * FROM Inventario_Teorico WHERE ID_EMPRESA ='"+ Com_IdEmpresa +"' AND ID_INVENTARIO_ENC ='"+ Com_Id_Inventario +"'";
            return SQL;
        }

        return SQL;
    }


    // Web Service handling Methods

    public void wsExecute(){

        fstr="No connect";scon=0;

        sprog = "Conectando ...";

        wsRtask.onProgressUpdate();

        try {
            if (getTest()==1) scon=1;
            idbg=idbg + sstr;
            if (scon==1) {
                fstr="Sync OK";
                if (!getData()) {
                    if(gl.licExist==0){
                        fstr="El dispositivo no tiene licencia";
                    }else {
                        fstr="Recepcion incompleta : "+fstr;
                    }
                }
            } else {
                fstr="No se puede conectar al web service : "+sstr;
            }
        } catch (Exception e) {
            scon=0;
            fstr="No se puede conectar al web service. "+e.getMessage();
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

    }

    public void wsFinished(){

        Prg.setVisibility(View.INVISIBLE);
        prgBar.setVisibility(View.INVISIBLE);

        try{
            if (fstr.equalsIgnoreCase("Sync OK")) {
                msgAskExit("Recepción completa.");
            } else {

                if(gl.licExist==0 && scon==1){

                    msgLic("No hay licencia existente de este dispositivo, validarla en BOF, su número de activación es: "+ gl.NoSerieHH);

                }else{
                    mu.msgbox("Ocurrió error : \n"+fstr+" ("+reccnt+") " + ferr);
                    gl.licExist=0;
                    isbusy=0;
                    return;
                }
            }

            isbusy=0;
        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }


    }

    private class AsyncCallRec extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                wsExecute();
            } catch (Exception e) {
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            wsFinished();
        }

        @Override
        protected void onPreExecute() {
            try {
            } catch (Exception e) {
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            try {
                Prg.setText(sprog);
            } catch (Exception e) {
            }
        }

    }


    // WEB SERVICE - ENVIO

    private boolean sendData() {

        clsRegistro_handheldObj regHH = new clsRegistro_handheldObj(this, Con, db);
        errflag=false;
        senv = "Envío terminado \n \n";

        try {
            regHH.fill();
            gl.IDregistro = regHH.first().id_registro;

            if(gl.tipoInv==1) {

                if (!envioInvCiego()) return false;
            }
            else if(gl.tipoInv==2 || gl.tipoInv==3){

                if (!envioInvDetalle()) return false;
            }
            else if(gl.tipoInv==5){
                if (!envioInvCiegoRfid()) return false;
            }

        } catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            errflag=true;fterr += "\n" + e.getMessage();dbg = e.getMessage();
            return false;
        }

        return true;
    }

    public void envCompleto(){
        clsInventario_encabezadoObj invEnc = new clsInventario_encabezadoObj(this, Con, db);
        clsInventario_ciegoObj invCiego = new clsInventario_ciegoObj(this, Con, db);
        clsInventario_ciego_RfidObj invCiegoRfid = new clsInventario_ciego_RfidObj(this, Con, db);
        clsInventario_detalleObj invDet = new clsInventario_detalleObj(this, Con, db);
        Cursor dt;
        String ss;
        String tn;
        Integer count,rg;

        if(gl.validaLicDB==0){
            msgbox("Base de datos vacia, recibir datos");
            relEnv.setVisibility(View.INVISIBLE);
            gl.validaLicDB=2;
        }else if(gl.validaLicDB==10){


            invEnc.fill();
            gl.tipoInv =  invEnc.first().tipo_inventario;
            gl.idInvEnc = invEnc.first().id_inventario_enc;

            count = 0;

            if(gl.tipoInv==1){

                invCiego.fill("WHERE ID_INVENTARIO_ENC = "+ gl.idInvEnc +" AND COMUNICADO = 'N'");
                count = invCiego.count;

            }
            else if(gl.tipoInv==2 || gl.tipoInv==3){

                invDet.fill("WHERE ID_INVENTARIO_ENC = "+ gl.idInvEnc +" AND COMUNICADO = 'N'");
                count = invDet.count;
            }
            else if(gl.tipoInv==5){
                invCiegoRfid.fill("WHERE ID_INVENTARIO_ENC = "+ gl.idInvEnc +" AND COMUNICADO = 'N'");
                count = invCiegoRfid.count;
            }


            if(count==0) ftflag = true;

            if(!ftflag){
                relRec.setVisibility(View.INVISIBLE);
                relEnv.setVisibility(View.VISIBLE);
                ret = true;
            }else {
                relRec.setVisibility(View.VISIBLE);
                relEnv.setVisibility(View.INVISIBLE);
                ret =  true;
            }

        }
    }

    public boolean envioUsuarios() {
        String ss;

        fterr = "";
        try {
            dbld.clear();
            dbld.insert("Usuario", "WHERE 1=1");

            listItems.clear();
            for (int i = 0; i < dbld.items.size(); i++) {
                listItems.add(dbld.items.get(i));
            }

            for (int i = 0; i < listItems.size(); i++) {

                ss=listItems.get(i);
                dbld.clear();
                dbld.add(ss);

                try {
                    if (commitSQL() == 1) {
                        //
                    } else {
                        fterr += sstr;dbg +=ss +" , ***";
                    }
                } catch (Exception e) {
                    addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
                    errflag=true;fterr += "\n" + e.getMessage();dbg = e.getMessage();
                }

            }
        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            errflag=true;fterr = e.getMessage();dbg = fterr;
        }

        return true;
    }

    public boolean envioInvCiego() {
        String ss;

        fterr = "";
        try {
            sprog = "Inventario Ciego...";wsStask.onProgressUpdate();

            dbld.clear();
            if (dbld.insert("temp_inventario_ciego", "WHERE ELIMINADO=0 AND ID_INVENTARIO_ENC = '"+ gl.idInvEnc+"'")){
                try {
                    //if (commitSQL() == 1) {

                        if(Procesar_Inventario_Ciego(gl.idInvEnc, gl.IDregistro)){
                            ss = "UPDATE INVENTARIO_CIEGO SET COMUNICADO = 'S'";
                            db.execSQL(ss);
                        }else{
                            errflag=true;
                            fterr="Error al procesar el inventario ciego";
                            return false;
                        }
                    /*} else {
                        errflag=true;
                        fterr = sstr;dbg +=" , ***";
                        return false;
                    }*/
                } catch (Exception e) {
                    addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
                    errflag=true;fterr=e.getMessage();dbg = e.getMessage();
                    return false;
                }
            } else {
                errflag=true;fterr="Error al crear los datos de inventario ciego";
                return false;
            }

        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            errflag=true;fterr = e.getMessage();dbg = fterr;
            return false;
        }

        return true;
    }

    public boolean envioInvCiegoRfid(){
        String ss;

        fterr = "";
        try {
            sprog = "Inventario Ciego Rfid...";wsStask.onProgressUpdate();

            dbld.clear();
            if (dbld.insertrfid("temp_inventario_ciego_rfid", "WHERE ELIMINADO=0 AND ID_INVENTARIO_ENC = '"+ gl.idInvEnc+"'")){
                try {

                    if(Procesar_Inventario_Ciego_Rfid(gl.idInvEnc, gl.IDregistro)){
                        ss = "UPDATE INVENTARIO_CIEGO_RFID SET COMUNICADO = 'S'";
                        db.execSQL(ss);
                    }else{
                        errflag=true;
                        fterr="Error al procesar el inventario ciego rfid";
                        return false;
                    }

                } catch (Exception e) {
                    addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
                    errflag=true;fterr=e.getMessage();dbg = e.getMessage();
                    return false;
                }
            } else {
                errflag=true;fterr="Error al crear los datos de inventario ciego rfid";
                return false;
            }

        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            errflag=true;fterr = e.getMessage();dbg = fterr;
            return false;
        }

        return true;
    }

    public boolean envioInvDetalle() {
        String ss;

        fterr = "";
        try {
            sprog = "Detalle de Inventario...";wsStask.onProgressUpdate();

            dbld.clear();
            if(dbld.insert("temp_inventario_detalle", "WHERE ELIMINADO=0 AND ID_INVENTARIO_ENC = '"+ gl.idInvEnc+"'")){
                try {
                    //if (commitSQL() == 1) {
                        if(Procesar_Inventario_Detalle(gl.idInvEnc, gl.IDregistro)){
                            ss = "UPDATE INVENTARIO_DETALLE SET COMUNICADO = 'S'";
                            db.execSQL(ss);

                        }else{
                            errflag=true;
                            fterr="Error al procesar el detalle del inventario";
                            return false;
                        }
                    /*} else {
                        errflag=true;
                        fterr = sstr;dbg +=" , ***";
                        return false;
                    }*/
                } catch (Exception e) {
                    addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
                    errflag=true;fterr += "\n" + e.getMessage();dbg = e.getMessage();
                    return false;
                }
            } else {
                errflag=true;fterr="Error al crear los datos del detalle del inventario";
                return false;
            }

        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            errflag=true;fterr = e.getMessage();dbg = fterr;
            return false;
        }

        return true;
    }

    // Web Service handling Methods

    public void wsSendExecute(){

        fterr="No connect";scon=0;

        try {

            if (getTest()==1) scon=1;

            if (scon==1) {
                fterr="Sync OK";

                if (!sendData()) {
                    fterr="Envio incompleto : "+sstr;
                } else {
                }
            } else {
                fterr="No se puede conectar al web service : "+sstr;
                errflag=true;
            }

        } catch (Exception e) {
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
            scon=0;
            fterr="No se puede conectar al web service. "+e.getMessage();
            errflag=true;
        }
    }

    public void wsSendFinished(){
        Prg.setVisibility(View.INVISIBLE);
        prgBar.setVisibility(View.INVISIBLE);

        if (errflag) {
            mu.msgbox(fterr);
        }else{
            msgAskExit("Envio completo");
        }

        isbusy=0;
    }

    private class AsyncCallSend extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                Looper.prepare();
                wsSendExecute();
            } catch (Exception e) {
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            wsSendFinished();
            Looper.loop();
        }

        @Override
        protected void onPreExecute() {  }

        @Override
        protected void onProgressUpdate(Void... values) {
            try {
                Prg.setText(sprog);
            } catch (Exception e) {
            }
        }

    }


    // Aux

    private void msgAskExit(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setCancelable(false);
        dialog.setTitle(R.string.app_name);
        dialog.setMessage(msg);

        dialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                if(gl.validaLicDB==2) gl.validaLicDB=4;
                if(ret){
                    MPrincipal();
                }
                finish();
            }
        });

        dialog.show();

    }

    private void msgLic(String msg) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setCancelable(false);
        dialog.setTitle(R.string.app_name);
        dialog.setMessage(msg);

        dialog.setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Prg.setText("");
                Prg2.setText("Su número de activación es: "+gl.NoSerieHH);
            }
        });

        dialog.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        dialog.show();

    }

    private void MPrincipal(){
        startActivity(new Intent(this, MenuPrincipal.class));
    }

    // Activity Events

    @Override
    public void onBackPressed() {
        if(gl.validaLicDB==2) super.finish();
        if (isbusy==0) super.onBackPressed();
    }


}
