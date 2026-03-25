package com.soen345.ticketReservation.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.soen345.ticketReservation.R;
import com.soen345.ticketReservation.model.Event;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnEventClickListener {
        void onClick(Event event);
    }

    private List<Event> events;
    private final OnEventClickListener listener;

    public EventAdapter(List<Event> events, OnEventClickListener listener) {
        this.events   = events;
        this.listener = listener;
    }

    public void updateEvents(List<Event> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() { return events.size(); }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvLocation, tvDate, tvPrice, tvSeats;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle    = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate     = itemView.findViewById(R.id.tvDate);
            tvPrice    = itemView.findViewById(R.id.tvPrice);
            tvSeats    = itemView.findViewById(R.id.tvSeats);
        }

        void bind(Event event, OnEventClickListener listener) {
            tvTitle.setText(event.getTitle());
            tvCategory.setText(event.getCategory() != null ? event.getCategory() : "");
            tvLocation.setText(event.getLocation() != null ? event.getLocation() : "");
            tvDate.setText(event.getEventDate() != null
                    ? event.getEventDate().replace("T", " ") : "");
            tvPrice.setText(event.getFormattedPrice());
            tvSeats.setText(event.getAvailableSeats() + " seats left");

            itemView.setOnClickListener(v -> listener.onClick(event));
        }
    }
}
