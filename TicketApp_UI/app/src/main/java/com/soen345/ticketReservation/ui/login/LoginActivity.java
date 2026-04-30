package com.soen345.ticketReservation.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.soen345.ticketReservation.R;
import com.soen345.ticketReservation.model.User;
import com.soen345.ticketReservation.service.FirebaseRepository;
import com.soen345.ticketReservation.ui.register.RegisterActivity;
import com.soen345.ticketReservation.util.SessionManager;

/** Simple login screen for existing users using email/phone + password. */
public class LoginActivity extends AppCompatActivity {

    private EditText etIdentifier, etPassword;
    private Button btnLogin, btnCreateAccount;
    private ProgressBar progressBar;
    private FirebaseRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Login");

        repository = new FirebaseRepository();
        etIdentifier = findViewById(R.id.etLoginIdentifier);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnCreateAccount.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String identifier = etIdentifier.getText().toString().trim();
        String password = etPassword.getText().toString();

        setLoading(true);
        repository.login(identifier, password, new FirebaseRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                setLoading(false);
                SessionManager.saveUser(LoginActivity.this, user);
                Toast.makeText(LoginActivity.this, "Welcome, " + user.getName() + "!", Toast.LENGTH_SHORT).show();
                SessionManager.openHomeForSavedRole(LoginActivity.this);
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnCreateAccount.setEnabled(!loading);
    }
}
