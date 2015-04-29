package edu.wpi.cs403x.dyvo.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class VobsDbAdapter {
    public static final String KEY_ROW_ID = "_id";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_CREATED_AT = "created_at";
    public static final String KEY_NEARBY = "nearby";
    public static final String[] ALL_COLUMNS = new String[] {KEY_ROW_ID, KEY_LONGITUDE, KEY_LATITUDE, KEY_CONTENT, KEY_USER_ID, KEY_CREATED_AT, KEY_NEARBY};

    private static final String TAG = "VobsDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "Dyvo";
    private static final String SQLITE_TABLE = "Vob";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + SQLITE_TABLE + " (" +
                    KEY_ROW_ID + " INTEGER PRIMARY KEY autoincrement," +
                    KEY_LONGITUDE + " REAL," +
                    KEY_LATITUDE + " REAL," +
                    KEY_CONTENT + " TEXT," +
                    KEY_USER_ID + " TEXT," +
                    KEY_CREATED_AT + " TEXT," +
                    KEY_NEARBY + " INTEGER);";


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

    public long createVob(String content, String userId, float longitude, float latitude, String createdAt, int nearby ) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_LONGITUDE, longitude);
        initialValues.put(KEY_LATITUDE, latitude);
        initialValues.put(KEY_CONTENT, content);
        initialValues.put(KEY_USER_ID, userId);
        initialValues.put(KEY_CREATED_AT, createdAt);
        initialValues.put(KEY_NEARBY, nearby);
        return mDb.insert(SQLITE_TABLE, null, initialValues);
    }

    public long createVob(String content, String userId, float longitude, float latitude, String createdAt) {
        return createVob(content, userId, longitude, latitude, createdAt, 0);
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
                KEY_ROW_ID + "=?",
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

    public Cursor fetchNearbyVobs() {
        Cursor cursor = mDb.query(SQLITE_TABLE,
                ALL_COLUMNS,
                KEY_NEARBY + "=1",
                null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }
}
