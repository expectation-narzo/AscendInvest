package com.ascend.invest.handlers;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ascend.invest.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionHandler {

    private String userId;
    private RecyclerView rvDeposit, rvWithdraw;
    private View cardDeposit, cardWithdraw;

    private List<Transaction> depositList, withdrawList;
    private TransactionAdapter depositAdapter, withdrawAdapter;

    public TransactionHandler(View rootView) {
        // Initialize views from the root view (Activity layout)
        rvDeposit = rootView.findViewById(R.id.rv_deposit_history);
        rvWithdraw = rootView.findViewById(R.id.rv_withdrawal_history);
        cardDeposit = rootView.findViewById(R.id.history_header); // Using header as visibility anchor
        cardWithdraw = rootView.findViewById(R.id.withdraw_history_header); // Using header as visibility anchor

        depositList = new ArrayList<>();
        withdrawList = new ArrayList<>();

        depositAdapter = new TransactionAdapter(depositList);
        withdrawAdapter = new TransactionAdapter(withdrawList);

        if (rvDeposit != null) {
            rvDeposit.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
            rvDeposit.setAdapter(depositAdapter);
            depositAdapter.setOnTransactionClickListener(this::showTransactionDetails);
        }

        if (rvWithdraw != null) {
            rvWithdraw.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
            rvWithdraw.setAdapter(withdrawAdapter);
            withdrawAdapter.setOnTransactionClickListener(this::showTransactionDetails);
        }
    }

    public void fetchTransactions(String userId) {
        this.userId = userId;
        if (userId != null) {
            fetchDepositTransactions();
            fetchWithdrawalTransactions();
        }
    }

    private void fetchDepositTransactions() {
        UserHandler.getInstance().getTransactionsRef(userId, "deposit").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                depositList.clear();
                for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                    Transaction transaction = transactionSnapshot.getValue(Transaction.class);
                    if (transaction != null) {
                        // Ensure type is set to deposit
                        if (transaction.getType() == null || transaction.getType().isEmpty()) {
                            // If type is not set, create a new transaction with type
                            transaction = new Transaction(
                                transaction.getId(),
                                transaction.getTitle(),
                                transaction.getSubtitle(),
                                transaction.getAmount(),
                                transaction.getStatus(),
                                "deposit",
                                transaction.getTimestamp()
                            );
                        }
                        depositList.add(transaction);
                    }
                }
                Collections.sort(depositList, (t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
                depositAdapter.notifyDataSetChanged();
                if (cardDeposit != null) {
                    cardDeposit.setVisibility(depositList.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });
    }

    private void fetchWithdrawalTransactions() {
        UserHandler.getInstance().getTransactionsRef(userId, "withdraw").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                withdrawList.clear();
                for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                    Transaction transaction = transactionSnapshot.getValue(Transaction.class);
                    if (transaction != null) {
                        // Ensure type is set to withdraw
                        if (transaction.getType() == null || transaction.getType().isEmpty()) {
                            // If type is not set, create a new transaction with type
                            transaction = new Transaction(
                                transaction.getId(),
                                transaction.getTitle(),
                                transaction.getSubtitle(),
                                transaction.getAmount(),
                                transaction.getStatus(),
                                "withdraw",
                                transaction.getTimestamp()
                            );
                        }
                        withdrawList.add(transaction);
                    }
                }
                Collections.sort(withdrawList, (t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
                withdrawAdapter.notifyDataSetChanged();
                if (cardWithdraw != null) {
                    cardWithdraw.setVisibility(withdrawList.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });
    }

    private void showTransactionDetails(Transaction transaction) {
        Context context = rvDeposit != null ? rvDeposit.getContext() : rvWithdraw.getContext();
        if (context == null) return;

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date(transaction.getTimestamp()));

        String details = "Transaction ID: " + transaction.getId() + "\n" +
                        "Type: " + transaction.getType() + "\n" +
                        "Title: " + transaction.getTitle() + "\n" +
                        "Amount: " + transaction.getAmount() + "\n" +
                        "Status: " + transaction.getStatus() + "\n" +
                        "Date: " + formattedDate + "\n" +
                        "Details: " + transaction.getSubtitle();

        new AlertDialog.Builder(context)
                .setTitle("Transaction Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }
}
