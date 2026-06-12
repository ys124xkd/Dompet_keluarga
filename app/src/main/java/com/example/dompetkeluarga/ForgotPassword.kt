package com.example.dompetkeluarga

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var btnResetPassword: Button
    private lateinit var etEmailForgot: EditText
    private lateinit var btnBackToLogin: Button
    private lateinit var refreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Bind views
        btnResetPassword = findViewById(R.id.btnResetPassword)
        etEmailForgot = findViewById(R.id.etEmailForgot)
        btnBackToLogin = findViewById(R.id.btnBackToLogin)
        refreshLayout = findViewById(R.id.swipe_refresh1)

        // Set up click listener for the "Reset Password" button
        btnResetPassword.setOnClickListener {
            val email = etEmailForgot.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Send password reset email
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password reset email sent. Check your inbox.", Toast.LENGTH_LONG).show()
                        finish() // Close the activity after sending the email
                    } else {
                        val errorMessage = task.exception?.message ?: "Failed to send password reset email. Please try again."
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                        Log.e("ForgotPasswordActivity", "Error sending password reset email", task.exception)
                    }
                }
        }

        // Set up click listener for the "Back to Login" button
        btnBackToLogin.setOnClickListener {
            val intent = Intent(this@ForgotPassword, Login::class.java)
            startActivity(intent)
            finish() // Close the current activity to prevent going back to it
        }
        // Fitur Swipe Refresh
        refreshLayout.setOnRefreshListener {
            // Mengosongkan teks di EditText
            etEmailForgot.text.clear()

            // Menampilkan pesan bahwa halaman telah diperbarui
            Toast.makeText(this, "Halaman diperbarui", Toast.LENGTH_SHORT).show()

            // Menyelesaikan animasi refresh
            refreshLayout.isRefreshing = false
        }
        // Adjust the padding of the main layout based on system window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forget)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}