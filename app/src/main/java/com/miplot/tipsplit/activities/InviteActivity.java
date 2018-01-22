package com.miplot.tipsplit.activities;

import android.content.ClipboardManager;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.miplot.tipsplit.Keys;
import com.miplot.tipsplit.MainApplication;
import com.miplot.tipsplit.R;

import java.util.HashMap;
import java.util.Map;

public class InviteActivity extends AppCompatActivity {
    public static final String GROUP_NAME_INTENT_KEY = "group_name_key";

    private MainApplication app;
    private String groupName;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        app = (MainApplication)getApplication();

        Intent intent = getIntent();
        groupName = intent.getStringExtra(GROUP_NAME_INTENT_KEY);

        textView = findViewById(R.id.inviteKeyTextView);
        textView.setText("");
    }

    public void onCreateInviteClick(View view) {
        Map<String, Object> inviteData = new HashMap<>();
        inviteData.put(Keys.INVITE_GROUP_NAME_KEY, groupName);
        inviteData.put(Keys.INVITE_FROM_LOGIN_KEY, app.getCurUser().getLogin());
        inviteData.put(Keys.INVITE_ACTIVE_KEY, true);
        app.getStore().collection(Keys.COL_INVITES).add(inviteData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String inviteKey = documentReference.getId();
                        textView.setText(inviteKey);

                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        clipboard.setText(inviteKey);
                        Snackbar.make(textView, "Invite key has been copied to clipboard", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(app, "Failed to generate invite key", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
