package com.soen345.ticketReservation.ui.eventdetail;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketReservation.R;
import com.soen345.ticketReservation.model.Event;
import com.soen345.ticketReservation.service.FirebaseRepository;
import com.soen345.ticketReservation.ui.booking.BookingConfirmationActivity;

/**
 * Event Detail screen.
 * Shows full event info and a "Book Now" button.
 * On book → calls FirebaseRepository.bookTicket() → goes to BookingConfirmation.
 */
public class EventDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvCategory, tvLocation, tvDate,
                     tvDescription, tvPrice, tvSeats, tvStatus;
    private Button    btnBook;
    private ProgressBar progressBar;

    private FirebaseRepository repository;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Event Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository  = new FirebaseRepository();

        tvTitle       = findViewById(R.id.tvDetailTitle);
        tvCategory    = findViewById(R.id.tvDetailCategory);
        tvLocation    = findViewById(R.id.tvDetailLocation);
        tvDate        = findViewById(R.id.tvDetailDate);
        tvDescription = findViewById(R.id.tvDetailDescription);
        tvPrice       = findViewById(R.id.tvDetailPrice);
        tvSeats       = findViewById(R.id.tvDetailSeats);
        tvStatus      = findViewById(R.id.tvDetailStatus);
        btnBook       = findViewById(R.id.btnBook);
        progressBar   = findViewById(R.id.progressBar);

        // Rebuild Event from intent extras (avoids Parcelable for simplicity)
        Intent intent = getIntent();
        currentEvent = new Event();
        currentEvent.setEventId(intent.getStringExtra("eventId"));
        currentEvent.setTitle(intent.getStringExtra("eventTitle"));
        currentEvent.setDescription(intent.getStringExtra("eventDescription"));
        currentEvent.setLocation(intent.getStringExtra("eventLocation"));
        currentEvent.setEventDate(intent.getStringExtra("eventDate"));
        currentEvent.setCategory(intent.getStringExtra("eventCategory"));
        currentEvent.setPrice(intent.getDoubleExtra("eventPrice", 0));
        currentEvent.setAvailableSeats(intent.getIntExtra("availableSeats", 0));
        currentEvent.setStatus(intent.getStringExtra("eventStatus"));

        displayEvent();

        btnBook.setOnClickListener(v -> bookTicket());
    }

    private void displayEvent() {
        tvTitle.setText(currentEvent.getTitle());
        tvCategory.setText("Category: " + currentEvent.getCategory());
        tvLocation.setText("📍 " + currentEvent.getLocation());
        tvDate.setText("🗓 " + (currentEvent.getEventDate() != null
                ? currentEvent.getEventDate().replace("T", " at ") : ""));
        tvDescription.setText(currentEvent.getDescription() != null
                ? currentEvent.getDescription() : "No description available.");
        tvPrice.setText("Price: " + currentEvent.getFormattedPrice());
        tvSeats.setText("Available seats: " + currentEvent.getAvailableSeats());
        tvStatus.setText("Status: " + currentEvent.getStatus());

        if (!currentEvent.hasAvailableSeats()) {
            btnBook.setEnabled(false);
            btnBook.setText("Sold Out");
        }
    }

    private void bookTicket() {
        SharedPreferences prefs = getSharedPreferences("TicketAppPrefs", MODE_PRIVATE);
        String userId = prefs.getString("userId", null);

        if (userId == null) {
            Toast.makeText(this, "Please register first.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnBook.setEnabled(false);

        repository.bookTicket(userId, currentEvent, new FirebaseRepository.ReservationCallback() {
            @Override
            public void onSuccess(com.soen345.ticketReservation.model.Reservation reservation) {
                progressBar.setVisibility(View.GONE);

                Intent intent = new Intent(EventDetailActivity.this,
                        BookingConfirmationActivity.class);
                intent.putExtra("reservationId",   reservation.getReservationId());
                intent.putExtra("ticketId",         reservation.getTicketId());
                intent.putExtra("eventTitle",       currentEvent.getTitle());
                intent.putExtra("eventLocation",    currentEvent.getLocation());
                intent.putExtra("eventDate",        currentEvent.getEventDate());
                intent.putExtra("totalAmount",      reservation.getTotalAmount());
                intent.putExtra("reservationDate",  reservation.getReservationDate());
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                btnBook.setEnabled(true);
                Toast.makeText(EventDetailActivity.this,
                        "Booking failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
