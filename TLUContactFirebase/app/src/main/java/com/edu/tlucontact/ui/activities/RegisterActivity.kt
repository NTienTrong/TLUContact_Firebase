package com.edu.tlucontact.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.edu.tlucontact.R
import com.edu.tlucontact.databinding.RegisterActivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import java.util.regex.Pattern
import com.edu.tlucontact.data.models.User
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: RegisterActivityBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RegisterActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.registerButton.setOnClickListener {
            validateAndRegister()
        }

        binding.loginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateAndRegister() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()
        val fullName = binding.fullNameEditText.text.toString().trim()

        var isValid = true

        // Validate Email
        if (email.isEmpty()) {
            binding.emailTextInputLayout.error = getString(R.string.email_required_register)
            isValid = false
        } else if (!Pattern.compile("^[a-zA-Z0-9._%+-]+@(tlu\\.edu\\.vn|e\\.tlu\\.edu\\.vn)$").matcher(email).matches()) {
            binding.emailTextInputLayout.error = getString(R.string.invalid_email_domain_register)
            isValid = false
        } else {
            binding.emailTextInputLayout.error = null
        }

        // Validate Password
        if (password.isEmpty()) {
            binding.passwordTextInputLayout.error = getString(R.string.password_required_register)
            isValid = false
        } else if (password.length < 8) {
            binding.passwordTextInputLayout.error = getString(R.string.password_too_short)
            isValid = false
        } else if (!Pattern.compile(".*[A-Z].*").matcher(password).matches()) {
            binding.passwordTextInputLayout.error = getString(R.string.password_no_uppercase)
            isValid = false
        } else if (!Pattern.compile(".*[a-z].*").matcher(password).matches()) {
            binding.passwordTextInputLayout.error = getString(R.string.password_no_lowercase)
            isValid = false
        } else if (!Pattern.compile(".*\\d.*").matcher(password).matches()) {
            binding.passwordTextInputLayout.error = getString(R.string.password_no_number)
            isValid = false
        } else if (!Pattern.compile(".*[@#$%^&+=!].*").matcher(password).matches()) {
            binding.passwordTextInputLayout.error = getString(R.string.password_no_special)
            isValid = false
        } else {
            binding.passwordTextInputLayout.error = null
        }

        // Validate Confirm Password
        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordTextInputLayout.error = getString(R.string.confirm_password_required)
            isValid = false
        } else if (password != confirmPassword) {
            binding.confirmPasswordTextInputLayout.error = getString(R.string.passwords_not_match)
            isValid = false
        } else {
            binding.confirmPasswordTextInputLayout.error = null
        }

        // Validate Full Name
        if (fullName.isEmpty()) {
            binding.fullNameTextInputLayout.error = getString(R.string.full_name_required)
            isValid = false
        } else if (!Pattern.compile("^[\\p{L} .'-]+$").matcher(fullName).matches()) {
            binding.fullNameTextInputLayout.error = getString(R.string.invalid_full_name)
            isValid = false
        } else {
            binding.fullNameTextInputLayout.error = null
        }

        if (isValid) {
            registerUser(email, password, fullName)
        }
    }

    private fun registerUser(email: String, password: String, fullName: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                // Lưu thông tin người dùng vào Firestore với trạng thái chưa xác thực
                                val newUser = User(
                                    uid = user.uid,
                                    email = email,
                                    role = if (email.endsWith("@tlu.edu.vn")) "CBGV" else "Sinh viên", // Xác định vai trò dựa trên email
                                    displayName = fullName,
                                )

                                firestore.collection("users").document(user.uid)
                                    .set(newUser.toMap())
                                    .addOnSuccessListener {
                                        // Hiển thị thông báo xác nhận email
                                        showVerificationDialog()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, R.string.register_failed, Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        binding.emailTextInputLayout.error = getString(R.string.email_already_exists)
                    } else {
                        Toast.makeText(this, R.string.register_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun showVerificationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.verification_email_sent_title))
        builder.setMessage(getString(R.string.verification_email_sent_message))
        builder.setPositiveButton(getString(R.string.go_to_login)) { _, _ ->
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        builder.setCancelable(false)
        builder.show()
    }
}
