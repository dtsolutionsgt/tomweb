package com.dts.tomweb;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.dts.base.AppMethods;
import com.dts.base.BaseDatos;
import com.dts.base.DateUtils;
import com.dts.base.MiscUtils;
import com.dts.base.appGlobals;
import com.dts.base.clsClasses;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class PBase extends Activity {

    protected int active;
    protected SQLiteDatabase db;
    protected BaseDatos Con;
    protected BaseDatos.Insert ins;
    protected BaseDatos.Update upd;
    protected String sql;

    public appGlobals gl;
    public MiscUtils mu;
    public DateUtils du;
    public AppMethods app;
    public clsClasses clsCls = new clsClasses();

    protected InputMethodManager keyboard;

    protected int callback,mode;
    protected int selid,selidx;
    protected long fecha,stamp;
    protected String s;

    public TextView popup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pbase);
    }

    public void InitBase(Bundle savedInstanceState) {

        Con = new BaseDatos(this);
        opendb();
        ins=Con.Ins;upd=Con.Upd;

        gl=((appGlobals) this.getApplication());
        gl.context=this;

        mu=new MiscUtils(this,gl);
        du=new DateUtils();
        app=new AppMethods(this,gl,Con,db);

        fecha=du.getActDateTime();stamp=du.getActDate();

        selid=-1;selidx=-1;
        callback =0;

        holdInstance(savedInstanceState);

    }

    public void opendb() {
        try {
            db = Con.getWritableDatabase();
            Con.vDatabase =db;
            active=1;
        } catch (Exception e) {
            mu.msgbox(e.getMessage());
            active= 0;
        }
    }


    protected void addlog(final String methodname, String msg, String info) {

        final String vmethodname = methodname;
        final String vmsg = msg;
        final String vinfo = info;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setAddlog(vmethodname,vmsg, vinfo);
            }
        }, 500);

    }

    protected void setAddlog(String methodname,String msg,String info) {

        BufferedWriter writer = null;
        FileWriter wfile;

        try {

            String fname = Environment.getExternalStorageDirectory()+"/Tomlog.txt";
            wfile=new FileWriter(fname,true);
            writer = new BufferedWriter(wfile);

            writer.write("Método: " + methodname + " Mensaje: " +msg + " Info: "+ info );
            writer.write("\r\n");

            writer.close();

        } catch (Exception e) {
            msgbox("Error " + e.getMessage());
        }
    }

    public void exec() {
        db.execSQL(sql);
    }

    public Cursor open() {
        Cursor dt;

        dt=Con.OpenDT(sql);
        try {
            dt.moveToFirst();
            if (dt!=null) dt.close();
        } catch (Exception e) {
        }

        return dt;
    }


    // Messages

    protected void toast(String msg) {

        if (mu.emptystr(msg)) return;

        Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    protected void toastlong(String msg) {

        if (mu.emptystr(msg)) return;

        Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    protected void toast(double val) {
        this.toast(""+val);
    }

    protected void msgbox(String msg){
        mu.msgbox(msg);
    }

    protected void msgbox(int val){
        mu.msgbox(""+val);
    }

    protected void msgbox(double val){
        mu.msgbox(""+val);
    }

    public void PopUp(String tx){

        try {
            // inflate the layout of the popup window
            LayoutInflater inflater = (LayoutInflater)
                    getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.popup_window, null);

            // create the popup window
            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true; // lets taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            // show the popup window
            // which view you pass in doesn't matter, it is only used for the window tolken
            popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

            popup = (TextView) popupView.findViewById(R.id.txtPopUp);
            popup.setText(tx);

            // dismiss the popup window when touched
            popupView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popupWindow.dismiss();
                    return true;
                }
            });
        }catch (Exception e){
            addlog(new Object() {}.getClass().getEnclosingMethod().getName(), e.getMessage(), "");
        }

    }



    // Instance

    protected void holdInstance(Bundle savedInstanceState) {
        if (savedInstanceState != null) gl.restoreInstance(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        gl.saveInstance(savedInstanceState);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        gl.restoreInstance(savedInstanceState);
    }


    // Misc

    protected boolean emptystr(String str) {
        return mu.emptystr(str);
    }


    // Activity Events

    @Override
    protected void onResume() {
        opendb();
        super.onResume();
    }

    @Override
    protected void onPause() {
        try {
            Con.close();   }
        catch (Exception e) { }
        active= 0;
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
