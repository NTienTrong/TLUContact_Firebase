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
import com.edu.tlucontact.databinding.StudentListActivityBinding
import com.edu.tlucontact.ui.adapter.StudentsAdapter
import com.edu.tlucontact.ui.viewmodels.StudentViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class StudentsListActivity : AppCompatActivity() {

    private lateinit var binding: StudentListActivityBinding
    private lateinit var studentsAdapter: StudentsAdapter
    private lateinit var studentViewModel: StudentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StudentListActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        binding.studentsRecyclerView.layoutManager = LinearLayoutManager(this)
        studentsAdapter = StudentsAdapter(mutableListOf()) { studentWithDisplayName ->
            val intent = Intent(this, StudentDetailActivity::class.java)
            intent.putExtra("student", studentWithDisplayName.student)
            intent.putExtra("displayName", studentWithDisplayName.displayName)
            startActivity(intent)
        }
        binding.studentsRecyclerView.adapter = studentsAdapter

        studentViewModel = ViewModelProvider(this)[StudentViewModel::class.java]

        studentViewModel.students.observe(this, Observer { students ->
            studentsAdapter.submitList(students)
        })

        setupSearch()
        setupFilter()
        setupSort()
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                studentViewModel.searchStudents(s.toString())
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
        val allClasses = studentViewModel.allStudents.value?.map { it.student.className }?.distinct()?.toMutableList() ?: mutableListOf()
        allClasses.add(0, "Tất cả") // Thêm tùy chọn "Tất cả"

        val adapter = ArrayAdapter(this, android.R.layout.select_dialog_singlechoice, allClasses.toTypedArray())

        MaterialAlertDialogBuilder(this)
            .setTitle("Lọc theo lớp")
            .setSingleChoiceItems(adapter, -1) { dialog, which ->
                val selectedClass = allClasses[which]
                studentViewModel.filterStudentsByClass(selectedClass.takeIf { it != "Tất cả" }) // Lọc theo lớp hoặc hiển thị tất cả
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
                    0 -> studentViewModel.sortStudentsByName(true)
                    1 -> studentViewModel.sortStudentsByName(false)
                }
            }
            .show()
    }
}