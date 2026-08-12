package com.ascend.invest.admin;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.ascend.invest.admin.databinding.ActivityManageLinksBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ManageLinksActivity extends AppCompatActivity {
    private ActivityManageLinksBinding binding;
    private DatabaseReference mUrlRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        binding = ActivityManageLinksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mUrlRef = FirebaseDatabase.getInstance().getReference("url");
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        fetchLinks();

        binding.btnSave.setOnClickListener(v -> {
            String telegram = binding.etTelegram.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String commissionStr = binding.etP2pCommission.getText().toString().trim();
            String appSign = binding.etAppSign.getText().toString().trim();
            
            mUrlRef.child("telegram").child("link").setValue(telegram);
            mUrlRef.child("email").child("address").setValue(email);

            if (!commissionStr.isEmpty()) {
                try {
                    double commission = Double.parseDouble(commissionStr);
                    FirebaseDatabase.getInstance().getReference("commission/p2p").setValue(commission);
                } catch (Exception ignored) {}
            }

            if (!appSign.isEmpty()) {
                FirebaseDatabase.getInstance().getReference("key/appsign").setValue(appSign);
            }
            
            Toast.makeText(this, "Master Configuration Updated Successfully", Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchLinks() {
        mUrlRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("telegram/link").exists()) {
                    binding.etTelegram.setText(snapshot.child("telegram/link").getValue(String.class));
                }
                if (snapshot.child("email/address").exists()) {
                    binding.etEmail.setText(snapshot.child("email/address").getValue(String.class));
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        FirebaseDatabase.getInstance().getReference("commission/p2p").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.etP2pCommission.setText(String.valueOf(snapshot.getValue()));
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        FirebaseDatabase.getInstance().getReference("key/appsign").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.etAppSign.setText(snapshot.getValue(String.class));
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
