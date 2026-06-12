package com.example.dompetkeluarga.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dompetkeluarga.R
import com.example.dompetkeluarga.databinding.ItemMountBinding
import com.example.dompetkeluarga.models.Transaction

class TransactionAdapter (private val transactions: ArrayList<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemMountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }

    override fun getItemCount(): Int = transactions.size

    class TransactionViewHolder(private val binding: ItemMountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            binding.tvTypeTransaksi.text = transaction.type
            binding.tvKategori.text = transaction.category
            binding.tvJumlahUang.text = transaction.amount.toString()
            binding.tvTanggal.text = transaction.date
            binding.tvCatatan.text = transaction.note

            // Use Glide to load the image if imageLocation is a URL or file path
            if (!transaction.imageLocation.isNullOrEmpty()) {
                Glide.with(binding.ivImageLocation.context)
                    .load(transaction.imageLocation)
                    .placeholder(R.drawable.ic_launcher_background) // Optional placeholder
                    .into(binding.ivImageLocation)
            }
        }
    }
}