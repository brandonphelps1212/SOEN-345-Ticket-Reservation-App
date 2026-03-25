package com.soen345.ticketReservation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.soen345.ticketReservation.R;
import com.soen345.ticketReservation.model.Reservation;

import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    public interface OnCancelClickListener {
        void onCancel(Reservation reservation);
    }

    private List<Reservation> reservations;
    private final OnCancelClickListener listener;

    public ReservationAdapter(List<Reservation> reservations, OnCancelClickListener listener) {
        this.reservations = reservations;
        this.listener     = listener;
    }

    public void updateReservations(List<Reservation> newList) {
        this.reservations = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        holder.bind(reservations.get(position), listener);
    }

    @Override
    public int getItemCount() { return reservations.size(); }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView tvResId, tvEventId, tvTicketId, tvAmount, tvDate, tvStatus;
        Button   btnCancel;

        ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvResId    = itemView.findViewById(R.id.tvResId);
            tvEventId  = itemView.findViewById(R.id.tvResEventId);
            tvTicketId = itemView.findViewById(R.id.tvResTicketId);
            tvAmount   = itemView.findViewById(R.id.tvResAmount);
            tvDate     = itemView.findViewById(R.id.tvResDate);
            tvStatus   = itemView.findViewById(R.id.tvResStatus);
            btnCancel  = itemView.findViewById(R.id.btnCancelReservation);
        }

        void bind(Reservation r, OnCancelClickListener listener) {
            tvResId.setText("Reservation: " + r.getReservationId());
            tvEventId.setText("Event ID: " + r.getEventId());
            tvTicketId.setText("Ticket: " + r.getTicketId());
            tvAmount.setText(String.format("Amount: $%.2f", r.getTotalAmount()));
            tvDate.setText("Booked: " + (r.getReservationDate() != null
                    ? r.getReservationDate().replace("T", " ") : ""));
            tvStatus.setText("Status: " + r.getStatus());

            boolean isCancelled = "CANCELLED".equals(r.getStatus());
            btnCancel.setEnabled(!isCancelled);
            btnCancel.setText(isCancelled ? "Cancelled" : "Cancel Reservation");

            if (!isCancelled) {
                btnCancel.setOnClickListener(v -> listener.onCancel(r));
            }
        }
    }
}
