package com.ascend.invest.handlers;

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
    public double price; // Price at which trade was locked
    public String upiId;
    public String transactionId; // UPI Ref No provided by buyer
    public String status; // PENDING, PAID, COMPLETED, DISPUTED, CANCELLED
    public long timestamp;
    public long expiryTime;
    public double commission;

    public P2POrder() {}

    public P2POrder(String id, String adId, String sellerUid, String sellerName, String buyerUid, String buyerName, double amount, double price, String upiId, double commission) {
        this.id = id;
        this.adId = adId;
        this.sellerUid = sellerUid;
        this.sellerName = sellerName;
        this.buyerUid = buyerUid;
        this.buyerName = buyerName;
        this.amount = amount;
        this.price = price;
        this.upiId = upiId;
        this.status = "PENDING";
        this.timestamp = System.currentTimeMillis();
        this.expiryTime = this.timestamp + (15 * 60 * 1000); // 15 Minutes
        this.commission = commission;
    }
}
