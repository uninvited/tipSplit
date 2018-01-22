package com.miplot.tipsplit;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PaymentsListAdapter extends RecyclerView.Adapter<PaymentsListAdapter.ViewHolder> {
    private List<User> users;
    private List<Double> sumPayed;
    private int size;

    public PaymentsListAdapter(List<User> users) {
        super();
        this.users = users;
        size = users.size();
        this.sumPayed = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            sumPayed.add(0.0);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private EditText sumPayedEditText;

        ViewHolder(View v, TextView nameTextView, EditText sumPayedEditText) {
            super(v);
            this.nameTextView = nameTextView;
            this.sumPayedEditText = sumPayedEditText;
        }
    }

    @Override
    public PaymentsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View convertView;
        convertView = inflater.inflate(R.layout.payment_list_item, parent, false);

        return new PaymentsListAdapter.ViewHolder(convertView,
                (TextView) convertView.findViewById(R.id.userName),
                (EditText) convertView.findViewById(R.id.userPayed));
    }

    @Override
    public void onBindViewHolder(final PaymentsListAdapter.ViewHolder holder, int position) {
        holder.nameTextView.setText(users.get(position).getName());
        holder.sumPayedEditText.setText(String.format(Locale.ENGLISH, "%.0f", sumPayed.get(position)));

        holder.sumPayedEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int pos = holder.getAdapterPosition();
                String str = s.toString();
                sumPayed.set(pos, str.isEmpty() ? 0.0 : Double.parseDouble(str));
            }
        });
    }

    @Override
    public int getItemCount() {
        return size;
    }

    public List<TxnItem> getSumsPayed() {
        List<TxnItem> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(new TxnItem(users.get(i), sumPayed.get(i)));
        }
        Log.e("TAG", "Sums paid: " + result);
        return result;
    }
}