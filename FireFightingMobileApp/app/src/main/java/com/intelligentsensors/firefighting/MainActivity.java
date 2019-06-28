package com.intelligentsensors.firefighting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.intelligentsensors.firefighting.util.CurrentUser;
import com.intelligentsensors.firefighting.util.UserRepository;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class MainActivity extends AppCompatActivity {

    UserRepository repo;
    AsyncHttpClient client = new AsyncHttpClient();
    EditText usernameField;
    EditText passwordField;
    CurrentUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repo = new UserRepository();

        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);

        Button loginButton = findViewById(R.id.registerButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(usernameField.getText().toString(), passwordField.getText().toString());
            }
        });

        TextView registerLabel = findViewById(R.id.registerLabel);
        registerLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        currentUser = (CurrentUser)getApplication();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    private void login(final String username, String password){

        try {
            AsyncHttpClient httpClient = new AsyncHttpClient();
            httpClient.addHeader("Accept", "application/json");
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("email", username);
            jsonParams.put("pass", password);
            StringEntity entity = new StringEntity(jsonParams.toString());
            client.post(this, getString(R.string.system_url) + getString(R.string.system_url_login), entity, "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject obj) {
                    System.err.println("onSuccess! statusCode " + statusCode + '\n' +
                            "Response " + obj.toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    System.err.println("onFailure! statusCode " + statusCode + '\n' +
                            "Response " + responseString);

                    if(statusCode == 200){
                        currentUser.setNewLoggedInUser(username, "A Name", false);
                        usernameField.setText("");
                        passwordField.setText("");
                        startActivity(new Intent(getApplicationContext(), MainScreen.class));
                    } else Toast.makeText(getApplicationContext(), getString(R.string.login_error), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void register(){
        startActivity(new Intent(this, RegisterActivity.class));
    }

    private boolean isLoggedInAdmin(){
        boolean result = false;

        return result;
    }

    @Override
    public void onBackPressed() {
       //Do nothing so as not to mess with activities ordering
    }

}
