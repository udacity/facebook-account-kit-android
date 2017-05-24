package com.example.udacity.surfconnect;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class FriendsActivity extends AppCompatActivity {

    CallbackManager callbackManager;
    RecyclerView recyclerView;
    TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        FontHelper.setCustomTypeface(findViewById(R.id.view_root));

        if (AccessToken.getCurrentAccessToken() == null) {
            // a Facebook Login access token is required
            finish();
            return;
        }

        emptyText = (TextView) findViewById(R.id.empty_text);
        recyclerView = (RecyclerView) findViewById(R.id.friends_list);
        callbackManager = CallbackManager.Factory.create();
    }

}
