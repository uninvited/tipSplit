package com.miplot.tipsplit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.miplot.tipsplit.Keys;
import com.miplot.tipsplit.MainApplication;
import com.miplot.tipsplit.PaymentsListAdapter;
import com.miplot.tipsplit.R;
import com.miplot.tipsplit.TxnItem;
import com.miplot.tipsplit.User;

public class TxnActivity extends AppCompatActivity {
    public static final String GROUP_NAME_INTENT_KEY = "group_id_key";
    public static final String LOGINS_LIST_INTENT_KEY = "uids_list_key";

    private MainApplication app;
    private String groupName;
    private List<User> users;

    private RecyclerView paymentsListView;
    private PaymentsListAdapter paymentsListAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private Button createTxnButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_txn);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        app = (MainApplication)getApplication();

        Intent intent = getIntent();
        final String[] loginsArray = intent.getStringArrayExtra(LOGINS_LIST_INTENT_KEY);
        groupName = intent.getStringExtra(GROUP_NAME_INTENT_KEY);

        paymentsListView = (RecyclerView) findViewById(R.id.txn_users_list);
        createTxnButton = (Button) findViewById(R.id.createTxnButton);
        createTxnButton.setVisibility(View.GONE);

        layoutManager = new LinearLayoutManager(this);
        paymentsListView.setLayoutManager(layoutManager);

        users = new ArrayList<>();

        app.getStore().collection(Keys.COL_GROUPS).document(groupName).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null) {
                                Map<String, Object> membersMap = (Map<String, Object>)document.get(Keys.GROUP_MEMBERS_KEY);
                                for (String login : loginsArray) {
                                    Map<String, Object> memberData = (Map<String, Object>)membersMap.get(login);
                                    String firstName = (String)memberData.get(Keys.GROUP_MEMBER_FIRST_NAME_KEY);
                                    String lastName = (String)memberData.get(Keys.GROUP_MEMBER_LAST_NAME_KEY);
                                    users.add(new User(login, firstName, lastName));
                                }
                                paymentsListAdapter = new PaymentsListAdapter(users);
                                paymentsListView.setAdapter(paymentsListAdapter);
                                createTxnButton.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to get users list", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void onCreateTxnClick(View view) {
        final double THRESHOLD = 0.01;
        List<TxnItem> sumsPayed = paymentsListAdapter.getSumsPayed();
        double totalSum = 0;
        boolean isTrivial = true;

        for (TxnItem txnItem : sumsPayed) {
            totalSum += txnItem.getSum();
            if (Math.abs(txnItem.getSum()) >= THRESHOLD) {
                isTrivial = false;
            }
        }
        double minusPerUser = totalSum / users.size();
        for (TxnItem txnItem : sumsPayed) {
            txnItem.setSum(txnItem.getSum() - minusPerUser);
        }

        if (!isTrivial) {
            createTransaction(groupName, sumsPayed);
            finish();
        } else {
            Snackbar.make(view, "Won't create trivial transaction", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }
    }

    private void createTransaction(final String groupName, final List<TxnItem> txnItems) {
        final DocumentReference dref = app.getStore().collection(Keys.COL_GROUPS).document(groupName);
        app.getStore().runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot group = transaction.get(dref);
                Map<String, Object> members = (Map<String, Object>)group.get(Keys.GROUP_MEMBERS_KEY);

                String txnId = UUID.randomUUID().toString();
                final DocumentReference txnDref = app.getStore().collection(Keys.COL_TRANSACTIONS).document(txnId);
                Map<String, Object> txnData = new HashMap<>();
                txnData.put(Keys.TXN_DATE_KEY, new Date());
                txnData.put(Keys.TXN_GROUP_KEY, groupName);
                txnData.put(Keys.TXN_CREATED_BY_KEY, app.getCurUser().getLogin());

                Map<String, Object> paymentsData = new HashMap<>();

                for (TxnItem txnItem : txnItems) {
                    String login = txnItem.getUser().getLogin();
                    Map<String, Object> memberData = (Map<String, Object>)members.get(login);
                    double oldBalance = (Double)memberData.get(Keys.GROUP_MEMBER_BALANCE_KEY);
                    double newBalance = oldBalance + txnItem.getSum();

                    transaction.update(dref, Keys.GROUP_MEMBERS_KEY + "." + login + "." + Keys.GROUP_MEMBER_BALANCE_KEY, newBalance);

                    paymentsData.put(login, txnItem.getSum());
                }
                txnData.put(Keys.TXN_SUMS_KEY, paymentsData);
                transaction.set(txnDref, txnData);
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Transaction created", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Transaction failed", Toast.LENGTH_SHORT).show();
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
