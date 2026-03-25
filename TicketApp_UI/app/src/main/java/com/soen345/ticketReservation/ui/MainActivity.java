package com.soen345.ticketReservation.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketReservation.ui.eventlist.EventListActivity;
import com.soen345.ticketReservation.ui.register.RegisterActivity;

/**
 * Entry point. Checks if a user is already "logged in" (userId stored in prefs).
 * If yes → go to EventList. If no → go to Register.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("TicketAppPrefs", MODE_PRIVATE);
        String savedUserId = prefs.getString("userId", null);

        if (savedUserId != null) {
            startActivity(new Intent(this, EventListActivity.class));
        } else {
            startActivity(new Intent(this, RegisterActivity.class));
        }
        finish();
    }
}
