package com.soen345.ticketReservation.ui.myreservations;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.soen345.ticketReservation.R;
import com.soen345.ticketReservation.adapter.ReservationAdapter;
import com.soen345.ticketReservation.model.Reservation;
import com.soen345.ticketReservation.service.FirebaseRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * My Reservations screen.
 *
 * Fulfils functional requirement:
 *   "Customers shall be able to cancel reservations."
 *
 * Shows all reservations for the logged-in user.
 * Each card has a "Cancel Reservation" button.
 * Cancellation mirrors ReservationService.cancelReservation():
 *   sets status → CANCELLED and increments availableSeats.
 */
public class MyReservationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReservationAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private FirebaseRepository repository;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservations);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Reservations");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        repository  = new FirebaseRepository();
        recyclerView = findViewById(R.id.recyclerReservations);
        progressBar  = findViewById(R.id.progressBar);
        tvEmpty      = findViewById(R.id.tvEmpty);

        SharedPreferences prefs = getSharedPreferences("TicketAppPrefs", MODE_PRIVATE);
        userId = prefs.getString("userId", null);

        adapter = new ReservationAdapter(new ArrayList<>(), this::confirmCancel);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadReservations();
    }

    private void loadReservations() {
        if (userId == null) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("No account found. Please register first.");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        repository.getReservationsForUser(userId, new FirebaseRepository.ReservationsCallback() {
            @Override
            public void onSuccess(List<Reservation> reservations) {
                progressBar.setVisibility(View.GONE);
                adapter.updateReservations(reservations);
                tvEmpty.setVisibility(reservations.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MyReservationsActivity.this,
                        "Error loading reservations: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Shows a confirmation dialog before cancelling */
    private void confirmCancel(Reservation reservation) {
        new AlertDialog.Builder(this)
            .setTitle("Cancel Reservation")
            .setMessage("Are you sure you want to cancel reservation "
                    + reservation.getReservationId() + "?\nThis cannot be undone.")
            .setPositiveButton("Yes, Cancel", (dialog, which) -> cancelReservation(reservation))
            .setNegativeButton("Keep it", null)
            .show();
    }

    private void cancelReservation(Reservation reservation) {
        progressBar.setVisibility(View.VISIBLE);
        repository.cancelReservation(reservation, new FirebaseRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MyReservationsActivity.this,
                        "Reservation cancelled successfully.", Toast.LENGTH_SHORT).show();
                loadReservations(); // Refresh
            }
            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MyReservationsActivity.this,
                        "Cancellation failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
