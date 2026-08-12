package com.ascend.invest.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ascend.invest.admin.databinding.ActivityUserSearchBinding;
import com.ascend.invest.admin.databinding.ItemUserBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserSearchActivity extends AppCompatActivity {
    private ActivityUserSearchBinding binding;
    private DatabaseReference mUserRef;
    private List<DataSnapshot> userList = new ArrayList<>();
    private List<DataSnapshot> filteredList = new ArrayList<>();
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        binding = ActivityUserSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mUserRef = FirebaseDatabase.getInstance().getReference("users");
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new UserAdapter();
        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        binding.rvUsers.setAdapter(adapter);

        fetchUsers();

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterUsers(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void fetchUsers() {
        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    userList.add(ds);
                }
                filterUsers(binding.etSearch.getText().toString());
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void filterUsers(String query) {
        filteredList.clear();
        for (DataSnapshot ds : userList) {
            String name = ds.child("username").getValue(String.class);
            String email = ds.child("email").getValue(String.class);
            if (query.isEmpty() || 
                (name != null && name.toLowerCase().contains(query.toLowerCase())) ||
                (email != null && email.toLowerCase().contains(query.toLowerCase()))) {
                filteredList.add(ds);
            }
        }
        adapter.notifyDataSetChanged();
        binding.tvEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DataSnapshot ds = filteredList.get(position);
            holder.binding.tvUserName.setText(ds.child("username").getValue(String.class));
            holder.binding.tvUserEmail.setText(ds.child("email").getValue(String.class));
            
            Object balanceVal = ds.child("wallet_balance").getValue();
            double walletBalance = 0;
            if (balanceVal instanceof Number) walletBalance = ((Number) balanceVal).doubleValue();
            
            holder.binding.tvUserWallet.setText(String.format(Locale.US, "$%.2f", walletBalance));
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(UserSearchActivity.this, UserDetailActivity.class);
                intent.putExtra("uid", ds.getKey());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return filteredList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ItemUserBinding binding;
            ViewHolder(ItemUserBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
