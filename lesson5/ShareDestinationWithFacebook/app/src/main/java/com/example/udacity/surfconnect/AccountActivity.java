package com.example.udacity.surfconnect;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.login.LoginManager;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import java.util.Locale;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

public class AccountActivity extends AppCompatActivity {

    ProfileTracker profileTracker;
    ImageView profilePic;
    TextView id;
    TextView infoLabel;
    TextView info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        FontHelper.setCustomTypeface(findViewById(R.id.view_root));

        profilePic = (ImageView) findViewById(R.id.profile_image);
        id = (TextView) findViewById(R.id.id);
        infoLabel = (TextView) findViewById(R.id.info_label);
        info = (TextView) findViewById(R.id.info);

        // register a receiver for the onCurrentProfileChanged event
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged (Profile oldProfile, Profile currentProfile) {
                if (currentProfile != null) {
                    displayProfileInfo(currentProfile);
                }
            }
        };

        if (AccessToken.getCurrentAccessToken() != null) {
            // If there is an access token then Login Button was used
            // Check if the profile has already been fetched
            Profile currentProfile = Profile.getCurrentProfile();
            if (currentProfile != null) {
                displayProfileInfo(currentProfile);
            }
            else {
                // Fetch the profile, which will trigger the onCurrentProfileChanged receiver
                Profile.fetchProfileForCurrentAccessToken();
            }
        }
        else {
            // Otherwise, get Account Kit login information
            AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                @Override
                public void onSuccess(final Account account) {
                    // get Account Kit ID
                    String accountKitId = account.getId();
                    id.setText(accountKitId);

                    PhoneNumber phoneNumber = account.getPhoneNumber();
                    if (account.getPhoneNumber() != null) {
                        // if the phone number is available, display it
                        String formattedPhoneNumber = formatPhoneNumber(phoneNumber.toString());
                        info.setText(formattedPhoneNumber);
                        infoLabel.setText(R.string.phone_label);
                    }
                    else {
                        // if the email address is available, display it
                        String emailString = account.getEmail();
                        info.setText(emailString);
                        infoLabel.setText(R.string.email_label);
                    }
                }

                @Override
                public void onError(final AccountKitError error) {
                    String toastMessage = error.getErrorType().getMessage();
                    Toast.makeText(AccountActivity.this, toastMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // unregister the profile tracker receiver
        profileTracker.stopTracking();
    }

    public void onLogout(View view) {
        // logout of Account Kit
        AccountKit.logOut();
        // logout of Login Button
        LoginManager.getInstance().logOut();

        // let MainActivity know the user logged out
        setResult(RESULT_OK);
        finish();
    }

    private void displayProfileInfo(Profile profile) {
        // get Profile ID
        String profileId = profile.getId();
        id.setText(profileId);

        // display the Profile name
        String name = profile.getName();
        info.setText(name);
        infoLabel.setText(R.string.name_label);

        // display the profile picture
        Uri profilePicUri = profile.getProfilePictureUri(100, 100);
        displayProfilePic(profilePicUri);
    }

    private String formatPhoneNumber(String phoneNumber) {
        // helper method to format the phone number for display
        try {
            PhoneNumberUtil pnu = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber pn = pnu.parse(phoneNumber, Locale.getDefault().getCountry());
            phoneNumber = pnu.format(pn, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
        }
        catch (NumberParseException e) {
            e.printStackTrace();
        }
        return phoneNumber;
    }

    private void displayProfilePic(Uri uri) {
        // helper method to load the profile pic in a circular imageview
        Transformation transformation = new RoundedTransformationBuilder()
                .cornerRadiusDp(30)
                .oval(false)
                .build();
        Picasso.with(AccountActivity.this)
                .load(uri)
                .placeholder(R.drawable.icon_profile_empty)
                .transform(transformation)
                .into(profilePic);
    }

}
