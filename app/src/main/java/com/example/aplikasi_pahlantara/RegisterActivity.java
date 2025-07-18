// Path: app/src/main/java/com/example/uts/RegisterActivity.java
package com.example.aplikasi_pahlantara;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log; // Import Log for logging error messages
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request; // Import Request for HTTP method types (GET, POST)
import com.android.volley.RequestQueue; // Import RequestQueue for managing network requests
import com.android.volley.Response; // Import Response for handling API responses
import com.android.volley.VolleyError; // Import VolleyError for handling network errors
import com.android.volley.toolbox.JsonArrayRequest; // Import JsonArrayRequest for GET requests that return JSON arrays
import com.android.volley.toolbox.JsonObjectRequest; // Import JsonObjectRequest for POST/PUT requests that use JSON objects
import com.android.volley.toolbox.Volley; // Import Volley for creating a RequestQueue

import org.json.JSONException; // Import JSONException for handling JSON parsing errors
import org.json.JSONObject; // Import JSONObject for working with JSON objects
import org.json.JSONArray; // Import JSONArray for working with JSON arrays (important for response.length())

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextRegUsername, editTextRegPassword, editTextRegConfirmPassword;
    private Button buttonRegister;
    private TextView textViewLogin;
    private RequestQueue requestQueue;

    private static final String API_USERS_URL = "https://6878b0d563f24f1fdc9f064a.mockapi.io/api/v1/user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        requestQueue = Volley.newRequestQueue(this);

        editTextRegUsername = findViewById(R.id.editTextRegUsername);
        editTextRegPassword = findViewById(R.id.editTextRegPassword);
        editTextRegConfirmPassword = findViewById(R.id.editTextRegConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);

        buttonRegister.setOnClickListener(view -> performRegistration());

        textViewLogin.setOnClickListener(view -> finish());
    }

    private void performRegistration() {
        String username = editTextRegUsername.getText().toString().trim();
        String password = editTextRegPassword.getText().toString().trim();
        String confirmPassword = editTextRegConfirmPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Password dan konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if username already exists via API
        checkUsernameAndRegister(username, password);
    }

    private void checkUsernameAndRegister(String username, String password) {
        JsonArrayRequest checkUsernameRequest = new JsonArrayRequest(Request.Method.GET, API_USERS_URL, null,
                response -> {
                    boolean usernameExists = false;
                    try {
                        // Fix: response.length() instead of response.lenght()
                        for (int i = 0; i < response.length(); i++) {
                            // Fix: getJSONObject() is called on a JSONArray element
                            JSONObject userJson = response.getJSONObject(i);
                            if (username.equals(userJson.optString("username"))) {
                                usernameExists = true;
                                break;
                            }
                        }

                        if (usernameExists) {
                            Toast.makeText(RegisterActivity.this, "Username sudah ada. Silakan pilih username lain.", Toast.LENGTH_SHORT).show();
                        } else {
                            // If username doesn't exist, proceed with registration
                            registerUserViaApi(username, password);
                        }
                    } catch (JSONException e) {
                        // Fix: getMessage() is called on the Exception object 'e'
                        Log.e("RegisterActivity", "JSON parsing error on check username: " + e.getMessage());
                        Toast.makeText(RegisterActivity.this, "Terjadi kesalahan saat memeriksa username.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Fix: getMessage() is called on the VolleyError object 'error'
                    Log.e("RegisterActivity", "Volley Error checking username: " + error.getMessage());
                    Toast.makeText(RegisterActivity.this, "Gagal terhubung ke server untuk memeriksa username.", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(checkUsernameRequest);
    }

    private void registerUserViaApi(String username, String password) {
        JSONObject userData = new JSONObject();
        try {
            userData.put("username", username);
            userData.put("password", password);
            userData.put("role", "penerbit"); // Default role upon registration
            userData.put("accPenerbit", false); // Default: not approved yet
        } catch (JSONException e) {
            Log.e("RegisterActivity", "Error creating JSON for registration: " + e.getMessage());
            Toast.makeText(this, "Terjadi kesalahan data.", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest registerRequest = new JsonObjectRequest(Request.Method.POST, API_USERS_URL, userData,
                response -> {
                    Toast.makeText(RegisterActivity.this, "Pendaftaran sebagai penerbit berhasil! Mohon tunggu persetujuan admin.", Toast.LENGTH_LONG).show();
                    finish();
                },
                error -> {
                    Log.e("RegisterActivity", "Volley Error POST registration: " + error.getMessage(), error);
                    Toast.makeText(RegisterActivity.this, "Pendaftaran gagal via API.", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(registerRequest);
    }
}