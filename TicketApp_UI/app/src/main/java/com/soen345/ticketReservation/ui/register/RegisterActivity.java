package com.soen345.ticketReservation.ui.register;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketReservation.R;
import com.soen345.ticketReservation.service.FirebaseRepository;
import com.soen345.ticketReservation.ui.eventlist.EventListActivity;

/**
 * Registration screen.
 * User can register with either email or phone number + password.
 * Mirrors UserService.registerByEmail() / registerByPhone()
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etContact, etPassword;
    private RadioGroup rgContactType;
    private RadioButton rbEmail, rbPhone;
    private Button btnRegister;
    private ProgressBar progressBar;

    private FirebaseRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        repository = new FirebaseRepository();

        etName       = findViewById(R.id.etName);
        etContact    = findViewById(R.id.etContact);
        etPassword   = findViewById(R.id.etPassword);
        rgContactType = findViewById(R.id.rgContactType);
        rbEmail      = findViewById(R.id.rbEmail);
        rbPhone      = findViewById(R.id.rbPhone);
        btnRegister  = findViewById(R.id.btnRegister);
        progressBar  = findViewById(R.id.progressBar);

        // Update hint based on selection
        rgContactType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbEmail) {
                etContact.setHint("Email address");
                etContact.setInputType(
                    android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS |
                    android.text.InputType.TYPE_CLASS_TEXT);
            } else {
                etContact.setHint("Phone number (e.g. +15141234567)");
                etContact.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
            }
        });

        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String name     = etName.getText().toString().trim();
        String contact  = etContact.getText().toString().trim();
        String password = etPassword.getText().toString();

        setLoading(true);

        if (rbEmail.isChecked()) {
            repository.registerByEmail(name, contact, password, new FirebaseRepository.UserCallback() {
                @Override
                public void onSuccess(com.soen345.ticketReservation.model.User user) {
                    saveUserAndProceed(user.getUserId(), user.getName());
                }
                @Override
                public void onError(String error) {
                    setLoading(false);
                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            repository.registerByPhone(name, contact, password, new FirebaseRepository.UserCallback() {
                @Override
                public void onSuccess(com.soen345.ticketReservation.model.User user) {
                    saveUserAndProceed(user.getUserId(), user.getName());
                }
                @Override
                public void onError(String error) {
                    setLoading(false);
                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void saveUserAndProceed(String userId, String name) {
        // Save userId locally so we don't need to re-register next launch
        SharedPreferences prefs = getSharedPreferences("TicketAppPrefs", MODE_PRIVATE);
        prefs.edit()
            .putString("userId", userId)
            .putString("userName", name)
            .apply();

        setLoading(false);
        Toast.makeText(this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();

        startActivity(new Intent(this, EventListActivity.class));
        finish();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
    }
}
