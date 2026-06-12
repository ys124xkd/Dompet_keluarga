package com.example.dompetkeluarga.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.dompetkeluarga.LaporanPendapatanFragmentAdminDirections
import com.example.dompetkeluarga.LaporanUangSakuAnakFragmentAdminDirections
import com.example.dompetkeluarga.UpdateUangSakuAnakFragmentDirections
import com.example.dompetkeluarga.adapter.Pendapatan_admin_adapter.ViewHolder
import com.example.dompetkeluarga.databinding.ItemUangSakuAnakBinding
import com.example.dompetkeluarga.models.Uang_Saku
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase

class Uang_saku_adapter(private val uangSakuList: List<Uang_Saku>) : RecyclerView.Adapter<Uang_saku_adapter.ViewHolder>() {

    // Filter list to only include items with category "Duit Saku anak"
    private val filteredPendapatanAdminList = uangSakuList.filter { it.category == "Duit Saku anak" }.toMutableList()

    // ViewHolder class to bind views for each item
    class ViewHolder(private val binding: ItemUangSakuAnakBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(uangSaku: Uang_Saku, onDelete: (Int) -> Unit) {
            binding.apply {
                tvNamaAnak.text = uangSaku.nama
                tvtanggal.text = uangSaku.date
                tvJumlahUang.text = "Rp ${String.format("%,d", uangSaku.amount ?: 0).replace(',', '.')}"
                tvCatatan.text = uangSaku.note ?: "N/A" // Display "N/A" if the note is null

                // Navigate to Update screen
                btnUpdate.setOnClickListener {
                    val action =
                        LaporanUangSakuAnakFragmentAdminDirections.actionLaporanUangSakuAnakFragmentAdminToUpdateUangSakuAnakFragment(
                            uangSaku.id.toString(),
                            uangSaku.nama.toString(),
                            uangSaku.date.toString(),
                            uangSaku.amount.toString(),
                            uangSaku.note.toString()
                        )
                    findNavController(binding.root).navigate(action)
                }

                // Set up click listener for the delete button
                btnDelete.setOnClickListener {
                    MaterialAlertDialogBuilder(binding.root.context)
                        .setTitle("Hapus data permanen")
                        .setMessage("Yakin mau hapus data?")
                        .setPositiveButton("Yes") { _, _ ->
                            val firebaseRef = FirebaseDatabase.getInstance().getReference("transactions_admin")
                            firebaseRef.child(uangSaku.id.toString()).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        binding.root.context,
                                        "Data sudah dihapus",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    // Notify the adapter to remove the item
                                    onDelete(adapterPosition)
                                }
                                .addOnFailureListener { error ->
                                    Toast.makeText(
                                        binding.root.context,
                                        "Error: ${error.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                        .setNegativeButton("Tidak") { _, _ ->
                            Toast.makeText(
                                binding.root.context,
                                "Data batal dihapus",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        .show()
                }
            }
        }
    }

    // Create new ViewHolder instances
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUangSakuAnakBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    // Bind data to the ViewHolder at the specified position
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = filteredPendapatanAdminList[position]
        holder.bind(currentItem) { pos -> removeItem(pos) }
    }

    // Return the total number of items in the filtered list
    override fun getItemCount(): Int = filteredPendapatanAdminList.size

    // Function to remove an item from the list and update the RecyclerView
    private fun removeItem(position: Int) {
        if (position >= 0 && position < filteredPendapatanAdminList.size) {
            filteredPendapatanAdminList.removeAt(position) // Remove the item
            notifyItemRemoved(position)                  // Notify RecyclerView
            notifyItemRangeChanged(position, itemCount)  // Notify range change
        }
    }
}


