package edu.wpi.cs403x.dyvo.api;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.wpi.cs403x.dyvo.db.VobsDbAdapter;

public class DyvoServer {
    private String email;
    private String authentication_token;
    private VobsDbAdapter db;
    private AsyncHttpClient client;
    private Context ctx;

    public DyvoServer(String email, String authentication_token, VobsDbAdapter db, Context ctx) {
        this.email = email;
        this.authentication_token = authentication_token;
        this.db = db;
        this.ctx = ctx;
    }

    public void refreshVobDatabaseDistanceBased(int latitude, int longitude, int distance, final DyvoServerAction action) {
        client = new AsyncHttpClient();
        client.addHeader("X-User-Email", email);
        client.addHeader("X-User-Token", authentication_token);

        RequestParams params = new RequestParams();

        params.put("latitude", latitude);
        params.put("longitude", longitude);
        params.put("distance", distance);

        client.get(ctx, "http://dyvo.herokuapp.com/api/vobs/within", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    db.deleteAllVobs();
                    JSONArray data = response.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject vob = data.getJSONObject(i);
                        String content = vob.getString("content");
                        String userId = vob.getString("user_id");
                        float longitude = (float) vob.getJSONArray("location").getDouble(0);
                        float latitude = (float) vob.getJSONArray("location").getDouble(1);
                        db.createVob(content, userId, longitude, latitude);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                action.onSuccess();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                action.onFailure();
            }
        });
    }

    public void refreshVobDatabase(final DyvoServerAction action) {

        client = new AsyncHttpClient();

        client.get(ctx, "http://dyvo.herokuapp.com/api/vobs", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    db.deleteAllVobs();
                    JSONArray data = response.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject vob = data.getJSONObject(i);
                        String content = vob.getString("content");
                        String userId = vob.getString("user_id");
                        float longitude = (float) vob.getJSONArray("location").getDouble(0);
                        float latitude = (float) vob.getJSONArray("location").getDouble(1);
                        db.createVob(content, userId, longitude, latitude);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                action.onSuccess();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                action.onFailure();
            }
        });
    }
}
