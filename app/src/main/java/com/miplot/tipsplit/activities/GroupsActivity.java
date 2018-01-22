package com.miplot.tipsplit.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.miplot.tipsplit.BuildConfig;
import com.miplot.tipsplit.Group;
import com.miplot.tipsplit.GroupsListAdapter;
import com.miplot.tipsplit.Keys;
import com.miplot.tipsplit.MainApplication;
import com.miplot.tipsplit.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupsActivity extends AppCompatActivity {
    private MainApplication app;

    private RecyclerView groupsListView;
    private GroupsListAdapter groupsListAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_groups);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        app = (MainApplication)getApplication();

        FloatingActionButton fabAddGroup = (FloatingActionButton) findViewById(R.id.add_group);
        fabAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 startActivity(new Intent(app, CreateGroupActivity.class));
            }
        });

        layoutManager = new LinearLayoutManager(this);
        groupsListView = (RecyclerView) findViewById(R.id.groups_list);
        groupsListView.setLayoutManager(layoutManager);

        List<Group> groups = new ArrayList<>();
        groupsListAdapter = new GroupsListAdapter(this, groups);
        groupsListView.setAdapter(groupsListAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (app.getCurUser() == null) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return;
        }

        app.getStore().collection(Keys.COL_GROUPS).addSnapshotListener(this,
                new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                final List<Group> groups = new ArrayList<>();

                for (DocumentSnapshot document : documentSnapshots) {
                    String name = document.getString(Keys.GROUP_NAME_KEY);
                    String description = document.getString(Keys.GROUP_DESCRIPTION_KEY);

                    Map<String, Double> membersMap = (Map<String, Double>)document.get(Keys.GROUP_MEMBERS_KEY);
                    if (membersMap != null && membersMap.containsKey(app.getCurUser().getLogin())) {
                        groups.add(new Group(name, name, description));
                    }
                }
                groupsListAdapter.updateData(groups);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.groups_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.new_group) {
            startActivity(new Intent(this, CreateGroupActivity.class));
            return true;
        }
        else if (id == R.id.join_group) {
            startActivity(new Intent(this, JoinGroupActivity.class));
            return true;
        } else if (id == R.id.logout) {
            app.forgetCurUser();
            startActivity(new Intent(this, WelcomeActivity.class));
            return true;
        } else if (id == R.id.about) {
            showAboutInfo();
        }
        return super.onOptionsItemSelected(item);
    }

    private class ConfirmationDialogBuilder extends AlertDialog.Builder {
        ConfirmationDialogBuilder(Context context) {
            super(context, R.style.MrcAlertDialog);
            setCancelable(true);
            setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.dismiss();
                }
            });
        }
    }

    private void showAboutInfo() {
        ConfirmationDialogBuilder builder = new ConfirmationDialogBuilder(this);
        builder.setTitle("About TipSplit application");
        builder.setMessage(
                "Version: " + BuildConfig.VERSION_NAME + "\n" +
                "Author: miplot");
        builder.show();
    }
}
