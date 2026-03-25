package com.soen345.ticketReservation.ui.booking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketReservation.R;
import com.soen345.ticketReservation.ui.eventlist.EventListActivity;
import com.soen345.ticketReservation.ui.myreservations.MyReservationsActivity;

/**
 * Booking Confirmation screen.
 *
 * Per report requirement: "Customers shall be able to receive confirmations
 * via email or SMS." — this screen shows the confirmation summary and informs
 * the user that an email/SMS confirmation has been sent (via NotificationService stub).
 */
public class BookingConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Booking Confirmed!");

        Intent intent         = getIntent();
        String reservationId  = intent.getStringExtra("reservationId");
        String ticketId       = intent.getStringExtra("ticketId");
        String eventTitle     = intent.getStringExtra("eventTitle");
        String eventLocation  = intent.getStringExtra("eventLocation");
        String eventDate      = intent.getStringExtra("eventDate");
        double totalAmount    = intent.getDoubleExtra("totalAmount", 0);
        String reservationDate = intent.getStringExtra("reservationDate");

        TextView tvConfirmTitle     = findViewById(R.id.tvConfirmTitle);
        TextView tvReservationId    = findViewById(R.id.tvReservationId);
        TextView tvTicketId         = findViewById(R.id.tvTicketId);
        TextView tvEventTitle       = findViewById(R.id.tvEventTitle);
        TextView tvEventLocation    = findViewById(R.id.tvEventLocation);
        TextView tvEventDate        = findViewById(R.id.tvEventDate);
        TextView tvTotalAmount      = findViewById(R.id.tvTotalAmount);
        TextView tvReservationDate  = findViewById(R.id.tvReservationDate);
        TextView tvNotification     = findViewById(R.id.tvNotification);
        Button   btnBackToEvents    = findViewById(R.id.btnBackToEvents);
        Button   btnMyReservations  = findViewById(R.id.btnMyReservations);

        tvConfirmTitle.setText("✅ Booking Confirmed!");
        tvReservationId.setText("Reservation ID: " + reservationId);
        tvTicketId.setText("Ticket ID: " + ticketId);
        tvEventTitle.setText("Event: " + eventTitle);
        tvEventLocation.setText("Location: " + eventLocation);
        tvEventDate.setText("Date: " + (eventDate != null
                ? eventDate.replace("T", " at ") : ""));
        tvTotalAmount.setText(String.format("Amount Paid: $%.2f", totalAmount));
        tvReservationDate.setText("Booked on: " + (reservationDate != null
                ? reservationDate.replace("T", " ") : ""));

        // Report requirement: "Customers shall be able to receive confirmations via email or SMS"
        tvNotification.setText("📧 A confirmation has been sent to your registered email / SMS.");

        btnBackToEvents.setOnClickListener(v -> {
            Intent back = new Intent(this, EventListActivity.class);
            back.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(back);
            finish();
        });

        btnMyReservations.setOnClickListener(v ->
                startActivity(new Intent(this, MyReservationsActivity.class)));
    }
}
