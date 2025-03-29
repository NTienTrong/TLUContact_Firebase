package com.edu.tlucontact.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.edu.tlucontact.R
import com.edu.tlucontact.data.models.Unit
import com.edu.tlucontact.databinding.UnitDetailActivityBinding


class UnitDetailActivity : AppCompatActivity() {

    private lateinit var binding: UnitDetailActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UnitDetailActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        val unit = intent.getParcelableExtra<Unit>("unit")

        if (unit != null) {
            binding.unitCodeTextView.text = "Mã đơn vị: ${unit.code}"
            binding.unitNameTextView.text = "Tên đơn vị: ${unit.name}"
            binding.unitTypeTextView.text = "Loại đơn vị: ${unit.type}"
            binding.unitEmailTextView.text = "Email: ${unit.email}"
            binding.unitPhoneTextView.text = "Điện thoại: ${unit.phone}"
            binding.unitAddressTextView.text = "Địa chỉ: ${unit.address}"
            binding.unitFaxTextView.text = "Fax: ${unit.fax}"

            Glide.with(this)
                .load(unit.logoURL)
                .placeholder(R.drawable.ic_default_unit_logo)
                .error(R.drawable.ic_default_unit_logo)
                .into(binding.unitLogoImageView)

            binding.callButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${unit.phone}"))
                startActivity(intent)
            }

            binding.emailButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${unit.email}"))
                startActivity(intent)
            }

            binding.messageButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${unit.phone}"))
                startActivity(intent)
            }
        }
    }
}