package com.intelligentsensors.firefighting;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class RegisterActivity extends AppCompatActivity {

    AsyncHttpClient client = new AsyncHttpClient();
    Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

    }

    private void register() {
        EditText email = findViewById(R.id.usernameField);
        EditText password = findViewById(R.id.passwordField);
        EditText name = findViewById(R.id.nameField);
        EditText usrType = findViewById(R.id.userTypeField);

        try {
            AsyncHttpClient httpClient = new AsyncHttpClient();
            httpClient.addHeader("Accept", "application/json");
            JSONObject jsonParams = new JSONObject();
            jsonParams.put("email", email.getText());
            jsonParams.put("pass", password.getText());
            jsonParams.put("name", name.getText());
            jsonParams.put("usrType", usrType.getText());
            StringEntity entity = new StringEntity(jsonParams.toString());
            client.post(this, getString(R.string.system_url) + getString(R.string.system_url_registerUser), entity, "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject obj) {
                    Toast.makeText(getApplication(),
                            "Success",
                            Toast.LENGTH_LONG).show();
                    System.out.println("statusCode " + statusCode + '\n' +
                            "Response " + obj.toString());
                    finish();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    if(statusCode == 200){
                        Toast.makeText(getApplication(),
                                "Success",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                    else {
                        Toast.makeText(getApplication(),
                                "statusCode " + statusCode + '\n' + "Response " + responseString,
                                Toast.LENGTH_LONG).show();
                        System.err.println("statusCode " + statusCode + '\n' +
                                "Response " + responseString);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
