package com.example.single_lottery.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.single_lottery.R;
import com.google.firebase.auth.FirebaseAuth;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_login);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
            String email = ((EditText) findViewById(R.id.emailInput)).getText().toString().trim();
            String password = ((EditText) findViewById(R.id.passwordInput)).getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            // 跳转到 AdminActivity
                            Intent intent = new Intent(this, AdminActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // 显示错误信息
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Login failed";
                            Toast.makeText(this, "Login Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}

