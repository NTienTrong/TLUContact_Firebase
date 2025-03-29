package com.edu.tlucontact.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.edu.tlucontact.MainActivity
import com.edu.tlucontact.R
import com.edu.tlucontact.databinding.LoginActivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginActivityBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.loginButton.setOnClickListener {
            validateAndLogin()
        }

        binding.forgotPasswordTextView.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateAndLogin() {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        var isValid = true

        if (email.isEmpty()) {
            binding.emailTextInputLayout.error = getString(R.string.email_required_login)
            isValid = false
        } else if (!Pattern.compile("^[a-zA-Z0-9._%+-]+@(tlu\\.edu\\.vn|e\\.tlu.edu\\.vn)$").matcher(email).matches()) {
            binding.emailTextInputLayout.error = getString(R.string.invalid_email_domain_login)
            isValid = false
        } else {
            binding.emailTextInputLayout.error = null
        }

        if (password.isEmpty()) {
            binding.passwordTextInputLayout.error = getString(R.string.password_required_login)
            isValid = false
        } else {
            binding.passwordTextInputLayout.error = null
        }

        if (isValid) {
            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        // Hiển thị trạng thái đang xử lý
        binding.loginButton.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.loginButton.isEnabled = true

                if (task.isSuccessful) {
                    val user = auth.currentUser

                    // Kiểm tra email đã được xác thực chưa
                    if (user != null && user.isEmailVerified) {
                        // Cập nhật trạng thái xác thực trong Firestore
                        updateEmailVerificationStatus(user.uid)

                        // Tiếp tục kiểm tra vai trò và chuyển hướng
                        checkUserRoleAndRedirect(user.uid)
                    } else {
                        // Email chưa được xác thực
                        showEmailVerificationAlert(email, password)
                    }
                } else {
                    Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateEmailVerificationStatus(uid: String) {
        // Cập nhật trạng thái xác thực email trong Firestore
        firestore.collection("users").document(uid)
            .update("isEmailVerified", true)
            .addOnFailureListener {
                // Xử lý lỗi nếu cần
            }
    }

    private fun checkUserRoleAndRedirect(uid: String) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    if (role == "admin") {
                        // Chuyển đến AdminActivity
                        startActivity(Intent(this, AdminActivity::class.java))
                        finish()
                    } else {
                        // Chuyển đến MainActivity
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEmailVerificationAlert(email: String, password: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.email_not_verified_title))
        builder.setMessage(getString(R.string.email_not_verified_message))

        builder.setPositiveButton(getString(R.string.resend_verification_email)) { _, _ ->
            resendVerificationEmail(email, password)
        }

        builder.setNegativeButton(getString(R.string.cancel), null)
        builder.show()
    }

    private fun resendVerificationEmail(email: String, password: String) {

        // Đăng nhập lại để gửi email xác thực
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { signInTask ->

                if (signInTask.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    getString(R.string.verification_email_resent),
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(
                                    this,
                                    getString(R.string.verification_email_failed),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.error_resending_email),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
