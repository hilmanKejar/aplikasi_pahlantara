package com.example.aplikasi_pahlantara;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class AdminPanelActivity extends AppCompatActivity {

    private Button buttonManagePublishers, buttonManagePahlawanData, buttonAdminLogout;
    private String loggedInUsername; // Untuk meneruskan username admin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        loggedInUsername = getIntent().getStringExtra("LOGGED_IN_USERNAME");
        if (loggedInUsername == null) {
            loggedInUsername = "admin"; // Fallback
        }

        buttonManagePublishers = findViewById(R.id.buttonManagePublishers);
        buttonManagePahlawanData = findViewById(R.id.buttonManagePahlawanData);
        buttonAdminLogout = findViewById(R.id.buttonAdminLogout);

        buttonManagePublishers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPanelActivity.this, ManagePublishersActivity.class);
            startActivity(intent);
        });

        buttonManagePahlawanData.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPanelActivity.this, DaftarPahlawanActivity.class);
            intent.putExtra("USER_ROLE", "admin");
            intent.putExtra("LOGGED_IN_USERNAME", loggedInUsername); // Teruskan username admin
            startActivity(intent);
        });

        buttonAdminLogout.setOnClickListener(v -> {
            Intent intent = new Intent(AdminPanelActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}