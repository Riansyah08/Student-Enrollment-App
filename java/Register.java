package com.example.examfin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private Button btnusr;
    private EditText mail, name, id, pass;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnusr = findViewById(R.id.NxtbtnUsr);
        name = findViewById(R.id.Name);
        id = findViewById(R.id.ID);
        mail = findViewById(R.id.Mail);
        pass = findViewById(R.id.pass);
        db = FirebaseFirestore.getInstance();

        btnusr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stdId = id.getText().toString();
                String stdName = name.getText().toString();
                String stdMail = mail.getText().toString();
                String stdPass = pass.getText().toString();

                if (TextUtils.isEmpty(stdId) || TextUtils.isEmpty(stdName) || TextUtils.isEmpty(stdMail) || TextUtils.isEmpty(stdPass)) {
                    Toast.makeText(Register.this, "ID, Mail and Name are required", Toast.LENGTH_SHORT).show();
                } else {
                    if (!isValidEmail(stdMail)) {
                        Toast.makeText(Register.this, "Invalid email format. It must contain '@'", Toast.LENGTH_SHORT).show();
                    } else {
                        addUser(stdId, stdName, stdMail, stdPass);
                        Intent intent = new Intent(Register.this, MainActivity.class);
                        startActivity(intent);

                        id.setText("");
                        name.setText("");
                        mail.setText("");
                        pass.setText("");
                    }
                }
            }
        });
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String loggedInAdmin = sharedPreferences.getString("loggedInUser", null);
        if (loggedInAdmin != null) {
            Intent intent = new Intent(Register.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void addUser(String Id, String Name, String Mail, String Pass) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("stdId", Id);
        user.put("stdName", Name);
        user.put("stdMail", Mail);
        user.put("stdPass", Pass);

        CollectionReference collections = db.collection("Students");
        DocumentReference doc = collections.document(Id);

        doc.set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(Register.this, "User added successfully", Toast.LENGTH_SHORT).show();
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Register.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
