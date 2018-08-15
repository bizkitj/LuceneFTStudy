package com.bizkit.ftstudy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataBaseTable {
    private static final String TAG = DataBaseTable.class.getSimpleName();

    private static final int DATABASE_VERSION = 2;
    private static final String FTS_VIRTUAL_TABLE_NAME = "fts_table";
    private static String DB_PATH = "";
    private static String DB_NAME = "list_example.db";// Database name
    private Context context;
    private DataBaseOpener dataBaseOpener;

    public DataBaseTable(Context context) {
        dataBaseOpener = new DataBaseOpener(context);
        this.context = context;
        try {
            dataBaseOpener.createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ftsTable(dataBaseOpener.getWritableDatabase());
    }

    public Cursor getMatches(String query) {
//        String selection = "SELECT docid AS _id," + Constants.COL_CONTENT + " FROM " + FTS_VIRTUAL_TABLE_NAME + " WHERE " + Constants.COL_CONTENT + " MATCH ?";
        String selection = "SELECT docid AS _id," + Constants.COL_CONTENT + " FROM " + FTS_VIRTUAL_TABLE_NAME + " WHERE " + Constants.COL_CONTENT + " MATCH ?";
        String[] selectionArgs = new String[]{query + "*"};
        SQLiteDatabase database = dataBaseOpener.getReadableDatabase();
        Cursor cursor = database.rawQuery(selection, selectionArgs);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    private void ftsTable(final SQLiteDatabase database) {
//        database.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS " + FTS_VIRTUAL_TABLE_NAME + " USING fts4 (" + Constants.COL_CONTENT + ");");
//        CREATE VIRTUAL TABLE fts_table USING fts4(content="xmwj", content);
//        CREATE VIRTUAL TABLE t_school USING fts4(name, content, tokenize=unicode61);
//        CREATE VIRTUAL TABLE t1 USING fts5(x, tokenize = 'porter ascii');

        String ftsVirtualTableCreationStatement = "CREATE VIRTUAL TABLE IF NOT EXISTS " + FTS_VIRTUAL_TABLE_NAME + " USING fts4(" + Constants.COL_CONTENT + ");";
//        String ftsVirtualTableCreationStatement = "CREATE VIRTUAL TABLE IF NOT EXISTS " + FTS_VIRTUAL_TABLE_NAME + " USING fts4(" + Constants.COL_CONTENT + "tokenize=unicode61" + ");";
        database.execSQL(ftsVirtualTableCreationStatement);
        Thread populateFtsTable = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ContentValues values = new ContentValues();
                    Cursor cursor = database.rawQuery("SELECT " + Constants.COL_CONTENT + " FROM xmwj", null);
                    if (cursor.getCount() > 0) {
                        if (cursor.moveToFirst()) {
                            do {
                                int columnIndex = cursor.getColumnIndex(Constants.COL_CONTENT);
                                values.put(Constants.COL_CONTENT, cursor.getString(columnIndex));
                                database.insert(FTS_VIRTUAL_TABLE_NAME, null, values);
                            } while (cursor.moveToNext());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        populateFtsTable.start();
    }

    private class DataBaseOpener extends SQLiteOpenHelper {
        private SQLiteDatabase dataBase;

        public DataBaseOpener(Context context) {
            super(context, DB_NAME, null, DATABASE_VERSION);
            if (android.os.Build.VERSION.SDK_INT >= 17) {
                DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
            } else {
                DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
            }
        }

        private void createDataBase() throws IOException {
            //If the database does not exist, copy it from the assets.
            if (!checkDataBase()) {
                this.getWritableDatabase(); //Creates an empty database on the system to rewrite it with your own database.
                try {
                    //Copy the database from assests
                    copyDataBase();
                    Log.e(TAG, "createDatabase database created");
                } catch (IOException mIOException) {
                    throw new Error("ErrorCopyingDataBase");
                }
            }
        }

        //Check that the database exists here: /data/data/your package/databases/Da Name
        private boolean checkDataBase() {
            File dbFile = new File(DB_PATH + DB_NAME);
            return dbFile.exists();
        }
        //Copy the database from assets

        /**
         * Copies your database from your local assets-folder to the just created empty database in the
         * system folder, from where it can be accessed and handled.
         * This is done by transferring byte stream.
         */
        private void copyDataBase() throws IOException {
            //Open your local db as the input stream
            InputStream mInput = context.getAssets().open(DB_NAME);
            // Path to the just created empty db
            String outFileName = DB_PATH + DB_NAME;
            //transfer bytes from the input file to the output file
            OutputStream mOutput = new FileOutputStream(outFileName);
            byte[] mBuffer = new byte[1024];
            int mLength;
            while ((mLength = mInput.read(mBuffer)) > 0) {
                mOutput.write(mBuffer, 0, mLength);
            }
            //Close the streams
            mOutput.flush();
            mOutput.close();
            mInput.close();
        }

        //This Method is used to close the data base connection.
        @Override
        public synchronized void close() {
            if (dataBase != null)
                dataBase.close();
            super.close();
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        }
    }
}
