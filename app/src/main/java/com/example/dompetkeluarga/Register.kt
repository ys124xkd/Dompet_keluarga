package com.example.dompetkeluarga

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Register : AppCompatActivity() {
    private lateinit var btnLogin: Button
    private lateinit var btnSimpan: Button
    private lateinit var txtNama: EditText
    private lateinit var txtEmail: EditText
    private lateinit var txtPassword: EditText
    private lateinit var txtConfirmPassword: EditText
    private lateinit var otentikasi: FirebaseAuth
    private lateinit var refreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        otentikasi = FirebaseAuth.getInstance()
        btnSimpan = findViewById(R.id.bSimpan)
        btnLogin = findViewById(R.id.bLogin)
        txtNama = findViewById(R.id.etNama)
        txtEmail = findViewById(R.id.etEmailReg)
        txtPassword = findViewById(R.id.etPasswordReg)
        txtConfirmPassword = findViewById(R.id.etconfirmPassword)
        refreshLayout = findViewById(R.id.swipe_refresh)

        btnSimpan.setOnClickListener {
            val nama = txtNama.text.toString().trim()
            val email = txtEmail.text.toString().trim()
            val password = txtPassword.text.toString().trim()
            val confirmPassword = txtConfirmPassword.text.toString().trim()

            // Validasi input
            if (nama.isEmpty()) {
                txtNama.error = "Nama tidak boleh kosong"
                txtNama.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                txtEmail.error = "Email tidak boleh kosong"
                txtEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                txtEmail.error = "Format penulisan Email salah"
                txtEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty() || password.length < 5) {
                txtPassword.error = "Password tidak boleh kosong dan minimal 5 karakter"
                txtPassword.requestFocus()
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty() || confirmPassword != password) {
                txtConfirmPassword.error = "Password dan konfirmasi password tidak cocok"
                txtConfirmPassword.requestFocus()
                return@setOnClickListener
            }

            // Panggil fungsi untuk mendaftarkan pengguna
            registerUser(nama, email, password)
        }

        btnLogin.setOnClickListener {
            Intent(this@Register, Login::class.java).also {
                startActivity(it)
            }
        }

        // Fitur Swipe Refresh
        refreshLayout.setOnRefreshListener {
            txtEmail.text.clear()
            txtPassword.text.clear()
            txtConfirmPassword.text.clear()
            txtNama.text.clear()

            Toast.makeText(this, "Halaman diperbarui", Toast.LENGTH_SHORT).show()
            refreshLayout.isRefreshing = false
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.Register)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun registerUser(nama: String, email: String, password: String) {
        otentikasi.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = otentikasi.currentUser?.uid
                    val user = mapOf(
                        "nama" to nama,
                        "email" to email,
                        "role" to "User" // Default role is "User"
                    )

                    userId?.let {
                        FirebaseDatabase.getInstance().getReference("Users")
                            .child(it)
                            .setValue(user)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    Intent(this@Register, User::class.java).also { intent ->
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                    }
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Gagal menyimpan data: ${dbTask.exception?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Registrasi gagal: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}