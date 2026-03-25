package com.soen345.ticketReservation.ui.eventlist;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.soen345.ticketReservation.R;
import com.soen345.ticketReservation.adapter.EventAdapter;
import com.soen345.ticketReservation.model.Event;
import com.soen345.ticketReservation.service.FirebaseRepository;
import com.soen345.ticketReservation.ui.eventdetail.EventDetailActivity;
import com.soen345.ticketReservation.ui.myreservations.MyReservationsActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Event List + Search/Filter screen.
 * Filters: location (text), category (spinner), date (DatePicker).
 * Mirrors EventService.search(category, location, date).
 */
public class EventListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private EditText etSearchLocation;
    private Spinner  spinnerCategory;
    private Button   btnPickDate, btnFilter, btnClearFilter;
    private TextView tvSelectedDate;

    private FirebaseRepository repository;
    private List<Event> allEvents = new ArrayList<>();
    private String selectedDateFilter = null; // "yyyy-MM-dd" or null

    private static final String[] CATEGORIES = {
        "All Categories", "MOVIE", "CONCERT", "SPORTS", "TRAVEL", "OTHER"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Browse Events");

        repository       = new FirebaseRepository();
        recyclerView     = findViewById(R.id.recyclerEvents);
        progressBar      = findViewById(R.id.progressBar);
        tvEmpty          = findViewById(R.id.tvEmpty);
        etSearchLocation = findViewById(R.id.etSearchLocation);
        spinnerCategory  = findViewById(R.id.spinnerCategory);
        btnPickDate      = findViewById(R.id.btnPickDate);
        tvSelectedDate   = findViewById(R.id.tvSelectedDate);
        btnFilter        = findViewById(R.id.btnFilter);
        btnClearFilter   = findViewById(R.id.btnClearFilter);

        adapter = new EventAdapter(new ArrayList<>(), event -> {
            Intent i = new Intent(this, EventDetailActivity.class);
            i.putExtra("eventId",          event.getEventId());
            i.putExtra("eventTitle",       event.getTitle());
            i.putExtra("eventDescription", event.getDescription());
            i.putExtra("eventLocation",    event.getLocation());
            i.putExtra("eventDate",        event.getEventDate());
            i.putExtra("eventCategory",    event.getCategory());
            i.putExtra("eventPrice",       event.getPrice());
            i.putExtra("availableSeats",   event.getAvailableSeats());
            i.putExtra("eventStatus",      event.getStatus());
            startActivity(i);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, CATEGORIES);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnFilter.setOnClickListener(v -> applyFilters());
        btnClearFilter.setOnClickListener(v -> clearFilters());

        loadEvents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "My Reservations")
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            startActivity(new Intent(this, MyReservationsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            selectedDateFilter = String.format(Locale.getDefault(),
                    "%04d-%02d-%02d", year, month + 1, day);
            tvSelectedDate.setText("Date: " + selectedDateFilter);
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadEvents() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        repository.getAllAvailableEvents(new FirebaseRepository.EventsCallback() {
            @Override public void onSuccess(List<Event> events) {
                progressBar.setVisibility(View.GONE);
                allEvents = events;
                adapter.updateEvents(events);
                tvEmpty.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(EventListActivity.this,
                        "Error loading events: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    /** Filters by category + location + date — mirrors EventService.search() */
    private void applyFilters() {
        String loc = etSearchLocation.getText().toString().trim().toLowerCase(Locale.ROOT);
        String cat = spinnerCategory.getSelectedItem().toString();

        List<Event> filtered = allEvents.stream().filter(e -> {
            boolean okLoc  = loc.isEmpty() || (e.getLocation() != null
                    && e.getLocation().toLowerCase(Locale.ROOT).contains(loc));
            boolean okCat  = "All Categories".equals(cat) || cat.equals(e.getCategory());
            boolean okDate = selectedDateFilter == null || (e.getEventDate() != null
                    && e.getEventDate().startsWith(selectedDateFilter));
            return okLoc && okCat && okDate;
        }).collect(Collectors.toList());

        adapter.updateEvents(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void clearFilters() {
        etSearchLocation.setText("");
        spinnerCategory.setSelection(0);
        selectedDateFilter = null;
        tvSelectedDate.setText("No date selected");
        adapter.updateEvents(allEvents);
        tvEmpty.setVisibility(allEvents.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
