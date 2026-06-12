package com.example.dompetkeluarga

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.dompetkeluarga.databinding.FragmentUpdateDataTransactionUserBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.util.Calendar
import java.util.Currency
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UpdateFragmentDataTransactionUser.newInstance] factory method to
 * create an instance of this fragment.
 */
class UpdateFragmentDataTransactionUser : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentUpdateDataTransactionUserBinding? = null
    private val binding get() = _binding!!
    private val args: UpdateFragmentDataTransactionUserArgs by navArgs()
    private lateinit var firebaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdateDataTransactionUserBinding.inflate(inflater, container, false)

        // Firebase reference
        firebaseRef = FirebaseDatabase.getInstance().getReference("transactions")

        binding.apply {
            // Set up dropdown for category
            val categories = listOf("Bonus", "Hiburan","Gajian","Tagihan","Lain-lain")
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
            acKategori.setAdapter(adapter)
            val type= listOf("Pendapatan","Pengeluaran")
            val adapter1 = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, type)
            aCPkeluarga.setAdapter(adapter1)

            // Set data from arguments
            aCPkeluarga.setText(args.type,false)
            acKategori.setText(args.category, false) // Prevent dropdown from opening on setText
            edtTanggal.setText(args.date)
            edtJumlahUang.setText(formatCurrency(args.amount.toDoubleOrNull() ?: 0.0))
            edtCatatan.setText(args.note)

            // Update button
            btnUpdate.setOnClickListener {
                ubahData()
                findNavController().navigate(R.id.action_updateFragmentDataTransactionUser_to_homeFragment_user3)
            }
            btnexit.setOnClickListener {
                    findNavController().navigate(R.id.action_updateFragmentDataTransactionUser_to_homeFragment_user3)
            }
            // DatePicker for date input
            edtTanggal.setOnClickListener {
                showDatePicker()
            }
            // Format currency for amount input
            edtJumlahUang.addTextChangedListener(object : TextWatcher {
                private var current = ""

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString() != current) {
                        edtJumlahUang.removeTextChangedListener(this)

                        val cleanString = s.toString().replace("[^\\d]".toRegex(), "")
                        val parsed = cleanString.toDoubleOrNull() ?: 0.0
                        val formatted = formatCurrency(parsed)

                        current = formatted
                        edtJumlahUang.setText(formatted)
                        edtJumlahUang.setSelection(formatted.length)

                        edtJumlahUang.addTextChangedListener(this)
                    }
                }
            })
        }

        return binding.root
    }

    private fun ubahData() {
        val tipeTransaksi=binding.aCPkeluarga.text.toString()
        val kategori = binding.acKategori.text.toString()
        val jumlahUang = binding.edtJumlahUang.text.toString().replace("[^\\d]".toRegex(), "").toDoubleOrNull() ?: 0.0 // Ambil nilai jumlah uang
        val tanggal = binding.edtTanggal.text.toString() // Ambil nilai tanggal


        val catatan = binding.edtCatatan.text.toString() // Ambil nilai catatan

        val data = mapOf(
            "type" to tipeTransaksi,
            "category" to kategori,
            "date" to tanggal,
            "amount" to jumlahUang,
            "note" to catatan
        )

        firebaseRef.child(args.id).updateChildren(data)
    }




    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            // Adjusted the format to "d/M/yyyy"
            val formattedDate = "${selectedDay}/${(selectedMonth + 1)}/${selectedYear}"
            binding.edtTanggal.setText(formattedDate)
        }, year, month, day)

        datePicker.show()
    }

    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance()
        formatter.maximumFractionDigits = 0
        formatter.currency = Currency.getInstance("IDR")
        return formatter.format(amount)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UpdateFragmentDataTransactionUser.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UpdateFragmentDataTransactionUser().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}