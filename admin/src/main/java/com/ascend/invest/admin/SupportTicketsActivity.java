package com.ascend.invest.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ascend.invest.admin.databinding.ActivityManageRequestsBinding;
import com.ascend.invest.admin.databinding.ItemSupportTicketAdminBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class SupportTicketsActivity extends AppCompatActivity {
    private ActivityManageRequestsBinding binding;
    private DatabaseReference mDatabase;
    private List<DataSnapshot> ticketList = new ArrayList<>();
    private TicketAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        binding = ActivityManageRequestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();
        binding.tvTitle.setText("Inquiry Queue");
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new TicketAdapter();
        binding.rvRequests.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRequests.setAdapter(adapter);

        fetchTickets();
    }

    private void fetchTickets() {
        mDatabase.child("support_tickets").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ticketList.clear();
                for (DataSnapshot userTickets : snapshot.getChildren()) {
                    for (DataSnapshot ticket : userTickets.getChildren()) {
                        if (TextUtils.isEmpty(ticket.child("adminReply").getValue(String.class))) {
                            ticketList.add(ticket);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                binding.tvEmpty.setVisibility(ticketList.isEmpty() ? View.VISIBLE : View.GONE);
                binding.tvEmpty.setText("No active inquiries");
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(ItemSupportTicketAdminBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DataSnapshot ds = ticketList.get(position);
            String title = ds.child("title").getValue(String.class);
            String desc = ds.child("description").getValue(String.class);
            String userId = ds.getRef().getParent().getKey();
            String ticketId = ds.getKey();

            holder.binding.tvTicketTitle.setText(title);
            holder.binding.tvTicketDesc.setText(desc);

            holder.binding.btnSendReply.setOnClickListener(v -> {
                Editable replyEditable = holder.binding.etReply.getText();
                String reply = (replyEditable != null) ? replyEditable.toString().trim() : "";
                if (!TextUtils.isEmpty(reply)) {
                    if (userId != null && ticketId != null) {
                        mDatabase.child("support_tickets").child(userId).child(ticketId).child("adminReply").setValue(reply);
                        mDatabase.child("support_tickets").child(userId).child(ticketId).child("status").setValue("Resolved");
                        Toast.makeText(SupportTicketsActivity.this, "Reply Transmitted", Toast.LENGTH_SHORT).show();
                        holder.binding.etReply.setText("");
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return ticketList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ItemSupportTicketAdminBinding binding;
            ViewHolder(ItemSupportTicketAdminBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
