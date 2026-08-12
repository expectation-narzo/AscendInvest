package com.ascend.invest.admin;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class P2POrder {
    public String id;
    public String adId;
    public String sellerUid;
    public String sellerName;
    public String buyerUid;
    public String buyerName;
    public double amount;
    public double price;
    public String upiId;
    public String transactionId;
    public String status;
    public long timestamp;
    public long expiryTime;
    public double commission;

    public P2POrder() {}
}
