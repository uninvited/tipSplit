package com.miplot.tipsplit;

import java.util.Date;
import java.util.List;

public class Txn {
    private Date date;
    private String authorLogin;
    private List<TxnItem> txnItems;

    public Txn(Date date, String authorLogin, List<TxnItem> txnItems) {
        this.date = date;
        this.authorLogin = authorLogin;
        this.txnItems = txnItems;
    }

    public Date getDate() {
        return date;
    }

    public String getAuthorLogin() {
        return authorLogin;
    }

    public List<TxnItem> getTxnItems() {
        return txnItems;
    }
}
