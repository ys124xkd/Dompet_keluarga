package com.example.dompetkeluarga

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.dompetkeluarga.notifications.DailyReminderWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class Login : AppCompatActivity() {
    private lateinit var btnRegister: Button
    private lateinit var btnLogin: Button
    private lateinit var txtEmail: EditText
    private lateinit var txtPassword: EditText
    private lateinit var otentikasi: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var txtetPasswordForget: TextView
    private lateinit var refreshLayout: SwipeRefreshLayout



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        scheduleDailyReminder()
        otentikasi = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        txtEmail = findViewById(R.id.etEmail)
        txtPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.bLogin)
        btnRegister = findViewById(R.id.bRegister)
        txtetPasswordForget = findViewById(R.id.vForgotPassword)
        refreshLayout = findViewById(R.id.swipe_refresh)
        txtetPasswordForget.setOnClickListener {
            val intent = Intent(this@Login, ForgotPassword::class.java)
            startActivity(intent)
            finish()
        }
        btnRegister.setOnClickListener {
            val intent = Intent(this@Login, Register::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val email = txtEmail.text.toString().trim()
            val password = txtPassword.text.toString().trim()

            // Validasi input
            if (email.isEmpty()) {
                txtEmail.error = "Email tidak boleh kosong"
                txtEmail.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty() || password.length < 5) {
                txtPassword.error = "Password tidak boleh kosong dan minimal 5 karakter"
                txtPassword.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                txtEmail.error = "Format penulisan email salah"
                txtEmail.requestFocus()
                return@setOnClickListener
            }

            loginUser(email, password)
        }
        // Fitur Swipe Refresh
        refreshLayout.setOnRefreshListener {
            // Mengosongkan teks di EditText
            txtEmail.text.clear()
            txtPassword.text.clear()

            // Menampilkan pesan bahwa halaman telah diperbarui
            Toast.makeText(this, "Halaman diperbarui", Toast.LENGTH_SHORT).show()

            // Menyelesaikan animasi refresh
            refreshLayout.isRefreshing = false
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.Login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun loginUser(email: String, password: String) {
        otentikasi.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("LoginActivity", "Login successful")
                    val currentUser = otentikasi.currentUser
                    currentUser?.let {
                        val userId = it.uid
                        Log.d("LoginActivity", "Fetching role for user ID: $userId")
                        database.child(userId).child("role").get()
                            .addOnSuccessListener { dataSnapshot ->
                                val role = dataSnapshot.value?.toString()
                                if (role.isNullOrEmpty()) {
                                    Log.w("LoginActivity", "Role not found for user ID: $userId")
                                    Toast.makeText(this, "Role pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                                } else {
                                    Log.d("LoginActivity", "Role found: $role")
                                    navigateToRoleBasedActivity(role)
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("LoginActivity", "Error getting role", exception)
                                Toast.makeText(this, "Gagal mendapatkan data role pengguna", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Terjadi kesalahan saat login"
                    Log.e("LoginActivity", "Login failed: $errorMessage")
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun scheduleDailyReminder() {
        val constraints = Constraints.Builder()
            .setRequiresCharging(true) // Opsional, hanya berjalan saat pengisian
            .build()

        // Jadwal pekerjaan untuk berjalan setiap 24 jam
        val dailyWorkRequest = PeriodicWorkRequest.Builder(DailyReminderWorker::class.java, 1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(dailyWorkRequest)
    }

    private fun navigateToRoleBasedActivity(role: String) {
        when (role) {
            "Admin" -> {
                Intent(this@Login, Admin::class.java).also { intent ->
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
            "User" -> {
                Intent(this@Login, User::class.java).also { intent ->
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
            else -> {
                Toast.makeText(this, "Role tidak valid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }
}