package com.example.dompetkeluarga.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.Navigation.createNavigateOnClickListener
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dompetkeluarga.Home_Fragment_AdminDirections
import com.example.dompetkeluarga.LaporanKeuanganFragmentAdminDirections
import com.example.dompetkeluarga.R
import com.example.dompetkeluarga.adapter.Transaksi_user_adapter.ViewHolder
import com.example.dompetkeluarga.databinding.ItemKeuanganAnakBinding
import com.example.dompetkeluarga.databinding.ItemTransactionUserBinding
import com.example.dompetkeluarga.models.Laporan_Keuangan_Anak
import com.example.dompetkeluarga.models.Transaksi_user

class Laporan_Keuangan_adapter (
    private val context: Context,
    private val laporanKeuangan: List<Laporan_Keuangan_Anak>,
    private val onItemClick: (Laporan_Keuangan_Anak) -> Unit
) : RecyclerView.Adapter<Laporan_Keuangan_adapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemKeuanganAnakBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemKeuanganAnakBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val laporan = laporanKeuangan[position]

        // Bind data ke UI
        holder.binding.apply {
            tvUserName.text = laporan.nama // Ambil nama dari laporan

            // Jika ada gambar profil
            Glide.with(context)
                .load(laporan.profileImage)
                .placeholder(R.drawable.ic_launcher_background)
                .into(ivUserProfile)
        }

        // Set klik listener
        holder.itemView.setOnClickListener {
            // Kirim data userId ke FragmentDetailUser menggunakan NavController
            val bundle = Bundle()
            bundle.putString("userId", laporan.id)  // Kirimkan userId

            // Menggunakan NavController untuk navigasi ke FragmentDetailUser
            val navController = Navigation.findNavController(it)
            navController.navigate(R.id.action_laporanKeuanganFragmentAdmin_to_riwayatLaporanKeuanganFragment, bundle)
        }
    }

    override fun getItemCount(): Int = laporanKeuangan.size
}