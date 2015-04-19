package edu.wpi.cs403x.dyvo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;


public class FacebookLoginActivity extends ActionBarActivity {
    public static final String PREFS_NAME = "LoginPrefs";

    CallbackManager callbackManager;
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize the view and login button
        setContentView(R.layout.activity_facebook_login);
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile","email","user_friends"));

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // Go to the Main Activity
//                startActivity(new Intent(FacebookLoginActivity.this, MainActivity.class));
                loginToServer();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

        if (AccessToken.getCurrentAccessToken() != null) {
            // The user is already logged in to Facebook, so log into the server
            loginToServer();
        }
    }

    public void loginToServer() {
        // Make a toast to notify the user that a login is taking place
        // TODO: change to a spinning wheel instead of a toast
        Toast.makeText(getApplicationContext(), "Logging into Dyvo servers...", Toast.LENGTH_LONG).show();

        // Perform the Login
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("access_token", AccessToken.getCurrentAccessToken().getToken());
        client.post("http://dyvo.herokuapp.com/api/facebook/login", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                SharedPreferences.Editor editor = settings.edit();
                try {
                    JSONObject data = response.getJSONObject("data");
                    editor.putString("authentication_token", data.getString("authentication_token"));
                    editor.putString("email", data.getString("email"));
                    editor.apply();
                    startActivity(new Intent(FacebookLoginActivity.this, MainActivity.class));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                String message = "Failed to login to Dyvo's servers.  Error code: " + statusCode;
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_facebook_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
