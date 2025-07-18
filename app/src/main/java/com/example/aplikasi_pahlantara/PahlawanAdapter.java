package com.example.aplikasi_pahlantara;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.List;
import android.graphics.Color; // Tidak lagi dibutuhkan untuk label, tapi biarkan saja

public class PahlawanAdapter extends RecyclerView.Adapter<PahlawanAdapter.PahlawanViewHolder> {

    private List<Pahlawan> pahlawanList;
    private OnItemClickListener listener;
    private String currentUserRole;

    public interface OnItemClickListener {
        void onDetailClick(Pahlawan pahlawan);
        void onEditClick(Pahlawan pahlawan);
        void onDeleteClick(Pahlawan pahlawan);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public PahlawanAdapter(List<Pahlawan> pahlawanList, String currentUserRole) {
        this.pahlawanList = pahlawanList;
        this.currentUserRole = currentUserRole;
    }

    public void setPahlawanList(List<Pahlawan> newPahlawanList) {
        this.pahlawanList = newPahlawanList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PahlawanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pahlawan, parent, false);
        return new PahlawanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PahlawanViewHolder holder, int position) {
        Pahlawan pahlawan = pahlawanList.get(position);

        // --- Muat Gambar dengan Glide ---
        String fotoPath = pahlawan.getFotoPath();
        if (fotoPath != null && !fotoPath.isEmpty()) {
            if (fotoPath.startsWith("http://") || fotoPath.startsWith("https://")) {
                Glide.with(holder.itemView.getContext())
                        .load(fotoPath)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(holder.imageViewPahlawan);
            } else {
                File imgFile = new File(fotoPath);
                if (imgFile.exists()) {
                    Glide.with(holder.itemView.getContext())
                            .load(imgFile)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .into(holder.imageViewPahlawan);
                } else {
                    holder.imageViewPahlawan.setImageResource(R.drawable.placeholder_image);
                }
            }
        } else {
            holder.imageViewPahlawan.setImageResource(R.drawable.placeholder_image);
        }

        // --- Set Teks Pahlawan ---
        holder.textViewNamaPahlawan.setText(pahlawan.getName());
        holder.textViewDeskripsiSingkat.setText(pahlawan.getShortStory());

        // --- HAPUS ATAU KOMENTARI BAGIAN INI UNTUK MENGHILANGKAN LABEL PEMBUAT ---
        holder.textViewCreatorLabel.setVisibility(View.GONE); // Pastikan ini selalu GONE


        // --- Listener Tombol ---
        holder.buttonDetail.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailClick(pahlawan);
            }
        });

        // Tampilkan/Sembunyikan tombol Edit & Hapus berdasarkan role
        if ("admin".equals(currentUserRole) || "penerbit".equals(currentUserRole)) {
            holder.buttonEdit.setVisibility(View.VISIBLE);
            holder.buttonHapus.setVisibility(View.VISIBLE);

            holder.buttonEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(pahlawan);
                }
            });

            holder.buttonHapus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(pahlawan);
                }
            });
        } else {
            holder.buttonEdit.setVisibility(View.GONE);
            holder.buttonHapus.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return pahlawanList.size();
    }

    public static class PahlawanViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewPahlawan;
        TextView textViewNamaPahlawan;
        TextView textViewDeskripsiSingkat;
        TextView textViewCreatorLabel;
        Button buttonDetail;
        ImageButton buttonEdit;
        ImageButton buttonHapus;

        public PahlawanViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPahlawan = itemView.findViewById(R.id.imageViewPahlawan);
            textViewNamaPahlawan = itemView.findViewById(R.id.textViewNamaPahlawan);
            textViewDeskripsiSingkat = itemView.findViewById(R.id.textViewDeskripsiSingkat);
            textViewCreatorLabel = itemView.findViewById(R.id.textViewCreatorLabel); // ID ini harus tetap ada di XML
            buttonDetail = itemView.findViewById(R.id.buttonDetail);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonHapus = itemView.findViewById(R.id.buttonHapus);
        }
    }
}