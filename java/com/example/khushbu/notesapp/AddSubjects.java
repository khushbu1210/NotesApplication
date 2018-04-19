package com.example.kashyap.notesapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

public class AddSubjects extends AppCompatActivity implements View.OnClickListener{

    // Database name
    private static final String DATABASE_NAME = "Notes";

    // SQL Create statement
    private static final String CREATE_SUBJECT_TABLE = "CREATE TABLE IF NOT EXISTS tbl_subject (subjectid INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "subjectname TEXT);";

    // Database instance
    private SQLiteDatabase mDatabase;

    EditText etSubject;
    Button btnSave;
    Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_subject);

        openOrCreateDatabase();
        gatherControls();
    }

    private void gatherControls() {
        etSubject = (EditText)findViewById(R.id.etSubject);

        btnSave = (Button)findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);

        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);
    }

    public void openOrCreateDatabase() {
        mDatabase = openOrCreateDatabase(DATABASE_NAME, SQLiteDatabase.CREATE_IF_NECESSARY, null);

        mDatabase.setLocale(Locale.getDefault());
        mDatabase.setVersion(1);

        mDatabase.execSQL(CREATE_SUBJECT_TABLE);
    }

    @Override
    public void onClick(View v) {
        Button b = (Button)v;

        if (b.getText().equals("Save")) {
//            if(saveSubject()) {
//                finish();
//            }
        }
        else {
            finish();
        }

//        Intent i = new Intent(getApplicationContext(), MainActivity.class);
//        startActivity(i);
    }

    private boolean saveSubject() {
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
}
