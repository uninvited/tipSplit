package com.miplot.tipsplit.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.miplot.tipsplit.MainApplication;
import com.miplot.tipsplit.R;
import com.miplot.tipsplit.User;

public class WelcomeActivity extends AppCompatActivity {
    private static final String TAG = "TAG";
    public static final int SIGNUP_REQUEST_CODE = 1;
    public static final int LOGIN_REQUEST_CODE = 2;

    private MainApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        app = (MainApplication) getApplication();

        User user = app.getCurUser();
        if (user != null) {
            toGroupsScreen();
        }
    }

    public void onSignUpClick(View view) {
        User user = app.getCurUser();
        if (user == null) {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivityForResult(intent, SIGNUP_REQUEST_CODE);
        } else {
            toGroupsScreen();
        }
    }

    public void onSignInClick(View view) {
        User user = app.getCurUser();
        if (user == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, LOGIN_REQUEST_CODE);
        } else {
            toGroupsScreen();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            String login;
            switch(requestCode) {
                case SIGNUP_REQUEST_CODE:
                    login = data.getStringExtra(SignUpActivity.RESULT_SIGNUP_KEY);
                    if (!login.isEmpty()) {
                        toGroupsScreen();
                    }
                    break;
                case LOGIN_REQUEST_CODE:
                    login = data.getStringExtra(LoginActivity.RESULT_LOGIN_KEY);
                    if (!login.isEmpty()) {
                        toGroupsScreen();
                    }
                    break;
            }
        }
    }

    private void toGroupsScreen() {
        startActivity(new Intent(this, GroupsActivity.class));
        finish();
    }
}
