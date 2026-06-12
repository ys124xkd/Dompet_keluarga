package com.example.dompetkeluarga

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.dompetkeluarga.databinding.FragmentUpdatePendapatanAdminBinding
import com.example.dompetkeluarga.databinding.FragmentUpdateUangSakuAnakBinding
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
 * Use the [UpdateUangSakuAnakFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UpdateUangSakuAnakFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentUpdateUangSakuAnakBinding? = null
    private val binding get() = _binding!!
    private val args: UpdateUangSakuAnakFragmentArgs by navArgs()
    private lateinit var firebaseRef: DatabaseReference
    private lateinit var userDatabaseRef: DatabaseReference
    private val userIdMap = mutableMapOf<String, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdateUangSakuAnakBinding.inflate(inflater, container, false)

        // Initialize Firebase references
        firebaseRef = FirebaseDatabase.getInstance().getReference("transactions_admin")
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("Users")

        setupUI()
        fetchAnakDropdownData()

        return binding.root
    }

    private fun setupUI() {
        binding.apply {
            edtNamaAnak.setText(args.nama)
            edtTanggal.setText(args.date)
            edtJumlahUang.setText(formatCurrency(args.amount.toDoubleOrNull() ?: 0.0))
            edtCatatan.setText(args.note)

            btnUpdate.setOnClickListener {
                ubahData()
                // Navigate to LaporanPendapatanFragmentAdmin and pop the current fragment off the stack
                findNavController().navigate(R.id.action_updateUangSakuAnakFragment_to_laporanUangSakuAnakFragmentAdmin)
                findNavController().popBackStack(R.id.updateUangSakuAnakFragment, true) // Pops the current fragment off the stack
            }

            btnExit.setOnClickListener {
                // Navigate to LaporanPendapatanFragmentAdmin and pop the current fragment off the stack
                findNavController().navigate(R.id.action_updateUangSakuAnakFragment_to_laporanUangSakuAnakFragmentAdmin)
                findNavController().popBackStack(R.id.updateUangSakuAnakFragment, true) // Pops the current fragment off the stack
            }

            edtTanggal.setOnClickListener {
                showDatePicker()
            }

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
    }

    private fun fetchAnakDropdownData() {
        val anakDropdown = binding.edtNamaAnak

        userDatabaseRef.get().addOnCompleteListener { task ->
            if (isAdded) { // Check if fragment is attached
                if (task.isSuccessful) {
                    val snapshot = task.result
                    if (snapshot != null && snapshot.exists()) {
                        val anakList = mutableListOf<String>()
                        snapshot.children.forEach { child ->
                            val role = child.child("role").getValue(String::class.java)
                            val name = child.child("nama").getValue(String::class.java)
                            val userId = child.key

                            if (role == "User" && name != null && userId != null) {
                                anakList.add(name)
                                userIdMap[name] = userId
                            }
                        }

                        if (anakList.isNotEmpty()) {
                            val anakAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, anakList)
                            anakDropdown.setAdapter(anakAdapter)
                        } else {
                            Toast.makeText(requireContext(), "Tidak ada data anak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Snapshot kosong", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat data anak: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {
            if (isAdded) {
                Toast.makeText(requireContext(), "Gagal memuat data anak: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun ubahData() {
        if (isAdded) { // Check if fragment is attached
            val nama = binding.edtNamaAnak.text.toString()
            val tanggal = binding.edtTanggal.text.toString()
            val jumlahUang = binding.edtJumlahUang.text.toString().replace("[^\\d]".toRegex(), "").toDoubleOrNull() ?: 0.0
            val catatan = binding.edtCatatan.text.toString()

            val userId = userIdMap[nama]
            if (userId != null) {
                val data = mapOf(
                    "nama" to nama,
                    "date" to tanggal,
                    "amount" to jumlahUang,
                    "note" to catatan
                )

                firebaseRef.child(args.id).updateChildren(data).addOnSuccessListener {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Gagal memperbarui data: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Nama anak tidak valid", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
            val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            binding.edtTanggal.setText(formattedDate)
        }, year, month, day)

        datePicker.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
