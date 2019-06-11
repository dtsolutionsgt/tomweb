package com.dts.base;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

public class appGlobals extends Application {

	public Context context;

	// Variables publicas

	public String nombreusuario;
	public boolean exitapp;

	// Variables guardados al momento de botar la aplicacion

	public int userid,rolid,itemid,itemidx;

	public void saveInstance(Bundle savedInstanceState) {
		try {
			savedInstanceState.putInt("userid",userid);
			savedInstanceState.putInt("rolid",rolid);
			savedInstanceState.putInt("itemid",itemid);
		} catch (Exception e) {
			toastlong("Save Instance : "+e.getMessage());
		}
	}

	public void restoreInstance(Bundle savedInstanceState) {
		try {
			userid=savedInstanceState.getInt("userid");
			rolid=savedInstanceState.getInt("rolid");
			itemid=savedInstanceState.getInt("itemid");
		} catch (Exception e) {
			toastlong("Restore Instance : "+e.getMessage());
			userid=0;rolid=0;itemid=0;
		}
	}

	private void toastlong(String msg) {

		Toast toast= Toast.makeText(getApplicationContext(),msg, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

}
