package com.ascend.invest.handlers;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ascend.invest.databinding.SectionDepositsBinding;
import com.ascend.invest.databinding.SectionWithdrawBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionHandler {

    private String userId;
    private SectionDepositsBinding depositBinding;
    private SectionWithdrawBinding withdrawBinding;

    private List<Transaction> depositList, withdrawList;
    private TransactionAdapter depositAdapter, withdrawAdapter;

    public TransactionHandler(SectionDepositsBinding depositBinding, SectionWithdrawBinding withdrawBinding) {
        this.depositBinding = depositBinding;
        this.withdrawBinding = withdrawBinding;

        depositList = new ArrayList<>();
        withdrawList = new ArrayList<>();

        depositAdapter = new TransactionAdapter(depositList);
        withdrawAdapter = new TransactionAdapter(withdrawList);

        if (depositBinding != null) {
            depositBinding.rvDepositHistory.setLayoutManager(new LinearLayoutManager(depositBinding.getRoot().getContext()));
            depositBinding.rvDepositHistory.setAdapter(depositAdapter);
            depositAdapter.setOnTransactionClickListener(this::showTransactionDetails);
        }

        if (withdrawBinding != null) {
            withdrawBinding.rvWithdrawalHistory.setLayoutManager(new LinearLayoutManager(withdrawBinding.getRoot().getContext()));
            withdrawBinding.rvWithdrawalHistory.setAdapter(withdrawAdapter);
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
                        if (transaction.getType() == null || transaction.getType().isEmpty()) {
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
                if (depositBinding != null) {
                    depositBinding.historyHeader.setVisibility(depositList.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
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
                        if (transaction.getType() == null || transaction.getType().isEmpty()) {
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
                if (withdrawBinding != null) {
                    withdrawBinding.withdrawHistoryHeader.setVisibility(withdrawList.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showTransactionDetails(Transaction transaction) {
        Context context = depositBinding != null ? depositBinding.getRoot().getContext() : 
                         (withdrawBinding != null ? withdrawBinding.getRoot().getContext() : null);
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
