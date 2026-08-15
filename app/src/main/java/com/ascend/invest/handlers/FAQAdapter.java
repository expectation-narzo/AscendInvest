package com.ascend.invest.handlers;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ascend.invest.databinding.ItemFaqBinding;
import java.util.List;

public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.ViewHolder> {

    private List<FAQ> faqList;

    public FAQAdapter(List<FAQ> faqList) {
        this.faqList = faqList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFaqBinding binding = ItemFaqBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FAQ faq = faqList.get(position);
        holder.binding.tvFaqQuestion.setText(faq.getTitle());
        holder.binding.tvFaqAnswer.setText(faq.getDescription());
    }

    @Override
    public int getItemCount() {
        return faqList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemFaqBinding binding;

        public ViewHolder(@NonNull ItemFaqBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
