package com.example.kashyap.notesapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;
import java.util.Locale;

public class FullImage extends AppCompatActivity implements SimpleGestureFilter.SimpleGestureListener{

    // Database name
    private static final String DATABASE_NAME = "Notes";

    // SQL Create statement
    private static final String CREATE_NOTES_TABLE = "CREATE TABLE IF NOT EXISTS tbl_media (mediaid INTEGER " +
            "PRIMARY KEY AUTOINCREMENT, notesid TEXT, mediaPath TEXT, mediaType TEXT);";

    // Database instance
    private SQLiteDatabase mDatabase;

    ImageView imgFullImage;

    ArrayList<String> arrImagePath;
    int position;

    private float x1,x2;
    static final int MIN_DISTANCE = 100;

    private SimpleGestureFilter detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        // Detect touched area
        detector = new SimpleGestureFilter(this,this);

        arrImagePath = getIntent().getExtras().getStringArrayList("arrImagePath");
        position = getIntent().getExtras().getInt("position");

        openOrCreateDatabase();
        gatherControl();
        loadFullImage();
    }

    public void openOrCreateDatabase() {
        mDatabase = openOrCreateDatabase(DATABASE_NAME, SQLiteDatabase.CREATE_IF_NECESSARY, null);

        mDatabase.setLocale(Locale.getDefault());
        mDatabase.setVersion(1);

        mDatabase.execSQL(CREATE_NOTES_TABLE);
    }

    private void gatherControl() {
        imgFullImage = (ImageView) findViewById(R.id.imgFullImage);
    }

    private void loadFullImage() {

        if (arrImagePath.size() == 0) {
            finish();
            return;
        }

        if (position == arrImagePath.size()) {
            position = 0;
        }
        else if (position < 0) {
            position = arrImagePath.size() - 1;
        }

        Bitmap bitmap = null;
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmap = BitmapFactory.decodeFile(arrImagePath.get(position));

        imgFullImage.setImageBitmap(bitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_photo_delete, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.delete_photo:
                showAlert();
                break;
        }
        return true;
    }

    private void showAlert() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Delete");
        dialogBuilder.setMessage("Are you sure?");
        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogAlert, int which) {
                deleteImage();
                position--;
                loadFullImage();
            }
        });

        dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogAlert, int which) {
                dialogAlert.dismiss();
            }
        });

        AlertDialog alert = dialogBuilder.create();
        alert.show();
    }

    private void deleteImage() {
        String astrArgs[] = {arrImagePath.get(position).toString()};
        mDatabase.delete("tbl_media", "mediaPath=?", astrArgs);

        Toast.makeText(getApplicationContext(), "Image Deleted", Toast.LENGTH_SHORT).show();
        arrImagePath.remove(position);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        this.detector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onSwipe(int direction) {

        switch (direction) {

            case SimpleGestureFilter.SWIPE_RIGHT :
                position--;
                loadFullImage();
                break;
            case SimpleGestureFilter.SWIPE_LEFT :
                position++;
                loadFullImage();
                break;
            case SimpleGestureFilter.SWIPE_DOWN :
                break;
            case SimpleGestureFilter.SWIPE_UP :
                break;

        }
    }

    @Override
    public void onDoubleTap() {
        //Toast.makeText(this, "Double Tap", Toast.LENGTH_SHORT).show();
    }
}
