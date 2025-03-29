package com.edu.tlucontact.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.edu.tlucontact.R
import com.edu.tlucontact.databinding.ForgotPasswordActivityBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ForgotPasswordActivityBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ForgotPasswordActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.resetPasswordButton.setOnClickListener {
            validateAndResetPassword()
        }
    }

    private fun validateAndResetPassword() {
        val email = binding.emailEditText.text.toString().trim()

        var isValid = true

        // Validate Email
        if (email.isEmpty()) {
            binding.emailTextInputLayout.error = getString(R.string.email_required)
            isValid = false
        } else if (!Pattern.compile("^[a-zA-Z0-9._%+-]+@(tlu\\.edu\\.vn|e\\.tlu\\.edu\\.vn)$").matcher(email).matches()) {
            binding.emailTextInputLayout.error = getString(R.string.invalid_email_domain_forgot_password)
            isValid = false
        } else {
            binding.emailTextInputLayout.error = null
        }

        if (isValid) {
            resetPassword(email)
        }
    }

    private fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, R.string.reset_password_success, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, R.string.reset_password_failed, Toast.LENGTH_SHORT).show()
                }
            }
    }
}