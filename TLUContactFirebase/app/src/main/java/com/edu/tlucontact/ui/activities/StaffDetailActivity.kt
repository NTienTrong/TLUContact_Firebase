package com.edu.tlucontact.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.edu.tlucontact.R
import com.edu.tlucontact.data.models.Staff
import com.edu.tlucontact.data.models.Unit
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class StaffDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.staff_detail_activity)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val staffId = intent.getStringExtra("staffId")
        val fullName = intent.getStringExtra("fullName")
        val position = intent.getStringExtra("position")
        val phone = intent.getStringExtra("phone")
        val email = intent.getStringExtra("email")
        val photoURL = intent.getStringExtra("photoURL")
        val unitPath = intent.getStringExtra("unitPath")
        val userId = intent.getStringExtra("userId")

        val unitRef = if (unitPath != null) {
            FirebaseFirestore.getInstance().document(unitPath)
        } else {
            null
        }

        val staff = Staff(staffId ?: "", fullName ?: "", position ?: "", phone, email, photoURL, unitRef, userId)

        val staffIdTextView = findViewById<TextView>(R.id.staffIdTextView)
        val staffNameTextView = findViewById<TextView>(R.id.staffNameTextView)
        val staffPositionTextView = findViewById<TextView>(R.id.staffPositionTextView)
        val staffUnitTextView = findViewById<TextView>(R.id.staffUnitTextView)
        val staffEmailTextView = findViewById<TextView>(R.id.staffEmailTextView)
        val staffPhoneTextView = findViewById<TextView>(R.id.staffPhoneTextView)
        val staffPhotoImageView = findViewById<ImageView>(R.id.staffPhotoImageView)

        staffIdTextView.text = "Mã CBGV: ${staff.staffId}"
        staffNameTextView.text = staff.fullName
        staffPositionTextView.text = staff.position
        staffEmailTextView.text = staff.email ?: "Không có email"
        staffPhoneTextView.text = staff.phone ?: "Không có số điện thoại"

        // Lấy tên đơn vị từ DocumentReference
        if (staff.unit != null) {
            staff.unit.get()
                .addOnSuccessListener { unitDocument ->
                    val unit = Unit.fromMap(unitDocument.data)
                    staffUnitTextView.text = unit?.name ?: "Chưa rõ đơn vị"
                }
                .addOnFailureListener {
                    staffUnitTextView.text = "Chưa rõ đơn vị"
                }
        } else {
            staffUnitTextView.text = "Chưa rõ đơn vị"
        }

        Glide.with(this)
            .load(staff.photoURL)
            .placeholder(R.drawable.ic_default_avatar)
            .error(R.drawable.ic_default_avatar)
            .into(staffPhotoImageView)

        val callButton = findViewById<MaterialButton>(R.id.callButton)
        val emailButton = findViewById<MaterialButton>(R.id.emailButton)
        val messageButton = findViewById<MaterialButton>(R.id.messageButton)

        callButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${staff.phone}"))
            startActivity(intent)
        }

        emailButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${staff.email}"))
            startActivity(intent)
        }

        messageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${staff.phone}"))
            startActivity(intent)
        }
    }
}