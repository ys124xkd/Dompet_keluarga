package com.example.dompetkeluarga

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.dompetkeluarga.databinding.FragmentRecylerHomeAdminBinding
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

class RecylerHomeAdminFragment : Fragment() {
    private var _binding: FragmentRecylerHomeAdminBinding? = null
    private val binding get() = _binding!!

    // Variabel global untuk menyimpan total pendapatan dan uang saku
    private var totalPendapatan = 0.0
    private var totalUangSaku = 0.0
    private var monthIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            monthIndex = it.getInt("monthIndex", 0) // Default value is 0 for January
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecylerHomeAdminBinding.inflate(inflater, container, false)
        fetchPendapatanBulanan()
        fetchUangSakuBulananAnak()
        return binding.root
    }

    private fun fetchPendapatanBulanan() {
        val adminRef = FirebaseDatabase.getInstance().getReference("transactions_admin")
        adminRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalPendapatanBulanan = 0.0

                for (transactionSnapshot in snapshot.children) {
                    try {
                        val date = transactionSnapshot.child("date").getValue(String::class.java)
                        val amount = transactionSnapshot.child("amount").getValue(Double::class.java)
                        val category = transactionSnapshot.child("category").getValue(String::class.java)

                        if (date != null && amount != null) {
                            val month = parseMonthFromDate(date)
                            Log.d("MonthFragment", "Retrieved date: $date, Parsed month: $month, Target monthIndex: $monthIndex")

                            if (month == monthIndex + 1) { // Correct comparison for 1-based month
                                if (category == "Gajian") {
                                    totalPendapatanBulanan += amount
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MonthFragment", "Error parsing transaction: ${e.message}")
                    }
                }
                totalPendapatan = totalPendapatanBulanan
                Log.d("MonthFragment", "Total pendapatan for monthIndex $monthIndex: $totalPendapatan")
                binding.tvPendapatan.text = "${totalPendapatan.formatCurrency()}"
                fetchJumlahSaldo() // Call after fetching pendapatan
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error loading data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchUangSakuBulananAnak() {
        val adminRef = FirebaseDatabase.getInstance().getReference("transactions_admin")
        adminRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalUangSakuBulanan = 0.0

                for (transactionSnapshot in snapshot.children) {
                    try {
                        val date = transactionSnapshot.child("date").getValue(String::class.java)
                        val amount = transactionSnapshot.child("amount").getValue(Double::class.java)
                        val category = transactionSnapshot.child("category").getValue(String::class.java)

                        if (date != null && amount != null) {
                            val month = parseMonthFromDate(date)
                            Log.d("MonthFragment", "Retrieved date: $date, Parsed month: $month, Target monthIndex: $monthIndex")

                            if (month == monthIndex + 1) { // Correct comparison for 1-based month
                                if (category == "Duit Saku anak") {
                                    totalUangSakuBulanan += amount
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("MonthFragment", "Error parsing transaction: ${e.message}")
                    }
                }
                totalUangSaku = totalUangSakuBulanan
                Log.d("MonthFragment", "Total uang saku for monthIndex $monthIndex: $totalUangSaku")
                binding.jumlahUangSakuAnak.text = "${totalUangSaku.formatCurrency()}"
                fetchJumlahSaldo() // Call after fetching uang saku
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error loading data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchJumlahSaldo() {
        val saldo = totalPendapatan - totalUangSaku

        // Format the saldo to "Rp 100.000" style
        val decimalFormatSymbols = DecimalFormatSymbols(Locale("in", "ID")).apply {
            currencySymbol = "Rp"
            groupingSeparator = '.'
        }
        val decimalFormat = DecimalFormat("Rp ###,###", decimalFormatSymbols)
        binding.tvSaldo.text = decimalFormat.format(saldo)
    }

    private fun Double.formatCurrency(): String {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return numberFormat.format(this).replace("Rp", "Rp ").replace(",", ".")
    }

    private fun parseMonthFromDate(date: String?): Int? {
        return try {
            val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault()) // Adjust format as needed
            val parsedDate = dateFormat.parse(date)
            parsedDate?.let {
                val calendar = Calendar.getInstance()
                calendar.time = it
                val month = calendar.get(Calendar.MONTH) + 1 // Convert zero-based to one-based
                Log.d("MonthFragment", "Parsed date: $parsedDate, Month: $month")
                month
            }
        } catch (e: Exception) {
            Log.e("MonthFragment", "Error parsing month from date: $date", e)
            null
        }
    }

    companion object {
        fun newInstance(monthIndex: Int): RecylerHomeAdminFragment {
            val fragment = RecylerHomeAdminFragment()
            val bundle = Bundle()
            bundle.putInt("monthIndex", monthIndex) // Pass as integer, not string
            fragment.arguments = bundle
            return fragment
        }
    }
}