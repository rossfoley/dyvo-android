package edu.wpi.cs403x.dyvo;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import edu.wpi.cs403x.dyvo.db.VobsDbAdapter;

/**
 * Created by cdhan_000 on 4/25/2015.
 */
public class VobViewCursorAdapter extends CursorAdapter {

    private LayoutInflater mInflater;

    public VobViewCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.vob_info, parent, false);
    }



    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        //Get UI Components
        final TextView contentTextView = (TextView) view.findViewById(R.id.vob_info_content);
        final TextView nameTextView = (TextView) view.findViewById(R.id.vob_info_user_name);
        final ImageView profileImageView = (ImageView) view.findViewById(R.id.vob_info_user_picture);

        //Set Content of Vob
        contentTextView.setText(cursor.getString(cursor.getColumnIndex(VobsDbAdapter.KEY_CONTENT)));

        //Create Client to contact facebook for user details
        final AsyncHttpClient client = new AsyncHttpClient();
        final String fbIdStr = cursor.getString(cursor.getColumnIndex(VobsDbAdapter.KEY_USER_ID));
        final RequestParams params = new RequestParams();
        params.put("access_token", AccessToken.getCurrentAccessToken().getToken());

        final RequestHandle requestHandle = client.get(context, "https://graph.facebook.com/?ids="+ fbIdStr +"&fields=name,picture",  params , new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {

                    //Extract data
                    JSONObject fbData = response.getJSONObject(fbIdStr);
                    final String pictureURL = fbData.getJSONObject("picture").getJSONObject("data").getString("url");
                    final String name = fbData.getString("name");

                    //Set Name
                    nameTextView.setText(name);

                    //Launch thread to find image
                    Thread imgThread = new Thread(new Runnable(){
                        @Override
                        public void run(){
                            try {
                                URL newurl = new URL(pictureURL);
                                final Bitmap userImage = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());

                                //Put image task on main thread
                                Handler mainHandler = new Handler(context.getMainLooper());
                                Runnable myRunnable = new Runnable(){
                                    public void run(){
                                        profileImageView.setImageBitmap(userImage);
                                    }
                                };
                                mainHandler.post(myRunnable);
                            } catch (MalformedURLException e){
                                e.printStackTrace();
                            } catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                    });
                    imgThread.start();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                String message = "Failed to reach facebook.  Error code: " + statusCode;
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });

    }
}
