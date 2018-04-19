package com.example.kashyap.notesapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.*;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener, ListView.OnItemLongClickListener {

    // Database name
    private static final String DATABASE_NAME = "Notes";

    // SQL Create statement
    private static final String CREATE_SUBJECT_TABLE = "CREATE TABLE IF NOT EXISTS tbl_subject (subjectid INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "subjectname TEXT);";

    // Database instance
    private SQLiteDatabase mDatabase;

    ListView lvSubject;

    private ArrayAdapter<String> listSubject;

    String strDeleteSubject = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openOrCreateDatabase();
        gatherControls();

        fetchAllSubjects();
    }

    public void openOrCreateDatabase() {
        mDatabase = openOrCreateDatabase(DATABASE_NAME, SQLiteDatabase.CREATE_IF_NECESSARY, null);

        mDatabase.setLocale(Locale.getDefault());
        mDatabase.setVersion(1);

        mDatabase.execSQL(CREATE_SUBJECT_TABLE);
    }

    private void gatherControls() {
        lvSubject = (ListView) findViewById(R.id.lvSubject);
        lvSubject.setOnItemClickListener(this);
        lvSubject.setLongClickable(true);
        lvSubject.setOnItemLongClickListener(this);
    }

    private void fetchAllSubjects() {
        Cursor c = mDatabase.query("tbl_subject", null, null, null, null, null, null);
        c.moveToFirst();

        ArrayList<String> arrSubject = new ArrayList<>();

        while (!c.isAfterLast()) {
            arrSubject.add(c.getString(c.getColumnIndex("subjectname")));

            c.moveToNext();
        }

        c.close();

        listSubject = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrSubject);

        lvSubject.setAdapter(listSubject);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (strDeleteSubject == null) {
            Intent i = new Intent(getApplicationContext(), NotesList.class);
            i.putExtra("SubjectName", lvSubject.getItemAtPosition(position).toString());
            startActivity(i);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add_new_item:

                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.add_subject);
//                dialog.setTitle("New Note");
                dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
                TextView tvTitle = (TextView) dialog.findViewById(R.id.tvTitle);
                tvTitle.setText("New Subject");
                final EditText etSubject = (EditText) dialog.findViewById(R.id.etSubject);
                etSubject.setHint("Enter note title");
                Button btnSave = (Button) dialog.findViewById(R.id.btnSave);

                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (saveSubject(etSubject)) {
                            dialog.dismiss();
                            fetchAllSubjects();
                        }
                    }
                });

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
                break;
        }
        return true;
    }

    private boolean saveSubject(EditText etSubject) {
        if (etSubject.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter Subject", Toast.LENGTH_SHORT).show();
            etSubject.requestFocus();
            return false;
        }

        ContentValues values = new ContentValues();
        values.put("subjectname", etSubject.getText().toString());

        mDatabase.insert("tbl_subject", null, values);

        return true;
    }

    private void deleteSubject() {
        if (strDeleteSubject != null) {
            String astrArgs[] = {strDeleteSubject.toString()};
            mDatabase.delete("tbl_subject", "subjectname=?", astrArgs);

            strDeleteSubject = null;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        strDeleteSubject = listSubject.getItem(position);

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.menu_delete);
        dialog.setTitle("Delete");
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        Button btnDelete = (Button) dialog.findViewById(R.id.btnDelete);
        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                showAlert();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strDeleteSubject = null;
                dialog.dismiss();
            }
        });

        dialog.show();

        return false;
    }

    private void showAlert() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Delete");
        dialogBuilder.setMessage("Are you sure?");
        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogAlert, int which) {
                deleteSubject();
                fetchAllSubjects();
            }
        });

        dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogAlert, int which) {
                strDeleteSubject = null;
                dialogAlert.dismiss();
            }
        });

        AlertDialog alert = dialogBuilder.create();
        alert.show();
    }
}
