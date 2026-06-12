package com.example.dompetkeluarga

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dompetkeluarga.adapter.TransactionAdapter
import com.example.dompetkeluarga.databinding.FragmentRecylerviewRiwayatLaporanKeuanganAnakBinding
import com.example.dompetkeluarga.models.Transaction
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RecylerViewRiwayatLaporanKeuanganAnakFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecylerViewRiwayatLaporanKeuanganAnakFragment : Fragment() {
    private var _binding: FragmentRecylerviewRiwayatLaporanKeuanganAnakBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionsList: ArrayList<Transaction>
    private lateinit var firebaseRef: DatabaseReference
    private var monthIndex: Int = 0 // Default month index, can be updated via arguments
    private var userId: String? = null // Variable to hold the userId

    private var totalUangSaku: Double = 0.0
    private var totalPendapatan: Double = 0.0
    private var totalPengeluaran: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecylerviewRiwayatLaporanKeuanganAnakBinding.inflate(inflater, container, false)
        firebaseRef = FirebaseDatabase.getInstance().getReference("transactions")
        transactionsList = arrayListOf() // Initializing the list

        // Get monthIndex and userId from arguments
        arguments?.let {
            monthIndex = it.getInt("monthIndex", 0)
            userId = it.getString("userId")
        }

        binding.recyclerViewMonth.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this.context)
        }

        // Fetch data using the provided userId
        userId?.let {
            fetchDataByUserId(it)
            fetchUangSaku()
            fetchPendapatanBulanan()
            fetchPengeluaranBulanan()
        }

        return binding.root
    }

    private fun fetchJumlahSaldo() {
        val saldo = totalPendapatan + totalUangSaku - totalPengeluaran

        // Format the saldo to "Rp 100.000" style
        val decimalFormatSymbols = DecimalFormatSymbols(Locale("in", "ID")).apply {
            currencySymbol = "Rp"
            groupingSeparator = '.'
        }
        val decimalFormat = DecimalFormat("Rp ###,###", decimalFormatSymbols)
        binding.tvJumlahSaldo.text = decimalFormat.format(saldo)
    }

    private fun fetchUangSaku() {
        val adminRef = FirebaseDatabase.getInstance().getReference("transactions_admin")

        adminRef.orderByChild("userId").equalTo(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalUangSaku = 0.0 // Reset before summing

                if (snapshot.exists()) {
                    for (transactionSnap in snapshot.children) {
                        val transaction = transactionSnap.getValue(Transaction::class.java)
                        transaction?.let {
                            val transactionDate = it.date // Assuming date is a String like "8/12/2024"
                            val transactionMonth = parseMonthFromDate(transactionDate)

                            // Log for debugging
                            Log.d("MonthFragment", "Transaction Date: $transactionDate, Parsed Month: $transactionMonth, Selected Month Index: $monthIndex")

                            if (transactionMonth != null && transactionMonth == monthIndex + 1) {
                                if (it.category == "Duit Saku anak") {
                                    totalUangSaku += it.amount?.toDouble() ?: 0.0
                                }
                            }
                        }
                    }
                }
                val decimalFormatSymbols = DecimalFormatSymbols(Locale("in", "ID")).apply {
                    currencySymbol = "Rp"
                    groupingSeparator = '.'
                }
                val decimalFormat = DecimalFormat("Rp ###,###", decimalFormatSymbols)
                binding.tvJumlahUangSaku.text = decimalFormat.format(totalUangSaku)
                fetchJumlahSaldo()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                Log.e("MonthFragment", "Database error: $error")
            }
        })
    }

    private fun fetchPengeluaranBulanan() {
        firebaseRef.orderByChild("userId").equalTo(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalPengeluaran = 0.0 // Reset before summing

                if (snapshot.exists()) {
                    for (transactionSnap in snapshot.children) {
                        val transaction = transactionSnap.getValue(Transaction::class.java)
                        transaction?.let {
                            val transactionDate = it.date // Assuming date is a String like "8/12/2024"
                            val transactionMonth = parseMonthFromDate(transactionDate)

                            if (transactionMonth != null && transactionMonth == monthIndex + 1) {
                                if (it.type == "Pengeluaran") {
                                    totalPengeluaran += it.amount?.toDouble() ?: 0.0
                                }
                            }
                        }
                    }
                }
                val decimalFormatSymbols = DecimalFormatSymbols(Locale("in", "ID")).apply {
                    currencySymbol = "Rp"
                    groupingSeparator = '.'
                }
                val decimalFormat = DecimalFormat("Rp ###,###", decimalFormatSymbols)
                binding.tvJumlahPengeluaran.text = decimalFormat.format(totalPengeluaran)
                fetchJumlahSaldo()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                Log.e("MonthFragment", "Database error: $error")
            }
        })
    }

    private fun fetchPendapatanBulanan() {
        firebaseRef.orderByChild("userId").equalTo(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                totalPendapatan = 0.0 // Reset before summing

                if (snapshot.exists()) {
                    for (transactionSnap in snapshot.children) {
                        val transaction = transactionSnap.getValue(Transaction::class.java)
                        transaction?.let {
                            val transactionDate = it.date // Assuming date is a String like "8/12/2024"
                            val transactionMonth = parseMonthFromDate(transactionDate)

                            if (transactionMonth != null && transactionMonth == monthIndex + 1) {
                                if (it.type == "Pendapatan") {
                                    totalPendapatan += it.amount?.toDouble() ?: 0.0
                                }
                            }
                        }
                    }
                }
                val decimalFormatSymbols = DecimalFormatSymbols(Locale("in", "ID")).apply {
                    currencySymbol = "Rp"
                    groupingSeparator = '.'
                }
                val decimalFormat = DecimalFormat("Rp ###,###", decimalFormatSymbols)
                binding.tvJumlahPendapatan.text = decimalFormat.format(totalPendapatan)
                fetchJumlahSaldo()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                Log.e("MonthFragment", "Database error: $error")
            }
        })
    }

    private fun fetchDataByUserId(userId: String) {
        firebaseRef.orderByChild("userId").equalTo(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                transactionsList.clear()
                if (snapshot.exists()) {
                    for (transactionSnap in snapshot.children) {
                        val transaction = transactionSnap.getValue(Transaction::class.java)
                        transaction?.let {
                            val transactionDate = it.date // Assuming date is a String like "8/12/2024"
                            val transactionMonth = parseMonthFromDate(transactionDate)

                            if (transactionMonth != null && transactionMonth == monthIndex + 1) {
                                transactionsList.add(it)
                            }
                        }
                    }
                }
                val transactionAdapter = TransactionAdapter(transactionsList)
                binding.recyclerViewMonth.adapter = transactionAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
            }
        })
    }

    // Helper function to parse the month from date string in "d/M/yyyy" format
    private fun parseMonthFromDate(date: String?): Int? {
        return try {
            val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
            val parsedDate = dateFormat.parse(date)
            parsedDate?.let {
                val calendar = Calendar.getInstance()
                calendar.time = it
                calendar.get(Calendar.MONTH) + 1 // Adding 1 because Calendar.MONTH is zero-based
            }
        } catch (e: Exception) {
            Log.e("MonthFragment", "Error parsing month from date: $date", e)
            null
        }
    }

    companion object {
        fun newInstance(monthIndex: Int, userId: String): RecylerViewRiwayatLaporanKeuanganAnakFragment {
            val fragment = RecylerViewRiwayatLaporanKeuanganAnakFragment()
            val bundle = Bundle()
            bundle.putInt("monthIndex", monthIndex) // Store the month index
            bundle.putString("userId", userId) // Store the userId
            fragment.arguments = bundle
            return fragment
        }
    }
}