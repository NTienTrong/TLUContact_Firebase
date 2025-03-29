package com.edu.tlucontact.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.edu.tlucontact.R
import com.edu.tlucontact.data.models.Student
import com.edu.tlucontact.databinding.StudentDetailActivityBinding

class StudentDetailActivity : AppCompatActivity() {

    private lateinit var binding: StudentDetailActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StudentDetailActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        val student = intent.getParcelableExtra<Student>("student")

        if (student != null) {
            binding.studentIdTextView.text = "Mã sinh viên: ${student.studentId}"
            binding.studentNameTextView.text = "Họ tên: ${student.fullName}"
            binding.studentClassTextView.text = "Lớp: ${student.className}"
            binding.studentEmailTextView.text = "Email: ${student.email}"
            binding.studentPhoneTextView.text = "Điện thoại: ${student.phone}"
            binding.studentAddressTextView.text = "Địa chỉ: ${student.address}"

            Glide.with(binding.studentPhotoImageView.context)
                .load(student.photoURL)
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .into(binding.studentPhotoImageView)

            binding.callButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${student.phone}"))
                startActivity(intent)
            }

            binding.emailButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${student.email}"))
                startActivity(intent)
            }

            binding.messageButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${student.phone}"))
                startActivity(intent)
            }
        }
    }
}