package com.dts.tomweb;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import android.support.v4.app.ActivityCompat;


public class MainActivity extends Activity {

    private boolean complete=false;

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
    }

    private void grantPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    startApplication();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            } else {
                startApplication();
            }
        } catch (Exception e) {}
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
