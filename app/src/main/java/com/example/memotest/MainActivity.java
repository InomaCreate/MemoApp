package com.example.memotest;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends AppCompatActivity {

    Button btnSave;
    Button btnDelete;
    ListView lvMemoList = null;
    int memoId = -1;
    int save_select = 0;    // 0:新規追加　1:編集の場合

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        lvMemoList = findViewById(R.id.lvMemoList);

        memoListDisplay();

        lvMemoList.setOnItemClickListener(new ListItemClickListener());

    }

    // 追加ボタン
    public void onAddButtonClick(View view){

        EditText etTitle = findViewById(R.id.etTitle);
        etTitle.setText("new memo");
        EditText etNote = findViewById(R.id.etNote);
        etNote.setText("");
        btnSave.setEnabled(true);

        save_select = 0;

    }

    // 保存ボタン
    public void onSaveButtonClick(View view){
        EditText etNote = findViewById(R.id.etNote);
        String note = etNote.getText().toString();

        EditText etTitle = findViewById(R.id.etTitle);
        String title = etTitle.getText().toString();

        DatabaseHelper helper = new DatabaseHelper(MainActivity.this);
        SQLiteDatabase db = helper.getWritableDatabase();

        if(save_select==1){
            // 編集からの保存時
            try {
                String sqlDelete = "DELETE FROM notememo WHERE _id = ?";
                SQLiteStatement stmt = db.compileStatement(sqlDelete);
                stmt.bindLong(1,memoId);
                stmt.executeUpdateDelete();

                String sqlInsert = "INSERT INTO notememo (_id, name, note) VALUES (?, ?, ?)";
                stmt = db.compileStatement(sqlInsert);
                stmt.bindLong(1,memoId);
                stmt.bindString(2,title);
                stmt.bindString(3,note);

                stmt.executeInsert();

            }
            finally {
                db.close();
            }
        }
        else {
            // 新規追加時
            try {
                String sqlInsert = "INSERT INTO notememo (name, note) VALUES (?, ?)";
                SQLiteStatement stmt = db.compileStatement(sqlInsert);
                stmt.bindString(1,title);
                stmt.bindString(2,note);

                stmt.executeInsert();
            }
            finally {
                db.close();
            }
        }


        etTitle.setText("");
        etNote.setText("");
        btnSave.setEnabled(false);
        btnDelete.setEnabled(false);

        memoListDisplay();

    }

    // メモリスト表示
    private void memoListDisplay(){

        DatabaseHelper helper = new DatabaseHelper(MainActivity.this);
        SQLiteDatabase db = helper.getReadableDatabase();
        try {
            String sql = "SELECT _id,name FROM notememo";
            Cursor cursor = db.rawQuery(sql,null);
            String[] from = {"name"};
            int[] to = {android.R.id.text1};
            SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_1,cursor,from,to,0);
            lvMemoList.setAdapter(simpleCursorAdapter);
        }
        finally {
            db.close();
        }

    }

    // Listをクリックしたときのリスナークラス
    private class ListItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            memoId = (int)id;
            save_select = 1; // 編集

            btnSave.setEnabled(true);
            btnDelete.setEnabled(true);

            DatabaseHelper helper = new DatabaseHelper(MainActivity.this);
            SQLiteDatabase db = helper.getReadableDatabase();
            try {
                String sql = "SELECT name, note FROM notememo WHERE _id = "+memoId;
                Cursor cursor = db.rawQuery(sql,null);
                String note = "";
                String title = "";
                while(cursor.moveToNext()){
                    int idxNote = cursor.getColumnIndex("note");
                    note = cursor.getString(idxNote);

                    int idxTitle = cursor.getColumnIndex("name");
                    title = cursor.getString(idxTitle);
                }
                EditText etNote = findViewById(R.id.etNote);
                etNote.setText(note);

                EditText etTitle = findViewById(R.id.etTitle);
                etTitle.setText(title);

            }
            finally {
                db.close();
            }

        }


    }

    // 削除ボタン
    public void onDeleteButtonClick(View view){
        DatabaseHelper helper = new DatabaseHelper(MainActivity.this);
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            String sqlDelete = "DELETE FROM notememo WHERE _id = ?";
            SQLiteStatement stmt = db.compileStatement(sqlDelete);
            stmt.bindLong(1,memoId);
            stmt.executeUpdateDelete();
        }
        finally {
            db.close();
        }

        EditText etTile = findViewById(R.id.etTitle);
        etTile.setText("");
        EditText etNote = findViewById(R.id.etNote);
        etNote.setText("");
        btnDelete.setEnabled(false);
        btnSave.setEnabled(false);

        memoListDisplay();

    }


}