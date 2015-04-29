package edu.wpi.cs403x.dyvo.api;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import edu.wpi.cs403x.dyvo.db.VobsDbAdapter;

/**
 * Created by cdhan_000 on 4/27/2015.
 */
public class FaceBookHelper {

    public void requestFaceBookDetails(final Context ctx, final String fbIdStr, final FaceBookHelperAction action){


        //Create Client to contact facebook for user details
        final AsyncHttpClient client = new AsyncHttpClient();
        final RequestParams params = new RequestParams();
        params.put("access_token", AccessToken.getCurrentAccessToken().getToken());

        client.get(ctx, "https://graph.facebook.com/?ids="+ fbIdStr +"&fields=name,picture&type=large&redirect=true&width=200&height=200",  params , new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    // Extract data
                    JSONObject fbData = response.getJSONObject(fbIdStr);
                    final String pictureURL = fbData.getJSONObject("picture").getJSONObject("data").getString("url");
                    final String name = fbData.getString("name");

                    // Launch thread to find image
                    new Thread(new Runnable(){
                        @Override
                        public void run(){
                            try {
                                URL newurl = new URL(pictureURL);
                                final Bitmap userImage = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());

                                //Put image task on main thread
                                Handler mainHandler = new Handler(ctx.getMainLooper());
                                Runnable myRunnable = new Runnable(){
                                    public void run(){
                                        action.onSuccess(name, userImage);
                                    }
                                };
                                mainHandler.post(myRunnable);
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                String message = "Failed to reach facebook.  Error code: " + statusCode;
                Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
                action.onFailure();
            }
        });

    }


}
