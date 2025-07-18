package com.example.aplikasi_pahlantara;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import java.io.File; // Untuk menangani path lokal
import android.util.Log; // Untuk debugging

public class DetailPahlawanActivity extends AppCompatActivity {

    private ImageView imageViewDetailPahlawan;
    private TextView textViewNamaDetail, textViewShortStoryDetail, textViewFullStoryDetail;
    private TextView textViewSourceDetail, textViewCreatorDetail; // TextView baru

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_pahlawan);

        // Inisialisasi Views dari layout
        imageViewDetailPahlawan = findViewById(R.id.imageViewDetailPahlawan);
        textViewNamaDetail = findViewById(R.id.textViewNamaDetail);
        textViewShortStoryDetail = findViewById(R.id.textViewShortStoryDetail); // Inisialisasi TextView baru
        textViewFullStoryDetail = findViewById(R.id.textViewFullStoryDetail);
        textViewSourceDetail = findViewById(R.id.textViewSourceDetail); // Inisialisasi TextView baru
        textViewCreatorDetail = findViewById(R.id.textViewCreatorDetail); // Inisialisasi TextView baru

        Button backButton = findViewById(R.id.buttonBackFromDetail);
        backButton.setOnClickListener(v -> finish());

        // Ambil semua data dari Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // int pahlawanId = extras.getInt("id", -1); // ID jika diperlukan di sini, sudah ada di intent
            String pahlawanName = extras.getString("name");
            String pahlawanShortStory = extras.getString("shortStory");
            String pahlawanFullStory = extras.getString("fullStory");
            String pahlawanFotoPath = extras.getString("fotoPath");
            String pahlawanSource = extras.getString("source");
            String pahlawanCreator = extras.getString("creatorUsername");

            // Set data ke Views
            textViewNamaDetail.setText(pahlawanName);
            textViewShortStoryDetail.setText(pahlawanShortStory); // Set teks ke TextView baru
            textViewFullStoryDetail.setText(pahlawanFullStory);
            textViewSourceDetail.setText("Sumber: " + (pahlawanSource != null ? pahlawanSource.toUpperCase() : "N/A")); // Set teks ke TextView baru
            textViewCreatorDetail.setText("Dibuat oleh: " + (pahlawanCreator != null ? pahlawanCreator : "Tidak Diketahui")); // Set teks ke TextView baru

            // Set Judul Activity
            setTitle(pahlawanName);

            // Muat gambar dengan Glide
            if (pahlawanFotoPath != null && !pahlawanFotoPath.isEmpty()) {
                // Cek apakah itu URL atau Path Lokal
                if (pahlawanFotoPath.startsWith("http://") || pahlawanFotoPath.startsWith("https://")) {
                    Glide.with(this)
                            .load(pahlawanFotoPath)
                            .placeholder(R.drawable.placeholder_image) // Gunakan placeholder_image
                            .error(R.drawable.error_image) // Gunakan error_image
                            .into(imageViewDetailPahlawan);
                } else { // Asumsi ini adalah path file lokal
                    File imgFile = new File(pahlawanFotoPath);
                    if (imgFile.exists()) {
                        Glide.with(this)
                                .load(imgFile)
                                .placeholder(R.drawable.placeholder_image)
                                .error(R.drawable.error_image)
                                .into(imageViewDetailPahlawan);
                    } else {
                        imageViewDetailPahlawan.setImageResource(R.drawable.placeholder_image);
                        Toast.makeText(this, "File gambar lokal tidak ditemukan: " + pahlawanFotoPath, Toast.LENGTH_LONG).show();
                        Log.e("DetailActivity", "File gambar lokal tidak ditemukan: " + pahlawanFotoPath);
                    }
                }
            } else {
                imageViewDetailPahlawan.setImageResource(R.drawable.placeholder_image);
            }
        } else {
            Toast.makeText(this, "Data pahlawan tidak valid!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}