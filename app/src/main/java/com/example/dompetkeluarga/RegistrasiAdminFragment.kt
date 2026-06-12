package com.example.dompetkeluarga

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.dompetkeluarga.databinding.FragmentEditProfileUserBinding
import com.example.dompetkeluarga.databinding.FragmentRegistrasiAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegistrasiAdminFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegistrasiAdminFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentRegistrasiAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var editNama: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegistrasiAdminBinding.inflate(inflater, container, false)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firebaseRef = FirebaseDatabase.getInstance().getReference("Users")

        editNama = binding.etNama
        editPassword = binding.etPasswordReg
        editEmail = binding.etEmailReg

        binding.bSimpan.setOnClickListener {
            val nama = editNama.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString().trim()

            // Validasi input
            if (nama.isEmpty()) {
                editNama.error = "Nama tidak boleh kosong"
                editNama.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                editEmail.error = "Email tidak boleh kosong"
                editEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editEmail.error = "Format penulisan Email salah"
                editEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty() || password.length < 5) {
                editPassword.error = "Password tidak boleh kosong dan minimal 5 karakter"
                editPassword.requestFocus()
                return@setOnClickListener
            }

            // Panggil fungsi untuk mendaftarkan pengguna
            registerAdmin(nama, email, password)
        }

        return binding.root
    }

    private fun registerAdmin(nama: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val user = mapOf(
                        "nama" to nama,
                        "email" to email,
                        "role" to "Admin"
                    )

                    userId?.let {
                        firebaseRef.child(it)
                            .setValue(user)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Registrasi berhasil!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    redirectToUserActivity()
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Gagal menyimpan data: ${dbTask.exception?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Registrasi gagal: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun redirectToUserActivity() {
        val intent = Intent(requireContext(), Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}