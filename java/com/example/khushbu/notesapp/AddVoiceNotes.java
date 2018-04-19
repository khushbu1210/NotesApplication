package com.example.kashyap.notesapp;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.media.*;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddVoiceNotes extends AppCompatActivity implements View.OnClickListener {

    // Database name
    private static final String DATABASE_NAME = "Notes";

    // SQL Create statement
    private static final String CREATE_NOTES_TABLE = "CREATE TABLE IF NOT EXISTS tbl_notes (notesid INTEGER " +
            "PRIMARY KEY AUTOINCREMENT, subjectid TEXT, title TEXT, datetime TEXT, location TEXT, notes TEXT);";

    // Database instance
    private SQLiteDatabase mDatabase;

    Button btnRecord;
    Button btnPlay;
    Button btnSave;

    String RECORDED_FILE;
    MediaRecorder audioListener;
    MediaPlayer player;

    String strNotesTitle;
    String strVoiceNotePath;

    private ArrayAdapter<String> listSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_voice_notes);

        strNotesTitle = getIntent().getExtras().getString("NotesTitle");

        //RECORDED_FILE = "/NotesApp/VoiceNotes/" + strNotesTitle + "/audio.mp4";

        DateFormat dateFormat = new SimpleDateFormat("MMMddyyyyHHmmss");
        RECORDED_FILE = "/" + strNotesTitle.replace(" ", "") + dateFormat.format(new Date()) + "audio.mp4";

        openOrCreateDatabase();
        gatherControls();

        btnPlay.setEnabled(false);
        btnSave.setEnabled(false);
    }

    public void openOrCreateDatabase() {
        mDatabase = openOrCreateDatabase(DATABASE_NAME, SQLiteDatabase.CREATE_IF_NECESSARY, null);

        mDatabase.setLocale(Locale.getDefault());
        mDatabase.setVersion(1);

        mDatabase.execSQL(CREATE_NOTES_TABLE);
    }

    private void gatherControls() {
        btnRecord = (Button)findViewById(R.id.btnRecord);
        btnRecord.setOnClickListener(this);

        btnPlay = (Button)findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(this);

        btnSave = (Button)findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);
    }

    protected void saveVoiceNotes() {

        ContentValues values = new ContentValues();

        values.put("notesid", strNotesTitle);
        values.put("mediaPath", strVoiceNotePath);
        values.put("mediaType", "Voice");

        mDatabase.insert("tbl_media", null, values);
    }

    @Override
    public void onClick(View v) {
        Button b = (Button)v;

        switch (b.getId()) {
            case R.id.btnRecord:
                if (btnRecord.getText().equals("Record")) {
                    if (audioListener == null) {
                        audioListener = new MediaRecorder();
                    }

                    // Fully qualified path name. In this case, we use the Files
                    // subdir
                    String pathForAppFiles = getFilesDir().getAbsolutePath();
                    pathForAppFiles += RECORDED_FILE;
                    Log.d("Audio filename:", pathForAppFiles);

                    //File destination = new File(pathForAppFiles);
                    //destination.mkdir();

                    File filename = new File(pathForAppFiles);
                    if (filename.exists()) {
                        filename.delete();
                    }
//
//                    try {
//                        filename.createNewFile();
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }

                    try {
                        audioListener.setAudioSource(MediaRecorder.AudioSource.MIC);
                        audioListener.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                        audioListener.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                        audioListener.setOutputFile(pathForAppFiles);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    try {
                        audioListener.prepare();
                        audioListener.start();
                    } catch (Exception e) {
                        Log.e("Audio", "Failed to prepare and start audio recording", e);
                    }

                    btnRecord.setText("Stop");

                    btnPlay.setEnabled(false);
                    btnSave.setEnabled(false);
                }
                else if (btnRecord.getText().equals("Stop")) {
                    if (audioListener == null)
                        return;

                    audioListener.stop();
                    audioListener.release();
                    audioListener = null;

                    String pathForAppFiles = getFilesDir().getAbsolutePath();
                    pathForAppFiles += RECORDED_FILE;
                    Log.d("Audio filename:", pathForAppFiles);

                    strVoiceNotePath = pathForAppFiles;

                    ContentValues values = new ContentValues(10);

                    values.put(MediaStore.MediaColumns.TITLE, "RecordedAudio");
                    values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp4");
                    values.put(MediaStore.MediaColumns.DATA, pathForAppFiles);

                    Uri uri = MediaStore.Audio.Media.getContentUriForPath(getFilesDir().getAbsolutePath() + pathForAppFiles);
                    getContentResolver().delete(uri, null, null);

                    Uri audioUri = getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
                    if (audioUri == null) {
                        Log.d("Audio", "Content resolver failed");
                        return;
                    }

                    // Force Media scanner to refresh now. Technically, this is
                    // unnecessary, as the media scanner will run periodically but
                    // helpful for testing.
                    //Log.d("Audio URI", "Path = " + audioUri.getPath());
                    //sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, audioUri));

                    //RingtoneManager.setActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE, audioUri);

                    btnRecord.setText("Record");

                    btnPlay.setEnabled(true);
                    btnSave.setEnabled(true);
                }
                break;

            case R.id.btnPlay:
                player = null;
                if (player == null) {
                    player = new MediaPlayer();
                }
                try {

                    // Fully qualified path name. In this case, we use the Files
                    // subdir
                    String audioFilePath = getFilesDir().getAbsolutePath();
                    audioFilePath += RECORDED_FILE;
                    //Log.d("Audio filename:", audioFilePath);

                    player.setDataSource(audioFilePath);
                    player.prepare();
                    player.start();
                } catch (Exception e) {
                    Log.e("Audio", "Playback failed.", e);
                }
                break;

            case R.id.btnSave:
                if (player != null) {
                    player.stop();
                    player.release();
                    player = null;
                }

                saveVoiceNotes();

                finish();
                break;
        }
    }
}
