package com.ascend.invest.handlers;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WalletAddressHandler {

    public interface WalletAddressCallback {
        void onAddressReceived(String address);
        void onError(String error);
    }

    public void getRandomAddress(WalletAddressCallback callback) {
        // Updated to use "deposit" node as requested by the user
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("deposit");
        
        mDatabase.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    List<String> addresses = new ArrayList<>();
                    
                    if (snapshot.hasChildren()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Object val = child.getValue();
                            if (val != null) {
                                addresses.add(val.toString());
                            }
                        }
                    } else {
                        Object val = snapshot.getValue();
                        if (val != null) {
                            addresses.add(val.toString());
                        }
                    }

                    if (!addresses.isEmpty()) {
                        // High-entropy secure shuffle for session-based selection
                        java.util.Collections.shuffle(addresses, new java.security.SecureRandom());
                        callback.onAddressReceived(addresses.get(0));
                    } else {
                        // Fallback to wallet_address if deposit node is empty
                        fetchFallback(callback);
                    }
                } else {
                    // Fallback to wallet_address if deposit node doesn't exist
                    fetchFallback(callback);
                }
            } else {
                fetchFallback(callback);
            }
        });
    }

    private void fetchFallback(WalletAddressCallback callback) {
        DatabaseReference fallbackRef = FirebaseDatabase.getInstance().getReference("wallet_address");
        fallbackRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                List<String> addresses = new ArrayList<>();
                if (snapshot.hasChildren()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Object val = child.getValue();
                        if (val != null) addresses.add(val.toString());
                    }
                } else {
                    Object val = snapshot.getValue();
                    if (val != null) addresses.add(val.toString());
                }

                if (!addresses.isEmpty()) {
                    java.util.Collections.shuffle(addresses, new java.security.SecureRandom());
                    callback.onAddressReceived(addresses.get(0));
                } else {
                    callback.onError("No wallet addresses available.");
                }
            } else {
                callback.onError("Database connection error.");
            }
        });
    }
}
