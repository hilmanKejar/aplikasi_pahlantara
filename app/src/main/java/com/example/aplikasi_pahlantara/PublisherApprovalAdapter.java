package com.example.aplikasi_pahlantara;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PublisherApprovalAdapter extends RecyclerView.Adapter<PublisherApprovalAdapter.PublisherViewHolder> {

    private List<User> publisherList;
    private OnApproveClickListener listener;

    public interface OnApproveClickListener {
        void onApproveClick(User user);
    }

    public void setOnApproveClickListener(OnApproveClickListener listener) {
        this.listener = listener;
    }

    public PublisherApprovalAdapter(List<User> publisherList) {
        this.publisherList = publisherList;
    }

    public void setPublisherList(List<User> newPublisherList) {
        this.publisherList = newPublisherList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PublisherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_publisher_approval, parent, false);
        return new PublisherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PublisherViewHolder holder, int position) {
        User user = publisherList.get(position);
        holder.textViewPublisherUsername.setText(user.getUsername());

        if (user.isAccPenerbit()) {
            holder.textViewApprovalStatus.setText("Status: Disetujui");
            holder.textViewApprovalStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.green));
            holder.buttonApprove.setVisibility(View.GONE);
        } else {
            holder.textViewApprovalStatus.setText("Status: Belum Disetujui");
            holder.textViewApprovalStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.red));
            holder.buttonApprove.setVisibility(View.VISIBLE);
            holder.buttonApprove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onApproveClick(user);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return publisherList.size();
    }

    public static class PublisherViewHolder extends RecyclerView.ViewHolder {
        TextView textViewPublisherUsername;
        TextView textViewApprovalStatus;
        Button buttonApprove;

        public PublisherViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPublisherUsername = itemView.findViewById(R.id.textViewPublisherUsername);
            textViewApprovalStatus = itemView.findViewById(R.id.textViewApprovalStatus);
            buttonApprove = itemView.findViewById(R.id.buttonApprove);
        }
    }
}