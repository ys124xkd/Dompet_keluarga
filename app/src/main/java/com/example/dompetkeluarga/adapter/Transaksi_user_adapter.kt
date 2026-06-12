package com.example.dompetkeluarga.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import com.example.dompetkeluarga.models.Transaksi_user
import androidx.recyclerview.widget.RecyclerView
import com.example.dompetkeluarga.HomeFragment_userDirections
import com.example.dompetkeluarga.databinding.ItemTransactionUserBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class Transaksi_user_adapter(
    private val transaksiUser: ArrayList<Transaksi_user>
) : RecyclerView.Adapter<Transaksi_user_adapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTransactionUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = transaksiUser[position]
        holder.binding.apply {
            tvTypeTransaksi.text = currentItem.type
            tvKategori.text = currentItem.category
            tvJumlahUang.text = "Rp ${String.format("%,d", currentItem.amount ?: 0).replace(',', '.')}"
            tvTanggal.text = currentItem.date
            tvCatatan.text = currentItem.note
            Picasso.get().load(currentItem.imageLocation).into(ivImageLocation)
            btnUpdate.setOnClickListener {
                val aksi = HomeFragment_userDirections.actionHomeFragmentUserToUpdateFragmentDataTransactionUser2(
                    currentItem.id.toString(),
                    currentItem.type.toString(),
                    currentItem.category.toString(),
                    currentItem.amount.toString(),
                    currentItem.date.toString(),
                    currentItem.note.toString())
                findNavController(holder.itemView).navigate(aksi)
            }
            btnDelete.setOnClickListener {
                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle("Hapus data permanen")
                    .setMessage("Yakin mau hapus data?")
                    .setPositiveButton("yes") { _, _ ->
                        val firebaseRef = FirebaseDatabase.getInstance().getReference("transactions")
                        firebaseRef.child(currentItem.id.toString()).removeValue()
                            .addOnSuccessListener {
                                if (position != RecyclerView.NO_POSITION && position < transaksiUser.size) {
                                    Toast.makeText(holder.itemView.context, "Data sudah dihapus", Toast.LENGTH_LONG).show()
                                    transaksiUser.removeAt(position)
                                    notifyItemRemoved(position)
                                    notifyItemRangeChanged(position, transaksiUser.size)
                                }
                            }
                            .addOnFailureListener { error ->
                                Toast.makeText(holder.itemView.context, "Error ${error.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                    .setNegativeButton("tidak") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    override fun getItemCount(): Int {
        return transaksiUser.size
    }
}
