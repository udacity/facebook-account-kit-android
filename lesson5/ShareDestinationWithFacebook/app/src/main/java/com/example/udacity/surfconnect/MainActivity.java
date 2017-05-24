package com.example.udacity.surfconnect;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareMediaContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.widget.ShareButton;
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
    EditText statusText;
    ImageButton doneButton;
    ShareButton shareButton;
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
        statusText = (EditText) findViewById(R.id.status_text);
        doneButton = (ImageButton) findViewById(R.id.done_button);
        shareButton = (ShareButton) findViewById(R.id.share_button);
        recyclerView = (RecyclerView) findViewById(R.id.destination_list);

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null) {
            // Hide the friends button if there is no Facebook Login access token
            friendsButton.setVisibility(View.GONE);
        }

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

        // set listener for when the keyboard done button is pressed in the status text field
        statusText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onClickDoneButton(null);
                    return true;
                }
                return false;
            }
        });

        // show/hide the Done and Share buttons when the status text field get/loses focus
        statusText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    shareButton.setVisibility(View.GONE);
                    doneButton.setVisibility(View.VISIBLE);
                }
                else {
                    doneButton.setVisibility(View.GONE);
                }
            }
        });

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

    public void onClickFriendsButton(View v) {
        // show the Friends activity
        Intent intent = new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(intent);
    }

    public void onClickAccountButton(View v) {
        // show the Account activity
        Intent intent = new Intent(MainActivity.this, AccountActivity.class);
        startActivityForResult(intent, ACCOUNT_ACTIVITY_REQUEST_CODE);
    }

    public void onClickDoneButton(View v) {
        // stop editing the status text field & hide the keyboard
        statusText.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);

        // Show the Share button if there is text and the Facebook app is installed
        if (statusText.getText().length() > 0 && isFacebookAppInstalled()) {
            // set a demo image as the share content
            Bitmap photo = BitmapFactory.decodeResource(getResources(), R.drawable.image_bolinas);
            SharePhoto sharePhoto = new SharePhoto.Builder()
                    .setBitmap(photo)
                    .build();
            ShareContent shareContent = new ShareMediaContent.Builder()
                    .addMedium(sharePhoto)
                    .build();
            shareButton.setShareContent(shareContent);
            shareButton.setVisibility(View.VISIBLE);
        }

    }

    private boolean isFacebookAppInstalled() {
        // helper method to detect if the Facebook app is installed
        boolean isInstalled = false;
        try {
            if (getPackageManager().getApplicationInfo("com.facebook.katana", 0) != null) {
                isInstalled = true;
            }
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return isInstalled;
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

