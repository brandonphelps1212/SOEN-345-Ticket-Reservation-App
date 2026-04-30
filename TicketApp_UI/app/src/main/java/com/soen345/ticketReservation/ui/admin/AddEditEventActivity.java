package com.soen345.ticketReservation.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketReservation.R;
import com.soen345.ticketReservation.model.Event;
import com.soen345.ticketReservation.service.FirebaseRepository;

/** Form used by admins to add a new event or edit an existing event. */
public class AddEditEventActivity extends AppCompatActivity {

    private static final String[] CATEGORIES = {"MOVIE", "CONCERT", "SPORTS", "TRAVEL", "OTHER"};
    private static final String[] STATUSES = {"ACTIVE", "CANCELLED", "SOLD_OUT"};

    private EditText etTitle, etDescription, etLocation, etEventDate, etTotalSeats, etAvailableSeats, etPrice;
    private Spinner spinnerCategory, spinnerStatus;
    private Button btnSave;
    private ProgressBar progressBar;

    private FirebaseRepository repository;
    private String eventId;
    private boolean editMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_event);

        repository = new FirebaseRepository();
        eventId = getIntent().getStringExtra("eventId");
        editMode = eventId != null && !eventId.trim().isEmpty();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(editMode ? "Edit Event" : "Add Event");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etTitle = findViewById(R.id.etEventTitle);
        etDescription = findViewById(R.id.etEventDescription);
        etLocation = findViewById(R.id.etEventLocation);
        etEventDate = findViewById(R.id.etEventDate);
        etTotalSeats = findViewById(R.id.etTotalSeats);
        etAvailableSeats = findViewById(R.id.etAvailableSeats);
        etPrice = findViewById(R.id.etEventPrice);
        spinnerCategory = findViewById(R.id.spinnerEventCategory);
        spinnerStatus = findViewById(R.id.spinnerEventStatus);
        btnSave = findViewById(R.id.btnSaveEvent);
        progressBar = findViewById(R.id.progressBar);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CATEGORIES);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, STATUSES);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        btnSave.setOnClickListener(v -> saveEvent());

        if (editMode) {
            loadEvent();
        }
    }

    private void loadEvent() {
        setLoading(true);
        repository.getEventById(eventId, new FirebaseRepository.EventCallback() {
            @Override
            public void onSuccess(Event event) {
                setLoading(false);
                populateForm(event);
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                Toast.makeText(AddEditEventActivity.this,
                        "Error loading event: " + error, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void populateForm(Event event) {
        etTitle.setText(event.getTitle());
        etDescription.setText(event.getDescription());
        etLocation.setText(event.getLocation());
        etEventDate.setText(event.getEventDate());
        etTotalSeats.setText(String.valueOf(event.getTotalSeats()));
        etAvailableSeats.setText(String.valueOf(event.getAvailableSeats()));
        etPrice.setText(String.valueOf(event.getPrice()));
        spinnerCategory.setSelection(indexOf(CATEGORIES, event.getCategory()));
        spinnerStatus.setSelection(indexOf(STATUSES, event.getStatus()));
    }

    private void saveEvent() {
        Event event = readEventFromForm();
        if (event == null) return;

        setLoading(true);
        FirebaseRepository.SimpleCallback callback = new FirebaseRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                Toast.makeText(AddEditEventActivity.this,
                        editMode ? "Event updated." : "Event added.", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                Toast.makeText(AddEditEventActivity.this,
                        "Save failed: " + error, Toast.LENGTH_LONG).show();
            }
        };

        if (editMode) {
            repository.updateEvent(event, callback);
        } else {
            repository.addEvent(event, callback);
        }
    }

    private Event readEventFromForm() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String eventDate = etEventDate.getText().toString().trim();
        String totalSeatsText = etTotalSeats.getText().toString().trim();
        String availableSeatsText = etAvailableSeats.getText().toString().trim();
        String priceText = etPrice.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || location.isEmpty() || eventDate.isEmpty()
                || totalSeatsText.isEmpty() || availableSeatsText.isEmpty() || priceText.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_LONG).show();
            return null;
        }
        if (!eventDate.contains("T")) {
            Toast.makeText(this, "Use ISO date format, for example 2026-06-15T20:00:00", Toast.LENGTH_LONG).show();
            return null;
        }

        int totalSeats;
        int availableSeats;
        double price;
        try {
            totalSeats = Integer.parseInt(totalSeatsText);
            availableSeats = Integer.parseInt(availableSeatsText);
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Seats must be whole numbers and price must be numeric.", Toast.LENGTH_LONG).show();
            return null;
        }

        if (totalSeats < 0 || availableSeats < 0 || availableSeats > totalSeats || price < 0) {
            Toast.makeText(this, "Check seats and price. Available seats cannot exceed total seats.", Toast.LENGTH_LONG).show();
            return null;
        }

        Event event = new Event();
        if (editMode) event.setEventId(eventId);
        event.setTitle(title);
        event.setDescription(description);
        event.setCategory(spinnerCategory.getSelectedItem().toString());
        event.setLocation(location);
        event.setEventDate(eventDate);
        event.setTotalSeats(totalSeats);
        event.setAvailableSeats(availableSeats);
        event.setPrice(price);
        event.setStatus(spinnerStatus.getSelectedItem().toString());
        return event;
    }

    private int indexOf(String[] values, String target) {
        if (target == null) return 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(target)) return i;
        }
        return 0;
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!loading);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
