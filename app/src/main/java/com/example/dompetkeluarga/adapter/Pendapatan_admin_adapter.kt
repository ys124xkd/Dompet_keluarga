package com.example.dompetkeluarga.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.dompetkeluarga.LaporanPendapatanFragmentAdminDirections
import com.example.dompetkeluarga.databinding.ItemLaporanPendapatanAdminBinding
import com.example.dompetkeluarga.models.pendapatan_admin
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase

class Pendapatan_admin_adapter(
    private val pendapatanAdminList: List<pendapatan_admin>
) : RecyclerView.Adapter<Pendapatan_admin_adapter.ViewHolder>() {

    // Filter list to only include items with category "Gajian"
    private val filteredPendapatanAdminList = pendapatanAdminList.filter { it.category == "Gajian" }

    class ViewHolder(private val binding: ItemLaporanPendapatanAdminBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pendapatanAdmin: pendapatan_admin, position: Int, adapter: Pendapatan_admin_adapter) {
            binding.apply {
                tvtanggal.text = pendapatanAdmin.date.toString()
                tvJumlahUang.text =
                    "Rp ${String.format("%,d", pendapatanAdmin.amount ?: 0).replace(',', '.')}"

                // Check for null and display "N/A" if the note is null
                tvCatatan.text = pendapatanAdmin.note?.toString() ?: "N/A"
                btnUpdate.setOnClickListener {
                    val action =
                        LaporanPendapatanFragmentAdminDirections.actionLaporanPendapatanFragmentAdminToUpdatePendapatanAdminFragment(
                            pendapatanAdmin.id.toString(),
                            pendapatanAdmin.date.toString(),
                            pendapatanAdmin.amount.toString(),
                            pendapatanAdmin.note.toString()
                        )
                    findNavController(binding.root).navigate(action)
                }

                    // Set up click listener for the delete button
                    btnDelete.setOnClickListener {
                        MaterialAlertDialogBuilder(binding.root.context)
                            .setTitle("Hapus data permanen")
                            .setMessage("Yakin mau hapus data?")
                            .setPositiveButton("Yes") { _, _ ->
                                val firebaseRef = FirebaseDatabase.getInstance()
                                    .getReference("transactions_admin")
                                firebaseRef.child(pendapatanAdmin.id.toString()).removeValue()
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            binding.root.context,
                                            "Data sudah dihapus",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        // Remove item from the list and notify the adapter
                                        adapter.removeItem(position)
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
        val binding = ItemLaporanPendapatanAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    // Bind data to the ViewHolder at the specified position
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = filteredPendapatanAdminList[position]
        holder.bind(currentItem, position, this)
    }

    // Return the total number of items in the filtered list
    override fun getItemCount(): Int = filteredPendapatanAdminList.size

    // Function to remove an item from the list and update the RecyclerView
    fun removeItem(position: Int) {
        if (position >= 0 && position < filteredPendapatanAdminList.size) {
            filteredPendapatanAdminList.toMutableList().removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount)
        }
    }
}