package com.miplot.tipsplit.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.miplot.tipsplit.Keys;
import com.miplot.tipsplit.MainApplication;
import com.miplot.tipsplit.R;
import com.miplot.tipsplit.User;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    public static final String RESULT_SIGNUP_KEY = "result_signup_key";

    private MainApplication app;

    private EditText editTextLogin;
    private EditText editTextPassword;
    private EditText editTextFirstName;
    private EditText editTextLastName;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        app = (MainApplication) getApplication();

        editTextLogin = (EditText)findViewById(R.id.editTextLogin);
        editTextPassword = (EditText)findViewById(R.id.editTextPassword);
        editTextFirstName = (EditText)findViewById(R.id.editTextFirstName);
        editTextLastName = (EditText)findViewById(R.id.editTextLastName);
    }

    public void onCreateUserClick(View view) {
        try {
            user = new User(
                    editTextLogin.getText().toString(),
                    editTextFirstName.getText().toString(),
                    editTextLastName.getText().toString());

            if (user.getLogin().length() < 2) {
                Toast.makeText(this, "Login must have at least 2 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (user.getFirstName().length() < 2) {
                Toast.makeText(this, "First name must have at least 2 characters", Toast.LENGTH_SHORT).show();
                return;
            }


            if (user.getLastName().length() < 2) {
                Toast.makeText(this, "Last name must have at least 2 characters", Toast.LENGTH_SHORT).show();
                return;
            }
            final String password = editTextPassword.getText().toString();

            app.getStore().collection(Keys.COL_USERS).document(user.getLogin()).get().addOnSuccessListener(
                    new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                Toast.makeText(getApplicationContext(), "This user already exists", Toast.LENGTH_LONG).show();
                            } else {
                                Map<String, Object> userData = new HashMap<>();
                                userData.put(Keys.USER_LOGIN_KEY, user.getLogin());
                                userData.put(Keys.USER_PASSWORD_KEY, Base64.encodeToString(password.getBytes(), 0));
                                userData.put(Keys.USER_FIRST_NAME_KEY, user.getFirstName());
                                userData.put(Keys.USER_LAST_NAME_KEY, user.getLastName());
                                app.getStore().collection(Keys.COL_USERS).document(user.getLogin()).set(userData)
                                        .addOnSuccessListener(onSignedUpListener);
                            }
                        }
                    });
        } catch (RuntimeException e) {
            Toast.makeText(this, "Unable to create user with this login", Toast.LENGTH_SHORT).show();
        }
    }

    private final OnSuccessListener<Void> onSignedUpListener = new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            Toast.makeText(getApplicationContext(), "User created!", Toast.LENGTH_LONG).show();

            app.saveCurUser(user);

            Intent intent = new Intent();
            intent.putExtra(RESULT_SIGNUP_KEY, user.getLogin());
            setResult(RESULT_OK, intent);
            finish();
        }
    };
}
