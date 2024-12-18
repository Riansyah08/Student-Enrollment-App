package com.example.examfin;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class EnrollmentMenu extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView summaryText;
    private List<String> selectedSubjects = new ArrayList<>();
    private List<Integer> subjectCredits = new ArrayList<>();
    private int totalCredits = 0;
    private static final int MAX_CREDITS = 24;

    // List to hold all subjects fetched from Firestore
    private List<String> subjectList = new ArrayList<>();
    private List<Integer> creditsList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment_menu);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        summaryText = findViewById(R.id.summaryText);
        ListView subjectListView = findViewById(R.id.subjectListView);
        Button showSummaryButton = findViewById(R.id.showSummaryButton);
        Button enrollButton = findViewById(R.id.enrollButton);

        // Initialize adapter for ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, subjectList);
        subjectListView.setAdapter(adapter);

        // Fetch and display subjects from Firestore
        fetchSubjects();

        // When a user selects a subject, add it to the selected subjects list
        subjectListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSubject = subjectList.get(position);
            int credits = creditsList.get(position);

            // Check if the subject is already selected
            if (selectedSubjects.contains(selectedSubject)) {
                Toast.makeText(EnrollmentMenu.this, selectedSubject + " is already selected.", Toast.LENGTH_SHORT).show();
                return; // Don't add the subject again if it's already selected
            }

            // Check if adding this subject exceeds the max credit limit
            if (totalCredits + credits <= MAX_CREDITS) {
                selectedSubjects.add(selectedSubject);
                subjectCredits.add(credits);
                totalCredits += credits;
                Toast.makeText(EnrollmentMenu.this, selectedSubject + " added!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EnrollmentMenu.this, "Max credit limit reached", Toast.LENGTH_SHORT).show();
            }
        });

        showSummaryButton.setOnClickListener(v -> toggleSummary());

        enrollButton.setOnClickListener(v -> enrollStudent());
    }

    private void fetchSubjects() {
        Log.d("EnrollmentMenu", "Fetching subjects from Firestore...");

        db.collection("subject")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        subjectList.clear();
                        creditsList.clear();

                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            Log.d("EnrollmentMenu", "Subjects retrieved successfully.");

                            // Populate the subjectList and creditsList
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String subject = document.getString("name");
                                Long creditsLong = document.getLong("credits");

                                if (subject == null || creditsLong == null) {
                                    Log.e("EnrollmentMenu", "Error: Missing subject name or credits in Firestore document");
                                    continue;
                                }

                                int credits = creditsLong.intValue();
                                subjectList.add(subject);
                                creditsList.add(credits);
                            }

                            // Notify adapter of data change
                            adapter.notifyDataSetChanged();

                            // Log sizes for debugging
                            Log.d("EnrollmentMenu", "Subjects loaded: " + subjectList.size());
                            Log.d("EnrollmentMenu", "Credits loaded: " + creditsList.size());

                        } else {
                            Log.d("EnrollmentMenu", "No subjects found in Firestore.");
                            Toast.makeText(EnrollmentMenu.this, "No subjects found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("EnrollmentMenu", "Error getting documents: ", task.getException());
                        Toast.makeText(EnrollmentMenu.this, "Failed to load subjects", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Show summary of selected subjects
    private void showSummary() {
        StringBuilder summary = new StringBuilder("Selected Subjects:\n");
        for (int i = 0; i < selectedSubjects.size(); i++) {
            summary.append(selectedSubjects.get(i))
                    .append(" (Credits: ")
                    .append(subjectCredits.get(i))
                    .append(")\n");
        }
        summary.append("\nTotal Credits: ").append(totalCredits);
        summaryText.setText(summary.toString());
    }

    // Toggle the visibility of the summary
    private void toggleSummary() {
        if (summaryText.getVisibility() == View.VISIBLE) {
            // Hide the summary
            summaryText.setVisibility(View.GONE);
        } else {
            // Show the summary
            showSummary();
            summaryText.setVisibility(View.VISIBLE);
        }
    }

    // Enroll the student and save to Firestore
    private void enrollStudent() {
        // Ensure that there are selected subjects before enrolling
        if (selectedSubjects.isEmpty()) {
            Toast.makeText(this, "Please select subjects to enroll.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a student enrollment object (replace with actual student data if needed)
        StudentEnrollment enrollment = new StudentEnrollment(
                "stdId",  
                "stdName",  
                "stdMail", 
                selectedSubjects,
                subjectCredits,
                totalCredits
        );

        // Save the enrollment data to Firestore
        db.collection("enrolledsubject")
                .add(enrollment)  // Use Firestore's auto-generated document ID
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EnrollmentMenu.this, "Enrollment successful!", Toast.LENGTH_SHORT).show();
                    Log.d("EnrollmentMenu", "Student enrollment saved.");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EnrollmentMenu.this, "Enrollment failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("EnrollmentMenu", "Error enrolling student: " + e.getMessage());
                });
    }

    public static class StudentEnrollment {
        private String studentId;
        private String studentName;
        private String studentEmail;
        private List<String> enrolledSubjects;
        private List<Integer> enrolledCredits;
        private int totalCredits;

        public StudentEnrollment(String studentId, String studentName, String studentEmail,
                                 List<String> enrolledSubjects, List<Integer> enrolledCredits, int totalCredits) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.studentEmail = studentEmail;
            this.enrolledSubjects = enrolledSubjects;
            this.enrolledCredits = enrolledCredits;
            this.totalCredits = totalCredits;
        }

        public String getStudentId() {
            return studentId;
        }

        public String getStudentName() {
            return studentName;
        }

        public String getStudentEmail() {
            return studentEmail;
        }

        public List<String> getEnrolledSubjects() {
            return enrolledSubjects;
        }

        public List<Integer> getEnrolledCredits() {
            return enrolledCredits;
        }

        public int getTotalCredits() {
            return totalCredits;
        }
    }
}
