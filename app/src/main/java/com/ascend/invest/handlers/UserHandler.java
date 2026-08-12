package com.ascend.invest.handlers;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Map;

public class UserHandler {
    private static UserHandler instance;
    private final DatabaseReference mUserRef;

    private UserHandler() {
        this.mUserRef = FirebaseDatabase.getInstance().getReference().child("users");
    }

    public static synchronized UserHandler getInstance() {
        if (instance == null) {
            instance = new UserHandler();
        }
        return instance;
    }

    public DatabaseReference getUserRef(String userId) {
        return mUserRef.child(userId);
    }

    public void getUserData(String userId, ValueEventListener listener) {
        mUserRef.child(userId).addListenerForSingleValueEvent(listener);
    }

    public void getUserDataFresh(String userId, ValueEventListener listener) {
        mUserRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Ensure we are getting the latest data from server, not cache
                listener.onDataChange(snapshot);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onCancelled(error);
            }
        });
    }

    public void listenToUserData(String userId, ValueEventListener listener) {
        DatabaseReference ref = mUserRef.child(userId);
        ref.addValueEventListener(listener);
    }

    public void updateUserData(String userId, Map<String, Object> updates, com.google.android.gms.tasks.OnSuccessListener<Void> successListener, com.google.android.gms.tasks.OnFailureListener failureListener) {
        mUserRef.child(userId).updateChildren(updates)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }
    
    public DatabaseReference getTransactionsRef(String userId, String type) {
        return mUserRef.child(userId).child("transactions").child(type);
    }

    public DatabaseReference getActivePlansRef(String userId) {
        return mUserRef.child(userId).child("active_plans");
    }

    public com.google.firebase.database.Query getUsersByReferrer(String referrerId) {
        return mUserRef.orderByChild("referredBy").equalTo(referrerId);
    }

    public com.google.firebase.database.Query getUserByReferralCode(String referralCode) {
        return FirebaseDatabase.getInstance().getReference().child("referral_codes").child(referralCode);
    }
}
