package com.soen345.ticketReservation.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketReservation.ui.admin.AdminEventListActivity;
import com.soen345.ticketReservation.ui.eventlist.EventListActivity;
import com.soen345.ticketReservation.ui.login.LoginActivity;
import com.soen345.ticketReservation.util.SessionManager;

/** Entry point. Routes logged-in ADMIN users to admin screens and CUSTOMER users to customer screens. */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.isLoggedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
        } else if ("ADMIN".equalsIgnoreCase(SessionManager.getRole(this))) {
            startActivity(new Intent(this, AdminEventListActivity.class));
        } else {
            startActivity(new Intent(this, EventListActivity.class));
        }
        finish();
    }
}
