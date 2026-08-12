package com.ascend.invest.handlers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChartHandler {

    private LineChart chart;
    private DatabaseReference mDatabase;
    private String userId;
    private String transactionType;
    private String themeColorHex;
    private int gradientResId;
    private Context context;
    private String titleFilter; // If not null, only transactions whose title contains this will be included

    public ChartHandler(LineChart chart, String transactionType, String hexColor, int gradientResId) {
        this(chart, transactionType, hexColor, gradientResId, null);
    }

    public ChartHandler(LineChart chart, String transactionType, String hexColor, int gradientResId, String titleFilter) {
        this.chart = chart;
        this.context = chart.getContext();
        this.transactionType = transactionType;
        this.themeColorHex = hexColor;
        this.gradientResId = gradientResId;
        this.titleFilter = titleFilter;
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
        applyTheme();
    }

    private void applyTheme() {
        chart.setClipToPadding(false);
        chart.setClipChildren(false);
        chart.getXAxis().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisLeft().setSpaceBottom(40f);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setHardwareAccelerationEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.setViewPortOffsets(0f, 0f, 0f, 0f);
        chart.setExtraOffsets(0f, 0f, 0f, 0f);
        chart.setMinOffset(0f);
        chart.setTouchEnabled(false);
        chart.invalidate();
    }

    public void init(String userId) {
        this.userId = userId;
        fetchTransactions();
    }

    private void fetchTransactions() {
        UserHandler.getInstance().getTransactionsRef(userId, transactionType)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Transaction> transactions = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Transaction t = ds.getValue(Transaction.class);
                            if (t != null && "Success".equals(t.getStatus())) {
                                if (titleFilter == null || (t.getTitle() != null && t.getTitle().contains(titleFilter))) {
                                    transactions.add(t);
                                }
                            }
                        }
                        updateChart(transactions);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void updateChart(List<Transaction> transactions) {
        ArrayList<Entry> entries = new ArrayList<>();
        double runningTotal = 0;
        int xIndex = 0;

        // Start with zero at the beginning
        entries.add(new Entry(xIndex++, 0f));

        Collections.sort(transactions, (t1, t2) -> Long.compare(t1.getTimestamp(), t2.getTimestamp()));

        for (Transaction t : transactions) {
            String amountValue = t.getAmount().replaceAll("[^0-9.]", "");
            try {
                double amount = Double.parseDouble(amountValue);
                runningTotal += amount;
                
                // Add exactly one point per transaction for an "exact entries" trend
                entries.add(new Entry(xIndex++, (float) runningTotal));
                
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        // If no transactions, add a second point to keep the chart valid
        if (entries.size() == 1) {
            entries.add(new Entry(1, 0f));
        }

        LineDataSet dataSet = new LineDataSet(entries, "");

        // --- PROFESSIONAL THEME ---
        dataSet.setColor(Color.parseColor(themeColorHex));
        dataSet.setLineWidth(3f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(Color.parseColor(themeColorHex));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.WHITE);
        dataSet.setCircleHoleRadius(2f);
        dataSet.setDrawValues(false);

        // Cubic intensity for a smoother, professional modern look
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.15f);

        dataSet.setDrawFilled(true);
        try {
            Drawable drawable = ContextCompat.getDrawable(context, gradientResId);
            if (drawable != null) {
                dataSet.setFillDrawable(drawable);
            } else {
                dataSet.setFillColor(Color.parseColor(themeColorHex));
            }
        } catch (Exception e) {
            dataSet.setFillColor(Color.parseColor(themeColorHex));
        }

        dataSet.setHighlightEnabled(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.animateX(800); // Add a professional entrance animation
        chart.invalidate();
    }
}
