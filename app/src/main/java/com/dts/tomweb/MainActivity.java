package com.dts.tomweb;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import android.support.v4.app.ActivityCompat;

import com.dts.base.MiscUtils;
import com.dts.base.appGlobals;
import com.dts.base.clsClasses;


public class MainActivity extends Activity {

    private boolean complete=false;

    public appGlobals gl;
    public MiscUtils mu;
    public PBase base;
    public clsClasses clsCls = new clsClasses();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Handler mtimer = new Handler();
        Runnable mrunner=new Runnable() {
            @Override
            public void run() {
                grantPermissions();
            }
        };
        mtimer.postDelayed(mrunner,50);

        gl=((appGlobals) this.getApplication());
        gl.context=this;

        mu=new MiscUtils(this,gl);

    }

    private void grantPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
                    startApplication();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_PHONE_STATE
                            }, 1);
                }
            } else {
                startApplication();
            }
        } catch (Exception e) {
            Toast.makeText(this, "..."+e, Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("MissingPermission")
    private String androidid() {
        String uniqueID = "";
        try {

            TelephonyManager tm = (TelephonyManager) this.getSystemService(this.TELEPHONY_SERVICE);
            uniqueID = tm.getDeviceId();

            if (uniqueID==null){
                uniqueID = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
            }

        } catch (Exception e) {
            //mu.msgbox(new Object() {}.getClass().getEnclosingMethod().getName() + " . " + e.getMessage());
            uniqueID = "0000000000";
        }

        return uniqueID;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ) {
                Toast.makeText(this, "Permisos aplicados.", Toast.LENGTH_SHORT).show();
                startApplication();
            } else {
                Toast.makeText(this, "Permisos incompletos.", Toast.LENGTH_LONG).show();
                super.finish();
            }
        } catch (Exception e){}
    }

    private void startApplication() {
        Handler mtimer = new Handler();
        Runnable mrunner=new Runnable() {
            @Override
            public void run() {

                Intent intent=new Intent(MainActivity.this,Ingreso.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                gl.NoSerieHH = androidid();
            }
        };
        mtimer.postDelayed(mrunner,50);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (complete) finish();else complete=true;
    }

}
