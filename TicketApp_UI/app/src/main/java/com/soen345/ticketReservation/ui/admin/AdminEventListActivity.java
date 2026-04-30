package com.soen345.ticketReservation.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.soen345.ticketReservation.R;
import com.soen345.ticketReservation.adapter.AdminEventAdapter;
import com.soen345.ticketReservation.model.Event;
import com.soen345.ticketReservation.service.FirebaseRepository;
import com.soen345.ticketReservation.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

/** Admin dashboard for viewing, adding, editing, and cancelling events. */
public class AdminEventListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminEventAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private Button btnAddEvent, btnLogout;
    private FirebaseRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_list);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Admin Events");

        repository = new FirebaseRepository();
        recyclerView = findViewById(R.id.recyclerAdminEvents);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnAddEvent = findViewById(R.id.btnAddEvent);
        btnLogout = findViewById(R.id.btnLogoutAdmin);

        adapter = new AdminEventAdapter(new ArrayList<>(), this::openEditEvent, this::confirmCancelEvent);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);

        btnAddEvent.setOnClickListener(v -> startActivity(new Intent(this, AddEditEventActivity.class)));
        btnLogout.setOnClickListener(v -> SessionManager.logoutAndOpenLogin(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    private void loadEvents() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        repository.getAllEventsForAdmin(new FirebaseRepository.EventsCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                progressBar.setVisibility(View.GONE);
                adapter.updateEvents(events);
                tvEmpty.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminEventListActivity.this,
                        "Error loading events: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openEditEvent(Event event) {
        Intent intent = new Intent(this, AddEditEventActivity.class);
        intent.putExtra("eventId", event.getEventId());
        startActivity(intent);
    }

    private void confirmCancelEvent(Event event) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Event")
                .setMessage("Cancel " + event.getTitle() + "?\nThe status will be changed to CANCELLED.")
                .setPositiveButton("Cancel Event", (dialog, which) -> cancelEvent(event))
                .setNegativeButton("Keep Event", null)
                .show();
    }

    private void cancelEvent(Event event) {
        progressBar.setVisibility(View.VISIBLE);
        repository.cancelEvent(event.getEventId(), new FirebaseRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminEventListActivity.this,
                        "Event cancelled successfully.", Toast.LENGTH_SHORT).show();
                loadEvents();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminEventListActivity.this,
                        "Could not cancel event: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
