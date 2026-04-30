package com.soen345.ticketReservation.ui.eventdetail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketReservation.R;
import com.soen345.ticketReservation.model.Event;
import com.soen345.ticketReservation.model.Reservation;
import com.soen345.ticketReservation.service.EmailSmsConfirmationService;
import com.soen345.ticketReservation.service.FirebaseRepository;
import com.soen345.ticketReservation.ui.booking.BookingConfirmationActivity;
import com.soen345.ticketReservation.util.SessionManager;

/** Event Detail screen. Books a ticket and generates simulated email/SMS confirmations. */
public class EventDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvCategory, tvLocation, tvDate,
                     tvDescription, tvPrice, tvSeats, tvStatus;
    private Button btnBook;
    private ProgressBar progressBar;

    private FirebaseRepository repository;
    private EmailSmsConfirmationService confirmationService;
    private Event currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Event Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository = new FirebaseRepository();
        confirmationService = new EmailSmsConfirmationService(repository);

        tvTitle = findViewById(R.id.tvDetailTitle);
        tvCategory = findViewById(R.id.tvDetailCategory);
        tvLocation = findViewById(R.id.tvDetailLocation);
        tvDate = findViewById(R.id.tvDetailDate);
        tvDescription = findViewById(R.id.tvDetailDescription);
        tvPrice = findViewById(R.id.tvDetailPrice);
        tvSeats = findViewById(R.id.tvDetailSeats);
        tvStatus = findViewById(R.id.tvDetailStatus);
        btnBook = findViewById(R.id.btnBook);
        progressBar = findViewById(R.id.progressBar);

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
        tvLocation.setText("Location: " + currentEvent.getLocation());
        tvDate.setText("Date: " + (currentEvent.getEventDate() != null
                ? currentEvent.getEventDate().replace("T", " at ") : ""));
        tvDescription.setText(currentEvent.getDescription() != null
                ? currentEvent.getDescription() : "No description available.");
        tvPrice.setText("Price: " + currentEvent.getFormattedPrice());
        tvSeats.setText("Available seats: " + currentEvent.getAvailableSeats());
        tvStatus.setText("Status: " + currentEvent.getStatus());

        if (!currentEvent.hasAvailableSeats()) {
            btnBook.setEnabled(false);
            btnBook.setText("Unavailable");
        }
    }

    private void bookTicket() {
        String userId = SessionManager.getUserId(this);

        if (userId == null) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnBook.setEnabled(false);

        repository.bookTicket(userId, currentEvent, new FirebaseRepository.ReservationCallback() {
            @Override
            public void onSuccess(Reservation reservation) {
                createConfirmationsThenOpenScreen(reservation);
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

    private void createConfirmationsThenOpenScreen(Reservation reservation) {
        String userId = SessionManager.getUserId(this);
        String email = SessionManager.getEmail(this);
        String phoneNumber = SessionManager.getPhoneNumber(this);

        confirmationService.createBookingConfirmations(userId, email, phoneNumber, reservation, currentEvent, result -> {
            progressBar.setVisibility(View.GONE);

            Toast.makeText(EventDetailActivity.this, result.statusMessage, Toast.LENGTH_LONG).show();

            Intent intent = new Intent(EventDetailActivity.this, BookingConfirmationActivity.class);
            intent.putExtra("reservationId", reservation.getReservationId());
            intent.putExtra("ticketId", reservation.getTicketId());
            intent.putExtra("eventTitle", currentEvent.getTitle());
            intent.putExtra("eventLocation", currentEvent.getLocation());
            intent.putExtra("eventDate", currentEvent.getEventDate());
            intent.putExtra("totalAmount", reservation.getTotalAmount());
            intent.putExtra("reservationDate", reservation.getReservationDate());
            intent.putExtra("reservationStatus", reservation.getStatus());
            intent.putExtra("confirmationStatus", result.statusMessage);
            intent.putExtra("emailRecipient", result.emailRecipient);
            intent.putExtra("smsRecipient", result.smsRecipient);
            intent.putExtra("confirmationMessage", result.confirmationMessage);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
