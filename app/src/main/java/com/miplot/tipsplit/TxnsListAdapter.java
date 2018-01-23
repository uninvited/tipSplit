package com.miplot.tipsplit;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class TxnsListAdapter extends RecyclerView.Adapter<TxnsListAdapter.ViewHolder> {
    private List<Txn> txns;
    private int size;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    NumberFormat decimalFormat = new DecimalFormat("#0.00");

    public TxnsListAdapter(List<Txn> txns) {
        super();
        this.txns = txns;
        size = txns.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView dateTextView;
        private TextView loginTextView;
        private TextView detailsTextView;

        ViewHolder(View v, TextView dateTextView, TextView loginTextView, TextView detailsTextView) {
            super(v);
            this.dateTextView = dateTextView;
            this.loginTextView = loginTextView;
            this.detailsTextView = detailsTextView;
        }
    }

    @Override
    public TxnsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View convertView;
        convertView = inflater.inflate(R.layout.txn_list_item, parent, false);

        return new TxnsListAdapter.ViewHolder(convertView,
                (TextView) convertView.findViewById(R.id.txnDate),
                (TextView) convertView.findViewById(R.id.authorLogin),
                (TextView) convertView.findViewById(R.id.txnDetails));
    }

    @Override
    public void onBindViewHolder(final TxnsListAdapter.ViewHolder holder, int position) {
        holder.loginTextView.setText("by " + txns.get(position).getAuthorLogin());
        holder.dateTextView.setText(dateFormat.format(txns.get(position).getDate()));

        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (TxnItem txnItem : txns.get(position).getTxnItems()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(txnItem.getUser().getLogin())
                    .append(": ")
                    .append(decimalFormat.format(txnItem.getSum()));
        }
        holder.detailsTextView.setText(sb.toString());
    }

    @Override
    public int getItemCount() {
        return size;
    }
}