package edu.wpi.cs403x.dyvo.db;


import android.content.Context;
import android.database.Cursor;
import android.widget.SimpleCursorAdapter;

import edu.wpi.cs403x.dyvo.R;
import edu.wpi.cs403x.dyvo.VobViewCursorAdapter;

/**
 * Created by cdhan_000 on 4/25/2015.
 */
public class CursorAdapter {

    private static CursorAdapter instance;
    public static CursorAdapter getInstance(){
        if (instance == null){
            instance = new CursorAdapter();
        }
        return instance;
    }

    private VobViewCursorAdapter adapter;
    private VobsDbAdapter dbHelper;

    private CursorAdapter(){
        //singleton constructor
    }

    public VobViewCursorAdapter getCursorAdapter(){
        return adapter;
    }

    public VobsDbAdapter getDBHelper(){
        return dbHelper;
    }

    public void initialize(Context ctx){

        // Initialize the database helper
        dbHelper = new VobsDbAdapter(ctx);
        dbHelper.open();

        Cursor cursor = dbHelper.fetchAllVobs();
//        String[] columns = new String[] {
//                VobsDbAdapter.KEY_CONTENT,
//                VobsDbAdapter.KEY_LONGITUDE,
//                VobsDbAdapter.KEY_LATITUDE,
//                VobsDbAdapter.KEY_USER_ID,
//                VobsDbAdapter.KEY_ROWID
//        };
//        int[] to = new int[] {
//                R.id.content,
//                R.id.longitude,
//                R.id.latitude,
//                R.id.user_id,
//                R.id.row_id
//        };

        adapter = new VobViewCursorAdapter(ctx, cursor, 0);
    }
}
