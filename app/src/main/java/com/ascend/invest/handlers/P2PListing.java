package com.ascend.invest.handlers;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class P2PListing {
    public String id;
    public String sellerUid;
    public String sellerName;
    public double totalAmount;
    public double remainingAmount;
    public double price;
    public double minLimit;
    public String upiId;
    public long timestamp;
    public String status; // ACTIVE, COMPLETED, CANCELLED

    public P2PListing() {}
}
