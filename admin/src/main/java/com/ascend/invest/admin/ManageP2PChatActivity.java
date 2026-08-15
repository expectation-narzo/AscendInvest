package com.ascend.invest.admin;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ascend.invest.admin.databinding.ActivityP2pChatAdminBinding;
import com.ascend.invest.admin.databinding.ItemChatReceiverBinding;
import com.ascend.invest.admin.databinding.ItemChatSenderBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManageP2PChatActivity extends AppCompatActivity {
    private ActivityP2pChatAdminBinding binding;
    private String orderId;
    private DatabaseReference mChatRef;
    private List<ChatMessage> messageList = new ArrayList<>();
    private ChatAdapter adapter;
    private P2POrder currentOrder;
    private String replyingToMsg = null;
    private String replyingToUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityP2pChatAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) {
            finish();
            return;
        }
        mChatRef = FirebaseDatabase.getInstance().getReference().child("p2p_dispute_chats").child(orderId);
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnCancelReply.setOnClickListener(v -> cancelReply());
        
        adapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvChatMessages.setLayoutManager(layoutManager);
        binding.rvChatMessages.setAdapter(adapter);
        
        binding.btnSendMessage.setOnClickListener(v -> sendMessage());
        fetchOrderDetails();
    }

    private void cancelReply(){
        replyingToMsg = null;
        replyingToUser = null;
        binding.llReplyInputContainer.setVisibility(View.GONE);
    }

    private void initiateReply(ChatMessage msg) {
        replyingToMsg = msg.message;
        if (msg.isAdmin) replyingToUser = "Support (You)";
        else if (currentOrder != null) {
            if (msg.senderId.equals(currentOrder.buyerUid)) replyingToUser = currentOrder.buyerName + " (Buyer)";
            else replyingToUser = currentOrder.sellerName + " (Seller)";
        } else replyingToUser = "User";
        binding.tvReplyInputUser.setText("Replying to " + replyingToUser);
        binding.tvReplyInputMsg.setText(replyingToMsg);
        binding.llReplyInputContainer.setVisibility(View.VISIBLE);
        binding.messageInput.requestFocus();
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(binding.messageInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
    }

    private void fetchOrderDetails() {
        FirebaseDatabase.getInstance().getReference().child("p2p_order_history").child(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentOrder = snapshot.getValue(P2POrder.class);
                        if (currentOrder != null) {
                            binding.tvChatSubtitle.setText("Trade #" + orderId.substring(0, Math.min(orderId.length(), 8)));
                            binding.tvChatTitle.setText(currentOrder.buyerName + " vs " + currentOrder.sellerName);
                            listenForMessages();
                        } else {
                            Toast.makeText(ManageP2PChatActivity.this, "Order details not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void listenForMessages() {
        mChatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ChatMessage msg = ds.getValue(ChatMessage.class);
                    if (msg != null) messageList.add(msg);
                }
                adapter.notifyDataSetChanged();
                binding.rvChatMessages.scrollToPosition(Math.max(0, messageList.size() - 1));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendMessage() {
        String text = binding.messageInput.getText().toString().trim();
        if (text.isEmpty()) return;
        ChatMessage msg = new ChatMessage("ADMIN", text, true, replyingToMsg, replyingToUser);
        mChatRef.push().setValue(msg);
        binding.messageInput.setText("");
        cancelReply();
    }

    private int getTelegramNameColor(String senderId) {
        int[] colors = {
            Color.parseColor("#E53935"), Color.parseColor("#43A047"), 
            Color.parseColor("#FB8C00"), Color.parseColor("#1E88E5"), 
            Color.parseColor("#8E24AA"), Color.parseColor("#00ACC1"), 
            Color.parseColor("#D81B60"), Color.parseColor("#F4511E")
        };
        int hash = Math.abs(senderId.hashCode());
        return colors[hash % colors.length];
    }

    class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_SENDER = 1;
        private static final int VIEW_TYPE_RECEIVER = 2;
        @Override
        public int getItemViewType(int position) {
            return messageList.get(position).isAdmin ? VIEW_TYPE_SENDER : VIEW_TYPE_RECEIVER;
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_SENDER) {
                return new SenderViewHolder(ItemChatSenderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            } else {
                return new ReceiverViewHolder(ItemChatReceiverBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }
        }
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ChatMessage msg = messageList.get(position);
            String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date(msg.timestamp));
            if (holder instanceof SenderViewHolder) {
                SenderViewHolder vh = (SenderViewHolder) holder;
                vh.binding.tvSenderMsg.setText(msg.message);
                vh.binding.tvSenderTime.setText(time + " (You)");
                if (msg.replyToMsg != null) {
                    vh.binding.llReplySender.setVisibility(View.VISIBLE);
                    vh.binding.tvReplyUserSender.setText(msg.replyToUser);
                    vh.binding.tvReplyMsgSender.setText(msg.replyToMsg);
                } else vh.binding.llReplySender.setVisibility(View.GONE);
            } else {
                ReceiverViewHolder vh = (ReceiverViewHolder) holder;
                vh.binding.tvReceiverMsg.setText(msg.message);
                vh.binding.tvReceiverTime.setText(time);
                if (currentOrder != null) {
                    if (msg.senderId != null && msg.senderId.equals(currentOrder.buyerUid)) {
                        vh.binding.tvReceiverName.setText(currentOrder.buyerName + " (Buyer)");
                        vh.binding.tvReceiverName.setTextColor(getTelegramNameColor(msg.senderId));
                    } else if (msg.senderId != null && msg.senderId.equals(currentOrder.sellerUid)) {
                        vh.binding.tvReceiverName.setText(currentOrder.sellerName + " (Seller)");
                        vh.binding.tvReceiverName.setTextColor(getTelegramNameColor(msg.senderId));
                    } else {
                        vh.binding.tvReceiverName.setText("User");
                        vh.binding.tvReceiverName.setTextColor(getTelegramNameColor(msg.senderId != null ? msg.senderId : "0"));
                    }
                }
                if (msg.replyToMsg != null) {
                    vh.binding.llReplyReceiver.setVisibility(View.VISIBLE);
                    vh.binding.tvReplyUserReceiver.setText(msg.replyToUser);
                    vh.binding.tvReplyMsgReceiver.setText(msg.replyToMsg);
                } else vh.binding.llReplyReceiver.setVisibility(View.GONE);
            }
            holder.itemView.setOnLongClickListener(v -> {
                initiateReply(msg);
                return true;
            });
        }
        @Override public int getItemCount() { return messageList.size(); }
        class SenderViewHolder extends RecyclerView.ViewHolder {
            ItemChatSenderBinding binding;
            SenderViewHolder(ItemChatSenderBinding b) { super(b.getRoot()); this.binding = b; }
        }
        class ReceiverViewHolder extends RecyclerView.ViewHolder {
            ItemChatReceiverBinding binding;
            ReceiverViewHolder(ItemChatReceiverBinding b) { super(b.getRoot()); this.binding = b; }
        }
    }
}
