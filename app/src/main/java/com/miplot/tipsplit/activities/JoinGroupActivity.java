package com.miplot.tipsplit.activities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.miplot.tipsplit.Keys;
import com.miplot.tipsplit.MainApplication;
import com.miplot.tipsplit.R;

import java.util.HashMap;
import java.util.Map;

public class JoinGroupActivity extends AppCompatActivity {

    private MainApplication app;
    private String groupName;

    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);
        app = (MainApplication)getApplication();

        editText = findViewById(R.id.editTextJoinGroup);
    }

    public void onJoinGroupClick(View view) {
        String inviteKey = editText.getText().toString();
        final DocumentReference dref = app.getStore().collection(Keys.COL_INVITES).document(inviteKey);

        app.getStore().runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                String login = app.getCurUser().getLogin();

                DocumentSnapshot invite = transaction.get(dref);
                groupName = invite.getString(Keys.INVITE_GROUP_NAME_KEY);
                DocumentReference groupDref = app.getStore().collection(Keys.COL_GROUPS).document(groupName);
                DocumentSnapshot group = transaction.get(groupDref);

                if (!invite.exists() || !group.exists() || !invite.getBoolean(Keys.INVITE_ACTIVE_KEY)) {
                    throw new FirebaseFirestoreException("Invalid Invite key",
                            FirebaseFirestoreException.Code.ABORTED);
                }
                Map<String, Object> members = (Map<String, Object>)group.get(Keys.GROUP_MEMBERS_KEY);
                if (members.containsKey(login)) {
                    throw new FirebaseFirestoreException("You are already in the group",
                            FirebaseFirestoreException.Code.ABORTED);
                }

                transaction.update(dref, Keys.INVITE_TO_LOGIN_KEY, login);
                transaction.update(dref, Keys.INVITE_ACTIVE_KEY, false);

                Map<String, Object> membersToMerge = new HashMap<>();
                Map<String, Object> member = new HashMap<>();
                Map<String, Object> memberData = new HashMap<>();
                memberData.put(Keys.GROUP_MEMBER_LOGIN_KEY, login);
                memberData.put(Keys.GROUP_MEMBER_FIRST_NAME_KEY, app.getCurUser().getFirstName());
                memberData.put(Keys.GROUP_MEMBER_LAST_NAME_KEY, app.getCurUser().getLastName());
                memberData.put(Keys.GROUP_MEMBER_BALANCE_KEY, (Double)0.0);
                member.put(login, memberData);
                membersToMerge.put(Keys.GROUP_MEMBERS_KEY, member);

                transaction.set(groupDref, membersToMerge, SetOptions.merge());

                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(app, "Successfully joined group " + groupName, Toast.LENGTH_LONG).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(app, "Failed to join group: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}
