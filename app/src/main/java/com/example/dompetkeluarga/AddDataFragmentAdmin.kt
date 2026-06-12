package com.example.dompetkeluarga

import android.app.TimePickerDialog
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
import com.example.dompetkeluarga.databinding.FragmentAddDataAdminBinding
import com.example.dompetkeluarga.databinding.FragmentAddDataBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddDataFragmentAdmin.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddDataFragmentAdmin : Fragment() {
        // TODO: Rename and change types of parameters
        private var _binding: FragmentAddDataAdminBinding? = null
        private val binding get() = _binding!!
        private val database = FirebaseDatabase.getInstance().reference
        private val auth = FirebaseAuth.getInstance()
        private var isDatePickerShown = false

        private val userIdMap = mutableMapOf<String, String>()

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            _binding = FragmentAddDataAdminBinding.inflate(inflater, container, false)
            setupUI()
            binding.btnExit.setOnClickListener {
                findNavController().navigate(R.id.action_addDataFragmentAdmin_to_home_Fragment_Admin)
            }
            return binding.root
        }

        private fun setupUI() {
            setupDropdowns()
            setupCurrencyFormatter()
            setupDatePicker()
            binding.btnSave.setOnClickListener { saveDataToFirebase() }
        }

    private fun setupDropdowns() {
        // Setup kategori dropdown
        val kategoriOptions = listOf("Duit Saku anak", "Gajian")
        val kategoriAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, kategoriOptions)
        binding.tvKategori.setAdapter(kategoriAdapter)

        // Listener untuk kategori dropdown
        binding.tvKategori.setOnItemClickListener { _, _, position, _ ->
            val selectedCategory = kategoriOptions[position]
            if (selectedCategory == "Gajian") {
                binding.tvNamaAnak.isEnabled = false
                binding.tvNamaAnak.setText("")
            } else {
                binding.tvNamaAnak.isEnabled = true
            }
        }

        // Fetch user data for anak dropdown
        fetchAnakDropdownData()
    }

    private fun fetchAnakDropdownData() {
        val anakDropdown = binding.tvNamaAnak

        database.child("Users")
            .get()
            .addOnCompleteListener { task ->
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
                                userIdMap[name] = userId // Simpan userId berdasarkan nama
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
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal memuat data anak: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

        private fun setupDatePicker() {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Pilih Tanggal")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            binding.edtTanggal.setOnClickListener {
                if (!isDatePickerShown) {
                    datePicker.show(childFragmentManager, "DATE_PICKER")
                    isDatePickerShown = true
                }
            }

            datePicker.addOnPositiveButtonClickListener { selection ->
                val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault()) // Format: day/month/year
                binding.edtTanggal.setText(sdf.format(Date(selection)))
                isDatePickerShown = false
            }

            datePicker.addOnDismissListener {
                isDatePickerShown = false
            }
        }

        private fun setupCurrencyFormatter() {
            val editText = binding.edtJumlahUang
            editText.addTextChangedListener(object : TextWatcher {
                private var current = ""

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString() != current) {
                        editText.removeTextChangedListener(this)
                        val cleanString = s.toString().replace("[Rp,.]".toRegex(), "")

                        try {
                            val parsed = cleanString.toDouble()
                            val formatted = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                                maximumFractionDigits = 0
                            }.format(parsed)

                            current = formatted
                            editText.setText(formatted)
                            editText.setSelection(formatted.length)
                        } catch (e: NumberFormatException) {
                            current = ""
                            Toast.makeText(requireContext(), "Input tidak valid", Toast.LENGTH_SHORT).show()
                        }

                        editText.addTextChangedListener(this)
                    }
                }
            })
        }

    private fun saveDataToFirebase() {
        val category = binding.tvKategori.text.toString()
        val nama = binding.tvNamaAnak.text.toString()
        val date = binding.edtTanggal.text.toString()
        val amountString = binding.edtJumlahUang.text.toString()
        val note = binding.edtCatatan.text.toString()
        val userId = userIdMap[nama]

        // Validasi input
        if (category.isEmpty() || date.isEmpty() || amountString.isEmpty()) {
            Toast.makeText(requireContext(), "Harap lengkapi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        // Jika kategori bukan "Gajian", nama harus diisi
        if (category != "Gajian" && userId == null) {
            Toast.makeText(requireContext(), "Harap pilih nama anak", Toast.LENGTH_SHORT).show()
            return
        }

        val cleanAmountString = amountString.replace("[Rp,.]".toRegex(), "")
        val amount = cleanAmountString.toDoubleOrNull()

        if (amount == null) {
            Toast.makeText(requireContext(), "Jumlah uang tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        // Data yang akan disimpan
        val data = mutableMapOf<String, Any>(
            "category" to category,
            "date" to date,
            "amount" to amount,
            "note" to note
        )

        // Hanya tambahkan nama dan userId jika kategori bukan "Gajian"
        if (category != "Gajian") {
            data["nama"] = nama
            data["userId"] = userId!!
        }

        // Simpan ke Firebase
        val transactionRef = database.child("transactions_admin").push()
        data["id"] = transactionRef.key ?: "unknown_id" // Tambahkan ID unik

        transactionRef.setValue(data).addOnSuccessListener {
            Toast.makeText(requireContext(), "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_addDataFragmentAdmin_to_home_Fragment_Admin)
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

        companion object {
            @JvmStatic
            fun newInstance(param1: String, param2: String) =
                AddDataFragmentAdmin().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
        }
    }
