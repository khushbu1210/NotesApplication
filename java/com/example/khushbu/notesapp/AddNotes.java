package com.example.kashyap.notesapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.kashyap.notesapp.location.AppLocationService;
import com.example.kashyap.notesapp.location.LocationAddress;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddNotes extends AppCompatActivity implements View.OnClickListener {

    // Database name
    private static final String DATABASE_NAME = "Notes";

    // SQL Create statement
    private static final String CREATE_NOTES_TABLE = "CREATE TABLE IF NOT EXISTS tbl_notes (notesid INTEGER " +
            "PRIMARY KEY AUTOINCREMENT, subjectid TEXT, title TEXT, datetime TEXT, location TEXT, notes TEXT);";


    // Database instance
    private SQLiteDatabase mDatabase;

    EditText etTitle;
    EditText etDatetime;
    EditText etLocation;
    EditText etNotes;

    Button btnPhotos;
    Button btnVoiceNotes;
    Button btnSave;
    Button btnCancel;

    String strSubjectname;
    String strNotesTitle;

    private AppLocationService appLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notes);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        strSubjectname = getIntent().getExtras().getString("SubjectName");
        strNotesTitle = getIntent().getExtras().getString("NoteTitle");

        openOrCreateDatabase();
        gatherControls();

        appLocationService = new AppLocationService(AddNotes.this);

        if (strNotesTitle != null) {
            fetchNote();

            etNotes.requestFocus();
        } else {
            etTitle.requestFocus();

            DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
            Date date = new Date();
            etDatetime.setText(dateFormat.format(date));
            fetchLocation();
        }
    }

    public void openOrCreateDatabase() {
        mDatabase = openOrCreateDatabase(DATABASE_NAME, SQLiteDatabase.CREATE_IF_NECESSARY, null);

        mDatabase.setLocale(Locale.getDefault());
        mDatabase.setVersion(1);

        mDatabase.execSQL(CREATE_NOTES_TABLE);
    }

    private void gatherControls() {
        etTitle = (EditText) findViewById(R.id.etTitle);
        etDatetime = (EditText) findViewById(R.id.etDatetime);
        etLocation = (EditText) findViewById(R.id.etLocation);
        etNotes = (EditText) findViewById(R.id.etNotes);

        btnPhotos = (Button) findViewById(R.id.btnPhotos);
        btnPhotos.setOnClickListener(this);

        btnVoiceNotes = (Button) findViewById(R.id.btnVoiceNotes);
        btnVoiceNotes.setOnClickListener(this);

        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);
    }

    private void fetchLocation() {
        Location gpsLocation = appLocationService.getLocation(LocationManager.GPS_PROVIDER);
        if (gpsLocation != null) {
            double latitude = gpsLocation.getLatitude();
            double longitude = gpsLocation.getLongitude();
            LocationAddress locationAddress = new LocationAddress();
            locationAddress.getAddressFromLocation(latitude, longitude, getApplicationContext(), new GeocoderHandler());
        } else {
            showSettingsAlert();
            etLocation.setText("No Location");
        }
    }

    protected Boolean saveNotes() {

        if (etTitle.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter Title", Toast.LENGTH_SHORT).show();
            etTitle.requestFocus();
            return false;
        }

        if (etNotes.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter Note Description", Toast.LENGTH_SHORT).show();
            etNotes.requestFocus();
            return false;
        }

        ContentValues values = new ContentValues();

        values.put("subjectid", strSubjectname);
        values.put("title", etTitle.getText().toString());
        values.put("datetime", etDatetime.getText().toString());
        values.put("location", etLocation.getText().toString());
        values.put("notes", etNotes.getText().toString());

        if (strNotesTitle == null) {
            Cursor c = mDatabase.query("tbl_notes", null, "subjectid=? AND title=?", new String[]{strSubjectname,
                    etTitle.getText().toString()}, null, null, null);
            c.moveToFirst();

            if (c.getCount() > 0) {
                Toast.makeText(this, "Title already exist!", Toast.LENGTH_LONG).show();

                return false;
            }

            mDatabase.insert("tbl_notes", null, values);
            strNotesTitle = etTitle.getText().toString();
        } else {
            mDatabase.update("tbl_notes", values, "title=?", new String[]{strNotesTitle});
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        Button b = (Button) v;

        if (b.getText().equals("Save")) {
            if (saveNotes()) {
                finish();
            }
        } else if (b.getText().equals("Cancel")) {
            finish();
        } else if (b.getText().equals("Photos")) {
            if (saveNotes()) {
                Intent i = new Intent(getApplicationContext(), PhotoGallery.class);
                i.putExtra("NotesTitle", strNotesTitle);
                startActivity(i);
            }
        } else if (b.getText().equals("Voice Notes")) {
            if (saveNotes()) {
                Intent i = new Intent(getApplicationContext(), VoiceNotes.class);
                i.putExtra("NotesTitle", strNotesTitle);
                startActivity(i);
            }
        }
    }

    private void fetchNote() {
        Cursor c = mDatabase.query("tbl_notes", null, "subjectid=? AND title=?", new String[]{strSubjectname, strNotesTitle}, null, null, null);
        c.moveToFirst();

        while (!c.isAfterLast()) {
            etTitle.setText(c.getString(c.getColumnIndex("title")));
            etTitle.setEnabled(false);
            etDatetime.setText(c.getString(c.getColumnIndex("datetime")));
            etLocation.setText(c.getString(c.getColumnIndex("location")));
            etNotes.setText(c.getString(c.getColumnIndex("notes")));

            c.moveToNext();
        }

        c.close();
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            etLocation.setText(locationAddress);
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                AddNotes.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        AddNotes.this.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }
}
