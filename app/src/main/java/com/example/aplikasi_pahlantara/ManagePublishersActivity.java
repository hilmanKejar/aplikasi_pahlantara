// Path: app/src/main/java/com/example/uts/ManagePublishersActivity.java
package com.example.aplikasi_pahlantara;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest; // Untuk PUT
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ManagePublishersActivity extends AppCompatActivity {

    private RecyclerView recyclerViewUnapprovedPublishers;
    private PublisherApprovalAdapter adapter;
    private List<User> unapprovedPublisherList;
    // private DatabaseHelper dbHelper; // Tidak lagi digunakan untuk user
    private RequestQueue requestQueue; // Untuk Volley

    private static final String API_USERS_URL = "https://6878b0d563f24f1fdc9f064a.mockapi.io/api/v1/user";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_publishers);

        // dbHelper = new DatabaseHelper(this); // Tidak lagi digunakan
        requestQueue = Volley.newRequestQueue(this); // Inisialisasi Volley

        unapprovedPublisherList = new ArrayList<>();

        recyclerViewUnapprovedPublishers = findViewById(R.id.recyclerViewUnapprovedPublishers);
        recyclerViewUnapprovedPublishers.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PublisherApprovalAdapter(unapprovedPublisherList);
        recyclerViewUnapprovedPublishers.setAdapter(adapter);

        adapter.setOnApproveClickListener(user -> {
            approvePublisherAccountApi(user); // Panggil metode API
        });

        Button backButton = findViewById(R.id.buttonBackFromManagePublishers);
        backButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPublishersFromApi(); // Muat dari API
    }

    private void loadPublishersFromApi() {
        unapprovedPublisherList.clear();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, API_USERS_URL, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject userJson = response.getJSONObject(i);
                            String id = userJson.getString("id"); // ID dari API (String)
                            String username = userJson.optString("username");
                            String password = userJson.optString("password");
                            String role = userJson.optString("role");
                            boolean accPenerbit = userJson.optBoolean("accPenerbit", false);

                            // Hanya tampilkan yang role-nya "penerbit" dan belum disetujui
                            if ("penerbit".equals(role) && !accPenerbit) {
                                User user = new User(id, username, password, role, accPenerbit);
                                unapprovedPublisherList.add(user);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        Toast.makeText(ManagePublishersActivity.this, "Data penerbit dimuat dari API.", Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        Log.e("ManagePublishers", "JSON parsing error: " + e.getMessage());
                        Toast.makeText(ManagePublishersActivity.this, "Error parsing data penerbit.", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e("ManagePublishers", "Volley Error loading publishers: " + error.getMessage(), error);
                    Toast.makeText(ManagePublishersActivity.this, "Gagal memuat data penerbit dari API.", Toast.LENGTH_LONG).show();
                });
        requestQueue.add(jsonArrayRequest);
    }

    private void approvePublisherAccountApi(User user) {
        String url = API_USERS_URL + "/" + user.getId(); // URL untuk PUT user spesifik

        // Buat objek JSON dengan status persetujuan yang diupdate
        JSONObject userData = new JSONObject();
        try {
            // Sertakan semua data user agar tidak terhapus (MockAPI.io melakukan merge/replace)
            userData.put("username", user.getUsername());
            userData.put("password", user.getPassword());
            userData.put("role", user.getRole());
            userData.put("accPenerbit", true); // Set menjadi true

            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url, userData,
                    response -> {
                        Toast.makeText(ManagePublishersActivity.this, "Akun " + user.getUsername() + " berhasil disetujui via API.", Toast.LENGTH_SHORT).show();
                        loadPublishersFromApi(); // Muat ulang daftar setelah disetujui
                    },
                    error -> {
                        Log.e("ManagePublishers", "Volley Error PUT approval: " + error.getMessage(), error);
                        Toast.makeText(ManagePublishersActivity.this, "Gagal menyetujui akun via API.", Toast.LENGTH_SHORT).show();
                    });
            requestQueue.add(putRequest);
        } catch (JSONException e) {
            Log.e("ManagePublishers", "Error creating JSON for approval: " + e.getMessage());
            Toast.makeText(this, "Terjadi kesalahan data persetujuan.", Toast.LENGTH_SHORT).show();
        }
    }
}