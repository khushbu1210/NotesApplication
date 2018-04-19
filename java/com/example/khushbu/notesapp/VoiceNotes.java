package com.example.kashyap.notesapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceNotes extends AppCompatActivity implements ListView.OnItemClickListener, ListView.OnItemLongClickListener {

    // Database name
    private static final String DATABASE_NAME = "Notes";

    // SQL Create statement
    private static final String CREATE_NOTES_TABLE = "CREATE TABLE IF NOT EXISTS tbl_media (mediaid INTEGER " +
            "PRIMARY KEY AUTOINCREMENT, notesid TEXT, mediaPath TEXT, mediaType TEXT);";

    // Database instance
    private SQLiteDatabase mDatabase;

    ListView lvVoiceNotes;

    String strNotesTitle;
    String voiceNotesPath;

    MediaPlayer player;

    ArrayList<String> arrVoiceNotesPath;
    private ArrayAdapter<String> listVoiceNotes;

    String strDeleteVoiceNote = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_notes);

        strNotesTitle = getIntent().getExtras().getString("NotesTitle");

        openOrCreateDatabase();
        gatherControls();
        fetchMedia();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        strNotesTitle = getIntent().getExtras().getString("NotesTitle");

        openOrCreateDatabase();
        gatherControls();
        fetchMedia();
    }

    public void openOrCreateDatabase() {
        mDatabase = openOrCreateDatabase(DATABASE_NAME, SQLiteDatabase.CREATE_IF_NECESSARY, null);

        mDatabase.setLocale(Locale.getDefault());
        mDatabase.setVersion(1);

        mDatabase.execSQL(CREATE_NOTES_TABLE);
    }

    private void gatherControls() {
        lvVoiceNotes = (ListView)findViewById(R.id.lvVoiceNotesList);
        lvVoiceNotes.setOnItemClickListener(this);
        lvVoiceNotes.setLongClickable(true);
        lvVoiceNotes.setOnItemLongClickListener(this);
    }

    private void fetchMedia() {
        Cursor c = mDatabase.query("tbl_media", null, "notesid=? AND mediaType=?", new String[] { strNotesTitle, "Voice" },
                null, null, null);
        c.moveToFirst();

        ArrayList<String> arrVoiceNotes = new ArrayList<>();
        arrVoiceNotesPath = new ArrayList<>();
        Integer count = 0;

        while(!c.isAfterLast()) {
            arrVoiceNotes.add("Voice Notes " + (count + 1));
            arrVoiceNotesPath.add(c.getString(c.getColumnIndex("mediaPath")));

            c.moveToNext();
            count++;
        }

        c.close();

        listVoiceNotes = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrVoiceNotes);

        lvVoiceNotes.setAdapter(listVoiceNotes);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (strDeleteVoiceNote == null) {
            player = null;
            if (player == null) {
                player = new MediaPlayer();
            }
            try {

                String audioFilePath = arrVoiceNotesPath.get(position);

                player.setDataSource(audioFilePath);
                player.prepare();
                player.start();
            } catch (Exception e) {
                Log.e("Audio", "Playback failed.", e);
            }
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
                Intent i = new Intent(getApplicationContext(), AddVoiceNotes.class);
                i.putExtra("NotesTitle", strNotesTitle);
                startActivity(i);
                break;
        }
        return true;
    }

    private void deleteVoiceNote() {
        if (strDeleteVoiceNote != null) {
            String astrArgs[] = {strDeleteVoiceNote.toString()};
            mDatabase.delete("tbl_media", "mediaPath=?", astrArgs);

            strDeleteVoiceNote = null;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        strDeleteVoiceNote = arrVoiceNotesPath.get(position);

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
                strDeleteVoiceNote = null;
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
                deleteVoiceNote();
                fetchMedia();
            }
        });

        dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogAlert, int which) {
                strDeleteVoiceNote = null;
                dialogAlert.dismiss();
            }
        });

        AlertDialog alert = dialogBuilder.create();
        alert.show();
    }
}
