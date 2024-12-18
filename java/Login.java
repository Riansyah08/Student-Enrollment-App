package com.example.examfin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Login extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private FirebaseFirestore db;

    private EditText edtEmail, edtPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = FirebaseFirestore.getInstance();

        edtEmail = findViewById(R.id.maill);
        edtPassword = findViewById(R.id.pass);
        btnLogin = findViewById(R.id.nextlgn);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                if (!isValidEmail(email)) {
                    Toast.makeText(Login.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(Login.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                } else {
                    readUser(email, password);
                }
            }
        });
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void readUser(String email, String password) {
        db.collection("Students")
                .whereEqualTo("stdMail", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean isAuthenticated = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String dbPassword = document.getString("stdPass");
                            if (dbPassword != null && dbPassword.equals(password)) {
                                isAuthenticated = true;
                                break;
                            }
                        }

                        if (isAuthenticated) {
                            Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, EnrollmentMenu.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(Login.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(Login.this, "Login failed. Please try again later.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
