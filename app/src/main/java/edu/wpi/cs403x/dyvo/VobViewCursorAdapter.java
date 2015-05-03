package edu.wpi.cs403x.dyvo;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import edu.wpi.cs403x.dyvo.api.FaceBookHelper;
import edu.wpi.cs403x.dyvo.api.FaceBookHelperAction;
import edu.wpi.cs403x.dyvo.api.LocationHelper;
import edu.wpi.cs403x.dyvo.db.VobsDbAdapter;

/**
 * Created by cdhan_000 on 4/25/2015.
 */
public class VobViewCursorAdapter extends CursorAdapter {

    private LayoutInflater mInflater;
    public static final int MAX_CONTENT_PREVIEW_LENGTH = 32;

    public VobViewCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.vob_info, parent, false);
    }

    @Override
    public void bindView(View view, final Context ctx, Cursor cursor) {
        //Get UI Components
        final TextView contentTextView = (TextView) view.findViewById(R.id.vob_info_content);
        final TextView nameTextView = (TextView) view.findViewById(R.id.vob_info_user_name);
        final TextView distanceTextView = (TextView) view.findViewById(R.id.vob_info_distance);
        final ImageView profileImageView = (ImageView) view.findViewById(R.id.vob_info_user_picture);
        final TextView timeView = (TextView) view.findViewById(R.id.vob_info_time);
        //Set Content of Vob
        String content = cursor.getString(cursor.getColumnIndex(VobsDbAdapter.KEY_CONTENT));
        if (content.length() > MAX_CONTENT_PREVIEW_LENGTH){
            content = content.substring(0, MAX_CONTENT_PREVIEW_LENGTH - 3) + "...";
        }
        contentTextView.setText(content);

        //Set Time of Vob
        String timeStr = cursor.getString(cursor.getColumnIndex(VobsDbAdapter.KEY_CREATED_AT));
        timeStr = getTimeDisplay(timeStr);
        timeView.setText(timeStr);

        //Set Lat + Long of Vob
        float spotLat = cursor.getFloat(cursor.getColumnIndex(VobsDbAdapter.KEY_LATITUDE));
        float spotLong = cursor.getFloat(cursor.getColumnIndex(VobsDbAdapter.KEY_LONGITUDE));

        String latLongStr = LocationHelper.getInstance().getDistanceToAsText(new LatLng(spotLat, spotLong));
        distanceTextView.setText(latLongStr);

        FaceBookHelper fb = new FaceBookHelper();
        final String fbIdStr = cursor.getString(cursor.getColumnIndex(VobsDbAdapter.KEY_USER_ID));
        fb.requestFaceBookDetails(ctx, fbIdStr, new FaceBookHelperAction() {
            @Override
            public void onSuccess(String name, Bitmap bitmap) {
                nameTextView.setText(name);
                profileImageView.setImageBitmap(bitmap);
            }

            @Override
            public void onFailure() {

            }
        });
    }

    private String getTimeDisplay(String timeStr){
        if (timeStr != null) {
            DateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            DateFormat formatter = new SimpleDateFormat("M/d/yy' - 'h:mm a");
            try {
                Date d = parser.parse(timeStr);
                int offset = TimeZone.getDefault().getOffset(new Date().getTime());
                d.setTime(d.getTime() + offset);

                String output = formatter.format(d);
                timeStr = output;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return timeStr;
        } else {
            return "";
        }
    }
}
