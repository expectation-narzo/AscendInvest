package com.ascend.invest.handlers;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ascend.invest.R;
import com.google.firebase.auth.FirebaseAuth;
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

public class P2PChatActivity extends AppCompatActivity {
    private String orderId;
    private String userId;
    private DatabaseReference mChatRef;
    private List<ChatMessage> messageList = new ArrayList<>();
    private ChatAdapter adapter;
    private RecyclerView rvMessages;
    private android.widget.EditText etInput;
    private P2POrder currentOrder;

    // Reply state
    private String replyingToMsg = null;
    private String replyingToUser = null;
    private View replyContainer;
    private TextView tvReplyUser, tvReplyMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p2p_chat);

        orderId = getIntent().getStringExtra("orderId");
        userId = FirebaseAuth.getInstance().getUid();

        if (orderId == null || userId == null) {
            finish();
            return;
        }

        mChatRef = FirebaseDatabase.getInstance().getReference().child("p2p_dispute_chats").child(orderId);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        rvMessages = findViewById(R.id.rv_chat_messages);
        etInput = findViewById(R.id.message_input);
        View btnSend = findViewById(R.id.btn_send_message);

        // Reply UI
        replyContainer = findViewById(R.id.ll_reply_input_container);
        tvReplyUser = findViewById(R.id.tv_reply_input_user);
        tvReplyMsg = findViewById(R.id.tv_reply_input_msg);
        findViewById(R.id.btn_cancel_reply).setOnClickListener(v -> cancelReply());

        adapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());

        fetchOrderDetails();
    }

    private void cancelReply() {
        replyingToMsg = null;
        replyingToUser = null;
        replyContainer.setVisibility(View.GONE);
    }

    private void initiateReply(ChatMessage msg) {
        replyingToMsg = msg.message;
        
        if (msg.isAdmin) replyingToUser = "System Support";
        else if (currentOrder != null) {
            if (msg.senderId.equals(currentOrder.buyerUid)) replyingToUser = currentOrder.buyerName + " (Buyer)";
            else replyingToUser = currentOrder.sellerName + " (Seller)";
        } else replyingToUser = "User";

        tvReplyUser.setText("Replying to " + replyingToUser);
        tvReplyMsg.setText(replyingToMsg);
        replyContainer.setVisibility(View.VISIBLE);
        etInput.requestFocus();

        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(etInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
    }

    private void fetchOrderDetails() {
        FirebaseDatabase.getInstance().getReference().child("p2p_order_history").child(orderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentOrder = snapshot.getValue(P2POrder.class);
                        if (currentOrder != null) {
                            TextView tvSubtitle = findViewById(R.id.tv_chat_subtitle);
                            tvSubtitle.setText("Trade #" + orderId.substring(0, Math.min(orderId.length(), 8)));

                            TextView tvTitle = findViewById(R.id.tv_chat_title);
                            boolean isBuyer = userId.equals(currentOrder.buyerUid);
                            String otherParty = isBuyer ? currentOrder.sellerName : currentOrder.buyerName;
                            tvTitle.setText(otherParty);

                            listenForMessages();
                        } else {
                            Toast.makeText(P2PChatActivity.this, "Order details not found", Toast.LENGTH_SHORT).show();
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
                rvMessages.scrollToPosition(Math.max(0, messageList.size() - 1));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty()) return;

        ChatMessage msg = new ChatMessage(userId, text, false, replyingToMsg, replyingToUser);
        mChatRef.push().setValue(msg);
        etInput.setText("");
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
            return messageList.get(position).senderId.equals(userId) ? VIEW_TYPE_SENDER : VIEW_TYPE_RECEIVER;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_SENDER) {
                return new SenderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_sender, parent, false));
            } else {
                return new ReceiverViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_receiver, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ChatMessage msg = messageList.get(position);
            String time = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(new Date(msg.timestamp));

            if (holder instanceof SenderViewHolder) {
                SenderViewHolder vh = (SenderViewHolder) holder;
                vh.tvMsg.setText(msg.message);
                vh.tvTime.setText(time);
                if (msg.replyToMsg != null) {
                    vh.llReply.setVisibility(View.VISIBLE);
                    vh.tvReplyUser.setText(msg.replyToUser);
                    vh.tvReplyMsg.setText(msg.replyToMsg);
                } else vh.llReply.setVisibility(View.GONE);
            } else {
                ReceiverViewHolder vh = (ReceiverViewHolder) holder;
                vh.tvMsg.setText(msg.message);
                vh.tvTime.setText(time);

                if (msg.isAdmin) {
                    vh.tvName.setText("System Support");
                    vh.tvName.setTextColor(Color.parseColor("#3390EC"));
                } else if (currentOrder != null) {
                    if (msg.senderId.equals(currentOrder.buyerUid)) {
                        vh.tvName.setText(currentOrder.buyerName + " (Buyer)");
                        vh.tvName.setTextColor(getTelegramNameColor(msg.senderId));
                    } else {
                        vh.tvName.setText(currentOrder.sellerName + " (Seller)");
                        vh.tvName.setTextColor(getTelegramNameColor(msg.senderId));
                    }
                }

                if (msg.replyToMsg != null) {
                    vh.llReply.setVisibility(View.VISIBLE);
                    vh.tvReplyUser.setText(msg.replyToUser);
                    vh.tvReplyMsg.setText(msg.replyToMsg);
                } else vh.llReply.setVisibility(View.GONE);
            }

            holder.itemView.setOnLongClickListener(v -> {
                initiateReply(msg);
                return true;
            });
        }

        @Override public int getItemCount() { return messageList.size(); }

        class SenderViewHolder extends RecyclerView.ViewHolder {
            TextView tvMsg, tvTime, tvReplyUser, tvReplyMsg;
            View llReply;
            SenderViewHolder(View v) {
                super(v);
                tvMsg = v.findViewById(R.id.tv_sender_msg);
                tvTime = v.findViewById(R.id.tv_sender_time);
                llReply = v.findViewById(R.id.ll_reply_sender);
                tvReplyUser = v.findViewById(R.id.tv_reply_user_sender);
                tvReplyMsg = v.findViewById(R.id.tv_reply_msg_sender);
            }
        }

        class ReceiverViewHolder extends RecyclerView.ViewHolder {
            TextView tvMsg, tvTime, tvName, tvReplyUser, tvReplyMsg;
            View llReply;
            ReceiverViewHolder(View v) {
                super(v);
                tvMsg = v.findViewById(R.id.tv_receiver_msg);
                tvTime = v.findViewById(R.id.tv_receiver_time);
                tvName = v.findViewById(R.id.tv_receiver_name);
                llReply = v.findViewById(R.id.ll_reply_receiver);
                tvReplyUser = v.findViewById(R.id.tv_reply_user_receiver);
                tvReplyMsg = v.findViewById(R.id.tv_reply_msg_receiver);
            }
        }
    }
}
