// Path: app/src/main/java/com/example/uts/DaftarPahlawanActivity.java
package com.example.aplikasi_pahlantara;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.content.Intent;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.SearchView;
import android.widget.Toast;
import android.util.Log;
import android.view.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest; // Untuk DELETE
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DaftarPahlawanActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPahlawan;
    private PahlawanAdapter adapter;
    private List<Pahlawan> pahlawanList;
    private List<Pahlawan> allPahlawanData; // Untuk menyimpan semua data sebelum filter
    private RequestQueue requestQueue;

    private String currentUserRole;
    private String loggedInUsername;

    private Button tambahPahlawanButton;
    private Button kembaliButton;
    private SearchView searchViewPahlawan;

    private static final String API_URL = "https://6878b0d563f24f1fdc9f064a.mockapi.io/api/v1/heroes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_pahlawan);

        requestQueue = Volley.newRequestQueue(this);

        pahlawanList = new ArrayList<>();
        allPahlawanData = new ArrayList<>();

        if (getIntent().hasExtra("USER_ROLE")) {
            currentUserRole = getIntent().getStringExtra("USER_ROLE");
        } else {
            currentUserRole = "guest";
        }
        if (getIntent().hasExtra("LOGGED_IN_USERNAME")) {
            loggedInUsername = getIntent().getStringExtra("LOGGED_IN_USERNAME");
        } else {
            loggedInUsername = "Guest";
        }

        recyclerViewPahlawan = findViewById(R.id.recyclerViewPahlawan);
        recyclerViewPahlawan.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PahlawanAdapter(pahlawanList, currentUserRole);
        recyclerViewPahlawan.setAdapter(adapter);

        tambahPahlawanButton = findViewById(R.id.tambahPahlawanButton);
        searchViewPahlawan = findViewById(R.id.searchViewPahlawan);
        kembaliButton = findViewById(R.id.kembaliButton);

        if ("admin".equals(currentUserRole) || "penerbit".equals(currentUserRole)) {
            tambahPahlawanButton.setVisibility(View.VISIBLE);
            tambahPahlawanButton.setOnClickListener(view -> {
                Intent intent = new Intent(DaftarPahlawanActivity.this, FormPahlawanActivity.class);
                intent.putExtra("LOGGED_IN_USERNAME", loggedInUsername); // Tetap kirim sebagai nilai default
                startActivity(intent);
            });
        } else {
            tambahPahlawanButton.setVisibility(View.GONE);
        }

        if ("penerbit".equals(currentUserRole)) {
            kembaliButton.setText(R.string.logout);
            kembaliButton.setOnClickListener(view -> {
                Intent intent = new Intent(DaftarPahlawanActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        } else {
            kembaliButton.setText(R.string.kembali);
            kembaliButton.setOnClickListener(view -> {
                Intent intent;
                if ("admin".equals(currentUserRole)) {
                    intent = new Intent(DaftarPahlawanActivity.this, AdminPanelActivity.class);
                } else {
                    intent = new Intent(DaftarPahlawanActivity.this, MainActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        }

        adapter.setOnItemClickListener(new PahlawanAdapter.OnItemClickListener() {
            @Override
            public void onDetailClick(Pahlawan pahlawan) {
                Intent intent = new Intent(DaftarPahlawanActivity.this, DetailPahlawanActivity.class);
                intent.putExtra("id", pahlawan.getId());
                intent.putExtra("name", pahlawan.getName());
                intent.putExtra("shortStory", pahlawan.getShortStory());
                intent.putExtra("fullStory", pahlawan.getFullStory());
                intent.putExtra("fotoPath", pahlawan.getFotoPath());
                intent.putExtra("source", pahlawan.getSource());
                intent.putExtra("creatorUsername", pahlawan.getCreatorUsername());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Pahlawan pahlawan) {
                if ("admin".equals(currentUserRole) || "penerbit".equals(currentUserRole)) {
                    // Semua pahlawan dari API bisa diedit oleh admin/penerbit
                    Intent intent = new Intent(DaftarPahlawanActivity.this, FormPahlawanActivity.class);
                    intent.putExtra("PAHLAWAN_ID", pahlawan.getId()); // Kirim ID untuk mode edit
                    intent.putExtra("LOGGED_IN_USERNAME", loggedInUsername); // Tetap kirim sebagai nilai default
                    startActivity(intent);
                } else {
                    Toast.makeText(DaftarPahlawanActivity.this, "Anda tidak memiliki izin untuk mengedit data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDeleteClick(Pahlawan pahlawan) {
                if ("admin".equals(currentUserRole) || "penerbit".equals(currentUserRole)) {
                    new AlertDialog.Builder(DaftarPahlawanActivity.this)
                            .setTitle("Hapus Pahlawan")
                            .setMessage("Apakah Anda yakin ingin menghapus " + pahlawan.getName() + "?")
                            .setPositiveButton("Ya", (dialog, which) -> {
                                deletePahlawanFromApi(pahlawan.getId(), pahlawan.getName());
                            })
                            .setNegativeButton("Tidak", null)
                            .show();
                } else {
                    Toast.makeText(DaftarPahlawanActivity.this, "Anda tidak memiliki izin untuk menghapus data.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        searchViewPahlawan.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPahlawan(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPahlawan(newText);
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPahlawanFromApi();
    }

    private void loadPahlawanFromApi() {
        pahlawanList.clear();
        allPahlawanData.clear();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, API_URL, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject pahlawanJson = response.getJSONObject(i);

                            int id = pahlawanJson.getInt("id");
                            String name = pahlawanJson.optString("name", "Nama Tidak Tersedia");
                            String shortStory = pahlawanJson.optString("shortStory", "Cerita singkat tidak tersedia.");
                            String fullStory = pahlawanJson.optString("fullStory", "Cerita lengkap tidak tersedia.");
                            String fotoPath = pahlawanJson.optString("fotoPath", "");
                            String source = pahlawanJson.optString("source", "API"); // Ambil dari API
                            String creatorUsername = pahlawanJson.optString("creatorUsername", "Unknown"); // Ambil dari API

                            Pahlawan pahlawan = new Pahlawan(id, name, shortStory, fullStory, fotoPath, source, creatorUsername);
                            pahlawanList.add(pahlawan);
                            allPahlawanData.add(pahlawan);
                        }
                        sortAndNotifyAdapter();
                        Toast.makeText(DaftarPahlawanActivity.this, "Data pahlawan berhasil dimuat!", Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        Log.e("DaftarPahlawanActivity", "JSON parsing error (API): " + e.getMessage());
                        Toast.makeText(DaftarPahlawanActivity.this, "Error parsing data pahlawan.", Toast.LENGTH_LONG).show();
                        sortAndNotifyAdapter();
                    }
                },
                error -> {
                    Log.e("DaftarPahlawanActivity", "Volley Error (API): " + error.getMessage(), error);
                    Toast.makeText(DaftarPahlawanActivity.this, "Gagal memuat data. Periksa koneksi internet.", Toast.LENGTH_LONG).show();
                    sortAndNotifyAdapter();
                });
        requestQueue.add(jsonArrayRequest);
    }

    private void deletePahlawanFromApi(int id, String name) {
        String url = API_URL + "/" + id;
        StringRequest deleteRequest = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    Toast.makeText(DaftarPahlawanActivity.this, name + " berhasil dihapus", Toast.LENGTH_SHORT).show();
                    loadPahlawanFromApi();
                },
                error -> {
                    Log.e("DaftarPahlawanActivity", "Volley Error DELETE: " + error.getMessage(), error);
                    Toast.makeText(DaftarPahlawanActivity.this, "Gagal menghapus " + name, Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(deleteRequest);
    }

    private void sortAndNotifyAdapter() {
        Collections.sort(pahlawanList, Comparator.comparing(Pahlawan::getName));
        adapter.notifyDataSetChanged();
    }

    private void filterPahlawan(String query) {
        pahlawanList.clear();
        if (query.isEmpty()) {
            pahlawanList.addAll(allPahlawanData);
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.getDefault());
            for (Pahlawan pahlawan : allPahlawanData) {
                if (pahlawan.getName().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery) ||
                        (pahlawan.getShortStory() != null && pahlawan.getShortStory().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) ||
                        (pahlawan.getCreatorUsername() != null && pahlawan.getCreatorUsername().toLowerCase(Locale.getDefault()).contains(lowerCaseQuery))) {
                    pahlawanList.add(pahlawan);
                }
            }
        }
        sortAndNotifyAdapter();
    }
}