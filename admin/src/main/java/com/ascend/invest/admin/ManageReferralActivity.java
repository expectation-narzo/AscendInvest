package com.ascend.invest.admin;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.ascend.invest.admin.databinding.ActivityManageReferralBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class ManageReferralActivity extends AppCompatActivity {
    private ActivityManageReferralBinding binding;
    private DatabaseReference mRef;
    private Map<Integer, TextInputEditText> editTexts = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageReferralBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mRef = FirebaseDatabase.getInstance().getReference("level/refer");
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        generateLevelUI();
        fetchCurrentLevels();

        binding.btnSave.setOnClickListener(v -> saveLevels());
    }

    private void generateLevelUI() {
        float density = getResources().getDisplayMetrics().density;
        for (int i = 1; i <= 20; i++) {
            TextInputLayout til = new TextInputLayout(this, null, com.google.android.material.R.style.Widget_MaterialComponents_TextInputLayout_OutlinedBox);
            til.setHint("Level " + i + " Percentage (%)");
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, (int) (12 * density));
            til.setLayoutParams(lp);

            TextInputEditText et = new TextInputEditText(til.getContext());
            et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            til.addView(et);
            
            binding.llLevelsContainer.addView(til);
            editTexts.put(i, et);
        }
    }

    private void fetchCurrentLevels() {
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (int i = 1; i <= 20; i++) {
                    String key = "level" + i;
                    if (snapshot.hasChild(key)) {
                        Object val = snapshot.child(key).getValue();
                        if (val != null) editTexts.get(i).setText(String.valueOf(val));
                    } else {
                        // Default values if not set
                        editTexts.get(i).setText(getDefaultPercentage(i));
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private String getDefaultPercentage(int level) {
        if (level == 1) return "10.0";
        if (level == 2) return "7.0";
        if (level == 3) return "5.0";
        if (level == 4) return "3.0";
        if (level >= 5 && level <= 10) return "2.0";
        if (level >= 11 && level <= 20) return "1.0";
        return "0.0";
    }

    private void saveLevels() {
        Map<String, Object> updates = new HashMap<>();
        try {
            for (int i = 1; i <= 20; i++) {
                String val = editTexts.get(i).getText().toString().trim();
                if (val.isEmpty()) val = "0.0";
                updates.put("level" + i, Double.parseDouble(val));
            }
            mRef.updateChildren(updates).addOnSuccessListener(aVoid -> 
                Toast.makeText(this, "Referral Levels Updated", Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }
}
