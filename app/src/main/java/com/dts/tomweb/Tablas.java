package com.dts.tomweb;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.dts.listadapt.LA_Tablas;
import com.dts.listadapt.LA_Tablas2;

import java.util.ArrayList;


public class Tablas extends PBase {

    private GridView grid,dgrid;
    private Spinner spin,spinf;
    private ProgressBar pbar;
    private EditText txt1;

    private ArrayList<String> spinlist = new ArrayList<String>();
    private ArrayList<String> values=new ArrayList<String>();
    private ArrayList<String> dvalues=new ArrayList<String>();
    private LA_Tablas adapter;
    private LA_Tablas2 dadapter;

    private int cw;
    private String scod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tablas);

        super.InitBase(savedInstanceState);

        grid = (GridView) findViewById(R.id.gridview1);
        dgrid = (GridView) findViewById(R.id.gridview2);
        spin = (Spinner) findViewById(R.id.spinner);
        pbar = (ProgressBar) findViewById(R.id.progressBar3);
        pbar.setVisibility(View.INVISIBLE);
        txt1 = (EditText) findViewById(R.id.editText1);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        cw = (int) ((displayMetrics.widthPixels - 22) / 5) - 1;

        setHandlers();

        fillSpinner();


    }

    //region Events

    public void doClear(View view) {
        try {
            txt1.setText("");
            txt1.requestFocus();
        } catch (Exception e) {
        }
    }

    public void doExit(View view) {
        finish();
    }

    public void doHelp(View view) { }

    private void setHandlers() {

        try {
            spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    TextView spinlabel;

                    try {
                        spinlabel = (TextView) parentView.getChildAt(0);
                        spinlabel.setTextColor(Color.BLACK);
                        spinlabel.setPadding(5, 0, 0, 0);
                        spinlabel.setTextSize(18);
                        spinlabel.setTypeface(spinlabel.getTypeface(), Typeface.BOLD);

                        scod = spinlist.get(position);
                        if (!scod.equalsIgnoreCase(" ")) {
                            txt1.setText("");
                            processTable();
                        }
                    } catch (Exception e) {
                        mu.msgbox(e.getMessage());
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    return;
                }

            });

            grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    try {
                        Object lvObj = grid.getItemAtPosition(position);
                        String item = (String) lvObj;

                        adapter.setSelectedIndex(position);
                        toast(item);
                    } catch (Exception e) {
                        mu.msgbox(e.getMessage());
                    }
                }

                ;
            });

            dgrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    try {
                        Object lvObj = dgrid.getItemAtPosition(position);
                        String item = (String) lvObj;

                        dadapter.setSelectedIndex(position);
                        toast(item);
                    } catch (Exception e) {
                        mu.msgbox(e.getMessage());
                    }
                }

                ;
            });

            dgrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                    try {
                        Object lvObj = dgrid.getItemAtPosition(position);
                        String item = (String) lvObj;

                        adapter.setSelectedIndex(position);
                        msgbox(item);
                    } catch (Exception e) {
                    }
                    return true;
                }
            });

            txt1.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                    if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                        switch (arg1) {
                            case KeyEvent.KEYCODE_ENTER:
                                processTable();
                                return true;
                        }
                    }
                    return false;
                }
            });
        } catch (Exception e) {
        }

    }

    //endregion


    //region Main

    private void processTable() {
        try {
            pbar.setVisibility(View.VISIBLE);

            Handler mmtimer = new Handler();
            Runnable mmrunner = new Runnable() {
                @Override
                public void run() {
                    pbar.setVisibility(View.VISIBLE);

                    values.clear();
                    dvalues.clear();

                    adapter = new LA_Tablas(Tablas.this, values);
                    grid.setAdapter(adapter);

                    dadapter = new LA_Tablas2(Tablas.this, dvalues);
                    dgrid.setAdapter(dadapter);
                }
            };
            mmtimer.postDelayed(mmrunner, 50);

            Handler mtimer = new Handler();
            Runnable mrunner = new Runnable() {
                @Override
                public void run() {
                    showData(scod);
                }
            };
            mtimer.postDelayed(mrunner, 1000);
        } catch (Exception e) {
        }

    }

    private void showData(String tn) {
        Cursor PRG, dt;
        String n, flt, ss = "";
        int cc = 1, j;

        try {
            try {
                ss = "SELECT ";

                sql = "PRAGMA table_info('" + tn + "')";
                PRG = db.rawQuery(sql, null);
                cc = PRG.getCount();

                PRG.moveToFirst();
                j = 0;

                while (!PRG.isAfterLast()) {
                    n = PRG.getString(PRG.getColumnIndex("name"));
                    // t=PRG.getString(PRG.getColumnIndex("type"));// INTEGER , TEXT , REAL

                    values.add(n);
                    ss = ss + n;
                    if (j < cc - 1) ss = ss + ",";
                    PRG.moveToNext();
                    j++;
                }

                ss = ss + " FROM " + tn;

                flt = txt1.getText().toString();
                if (!mu.emptystr(flt)) ss = ss + " WHERE " + flt;

            } catch (Exception e) {
            }

            ViewGroup.LayoutParams layoutParams = grid.getLayoutParams();
            layoutParams.width = ((int) (cw * cc)) + 25;
            grid.setLayoutParams(layoutParams);

            grid.setColumnWidth(cw);
            grid.setStretchMode(GridView.NO_STRETCH);
            grid.setNumColumns(cc);

            adapter = new LA_Tablas(this, values);
            grid.setAdapter(adapter);


            ViewGroup.LayoutParams dlayoutParams = dgrid.getLayoutParams();
            dlayoutParams.width = ((int) (cw * cc)) + 25;
            dgrid.setLayoutParams(dlayoutParams);

            dgrid.setColumnWidth(cw);
            dgrid.setStretchMode(GridView.NO_STRETCH);
            dgrid.setNumColumns(cc);

            try {
                dt = Con.OpenDT(ss);
                if (dt.getCount() == 0) {
                    pbar.setVisibility(View.INVISIBLE);
                    return;
                }

                dt.moveToFirst();
                while (!dt.isAfterLast()) {

                    for (int i = 0; i < cc; i++) {
                        try {
                            ss = dt.getString(i);
                        } catch (Exception e) {
                            ss = "?";
                        }
                        dvalues.add(ss);
                    }
                    dt.moveToNext();
                }
                if (dt!=null) dt.close();
            } catch (Exception e) {
            }

            dadapter = new LA_Tablas2(this, dvalues);
            dgrid.setAdapter(dadapter);

            pbar.setVisibility(View.INVISIBLE);
        }catch (Exception e){
        }


    }

    //endregion

    //region Aux

    private void fillSpinner() {
        Cursor DT;

        spinlist.clear();
        spinlist.add(" ");

        try {
            try {
                sql = "SELECT name FROM sqlite_master WHERE type='table' AND name<>'android_metadata' order by name";
                DT = Con.OpenDT(sql);

                DT.moveToFirst();
                while (!DT.isAfterLast()) {
                    spinlist.add(DT.getString(0));
                    DT.moveToNext();
                }
                if (DT!=null) DT.close();
            } catch (Exception e) {
                mu.msgbox(e.getMessage());
            }

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinlist);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spin.setAdapter(dataAdapter);
        }catch (Exception e){
        }

    }

    //endregion

}
