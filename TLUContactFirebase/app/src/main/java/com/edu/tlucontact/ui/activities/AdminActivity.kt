package com.edu.tlucontact.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.edu.tlucontact.R
import com.edu.tlucontact.databinding.AdminActivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: AdminActivityBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AdminActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupToolbar()
        hideUserInfo()
        setupAdminFunctions()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.logoutImageView.setOnClickListener { showLogoutConfirmationDialog() }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setMessage("Bạn có chắc chắn muốn đăng xuất?")
            .setPositiveButton("Có") { _, _ -> logout() }
            .setNegativeButton("Không") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun logout() {
        auth.signOut()

        val intent = Intent(this@AdminActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun hideUserInfo() {
        binding.userAvatarImageView.visibility = android.view.View.GONE
        binding.userNameTextView.visibility = android.view.View.GONE
        binding.userTypeTextView.visibility = android.view.View.GONE
        binding.userEmailTextView.visibility = android.view.View.GONE
        binding.userIdTextView.visibility = android.view.View.GONE
        binding.userPhoneTextView.visibility = android.view.View.GONE
    }

    private fun setupAdminFunctions() {
        binding.unitsDirectoryButton.setOnClickListener {
            // Chuyển đến màn hình quản lý đơn vị (ví dụ: UnitListActivity)
            val intent = Intent(this, UnitsListActivity::class.java)
            startActivity(intent)
        }

        binding.facultyStaffDirectoryButton.setOnClickListener {
            // Chuyển đến màn hình quản lý CBGV (ví dụ: StaffListActivity)
            val intent = Intent(this, StaffListActivity::class.java)
            startActivity(intent)
        }

        binding.studentDirectoryButton.setOnClickListener {
            // Chuyển đến màn hình quản lý sinh viên (ví dụ: StudentListActivity)
            val intent = Intent(this, StudentsListActivity::class.java)
            startActivity(intent)
        }

        binding.addButton.setOnClickListener {
            // Hiển thị dialog hoặc màn hình thêm mới
        }
    }

//    private fun showAddDialog() {
//        val options = arrayOf("Đơn vị", "CBGV", "Sinh viên")
//
//        AlertDialog.Builder(this)
//            .setTitle("Thêm mới")
//            .setItems(options) { _, which ->
//                when (which) {
//                    0 -> {
//                        // Thêm đơn vị (ví dụ: EditUnitActivity)
//                        val intent = Intent(this, EditUnitActivity::class.java)
//                        startActivity(intent)
//                    }
//                    1 -> {
//                        // Thêm CBGV (ví dụ: EditStaffActivity)
//                        val intent = Intent(this, EditStaffActivity::class.java)
//                        startActivity(intent)
//                    }
//                    2 -> {
//                        // Thêm sinh viên (ví dụ: EditStudentActivity)
//                        val intent = Intent(this, EditStudentActivity::class.java)
//                        startActivity(intent)
//                    }
//                }
//            }
//            .show()
//    }
}