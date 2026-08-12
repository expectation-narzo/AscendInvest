package com.ascend.invest.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ascend.invest.admin.databinding.ActivityManageFaqBinding;
import com.ascend.invest.admin.databinding.ItemFaqAdminBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ManageFaqActivity extends AppCompatActivity {
    private ActivityManageFaqBinding binding;
    private DatabaseReference mFaqRef;
    private List<FAQ> faqList = new ArrayList<>();
    private FaqAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        binding = ActivityManageFaqBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mFaqRef = FirebaseDatabase.getInstance().getReference("faq");
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new FaqAdapter();
        binding.rvFaq.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFaq.setAdapter(adapter);

        binding.fabAddFaq.setOnClickListener(v -> addNewFaq());

        fetchFaqs();
    }

    private void fetchFaqs() {
        mFaqRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                faqList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    FAQ faq = ds.getValue(FAQ.class);
                    if (faq != null) {
                        faq.setId(ds.getKey());
                        faqList.add(faq);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addNewFaq() {
        mFaqRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int maxId = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    try {
                        int id = Integer.parseInt(ds.getKey());
                        if (id > maxId) maxId = id;
                    } catch (Exception ignored) {}
                }
                String nextId = String.valueOf(maxId + 1);
                FAQ faq = new FAQ(nextId, "New Question", "New Answer");
                mFaqRef.child(nextId).setValue(faq);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(ItemFaqAdminBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FAQ faq = faqList.get(position);
            holder.binding.etQuestion.setText(faq.getTitle());
            holder.binding.etAnswer.setText(faq.getDescription());

            holder.binding.btnUpdate.setOnClickListener(v -> {
                String q = holder.binding.etQuestion.getText().toString().trim();
                String a = holder.binding.etAnswer.getText().toString().trim();
                mFaqRef.child(faq.getId()).child("title").setValue(q);
                mFaqRef.child(faq.getId()).child("description").setValue(a);
                Toast.makeText(ManageFaqActivity.this, "FAQ Updated", Toast.LENGTH_SHORT).show();
            });

            holder.binding.btnDelete.setOnClickListener(v -> {
                mFaqRef.child(faq.getId()).removeValue();
                Toast.makeText(ManageFaqActivity.this, "FAQ Deleted", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return faqList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ItemFaqAdminBinding binding;
            ViewHolder(ItemFaqAdminBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
