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

			sql = "CREATE TABLE [Articulo] (" +
					"id_articulo TEXT NOT NULL," +
					"id_empresa INTEGER NOT NULL," +
					"codigo_barra TEXT NOT NULL," +
					"descripcion TEXT NOT NULL," +
					"costo REAL NOT NULL," +
					"tipo_conteo TEXT NOT NULL," +
					"PRIMARY KEY ([id_articulo])" +
					");";
			db.execSQL(sql);

			sql = "CREATE INDEX Articulo_idx1 ON Articulo(codigo_barra)";
			db.execSQL(sql);
			sql = "CREATE INDEX Articulo_idx2 ON Articulo(descripcion)";
			db.execSQL(sql);

			sql = "CREATE TABLE [Articulo_codigo_barra] (" +
					"id_articulo TEXT NOT NULL," +
					"codigo_barra TEXT NOT NULL," +
					"id_empresa INTEGER NOT NULL," +
					"PRIMARY KEY ([id_empresa],[codigo_barra],[id_articulo])" +
					");";
			db.execSQL(sql);

			sql = "CREATE TABLE [Estado_inventario] (" +
					"Id_estado INTEGER NOT NULL," +
					"nombre TEXT NOT NULL," +
					"PRIMARY KEY ([Id_estado])" +
					");";
			db.execSQL(sql);

			sql = "CREATE TABLE [Estatus_handheld] (" +
					"id INTEGER NOT NULL," +
					"nombre TEXT NOT NULL," +
					"PRIMARY KEY ([id])" +
					");";
			db.execSQL(sql);

			sql = "CREATE TABLE [Inventario_ciego] (" +
					"id_inventario_enc INTEGER NOT NULL," +
					"codigo_barra TEXT NOT NULL," +
					"cantidad REAL NOT NULL," +
					"id INTEGER PRIMARY KEY AUTOINCREMENT," +
					"comunicado TEXT NOT NULL," +
					"ubicacion TEXT NOT NULL," +
					"id_operador INTEGER NOT NULL," +
					"fecha TEXT NOT NULL," +
					"Id_registro INTEGER NOT NULL," +
					"eliminado INTEGER NOT NULL" +
					")";
			db.execSQL(sql);

			sql = "CREATE TABLE [Inventario_detalle] (" +
					"id_inventario_det INTEGER PRIMARY KEY AUTOINCREMENT," +
					"id_inventario_enc INTEGER NOT NULL," +
					"id_articulo TEXT NOT NULL," +
					"ubicacion TEXT NOT NULL," +
					"cantidad REAL NOT NULL," +
					"codigo_barra TEXT NOT NULL," +
					"comunicado TEXT NOT NULL," +
					"id_operador INTEGER NOT NULL," +
					"fecha TEXT NOT NULL," +
					"Id_registro INTEGER NOT NULL," +
					"eliminado INTEGER NOT NULL" +
					")";
			db.execSQL(sql);

			sql = "CREATE TABLE [Inventario_encabezado] (" +
					"id_inventario_enc INTEGER NOT NULL," +
					"id_estado TEXT NOT NULL," +
					"id_empresa INTEGER NOT NULL," +
					"fecha_inicio TEXT NOT NULL," +
					"fecha_final TEXT NOT NULL," +
					"nombre TEXT NOT NULL," +
					"id_usuario INTEGER NOT NULL," +
					"tipo_inventario INTEGER NOT NULL," +
					"PRIMARY KEY ([id_inventario_enc])" +
					");";
			db.execSQL(sql);

			sql = "CREATE TABLE [Inventario_operador] (" +
					"id_inventario_enc INTEGER NOT NULL," +
					"id_operador INTEGER NOT NULL," +
					"PRIMARY KEY ([id_inventario_enc],[id_operador])" +
					");";
			db.execSQL(sql);

			sql = "CREATE TABLE [Inventario_teorico] (" +
					"id_empresa INTEGER NOT NULL," +
					"id_articulo TEXT NOT NULL," +
					"descripcion TEXT NOT NULL," +
					"cantidad REAL NOT NULL," +
					"codigo_barra TEXT NOT NULL," +
					"costo REAL NOT NULL," +
					"tipo_conteo TEXT NOT NULL," +
					"id_inventario_enc INTEGER NOT NULL" +
					");";
			db.execSQL(sql);

			sql = "CREATE TABLE [Operadores] (" +
					"id_operador INTEGER NOT NULL," +
					"id_empresa INTEGER NOT NULL," +
					"codigo TEXT NOT NULL," +
					"clave TEXT NOT NULL," +
					"nombre REAL NOT NULL," +
					"PRIMARY KEY ([id_operador])" +
					");";
			db.execSQL(sql);

			sql = "CREATE TABLE [Registro_handheld] (" +
					"id_registro INTEGER NOT NULL," +
					"id_empresa INTEGER NOT NULL," +
					"fecha_registro TEXT NOT NULL," +
					"serie_dispositivo TEXT NOT NULL," +
					"id_estatus TEXT NOT NULL," +
					"id_pais TEXT NOT NULL," +
					"descripcion TEXT NOT NULL," +
					"PRIMARY KEY ([id_empresa],[serie_dispositivo])" +
					");";
			db.execSQL(sql);


			//*******************************************

			sql = "CREATE TABLE [Params] (" +
					"ID integer NOT NULL," +
					"dbver INTEGER  NOT NULL," +
					"param1 TEXT  NOT NULL," +
					"param2 TEXT  NOT NULL," +
					"param3 INTEGER  NOT NULL," +
					"param4 INTEGER  NOT NULL," +
					"lic1 TEXT  NOT NULL," +
					"lic2 TEXT  NOT NULL," +
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
			db.execSQL("INSERT INTO Params VALUES (0,0,'','',0,0,'','');");

			return 1;
		} catch (SQLiteException e) {
			msgbox(e.getMessage());
			return 0;
		}

	}
	
	private void msgbox(String msg) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(vcontext);

        dialog.setCancelable(false);
		dialog.setTitle(R.string.app_name);
		dialog.setMessage(msg);

		dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int which) {}
    	});
		dialog.show();
	
	}   	
	
}