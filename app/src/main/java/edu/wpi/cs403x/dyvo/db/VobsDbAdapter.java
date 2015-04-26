package edu.wpi.cs403x.dyvo.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class VobsDbAdapter {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_USER_ID = "user_id";
    public static final String[] ALL_COLUMNS = new String[] {KEY_ROWID, KEY_LONGITUDE, KEY_LATITUDE, KEY_CONTENT, KEY_USER_ID};

    private static final String TAG = "CountriesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "Dyvo";
    private static final String SQLITE_TABLE = "Vob";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + SQLITE_TABLE + " (" +
                    KEY_ROWID + " INTEGER PRIMARY KEY autoincrement," +
                    KEY_LONGITUDE + " REAL," +
                    KEY_LATITUDE + " REAL," +
                    KEY_CONTENT + " TEXT," +
                    KEY_USER_ID + " TEXT);";

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w(TAG, DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);
            onCreate(db);
        }
    }

    public VobsDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public VobsDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    public long createVob(String content, String userId, float longitude, float latitude) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_LONGITUDE, longitude);
        initialValues.put(KEY_LATITUDE, latitude);
        initialValues.put(KEY_CONTENT, content);
        initialValues.put(KEY_USER_ID, userId);

        return mDb.insert(SQLITE_TABLE, null, initialValues);
    }

    public boolean deleteAllVobs() {
        int doneDelete = 0;
        doneDelete = mDb.delete(SQLITE_TABLE, null , null);
        Log.w(TAG, Integer.toString(doneDelete));
        return doneDelete > 0;
    }

    public Cursor fetchAllVobs() {
        Cursor mCursor = mDb.query(SQLITE_TABLE, ALL_COLUMNS, null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        return mCursor;
    }

    public Cursor fetchVobById(String id) {
        Cursor cursor = mDb.query(SQLITE_TABLE,
                ALL_COLUMNS,
                KEY_ROWID + "=?",
                new String[] {id},
                null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }

    public Cursor fetchVobsByUser(String uid) {
        Cursor cursor = mDb.query(SQLITE_TABLE,
                ALL_COLUMNS,
                KEY_USER_ID + "=?",
                new String[] {uid},
                null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }
}
