package com.edu.tlucontact.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.edu.tlucontact.data.models.StudentWithDisplayName
import com.edu.tlucontact.data.repositories.StudentRepository

class StudentViewModel : ViewModel() {

    private val repository = StudentRepository()
    private val _allStudents = MutableLiveData<List<StudentWithDisplayName>>()
    private val _filteredStudents = MutableLiveData<List<StudentWithDisplayName>>()
    val students: LiveData<List<StudentWithDisplayName>> = _filteredStudents
    val allStudents: LiveData<List<StudentWithDisplayName>> = _allStudents

    private var currentFilter: String? = null
    private var currentSearchQuery: String? = null
    private var currentSortAscending: Boolean = true

    init {
        loadStudents()
    }

    private fun loadStudents() {
        repository.getStudentsWithDisplayName().observeForever { students ->
            _allStudents.value = students
            applyFilters()
        }
    }

    fun filterStudentsByClass(className: String?) {
        currentFilter = className
        applyFilters()
    }

    fun searchStudents(query: String?) {
        currentSearchQuery = query
        applyFilters()
    }

    fun sortStudentsByName(ascending: Boolean) {
        currentSortAscending = ascending
        applyFilters()
    }

    private fun applyFilters() {
        var filteredList = _allStudents.value ?: emptyList()

        // Lọc theo lớp
        val filter = currentFilter
        if (filter != null && filter.isNotEmpty()) {
            filteredList = filteredList.filter { it.student.className.equals(filter, ignoreCase = true) }
        }

        // Tìm kiếm
        val searchQuery = currentSearchQuery
        if (searchQuery != null && searchQuery.isNotEmpty()) {
            filteredList = filteredList.filter { it.student.fullName.contains(searchQuery, ignoreCase = true) }
        }

        // Sắp xếp
        filteredList = if (currentSortAscending) {
            filteredList.sortedBy { it.displayName }
        } else {
            filteredList.sortedByDescending { it.displayName }
        }

        _filteredStudents.value = filteredList
    }
}