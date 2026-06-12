package com.example.dompetkeluarga

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.dompetkeluarga.databinding.FragmentLaporanPendapatanAdminBinding
import com.example.dompetkeluarga.databinding.FragmentUpdateDataTransactionUserBinding
import com.example.dompetkeluarga.databinding.FragmentUpdatePendapatanAdminBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.util.Calendar
import java.util.Currency

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UpdatePendapatanAdminFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UpdatePendapatanAdminFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentUpdatePendapatanAdminBinding? = null
    private val binding get() = _binding!!
    private val args: UpdatePendapatanAdminFragmentArgs by navArgs()
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
        _binding = FragmentUpdatePendapatanAdminBinding.inflate(inflater, container, false)

        // Firebase reference
        firebaseRef = FirebaseDatabase.getInstance().getReference("transactions_admin")
        binding.apply {
            edtTanggal.setText(args.date)
            edtJumlahUang.setText(formatCurrency(args.amount.toDoubleOrNull() ?: 0.0))
            edtCatatan.setText(args.note)

            btnUpdate.setOnClickListener {
                ubahData()
                // Navigate to LaporanPendapatanFragmentAdmin and pop the current fragment off the stack
                findNavController().navigate(R.id.action_updatePendapatanAdminFragment_to_laporanPendapatanFragmentAdmin2)
                findNavController().popBackStack(R.id.updatePendapatanAdminFragment, true) // Pops the current fragment off the stack
            }

            btnexit.setOnClickListener {
                // Navigate to LaporanPendapatanFragmentAdmin and pop the current fragment off the stack
                findNavController().navigate(R.id.action_updatePendapatanAdminFragment_to_laporanPendapatanFragmentAdmin2)
                findNavController().popBackStack(R.id.updatePendapatanAdminFragment, true) // Pops the current fragment off the stack
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
        val tanggal = binding.edtTanggal.text.toString() // Ambil nilai tanggal
        val jumlahUang = binding.edtJumlahUang.text.toString().replace("[^\\d]".toRegex(), "").toDoubleOrNull() ?: 0.0 // Ambil nilai jumlah uang
        val catatan=binding.edtCatatan.text.toString()

        val data = mapOf(
            "date" to tanggal,
            "amount" to jumlahUang,
            "note" to catatan
        )

        firebaseRef.child(args.id).updateChildren(data)
    }


    private fun formatCurrency(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance()
        formatter.maximumFractionDigits = 0
        formatter.currency = Currency.getInstance("IDR")
        return formatter.format(amount)
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
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UpdatePendapatanAdminFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UpdatePendapatanAdminFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}