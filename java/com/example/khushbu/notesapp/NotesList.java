package com.example.kashyap.notesapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Locale;

public class NotesList extends AppCompatActivity implements ListView.OnItemClickListener, TextWatcher,
        ListView.OnItemLongClickListener {

    // Database name
    private static final String DATABASE_NAME = "Notes";

    // SQL Create statement
    private static final String CREATE_NOTES_TABLE = "CREATE TABLE IF NOT EXISTS tbl_notes (notesid INTEGER " +
            "PRIMARY KEY AUTOINCREMENT, subjectid TEXT, title TEXT, datetime TEXT, location TEXT, notes TEXT);";

    // Database instance
    private SQLiteDatabase mDatabase;

    EditText etSearch;
    ListView lvNotes;

    String strSubjectname;
    String strFilterSearch;

    String strDeleteNote = null;

    private ArrayAdapter<String> listSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_list);

        strSubjectname = getIntent().getExtras().getString("SubjectName");

        openOrCreateDatabase();
        gatherControls();
        fetchAllNotes(null, null, false);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        strSubjectname = getIntent().getExtras().getString("SubjectName");

        openOrCreateDatabase();
        gatherControls();
        fetchAllNotes(null, null, false);
    }

    public void openOrCreateDatabase() {
        mDatabase = openOrCreateDatabase(DATABASE_NAME, SQLiteDatabase.CREATE_IF_NECESSARY, null);

        mDatabase.setLocale(Locale.getDefault());
        mDatabase.setVersion(1);

        mDatabase.execSQL(CREATE_NOTES_TABLE);
    }

    private void gatherControls() {
        etSearch = (EditText) findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(this);

        lvNotes = (ListView) findViewById(R.id.lvNotes);
        lvNotes.setOnItemClickListener(this);
        lvNotes.setLongClickable(true);
        lvNotes.setOnItemLongClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (strDeleteNote == null) {
            Intent i = new Intent(getApplicationContext(), AddNotes.class);
            i.putExtra("NoteTitle", lvNotes.getItemAtPosition(position).toString());
            i.putExtra("SubjectName", strSubjectname);
            startActivity(i);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_note_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_new_item:
                Intent i = new Intent(getApplicationContext(), AddNotes.class);
                i.putExtra("SubjectName", strSubjectname);
                startActivity(i);
                break;
            case R.id.sorting:
                sortingDialog();
                break;
        }
        return true;
    }

    private void fetchAllNotes(String strSearch, String strFilter, Boolean IsAsc) {
        Cursor c;
        String sort = " Collate NOCASE ";
        if (!IsAsc) {
            sort = " Desc";
        }

        if ((strSearch == null || strSearch.isEmpty()) && strFilter == null) {
            c = mDatabase.query("tbl_notes", null, "subjectid=?", new String[]{strSubjectname}, null, null, null);
        } else if (strSearch != null && strFilter == null) {
            c = mDatabase.query("tbl_notes", null, "subjectid=? AND (title LIKE ? OR notes LIKE ?)",
                    new String[]{strSubjectname, "%" + strSearch + "%", "%" + strSearch + "%"}, null, null, null);
        } else if (strFilter != null) {
            if (strSearch == null || strSearch.isEmpty()) {
                c = mDatabase.query("tbl_notes", null, "subjectid=?", new String[]{strSubjectname}, null, null, strFilter +
                        sort);
            } else {
                c = mDatabase.query("tbl_notes", null, "subjectid=? AND (title LIKE ? OR notes LIKE ?)", new String[]{strSubjectname,
                        "%" + strSearch + "%", "%" + strSearch + "%"}, null, null, strFilter + sort);
            }
        } else {
            c = mDatabase.query("tbl_notes", null, "subjectid=?", new String[]{strSubjectname}, null, null, null);
        }

        c.moveToFirst();

        ArrayList<String> arrSubject = new ArrayList<>();

        while (!c.isAfterLast()) {
            arrSubject.add(c.getString(c.getColumnIndex("title")));

            c.moveToNext();
        }

        c.close();

        listSubject = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrSubject);

        lvNotes.setAdapter(listSubject);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        fetchAllNotes(s.toString(), null, false);
        strFilterSearch = s.toString();
        //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private void sortingDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.sorting);
        dialog.setTitle("Sort By");
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        Button btnTitleAsc = (Button) dialog.findViewById(R.id.btnTitleAsc);
        Button btnTitleDesc = (Button) dialog.findViewById(R.id.btnTitleDesc);
        Button btnDateAsc = (Button) dialog.findViewById(R.id.btnDateAsc);
        Button btnDateDesc = (Button) dialog.findViewById(R.id.btnDateDesc);
        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);

        btnTitleAsc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchAllNotes(strFilterSearch, "title", true);
                dialog.dismiss();
            }
        });

        btnTitleDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchAllNotes(strFilterSearch, "title", false);
                dialog.dismiss();
            }
        });

        btnDateAsc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchAllNotes(strFilterSearch, "datetime", true);
                dialog.dismiss();
            }
        });

        btnDateDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchAllNotes(strFilterSearch, "datetime", false);
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void deleteNote() {
        if (strDeleteNote != null) {
            String astrArgs[] = {strDeleteNote.toString()};
            mDatabase.delete("tbl_notes", "title=?", astrArgs);

            strDeleteNote = null;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        strDeleteNote = listSubject.getItem(position);

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
                strDeleteNote = null;
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
                deleteNote();
                fetchAllNotes(null, null, false);
                etSearch.setText("");
            }
        });

        dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogAlert, int which) {
                strDeleteNote = null;
                dialogAlert.dismiss();
            }
        });

        AlertDialog alert = dialogBuilder.create();
        alert.show();
    }
}
