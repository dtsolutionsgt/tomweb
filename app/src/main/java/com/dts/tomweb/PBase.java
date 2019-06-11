package com.dts.tomweb;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.dts.base.AppMethods;
import com.dts.base.BaseDatos;
import com.dts.base.DateUtils;
import com.dts.base.MiscUtils;
import com.dts.base.appGlobals;
import com.dts.base.clsClasses;

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

    protected int callback =0,mode;
    protected int selid,selidx;
    protected long fecha,stamp;
    protected String s;


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

    public void exec() {
        db.execSQL(sql);
    }

    public Cursor open() {
        Cursor dt;

        dt=Con.OpenDT(sql);
        try {
            dt.moveToFirst();
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
