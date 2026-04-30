package com.soen345.ticketReservation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.soen345.ticketReservation.R;
import com.soen345.ticketReservation.model.Event;

import java.util.List;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.AdminEventViewHolder> {

    public interface OnEditClickListener {
        void onEdit(Event event);
    }

    public interface OnCancelClickListener {
        void onCancel(Event event);
    }

    private List<Event> events;
    private final OnEditClickListener editListener;
    private final OnCancelClickListener cancelListener;

    public AdminEventAdapter(List<Event> events, OnEditClickListener editListener, OnCancelClickListener cancelListener) {
        this.events = events;
        this.editListener = editListener;
        this.cancelListener = cancelListener;
    }

    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_event, parent, false);
        return new AdminEventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminEventViewHolder holder, int position) {
        holder.bind(events.get(position), editListener, cancelListener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class AdminEventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvEventId, tvCategoryStatus, tvLocationDate, tvSeatsPrice;
        Button btnEdit, btnCancel;

        AdminEventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAdminEventTitle);
            tvEventId = itemView.findViewById(R.id.tvAdminEventId);
            tvCategoryStatus = itemView.findViewById(R.id.tvAdminCategoryStatus);
            tvLocationDate = itemView.findViewById(R.id.tvAdminLocationDate);
            tvSeatsPrice = itemView.findViewById(R.id.tvAdminSeatsPrice);
            btnEdit = itemView.findViewById(R.id.btnEditEvent);
            btnCancel = itemView.findViewById(R.id.btnCancelEvent);
        }

        void bind(Event event, OnEditClickListener editListener, OnCancelClickListener cancelListener) {
            tvTitle.setText(event.getTitle());
            tvEventId.setText("ID: " + event.getEventId());
            tvCategoryStatus.setText("Category: " + safe(event.getCategory()) + " | Status: " + safe(event.getStatus()));
            tvLocationDate.setText("Location: " + safe(event.getLocation()) + "\nDate: " + safe(event.getEventDate()).replace("T", " "));
            tvSeatsPrice.setText("Seats: " + event.getAvailableSeats() + "/" + event.getTotalSeats()
                    + " | Price: " + event.getFormattedPrice());

            btnEdit.setOnClickListener(v -> editListener.onEdit(event));

            boolean isCancelled = "CANCELLED".equals(event.getStatus());
            btnCancel.setEnabled(!isCancelled);
            btnCancel.setText(isCancelled ? "Already Cancelled" : "Cancel Event");
            btnCancel.setOnClickListener(isCancelled ? null : v -> cancelListener.onCancel(event));
        }

        private String safe(String value) {
            return value == null ? "" : value;
        }
    }
}
