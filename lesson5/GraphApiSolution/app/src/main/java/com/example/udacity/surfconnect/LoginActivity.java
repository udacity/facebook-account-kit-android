package com.example.udacity.surfconnect;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    public static int APP_REQUEST_CODE = 1;
    EditText loginText;
    LoginButton loginButton;
    Button accountkitButton;
    CallbackManager callbackManager;
    AppEventsLogger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FontHelper.setCustomTypeface(findViewById(R.id.view_root));
        logger = AppEventsLogger.newLogger(this);

        loginText = (EditText) findViewById(R.id.login_text);
        accountkitButton = (Button) findViewById(R.id.accountkit_button);
        accountkitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String login = loginText.getText().toString();
                if (login.contains("@")) {
                    logger.logEvent("emailLogin");
                    onAccountKitLogin(login, LoginType.EMAIL);
                } else {
                    logger.logEvent("phoneLogin");
                    onAccountKitLogin(login, LoginType.PHONE);
                }
            }
        });

        loginButton = (LoginButton) findViewById(R.id.facebook_login_button);
        loginButton.setReadPermissions("email");

        // Login Button callback registration
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                launchMainActivity();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException exception) {
                // display error
                String toastMessage = exception.getMessage();
                Toast.makeText(LoginActivity.this, toastMessage, Toast.LENGTH_LONG).show();
            }
        });

        // check for an existing access token
        AccessToken accessToken = AccountKit.getCurrentAccessToken();
        com.facebook.AccessToken loginToken = com.facebook.AccessToken.getCurrentAccessToken();
        if (accessToken != null || loginToken != null) {
            // if previously logged in, proceed to the account activity
            launchMainActivity();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Forward result to the callback manager for Login Button
        callbackManager.onActivityResult(requestCode, resultCode, data);

        // For Account Kit, confirm that this response matches your request
        if (requestCode == APP_REQUEST_CODE) {
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if (loginResult.getError() != null) {
                // display login error
                String toastMessage = loginResult.getError().getErrorType().getMessage();
                Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
            } else if (loginResult.getAccessToken() != null) {
                // on successful login, proceed to the account activity
                launchMainActivity();
            }
        }
    }

    private void onAccountKitLogin(final String login, final LoginType loginType) {
        // create intent for the Account Kit activity
        final Intent intent = new Intent(this, AccountKitActivity.class);

        // configure login type and response type
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        loginType,
                        AccountKitActivity.ResponseType.TOKEN
                );

        if (loginType == LoginType.EMAIL) {
            configurationBuilder.setInitialEmail(login);
        }
        else {
            PhoneNumber phoneNumber = new PhoneNumber(Locale.getDefault().getCountry(), login, null);
            configurationBuilder.setInitialPhoneNumber(phoneNumber);
        }
        final AccountKitConfiguration configuration = configurationBuilder.build();

        // launch the Account Kit activity
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configuration);
        startActivityForResult(intent, APP_REQUEST_CODE);
    }

    private void launchMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
