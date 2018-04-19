package com.example.kashyap.notesapp;

import android.app.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.widget.*;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class PhotoGallery extends AppCompatActivity implements GridView.OnItemClickListener{

    // Database name
    private static final String DATABASE_NAME = "Notes";

    // SQL Create statement
    private static final String CREATE_NOTES_TABLE = "CREATE TABLE IF NOT EXISTS tbl_media (mediaid INTEGER " +
            "PRIMARY KEY AUTOINCREMENT, notesid TEXT, mediaPath TEXT, mediaType TEXT);";

    // Database instance
    private SQLiteDatabase mDatabase;

    GridView gvPhotos;

    private ArrayAdapter<String> listSubject;

    String strNotesTitle;
    String imgPath;

    int REQUEST_CAMERA = 0, SELECT_FILE = 1;

    private ImageAdapter adapter;

    ArrayList<String> arrImagePath;

    private static final int MEDIA_TYPE_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);

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
        gvPhotos = (GridView) findViewById(R.id.gvPhotoGallery);
        gvPhotos.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_photos, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add_photo:

                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.add_photo);
                dialog.setTitle("Add Photo");

                Button btnCamera = (Button) dialog.findViewById(R.id.btnCamera);
                Button btnGallery = (Button) dialog.findViewById(R.id.btnGallery);
                TextView tvCancel = (TextView) dialog.findViewById(R.id.tvCancel);

                btnCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, REQUEST_CAMERA);
                    }
                });

                btnGallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.
                                EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
                    }
                });

                tvCancel.setOnClickListener(new View.OnClickListener() {
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

    private String currentDateFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
        String currentTimeStamp = dateFormat.format(new Date());
        return currentTimeStamp;
    }

    private void storeCameraPhotoInSDCard(Bitmap bitmap, String currentDate) {
        File outputFile = new File(Environment.getExternalStorageDirectory(), "photo_" + currentDate + ".jpg");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap getImageFileFromSDCard(String filename) {
        Bitmap bitmap = null;
        File imageFile = new File(Environment.getExternalStorageDirectory() + filename);
        try {
            FileInputStream fis = new FileInputStream(imageFile);
            bitmap = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    protected void savePhotos() {

        ContentValues values = new ContentValues();

        values.put("notesid", strNotesTitle);
        values.put("mediaPath", imgPath);
        values.put("mediaType", "Image");

        mDatabase.insert("tbl_media", null, values);
    }

    private void fetchMedia() {
        Cursor c = mDatabase.query("tbl_media", null, "notesid=? AND mediaType=?", new String[]{strNotesTitle, "Image"},
                null, null, null);
        c.moveToFirst();

        arrImagePath = new ArrayList<>();

        while (!c.isAfterLast()) {
            arrImagePath.add(c.getString(c.getColumnIndex("mediaPath")));
            c.moveToNext();
        }
        c.close();

        adapter = new ImageAdapter(this);
        for (int i = 0; i < arrImagePath.size(); i++) {
            Bitmap bitmap = null;
            System.out.println("image path:" + arrImagePath.get(i));
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeFile(arrImagePath.get(i));
            adapter.setImage(bitmap);
        }

        gvPhotos.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            return;
        }
        Bitmap thumbnail = null;
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            if (data != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                thumbnail = (Bitmap) data.getExtras().get("data");

                thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        storeImage(thumbnail);
        savePhotos();

        adapter.setImage(thumbnail);
        adapter.notifyDataSetChanged();
    }

    private File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("MMMddyyyyHHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "/" + strNotesTitle.replace(" ", "") + timeStamp + "image.jpg");
        } else {
            return null;
        }
        return mediaFile;
    }

    private void storeImage(Bitmap mBitmap) {
        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream fOut = null;
        DateFormat dateFormat = new SimpleDateFormat("MMMddyyyyHHmmss");
        File file = new File(path, "/" + strNotesTitle.replace(" ", "") + dateFormat.format(new Date()) + "image.jpg"); // the File to save to

        try {
            fOut = new FileOutputStream(file);
            Bitmap pictureBitmap = mBitmap; // obtaining the Bitmap
            pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
            fOut.flush();
            fOut.close(); // do not forget to close the stream
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imgPath = file.getPath();
    }

    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
                null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();

        String selectedImagePath = cursor.getString(column_index);

        Bitmap bm;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectedImagePath, options);
        final int REQUIRED_SIZE = 200;
        int scale = 1;
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE && options.outHeight / scale / 2 >= REQUIRED_SIZE)
            scale *= 2;
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(selectedImagePath, options);

        storeImage(bm);
        savePhotos();

        adapter.setImage(bm);
        adapter.notifyDataSetChanged();
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(getApplicationContext(), FullImage.class);
        i.putExtra("arrImagePath", arrImagePath);
        i.putExtra("position", position);
        i.putExtra("NotesTitle", strNotesTitle);
        startActivity(i);
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<Bitmap> pathList = new ArrayList<>();

        public int getCount() {
            return pathList.size();
        }

        public Object getItem(int position) {
            return pathList.get(position);
        }

        public void setImage(Bitmap path) { pathList.add(path); }

        public long getItemId(int position) {
            return 0;
        }

        public ImageAdapter(PhotoGallery c) {
            mContext = c;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
//            imageView.setImageResource(pathList.get(position));
            imageView.setImageBitmap(pathList.get(position));
            return imageView;
        }
    }
}
