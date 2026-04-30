package com.soen345.ticketReservation.ui.register;

import android.content.Intent;
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
import com.soen345.ticketReservation.model.User;
import com.soen345.ticketReservation.service.FirebaseRepository;
import com.soen345.ticketReservation.ui.login.LoginActivity;
import com.soen345.ticketReservation.util.SessionManager;

/** Registration screen. New app registrations are CUSTOMER accounts. */
public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etContact, etPassword;
    private RadioGroup rgContactType;
    private RadioButton rbEmail, rbPhone;
    private Button btnRegister, btnGoToLogin;
    private ProgressBar progressBar;

    private FirebaseRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Create Account");

        repository = new FirebaseRepository();

        etName = findViewById(R.id.etName);
        etContact = findViewById(R.id.etContact);
        etPassword = findViewById(R.id.etPassword);
        rgContactType = findViewById(R.id.rgContactType);
        rbEmail = findViewById(R.id.rbEmail);
        rbPhone = findViewById(R.id.rbPhone);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);
        progressBar = findViewById(R.id.progressBar);

        rgContactType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbEmail) {
                etContact.setHint("Email address");
                etContact.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS |
                        android.text.InputType.TYPE_CLASS_TEXT);
            } else {
                etContact.setHint("Phone number (e.g. +15141234567)");
                etContact.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
            }
        });

        btnRegister.setOnClickListener(v -> attemptRegister());
        btnGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void attemptRegister() {
        String name = etName.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String password = etPassword.getText().toString();

        setLoading(true);

        FirebaseRepository.UserCallback callback = new FirebaseRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                saveUserAndProceed(user);
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
            }
        };

        if (rbEmail.isChecked()) {
            repository.registerByEmail(name, contact, password, callback);
        } else {
            repository.registerByPhone(name, contact, password, callback);
        }
    }

    private void saveUserAndProceed(User user) {
        SessionManager.saveUser(this, user);
        setLoading(false);
        Toast.makeText(this, "Welcome, " + user.getName() + "!", Toast.LENGTH_SHORT).show();
        SessionManager.openHomeForSavedRole(this);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
        btnGoToLogin.setEnabled(!loading);
    }
}
