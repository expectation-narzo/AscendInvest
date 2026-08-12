package com.ascend.invest.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ascend.invest.admin.databinding.ActivityManageAnnouncementsBinding;
import com.ascend.invest.admin.databinding.DialogCreateAnnouncementBinding;
import com.ascend.invest.admin.databinding.ItemAnnouncementAdminBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManageAnnouncementsActivity extends AppCompatActivity {
    private ActivityManageAnnouncementsBinding binding;
    private DatabaseReference mRef;
    private List<Announcement> list = new ArrayList<>();
    private AnnouncementAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidx.activity.EdgeToEdge.enable(this);
        binding = ActivityManageAnnouncementsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mRef = FirebaseDatabase.getInstance().getReference("announcement");
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new AnnouncementAdapter();
        binding.rvAnnouncements.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAnnouncements.setAdapter(adapter);

        binding.fabAddBroadcast.setOnClickListener(v -> showDialog());

        fetch();
    }

    private void fetch() {
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Announcement a = ds.getValue(Announcement.class);
                    if (a != null) {
                        a.setId(ds.getKey());
                        list.add(a);
                    }
                }
                Collections.reverse(list);
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogCreateAnnouncementBinding dBinding = DialogCreateAnnouncementBinding.inflate(getLayoutInflater());
        builder.setView(dBinding.getRoot());
        AlertDialog dialog = builder.create();

        dBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());
        dBinding.btnSend.setOnClickListener(v -> {
            String title = dBinding.etTitle.getText().toString().trim();
            String msg = dBinding.etMessage.getText().toString().trim();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(msg)) return;

            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int max = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        try {
                            int id = Integer.parseInt(ds.getKey());
                            if (id > max) max = id;
                        } catch (Exception ignored) {}
                    }
                    String next = String.valueOf(max + 1);
                    Announcement a = new Announcement(next, title, msg, System.currentTimeMillis());
                    mRef.child(next).setValue(a).addOnSuccessListener(v1 -> {
                        Toast.makeText(ManageAnnouncementsActivity.this, "Broadcast Dispatched", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        });
        dialog.show();
    }

    class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new ViewHolder(ItemAnnouncementAdminBinding.inflate(LayoutInflater.from(p.getContext()), p, false));
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            Announcement a = list.get(pos);
            h.binding.tvAnnouncementTitle.setText(a.getTitle());
            h.binding.tvAnnouncementMessage.setText(a.getMessage());
            h.binding.btnDelete.setOnClickListener(v -> mRef.child(a.getId()).removeValue());
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ItemAnnouncementAdminBinding binding;
            ViewHolder(ItemAnnouncementAdminBinding b) { super(b.getRoot()); this.binding = b; }
        }
    }
}
