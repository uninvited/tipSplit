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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.miplot.tipsplit.Keys;
import com.miplot.tipsplit.MainApplication;
import com.miplot.tipsplit.R;

import java.util.HashMap;
import java.util.Map;


public class CreateGroupActivity extends AppCompatActivity {
    MainApplication app;

    private EditText editTextName;
    private EditText editTextDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        app = (MainApplication)getApplication();

        editTextName = (EditText)findViewById(R.id.editTextGroupName);
        editTextDescription = (EditText)findViewById(R.id.editTextGroupDescription);
    }

    public void onCreateGroupClick(View view) {
        try {
            final String name = editTextName.getText().toString();
            final String description = editTextDescription.getText().toString();

            if (name.length() < 2) {
                Toast.makeText(this, "Group name must have at least 2 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            final CollectionReference groupsCref = app.getStore().collection(Keys.COL_GROUPS);
            final DocumentReference groupDref = groupsCref.document(name);

            app.getStore().runTransaction(new Transaction.Function<Void>() {
                @Nullable
                @Override
                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                    if (transaction.get(groupDref).exists()) {
                        throw new FirebaseFirestoreException("Group exists",
                                FirebaseFirestoreException.Code.ABORTED);
                    }

                    final Map<String, Object> group = new HashMap<>();
                    group.put(Keys.GROUP_NAME_KEY, name);
                    group.put(Keys.GROUP_DESCRIPTION_KEY, description);

                    Map<String, Object> members = new HashMap<>();
                    Map<String, Object> memberData = new HashMap<>();
                    memberData.put(Keys.GROUP_MEMBER_LOGIN_KEY, app.getCurUser().getLogin());
                    memberData.put(Keys.GROUP_MEMBER_FIRST_NAME_KEY, app.getCurUser().getFirstName());
                    memberData.put(Keys.GROUP_MEMBER_LAST_NAME_KEY, app.getCurUser().getLastName());
                    memberData.put(Keys.GROUP_MEMBER_BALANCE_KEY, (Double)0.0);
                    members.put(app.getCurUser().getLogin(), memberData);

                    group.put(Keys.GROUP_MEMBERS_KEY, members);

                    transaction.set(groupDref, group);

                    return null;
                }
            })
                    .addOnSuccessListener(onGroupCreated)
                    .addOnFailureListener(onCreateGroupFailed);
        } catch (RuntimeException e) {
            Toast.makeText(this, "Failed to create group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private final OnSuccessListener<Void> onGroupCreated = new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            Toast.makeText(getApplicationContext(), "Group created", Toast.LENGTH_SHORT).show();
            finish();
        }
    };

    private final OnFailureListener onCreateGroupFailed = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(getApplicationContext(), "Failed to create group: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    };
}
