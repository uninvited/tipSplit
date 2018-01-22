package com.miplot.tipsplit;

public class TxnItem {
    private User user;
    private double sum;

    public TxnItem(User user, double sum) {
        this.user = user;
        this.sum = sum;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User uid) {
        this.user = user;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }
}
