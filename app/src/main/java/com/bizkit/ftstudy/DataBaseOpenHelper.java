package com.bizkit.ftstudy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataBaseOpenHelper extends SQLiteOpenHelper {
    private static final String TABLE = "xmwj";
    private static final String FTS_VIRTUAL_TABLE_NAME = "fts_table";
    //https://stackoverflow.com/questions/9109438/how-to-use-an-existing-database-with-an-android-application
    private static String TAG = "DataBaseOpenHelper"; // Tag just for the LogCat window
    //destination path (location) of our database on device
    private static String DB_PATH = "";
    private static String DB_NAME = "list_example.db";// Database name
    private final Context mContext;
    private SQLiteDatabase mDataBase;

    DataBaseOpenHelper(Context context) {
        super(context, DB_NAME, null, 2);// 1? Its database Version

        if (android.os.Build.VERSION.SDK_INT >= 17) {
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }
        this.mContext = context;
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
        //Log.v("dbFile", dbFile + "   "+ dbFile.exists());
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
        InputStream mInput = mContext.getAssets().open(DB_NAME);
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

    //Open the database, so we can query it
    public boolean openDataBase() throws SQLException {
        String mPath = DB_PATH + DB_NAME;
        //Log.v("mPath", mPath);
        mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        //mDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        return mDataBase != null;
    }

    //This Method is used to close the data base connection.
    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        mDataBase = sqLiteDatabase;
        try {
            createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mDataBase.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS " + FTS_VIRTUAL_TABLE_NAME + " USING fts4 (" + Constants.COL_CONTENT + ");");
        populateFTStable();
    }

    private void populateFTStable() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    ContentValues values = new ContentValues();
                    Cursor cursor = mDataBase.rawQuery("SELECT " + Constants.COL_CONTENT + " FROM xmwj", null);
                    if (cursor.getCount() > 0) {
                        if (cursor.moveToFirst()) {
                            do {
                                int columnIndex = cursor.getColumnIndex(Constants.COL_CONTENT);
                                values.put(Constants.COL_CONTENT, cursor.getString(columnIndex));
                                mDataBase.insert(FTS_VIRTUAL_TABLE_NAME, null, values);
                            } while (cursor.moveToNext());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


}
