package com.edu.tlucontact.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.edu.tlucontact.databinding.ActivityUnitsListBinding
import com.edu.tlucontact.ui.adapter.UnitsAdapter
import com.edu.tlucontact.ui.viewmodels.UnitViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class UnitsListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnitsListBinding
    private lateinit var unitAdapter: UnitsAdapter
    private lateinit var unitViewModel: UnitViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnitsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        binding.unitsRecyclerView.layoutManager = LinearLayoutManager(this)
        unitAdapter = UnitsAdapter(mutableListOf()) { unit ->
            val intent = Intent(this, UnitDetailActivity::class.java)
            intent.putExtra("unit", unit)
            startActivity(intent)
        }
        binding.unitsRecyclerView.adapter = unitAdapter

        unitViewModel = ViewModelProvider(this)[UnitViewModel::class.java]

        unitViewModel.units.observe(this, Observer { units ->
            unitAdapter.submitList(units)
        })

        setupSearch()
        setupFilter()
        setupSort()
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                unitViewModel.searchUnits(s.toString())
                binding.clearSearchImageView.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.clearSearchImageView.setOnClickListener {
            binding.searchEditText.text.clear()
        }
    }

    private fun setupFilter() {
        binding.filterButton.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun setupSort() {
        binding.sortButton.setOnClickListener {
            showSortDialog()
        }
    }

    private fun showFilterDialog() {
        val allTypes = mutableListOf("Tất cả", "Khoa", "Phòng") // Thêm "Tất cả"
        val adapter = ArrayAdapter(this, android.R.layout.select_dialog_singlechoice, allTypes)

        MaterialAlertDialogBuilder(this)
            .setTitle("Lọc theo loại đơn vị")
            .setSingleChoiceItems(adapter, -1) { dialog, which ->
                val selectedType = allTypes[which]
                unitViewModel.filterUnitsByType(selectedType.takeIf { it != "Tất cả" }) // Lọc hoặc hiển thị tất cả
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showSortDialog() {
        val options = arrayOf("Tên (A-Z)", "Tên (Z-A)")

        MaterialAlertDialogBuilder(this)
            .setTitle("Sắp xếp theo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> unitViewModel.sortUnitsByName(true)
                    1 -> unitViewModel.sortUnitsByName(false)
                }
            }
            .show()
    }
}