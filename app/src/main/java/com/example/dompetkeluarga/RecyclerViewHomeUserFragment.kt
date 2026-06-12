package com.example.dompetkeluarga

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dompetkeluarga.adapter.Transaksi_user_adapter
import com.example.dompetkeluarga.databinding.FragmentRecyclerViewHomeUserBinding
import com.example.dompetkeluarga.models.Transaksi_user
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RecyclerViewHomeUserFragment : Fragment() {

    private var _binding: FragmentRecyclerViewHomeUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionUserList: ArrayList<Transaksi_user>
    private lateinit var firebaseRef: DatabaseReference
    private var monthIndex: Int = 0
    private var totalPendapatanUser: Long = 0
    private var totalPengeluaranUser: Long = 0
    private var totalUangSaku: Double = 0.0
    private var userId: String? = null
    private lateinit var adapter: Transaksi_user_adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            monthIndex = it.getInt("monthIndex", -1)
            userId = it.getString("userId")
        }

        Log.d("FragmentArguments", "onCreate - monthIndex: $monthIndex, userId: $userId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecyclerViewHomeUserBinding.inflate(inflater, container, false)
        firebaseRef = FirebaseDatabase.getInstance().getReference("transactions")
        transactionUserList = arrayListOf()

        initializeRecyclerView()

        if (monthIndex == -1 || userId.isNullOrEmpty()) {
            context?.let { ctx ->
                Toast.makeText(ctx, "Invalid month index or user ID", Toast.LENGTH_SHORT).show()
            }
        } else {
            fetchData()
            fetchPendapatanUser()
            fetchPengeluaranUser()
            fetchUangSakuBulanan()
        }

        return binding.root
    }

    private fun initializeRecyclerView() {
        binding.recyclerViewMonth.layoutManager = LinearLayoutManager(context)
        adapter = Transaksi_user_adapter(transactionUserList)
        binding.recyclerViewMonth.adapter = adapter
    }

    private fun parseMonthFromDate(date: String?): Int? {
        return try {
            val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
            val parsedDate = dateFormat.parse(date)
            parsedDate?.let {
                val calendar = Calendar.getInstance()
                calendar.time = it
                calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is zero-based
            }
        } catch (e: Exception) {
            Log.e("MonthFragment", "Error parsing month from date: $date", e)
            null
        }
    }

    private fun fetchData() {
        Log.d("FragmentArguments", "fetchData - monthIndex: $monthIndex, userId: $userId")

        firebaseRef.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    transactionUserList.clear()
                    if (snapshot.exists()) {
                        for (transactionUserSnap in snapshot.children) {
                            val transactionUser = transactionUserSnap.getValue(Transaksi_user::class.java)
                            if (transactionUser != null && parseMonthFromDate(transactionUser.date) == monthIndex + 1) {
                                transactionUserList.add(transactionUser)
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    showError(error.message)
                }
            })
    }

    private fun fetchPendapatanUser() {
        firebaseRef.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalPendapatan: Long = 0
                    if (snapshot.exists()) {
                        for (transactionUserSnap in snapshot.children) {
                            val transactionUser = transactionUserSnap.getValue(Transaksi_user::class.java)
                            if (transactionUser != null && transactionUser.type == "Pendapatan") {
                                val transactionMonth = parseMonthFromDate(transactionUser.date)
                                if (transactionMonth == monthIndex + 1) {
                                    totalPendapatan += transactionUser.amount ?: 0L
                                }
                            }
                        }
                    }
                    totalPendapatanUser = totalPendapatan
                    binding.tvJumlahPendapatan.text = "${totalPendapatan.formatCurrency()}"
                    fetchJumlahSaldo() // Update saldo after fetching pendapatan
                }

                override fun onCancelled(error: DatabaseError) {
                    showError(error.message)
                }
            })
    }

    private fun fetchPengeluaranUser() {
        firebaseRef.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var totalPengeluaran: Long = 0
                    if (snapshot.exists()) {
                        for (transactionUserSnap in snapshot.children) {
                            val transactionUser = transactionUserSnap.getValue(Transaksi_user::class.java)
                            if (transactionUser != null && transactionUser.type == "Pengeluaran") {
                                val transactionMonth = parseMonthFromDate(transactionUser.date)
                                if (transactionMonth == monthIndex + 1) {
                                    totalPengeluaran += transactionUser.amount ?: 0L
                                }
                            }
                        }
                    }
                    totalPengeluaranUser = totalPengeluaran
                    binding.tvJumlahPengeluaran.text = "${totalPengeluaran.formatCurrency()}"
                    fetchJumlahSaldo() // Update saldo after fetching pengeluaran
                }

                override fun onCancelled(error: DatabaseError) {
                    showError(error.message)
                }
            })
    }

    private fun fetchUangSakuBulanan() {
        val adminRef = FirebaseDatabase.getInstance().getReference("transactions_admin")
        adminRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalUangSakuBulanan = 0.0

                for (transactionSnapshot in snapshot.children) {
                    try {
                        val date = transactionSnapshot.child("date").getValue(String::class.java)
                        val amount = transactionSnapshot.child("amount").getValue(Double::class.java)
                        val category = transactionSnapshot.child("category").getValue(String::class.java)

                        if (date != null && amount != null && category == "Duit Saku anak") {
                            val month = parseMonthFromDate(date)
                            if (month == monthIndex + 1) {
                                totalUangSakuBulanan += amount
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MonthFragment", "Error parsing transaction: ${e.message}")
                    }
                }
                totalUangSaku = totalUangSakuBulanan
                binding.tvJumlahUangSaku.text = totalUangSaku.formatCurrency()
                fetchJumlahSaldo() // Update saldo after fetching uang saku
            }

            override fun onCancelled(error: DatabaseError) {
                showError(error.message)
            }
        })
    }

    private fun fetchJumlahSaldo() {
        val saldo = totalPendapatanUser + totalUangSaku - totalPengeluaranUser

        // Format the saldo to "Rp 100.000" style
        val decimalFormatSymbols = DecimalFormatSymbols(Locale("in", "ID")).apply {
            currencySymbol = "Rp"
            groupingSeparator = '.'
        }
        val decimalFormat = DecimalFormat("Rp ###,###", decimalFormatSymbols)
        binding.tvJumlahSaldo.text = decimalFormat.format(saldo)
    }

    private fun showError(message: String) {
        context?.let { ctx ->
            Toast.makeText(ctx, "Error: $message", Toast.LENGTH_LONG).show()
        }
    }

    private fun Long.formatCurrency(): String {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return numberFormat.format(this).replace("Rp", "Rp ").replace(",", ".")
    }

    private fun Double.formatCurrency(): String {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return numberFormat.format(this).replace("Rp", "Rp ").replace(",", ".")
    }

    companion object {
        fun newInstance(monthIndex: Int, userId: String): RecyclerViewHomeUserFragment {
            return RecyclerViewHomeUserFragment().apply {
                arguments = Bundle().apply {
                    putInt("monthIndex", monthIndex)
                    putString("userId", userId)
                }
            }
        }
    }
}