package com.soen345.ticketReservation.ui.booking;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketReservation.R;
import com.soen345.ticketReservation.ui.eventlist.EventListActivity;
import com.soen345.ticketReservation.ui.myreservations.MyReservationsActivity;

/** Shows booking details and the simulated email/SMS confirmation information. */
public class BookingConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Booking Confirmed!");

        Intent intent = getIntent();
        String reservationId = intent.getStringExtra("reservationId");
        String ticketId = intent.getStringExtra("ticketId");
        String eventTitle = intent.getStringExtra("eventTitle");
        String eventLocation = intent.getStringExtra("eventLocation");
        String eventDate = intent.getStringExtra("eventDate");
        double totalAmount = intent.getDoubleExtra("totalAmount", 0);
        String reservationDate = intent.getStringExtra("reservationDate");
        String reservationStatus = intent.getStringExtra("reservationStatus");
        String confirmationStatus = intent.getStringExtra("confirmationStatus");
        String emailRecipient = intent.getStringExtra("emailRecipient");
        String smsRecipient = intent.getStringExtra("smsRecipient");
        String confirmationMessage = intent.getStringExtra("confirmationMessage");

        TextView tvConfirmTitle = findViewById(R.id.tvConfirmTitle);
        TextView tvReservationId = findViewById(R.id.tvReservationId);
        TextView tvTicketId = findViewById(R.id.tvTicketId);
        TextView tvEventTitle = findViewById(R.id.tvEventTitle);
        TextView tvEventLocation = findViewById(R.id.tvEventLocation);
        TextView tvEventDate = findViewById(R.id.tvEventDate);
        TextView tvTotalAmount = findViewById(R.id.tvTotalAmount);
        TextView tvReservationDate = findViewById(R.id.tvReservationDate);
        TextView tvReservationStatus = findViewById(R.id.tvReservationStatus);
        TextView tvNotification = findViewById(R.id.tvNotification);
        TextView tvConfirmationStatus = findViewById(R.id.tvConfirmationStatus);
        TextView tvEmailRecipient = findViewById(R.id.tvEmailRecipient);
        TextView tvSmsRecipient = findViewById(R.id.tvSmsRecipient);
        TextView tvConfirmationMessage = findViewById(R.id.tvConfirmationMessage);
        Button btnBackToEvents = findViewById(R.id.btnBackToEvents);
        Button btnMyReservations = findViewById(R.id.btnMyReservations);

        tvConfirmTitle.setText("Booking Confirmed!");
        tvReservationId.setText("Reservation ID: " + safe(reservationId));
        tvTicketId.setText("Ticket ID: " + safe(ticketId));
        tvEventTitle.setText("Event: " + safe(eventTitle));
        tvEventLocation.setText("Location: " + safe(eventLocation));
        tvEventDate.setText("Date: " + (eventDate != null ? eventDate.replace("T", " at ") : ""));
        tvTotalAmount.setText(String.format("Amount Paid: $%.2f", totalAmount));
        tvReservationDate.setText("Booked on: " + (reservationDate != null ? reservationDate.replace("T", " ") : ""));
        tvReservationStatus.setText("Reservation Status: " + safe(reservationStatus));

        tvNotification.setText("Email and SMS confirmation records were generated in Firebase when recipients were available.");
        tvConfirmationStatus.setText("Confirmation Status: " + safe(confirmationStatus));
        tvEmailRecipient.setText("Email Recipient: " + safe(emailRecipient));
        tvSmsRecipient.setText("SMS Recipient: " + safe(smsRecipient));
        tvConfirmationMessage.setText(safe(confirmationMessage));

        btnBackToEvents.setOnClickListener(v -> {
            Intent back = new Intent(this, EventListActivity.class);
            back.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(back);
            finish();
        });

        btnMyReservations.setOnClickListener(v ->
                startActivity(new Intent(this, MyReservationsActivity.class)));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
