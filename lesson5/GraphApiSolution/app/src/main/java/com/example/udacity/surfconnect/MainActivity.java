package com.example.udacity.surfconnect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static int ACCOUNT_ACTIVITY_REQUEST_CODE = 1;
    ProfileTracker profileTracker;
    ImageView friendsButton;
    ImageView accountButton;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        friendsButton = (ImageView) findViewById(R.id.friends_button);
        accountButton = (ImageView) findViewById(R.id.account_button);
        recyclerView = (RecyclerView) findViewById(R.id.destination_list);

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null) {
            // Hide the find friends button if there is no Facebook Login access token
            friendsButton.setVisibility(View.GONE);
        }
        else {
            // set click listener on find friends button
            friendsButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, FriendsActivity.class);
                    startActivity(intent);
                }
            });
        }

        // set click listener on account button
        accountButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                startActivityForResult(intent, ACCOUNT_ACTIVITY_REQUEST_CODE);
            }
        });

        // register a receiver for the onCurrentProfileChanged event
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged (Profile oldProfile, Profile currentProfile) {
                if (currentProfile != null) {
                    displayProfilePic(currentProfile);
                }
            }
        };

        // show profile pic on account button
        Profile currentProfile = Profile.getCurrentProfile();
        if (currentProfile != null) {
            displayProfilePic(currentProfile);
        }
        else {
            // Fetch the profile, which will trigger the onCurrentProfileChanged receiver
            Profile.fetchProfileForCurrentAccessToken();
        }

        // for demonstration, get some friend profile pics (if available)
        if (accessToken != null && accessToken.getPermissions().contains("user_friends")) {
            // make the API call to fetch friends list
            Bundle parameters = new Bundle();
            parameters.putString("fields", "picture");
            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/me/friends",
                    parameters,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            ArrayList<String> friendPics = new ArrayList<>();

                            if (response.getError() == null) {
                                // parse json data
                                JSONObject jsonResponse = response.getJSONObject();
                                try {
                                    JSONArray jsonData = jsonResponse.getJSONArray("data");
                                    for (int i = 0; i < jsonData.length(); i++) {
                                        JSONObject jsonUser = jsonData.getJSONObject(i);
                                        String image = jsonUser.getJSONObject("picture").getJSONObject("data").getString("url");
                                        friendPics.add(image);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            // initialize the list view adapter with friend pics
                            recyclerView.setAdapter(new DestinationsAdapter(friendPics));
                        }
                    }
            ).executeAsync();
        }
        else {
            // initialize the list view adapter without friend pics
            recyclerView.setAdapter(new DestinationsAdapter());
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACCOUNT_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            // if the user logged out in AccountActivity, show the login screen
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // unregister the profile tracker receiver
        profileTracker.stopTracking();
    }

    private void displayProfilePic(Profile profile) {
        // helper method to load the profile pic in a circular imageview
        Uri uri = profile.getProfilePictureUri(28, 28);
        Transformation transformation = new RoundedTransformationBuilder()
                .cornerRadiusDp(30)
                .oval(false)
                .build();
        Picasso.with(this)
                .load(uri)
                .placeholder(R.drawable.icon_profile_empty)
                .transform(transformation)
                .into(accountButton);
    }

}

