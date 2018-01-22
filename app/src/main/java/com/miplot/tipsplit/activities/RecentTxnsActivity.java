package com.miplot.tipsplit.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.miplot.tipsplit.Keys;
import com.miplot.tipsplit.MainApplication;
import com.miplot.tipsplit.R;
import com.miplot.tipsplit.Txn;
import com.miplot.tipsplit.TxnItem;
import com.miplot.tipsplit.TxnsListAdapter;
import com.miplot.tipsplit.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class RecentTxnsActivity extends AppCompatActivity {
    public static final String GROUP_NAME_INTENT_KEY = "group_name_key";

    private MainApplication app;
    private Context context;
    private String groupName;

    private RecyclerView txnsListView;
    private TxnsListAdapter txnsListAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_txns);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        app = (MainApplication) getApplication();
        context = this;

        Intent intent = getIntent();
        groupName = intent.getStringExtra(GROUP_NAME_INTENT_KEY);
        if (groupName == null || groupName.isEmpty()) {
            throw new RuntimeException("Missing group id in intent");
        }
        layoutManager = new LinearLayoutManager(this);
        txnsListView = (RecyclerView) findViewById(R.id.recent_txns_list);
        txnsListView.setLayoutManager(layoutManager);

        final CollectionReference txnsCref = app.getStore().collection(Keys.COL_TRANSACTIONS);

        txnsCref.whereEqualTo(Keys.TXN_GROUP_KEY, groupName)
                //.orderBy(Keys.TXN_CREATED_BY_KEY, Query.Direction.DESCENDING)
                //.limit(20)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots) {
                List<Txn> transactions = new ArrayList<>();
                for (DocumentSnapshot document : documentSnapshots) {
                    String login = document.getString(Keys.TXN_CREATED_BY_KEY);
                    Date date = document.getDate(Keys.TXN_DATE_KEY);
                    List<TxnItem> txnItems = new ArrayList<>();
                    Map<String, Object> details = (Map<String, Object>)document.get(Keys.TXN_SUMS_KEY);

                    for (Map.Entry<String, Object> item : details.entrySet()) {
                        TxnItem txnItem = new TxnItem(new User(item.getKey(), "", ""), (Double)item.getValue());
                        txnItems.add(txnItem);
                    }
                    transactions.add(new Txn(date, login, txnItems));
                }
                Collections.sort(transactions, new Comparator<Txn>() {
                    @Override
                    public int compare(Txn t1, Txn t2) {
                        return t2.getDate().compareTo(t1.getDate());
                    }
                });
                transactions = transactions.subList(0, Math.min(transactions.size(), 20));
                txnsListAdapter = new TxnsListAdapter(transactions);
                txnsListView.setAdapter(txnsListAdapter);
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed to load transactions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
