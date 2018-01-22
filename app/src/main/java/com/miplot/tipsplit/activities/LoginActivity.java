package com.miplot.tipsplit.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.miplot.tipsplit.Keys;
import com.miplot.tipsplit.MainApplication;
import com.miplot.tipsplit.R;
import com.miplot.tipsplit.User;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    public static final String RESULT_LOGIN_KEY = "result_login_key";

    private MainApplication app;

    private EditText editTextLogin;
    private EditText editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        app = (MainApplication) getApplication();

        editTextLogin = (EditText)findViewById(R.id.editTextLogin);
        editTextPassword = (EditText)findViewById(R.id.editTextPassword);
    }

    public void onLoginClick(View view) {
        try {
            final String login = editTextLogin.getText().toString();
            final String password = editTextPassword.getText().toString();

            app.getStore().collection(Keys.COL_USERS).document(login).get().addOnSuccessListener(
                    new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String correctPass = documentSnapshot.getString(Keys.USER_PASSWORD_KEY);
                                if (correctPass.equals(Base64.encodeToString(password.getBytes(), 0))) {

                                    String firstName = documentSnapshot.getString(Keys.USER_FIRST_NAME_KEY);
                                    String lastName = documentSnapshot.getString(Keys.USER_LAST_NAME_KEY);
                                    User user = new User(login, firstName, lastName);
                                    app.saveCurUser(user);

                                    Toast.makeText(getApplicationContext(), "Welcome, " + firstName, Toast.LENGTH_LONG).show();

                                    Intent intent = new Intent();
                                    intent.putExtra(RESULT_LOGIN_KEY, user.getLogin());
                                    setResult(RESULT_OK, intent);
                                    finish();
                                    return;
                                }
                            }
                            Toast.makeText(getApplicationContext(), "Invalid Login/Password", Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Unable to log in: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (RuntimeException e) {
            Toast.makeText(this, "Unable to create user with this login", Toast.LENGTH_SHORT).show();
        }
    }
}
