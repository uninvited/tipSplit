package com.miplot.tipsplit.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.miplot.tipsplit.GroupMembersListAdapter;
import com.miplot.tipsplit.Keys;
import com.miplot.tipsplit.MainApplication;
import com.miplot.tipsplit.R;
import com.miplot.tipsplit.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class GroupMembersActivity extends AppCompatActivity {
    public static final String GROUP_NAME_INTENT_KEY = "group_name_key";

    private Context context;
    private MainApplication app;
    private String groupName;

    private List<GroupMembersListAdapter.DisplayedUser> displayedUsers;

    private RecyclerView groupUsersListView;
    private GroupMembersListAdapter groupMembersListAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_members);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        context = this;
        app = (MainApplication)getApplication();

        Intent intent = getIntent();
        groupName = intent.getStringExtra(GROUP_NAME_INTENT_KEY);
        if (groupName == null || groupName.isEmpty()) {
            throw new RuntimeException("Missing group id in intent");
        }

        layoutManager = new LinearLayoutManager(this);
        groupUsersListView = (RecyclerView) findViewById(R.id.group_members_list);
        groupUsersListView.setLayoutManager(layoutManager);

        displayedUsers = new ArrayList<>();
        groupMembersListAdapter = new GroupMembersListAdapter(context, displayedUsers);
        groupUsersListView.setAdapter(groupMembersListAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final CollectionReference groupsCref = app.getStore().collection(Keys.COL_GROUPS);
        final DocumentReference groupDref = groupsCref.document(groupName);
        groupDref.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                final List<GroupMembersListAdapter.DisplayedUser> displayedUsers = new ArrayList<>();

                Log.d("TAG", "Received group update");

                Map<String, Object> membersMap = (Map<String, Object>)documentSnapshot.get(Keys.GROUP_MEMBERS_KEY);
                for (Map.Entry<String, Object> member : membersMap.entrySet()) {
                    String login = member.getKey();
                    Map<String, Object> memberData = (Map<String, Object>)member.getValue();
                    String firstName = (String)memberData.get(Keys.GROUP_MEMBER_FIRST_NAME_KEY);
                    String lastName = (String)memberData.get(Keys.GROUP_MEMBER_LAST_NAME_KEY);
                    Double balance = (Double)memberData.get(Keys.GROUP_MEMBER_BALANCE_KEY);

                    GroupMembersListAdapter.DisplayedUser displayedUser = new GroupMembersListAdapter.DisplayedUser();
                    displayedUser.balance = balance;
                    displayedUser.user = new User(login, firstName, lastName);
                    displayedUsers.add(displayedUser);
                }
                Collections.sort(displayedUsers, new Comparator<GroupMembersListAdapter.DisplayedUser>() {
                    @Override
                    public int compare(GroupMembersListAdapter.DisplayedUser u1, GroupMembersListAdapter.DisplayedUser u2) {
                        return Double.valueOf(u1.balance).compareTo(u2.balance);
                    }
                });
                groupMembersListAdapter = new GroupMembersListAdapter(context, displayedUsers);
                groupUsersListView.setAdapter(groupMembersListAdapter);
            }
        });
    }

    public void onPrepareTxnClick(View view) {
        List<User> checkedUsers = groupMembersListAdapter.getCheckedUsers();

        String[] participantsList = new String[checkedUsers.size()];
        for (int i = 0; i < checkedUsers.size(); i++) {
            participantsList[i] = checkedUsers.get(i).getLogin();
        }

        if (checkedUsers.size() > 1) {
            Intent intent = new Intent(context, TxnActivity.class);

            intent.putExtra(TxnActivity.LOGINS_LIST_INTENT_KEY, participantsList);
            intent.putExtra(TxnActivity.GROUP_NAME_INTENT_KEY, groupName);
            startActivity(intent);
        } else {
            Snackbar.make(view, "Select at least 2 people", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.group_members_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        if (id == R.id.invite_user) {
            Intent intent = new Intent(context, InviteActivity.class);
            intent.putExtra(InviteActivity.GROUP_NAME_INTENT_KEY, groupName);
            context.startActivity(intent);
            return true;
        } else if (id == R.id.add_transaction) {
            onPrepareTxnClick(groupUsersListView);
            return true;
        } else if (id == R.id.recent_transactions) {
            Intent intent = new Intent(context, RecentTxnsActivity.class);
            intent.putExtra(RecentTxnsActivity.GROUP_NAME_INTENT_KEY, groupName);
            context.startActivity(intent);
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
