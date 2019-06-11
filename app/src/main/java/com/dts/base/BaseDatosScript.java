package com.dts.base;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.dts.tomweb.R;

public class BaseDatosScript {
	
	private Context vcontext;
	
	public BaseDatosScript(Context context) {
		vcontext=context;
	}
	
	public int scriptDatabase(SQLiteDatabase database) {
		try {
			if (scriptTablas(database)==0) return 0; else return 1;
		} catch (SQLiteException e) {
			msgbox(e.getMessage());
			return 0;
		}
	}

	private int scriptTablas(SQLiteDatabase db) {
		String sql;

		try {

			sql = "CREATE TABLE [Usuario] (" +
					"[ID] INTEGER NOT NULL," +
					"[Nombre] TEXT NOT NULL," +
					"[Activo] TEXT NOT NULL," +
					"[Login] TEXT NOT NULL," +
					"[Clave] TEXT NOT NULL," +
					"[Rol] INTEGER NOT NULL," +
					"PRIMARY KEY ([ID])" +
					");";
			db.execSQL(sql);

			sql = "CREATE INDEX Usuario_idx1 ON Usuario(Nombre)";
			db.execSQL(sql);


			sql="CREATE TABLE [Rol] ("+
					"ID INTEGER NOT NULL,"+
					"Nombre TEXT NOT NULL,"+
					"PRIMARY KEY ([ID])"+
					");";
			db.execSQL(sql);

			sql="CREATE INDEX Rol_idx1 ON Rol(Nombre)";
			db.execSQL(sql);


			sql = "CREATE TABLE [Params] (" +
					"ID integer NOT NULL," +
					"dbver INTEGER  NOT NULL," +
					"param1 TEXT  NOT NULL," +
					"param2 TEXT  NOT NULL," +
					"param3 INTEGER  NOT NULL," +
					"param4 INTEGER  NOT NULL," +
					"lic1 TEXT  NOT NULL," +
					"lic2 INTEGER  NOT NULL," +
					"PRIMARY KEY ([ID])" +
					");";
			db.execSQL(sql);

			return 1;

		} catch (SQLiteException e) {
			msgbox(e.getMessage());
			return 0;
		}
	}

	public int scriptData(SQLiteDatabase db) {

		try {
			db.execSQL("INSERT INTO Params VALUES (0,0,'','',0,0,'',0);");

			db.execSQL("INSERT INTO Rol VALUES (1,'Operador');");
			db.execSQL("INSERT INTO Rol VALUES (2,'Supervisor');");
			db.execSQL("INSERT INTO Rol VALUES (3,'Administrador');");
			db.execSQL("INSERT INTO Rol VALUES (4,'Gerente');");

			db.execSQL("INSERT INTO Usuario VALUES (1,'Desarrollo',1,'1','1',0);");

			return 1;
		} catch (SQLiteException e) {
			msgbox(e.getMessage());
			return 0;
		}

	}
	
	private void msgbox(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(vcontext);
    	
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg);

		dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {}
    	});
		dialog.show();
	
	}   	
	
}