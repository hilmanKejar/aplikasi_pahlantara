// Path: app/src/main/java/com/example/uts/MainActivity.java
package com.example.aplikasi_pahlantara;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Untuk debugging
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest; // Untuk mendapatkan daftar user
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegisterPublisher, textViewGuestLogin;

    private DatabaseHelper dbHelper; // Digunakan untuk admin default lokal (opsional)
    private RequestQueue requestQueue; // Untuk Volley

    private static final String API_USERS_URL = "https://6878b0d563f24f1fdc9f064a.mockapi.io/api/v1/user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this); // Inisialisasi DatabaseHelper untuk user lokal (admin default)
        requestQueue = Volley.newRequestQueue(this); // Inisialisasi Volley

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegisterPublisher = findViewById(R.id.textViewRegisterPublisher);
        textViewGuestLogin = findViewById(R.id.textViewGuestLogin);

        buttonLogin.setOnClickListener(view -> performLogin());

        textViewRegisterPublisher.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        textViewGuestLogin.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, DaftarPahlawanActivity.class);
            intent.putExtra("USER_ROLE", "guest");
            intent.putExtra("LOGGED_IN_USERNAME", "Tamu");
            startActivity(intent);
            finish();
        });
    }

    private void performLogin() {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username dan password tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Coba Login Admin Lokal (untuk bootstrapping awal) ---
        // Jika ada admin default di DB lokal, ini bisa digunakan sebagai fallback
        User localAdmin = dbHelper.getUserLocal(username, password);
        if (localAdmin != null && "admin".equals(localAdmin.getRole())) {
            Toast.makeText(MainActivity.this, "Login Berhasil sebagai Admin Lokal!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, AdminPanelActivity.class);
            intent.putExtra("LOGGED_IN_USERNAME", localAdmin.getUsername());
            startActivity(intent);
            finish();
            return;
        }

        // --- Login via API ---
        // Fetch semua user dari API untuk validasi
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, API_USERS_URL, null,
                response -> {
                    boolean loginSuccess = false;
                    String userRole = "guest";
                    String loggedInUser = "";
                    boolean isApproved = false;

                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject userJson = response.getJSONObject(i);
                            String apiUsername = userJson.optString("username");
                            String apiPassword = userJson.optString("password");
                            String apiRole = userJson.optString("role");
                            boolean apiAccPenerbit = userJson.optBoolean("accPenerbit", false); // Default false

                            if (username.equals(apiUsername) && password.equals(apiPassword)) {
                                loginSuccess = true;
                                userRole = apiRole;
                                loggedInUser = apiUsername;
                                isApproved = apiAccPenerbit;
                                break;
                            }
                        }

                        if (loginSuccess) {
                            if ("penerbit".equals(userRole) && !isApproved) {
                                Toast.makeText(MainActivity.this, "Akun Anda belum disetujui oleh admin. Mohon tunggu.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Login Berhasil sebagai " + userRole + "!", Toast.LENGTH_SHORT).show();
                                Intent intent;
                                if ("admin".equals(userRole)) {
                                    intent = new Intent(MainActivity.this, AdminPanelActivity.class);
                                } else { // penerbit yang disetujui atau user biasa
                                    intent = new Intent(MainActivity.this, DaftarPahlawanActivity.class);
                                }
                                intent.putExtra("USER_ROLE", userRole);
                                intent.putExtra("LOGGED_IN_USERNAME", loggedInUser);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Username atau password salah.", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        Log.e("MainActivity", "JSON parsing error: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "Terjadi kesalahan saat memproses data pengguna.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("MainActivity", "Volley Error: " + error.getMessage());
                    Toast.makeText(MainActivity.this, "Gagal terhubung ke server. Periksa koneksi internet.", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(jsonArrayRequest);
    }
}