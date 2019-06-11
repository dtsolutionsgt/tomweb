package com.dts.base;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.Gravity;
import android.widget.Toast;

import java.util.Calendar;

public class AppMethods {

	private Context cont;
	private appGlobals gl;
	private SQLiteDatabase db;
	private BaseDatos.Insert ins;
	private BaseDatos.Update upd;
	private BaseDatos Con;
	
	public AppMethods(Context context, appGlobals global, BaseDatos dbconnection, SQLiteDatabase database) {
		cont=context; 
		gl=global;
		Con=dbconnection;
		db=database;
		
		ins=Con.Ins;
		upd=Con.Upd;
	}
	
	public void reconnect(BaseDatos dbconnection, SQLiteDatabase database) {
		Con=dbconnection;
		db=database;
		
		ins=Con.Ins;
		upd=Con.Upd;
	}
	
	
	// Public
	
	public String getCorrel() {
		int f,cyear,cmonth,cday,ch,cm,cs,vd,vh;

		final Calendar c = Calendar.getInstance();

		cyear = c.get(Calendar.YEAR);cyear=cyear % 10;
		cmonth = c.get(Calendar.MONTH)+1;
		cday = c.get(Calendar.DAY_OF_MONTH);
		ch=c.get(Calendar.HOUR_OF_DAY);
		cm=c.get(Calendar.MINUTE);
		cs=c.get(Calendar.SECOND);

		vd=cyear*384+cmonth*32+cday;
		vh=ch*3600+cm*60+cs;

		f=vd*100000+vh;

		return f+"_";
	}
	

	// Common
	
	protected void toast(String msg) {
		Toast toast= Toast.makeText(cont,msg, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.TOP, 0, 0);
		toast.show();
	}


}
