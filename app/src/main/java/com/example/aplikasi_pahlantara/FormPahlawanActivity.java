// Path: app/src/main/java/com/example/uts/FormPahlawanActivity.java
package com.example.aplikasi_pahlantara;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class FormPahlawanActivity extends AppCompatActivity {

    private TextInputEditText editTextNama, editTextShortStory, editTextFullStory;
    private TextInputEditText editTextSource, editTextCreatorUsername; // Deklarasi TextInputEditText baru
    private Button buttonSave;
    private Button buttonBack;
    private ImageView imageViewPreviewFoto;

    private int pahlawanId = -1; // -1 menandakan mode tambah, bukan edit
    private String currentFotoPath; // Path gambar yang saat ini dipilih/dimuat

    private RequestQueue requestQueue; // Untuk Volley
    private static final String API_BASE_URL = "https://6878b0d563f24f1fdc9f064a.mockapi.io/api/v1/heroes";

    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_pahlawan);

        requestQueue = Volley.newRequestQueue(this);

        // Inisialisasi Views sesuai ID di XML
        editTextNama = findViewById(R.id.editTextNama);
        editTextShortStory = findViewById(R.id.editTextShortStory);
        editTextFullStory = findViewById(R.id.editTextFullStory);
        imageViewPreviewFoto = findViewById(R.id.imageViewPreviewFoto);
        buttonSave = findViewById(R.id.buttonSave);
        buttonBack = findViewById(R.id.buttonBack);

        // Inisialisasi TextInputEditText baru
        editTextSource = findViewById(R.id.editTextSource);
        editTextCreatorUsername = findViewById(R.id.editTextCreatorUsername);

        // Set default value untuk Source dan CreatorUsername (jika ada)
        String loggedInUsername = getIntent().getStringExtra("LOGGED_IN_USERNAME");
        if (loggedInUsername != null && !loggedInUsername.isEmpty()) {
            editTextCreatorUsername.setText(loggedInUsername);
        } else {
            editTextCreatorUsername.setText("Pengguna"); // Default jika tidak ada login
        }
        editTextSource.setText("manual"); // Default source "manual"

        // --- Inisialisasi Launcher untuk Pemilihan Gambar & Izin ---
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            try {
                                currentFotoPath = saveImageToInternalStorage(selectedImageUri);
                                Glide.with(this).load(currentFotoPath).into(imageViewPreviewFoto);
                            } catch (Exception e) {
                                Toast.makeText(this, "Gagal memuat gambar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                                imageViewPreviewFoto.setImageResource(R.drawable.ic_camera_placeholder);
                            }
                        }
                    }
                });

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImageChooser();
                    } else {
                        Toast.makeText(this, "Izin akses penyimpanan ditolak. Tidak dapat memilih gambar.", Toast.LENGTH_SHORT).show();
                    }
                });

        imageViewPreviewFoto.setOnClickListener(v -> checkPermissionAndOpenChooser());

        // Cek mode edit atau tambah
        if (getIntent().hasExtra("PAHLAWAN_ID")) {
            pahlawanId = getIntent().getIntExtra("PAHLAWAN_ID", -1);
            if (pahlawanId != -1) {
                loadPahlawanDataForEdit(pahlawanId); // Muat dari API
            }
        } else {
            imageViewPreviewFoto.setImageResource(R.drawable.ic_camera_placeholder);
            setTitle(getString(R.string.judul_form_pahlawan));
        }

        buttonSave.setOnClickListener(view -> savePahlawan());
        buttonBack.setOnClickListener(v -> finish());
    }

    // --- Metode untuk Izin dan Pemilih Gambar ---
    private void checkPermissionAndOpenChooser() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openImageChooser();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else { // Untuk Android 12 (API 32) ke bawah
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openImageChooser();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private String saveImageToInternalStorage(Uri uri) throws Exception {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        File destinationFile = null;

        try {
            ContentResolver contentResolver = getContentResolver();
            inputStream = contentResolver.openInputStream(uri);

            File imagesDir = new File(getFilesDir(), "images_pahlawan");
            if (!imagesDir.exists()) {
                imagesDir.mkdirs();
            }

            String fileName = UUID.randomUUID().toString() + ".jpg";
            destinationFile = new File(imagesDir, fileName);
            outputStream = new FileOutputStream(destinationFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            return destinationFile.getAbsolutePath();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    // --- Metode untuk Mode Edit (Memuat dari API) ---
    private void loadPahlawanDataForEdit(int id) {
        String url = API_BASE_URL + "/" + id;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String name = response.getString("name");
                        String shortStory = response.getString("shortStory");
                        String fullStory = response.optString("fullStory", "");
                        String fotoPath = response.optString("fotoPath", "");
                        String source = response.optString("source", "manual"); // Ambil dari API
                        String creatorUsername = response.optString("creatorUsername", "Tidak Diketahui"); // Ambil dari API

                        editTextNama.setText(name);
                        editTextShortStory.setText(shortStory);
                        editTextFullStory.setText(fullStory);
                        editTextSource.setText(source); // Set nilai Source
                        editTextCreatorUsername.setText(creatorUsername); // Set nilai CreatorUsername
                        currentFotoPath = fotoPath;

                        if (currentFotoPath != null && !currentFotoPath.isEmpty()) {
                            Glide.with(this).load(currentFotoPath).placeholder(R.drawable.placeholder_image).error(R.drawable.error_image).into(imageViewPreviewFoto);
                        } else {
                            imageViewPreviewFoto.setImageResource(R.drawable.ic_camera_placeholder);
                        }
                        setTitle(getString(R.string.edit_pahlawan));
                    } catch (JSONException e) {
                        Log.e("FormPahlawanActivity", "JSON parsing error: " + e.getMessage());
                        Toast.makeText(this, "Error parsing data pahlawan.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                },
                error -> {
                    Log.e("FormPahlawanActivity", "Volley Error loading pahlawan: " + error.getMessage(), error);
                    Toast.makeText(this, "Gagal memuat data pahlawan dari API.", Toast.LENGTH_SHORT).show();
                    finish();
                });
        requestQueue.add(jsonObjectRequest);
    }

    // --- Metode untuk Menyimpan/Memperbarui Pahlawan (ke API) ---
    private void savePahlawan() {
        String name = editTextNama.getText().toString().trim();
        String shortStory = editTextShortStory.getText().toString().trim();
        String fullStory = editTextFullStory.getText().toString().trim();
        String source = editTextSource.getText().toString().trim(); // Ambil dari input user
        String creator = editTextCreatorUsername.getText().toString().trim(); // Ambil dari input user

        // --- Validasi Input ---
        if (TextUtils.isEmpty(name)) {
            editTextNama.setError("Nama pahlawan tidak boleh kosong");
            return;
        }
        if (TextUtils.isEmpty(shortStory)) {
            editTextShortStory.setError("Cerita singkat tidak boleh kosong");
            return;
        }
        if (TextUtils.isEmpty(source)) {
            editTextSource.setError("Sumber data tidak boleh kosong");
            return;
        }
        if (TextUtils.isEmpty(creator)) {
            editTextCreatorUsername.setError("Nama pembuat tidak boleh kosong");
            return;
        }
        if (TextUtils.isEmpty(currentFotoPath)) {
            Toast.makeText(this, "Foto pahlawan harus dipilih!", Toast.LENGTH_SHORT).show();
            imageViewPreviewFoto.setImageResource(R.drawable.error_image);
            return;
        }

        JSONObject pahlawanData = new JSONObject();
        try {
            pahlawanData.put("name", name);
            pahlawanData.put("shortStory", shortStory);
            pahlawanData.put("fullStory", fullStory);
            pahlawanData.put("fotoPath", currentFotoPath);
            pahlawanData.put("source", source); // Gunakan source dari input user
            pahlawanData.put("creatorUsername", creator); // Gunakan creator dari input user
        } catch (JSONException e) {
            Log.e("FormPahlawanActivity", "Error creating JSON: " + e.getMessage());
            Toast.makeText(this, "Terjadi kesalahan data.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pahlawanId == -1) { // Mode Tambah (POST)
            JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, API_BASE_URL, pahlawanData,
                    response -> {
                        Toast.makeText(FormPahlawanActivity.this, "Pahlawan berhasil ditambahkan via API!", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    error -> {
                        Log.e("FormPahlawanActivity", "Volley Error POST: " + error.getMessage(), error);
                        Toast.makeText(FormPahlawanActivity.this, "Gagal menambahkan pahlawan via API.", Toast.LENGTH_SHORT).show();
                    });
            requestQueue.add(postRequest);
        } else { // Mode Edit (PUT)
            String url = API_BASE_URL + "/" + pahlawanId;
            JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url, pahlawanData,
                    response -> {
                        Toast.makeText(FormPahlawanActivity.this, "Pahlawan berhasil diperbarui via API!", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    error -> {
                        Log.e("FormPahlawanActivity", "Volley Error PUT: " + error.getMessage(), error);
                        Toast.makeText(FormPahlawanActivity.this, "Gagal memperbarui pahlawan via API.", Toast.LENGTH_SHORT).show();
                    });
            requestQueue.add(putRequest);
        }
    }
}