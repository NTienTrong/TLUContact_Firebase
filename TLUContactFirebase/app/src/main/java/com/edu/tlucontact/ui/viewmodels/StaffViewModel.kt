package com.edu.tlucontact.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edu.tlucontact.data.models.Staff
import com.edu.tlucontact.data.repositories.StaffRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StaffViewModel : ViewModel() {

    private val repository = StaffRepository()

    // LiveData cho danh sách CBGV
    private val _staffList = MutableLiveData<List<Staff>>()
    val staffList: LiveData<List<Staff>> = _staffList

    // LiveData cho danh sách đơn vị
    private val _unitList = MutableLiveData<List<String>>()
    val unitList: LiveData<List<String>> = _unitList

    // LiveData cho trạng thái loading
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Lưu trữ danh sách gốc
    private val allStaffList = mutableListOf<Staff>()

    // StateFlow để quản lý các trạng thái lọc và sắp xếp
    private val searchQuery = MutableStateFlow("")
    private val selectedUnits = MutableStateFlow<List<String>>(emptyList())
    private val sortOption = MutableStateFlow(SortOption.NAME_ASC)

    enum class SortOption {
        NAME_ASC, NAME_DESC, POSITION_ASC, POSITION_DESC, UNIT_ASC, UNIT_DESC
    }

    init {
        loadStaffList()
        setupFilterFlow()
    }

    // Trong StaffViewModel.kt
    private fun loadStaffList() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val staffList = repository.getStaffListAsync()
                withContext(Dispatchers.Main) {
                    allStaffList.clear()
                    allStaffList.addAll(staffList)
                    _staffList.value = staffList

                    // Lấy danh sách đơn vị duy nhất
                    extractUnitList(staffList)
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _staffList.value = emptyList()
                    _isLoading.value = false
                }
            }
        }
    }

    // Gọi loadStaffList khi có sự thay đổi dữ liệu.
    fun refreshStaffList() {
        loadStaffList()
    }

    private fun extractUnitList(staffList: List<Staff>) {
        viewModelScope.launch(Dispatchers.IO) {
            val units = mutableSetOf<String>()

            staffList.forEach { staff ->
                staff.unit?.let { unitRef ->
                    repository.getUnitDetailsCoroutine(unitRef)?.let { unit ->
                        unit.name?.let { units.add(it) }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                _unitList.value = units.toList().sorted()
            }
        }
    }

    private fun setupFilterFlow() {
        viewModelScope.launch {
            combine(
                searchQuery,
                selectedUnits,
                sortOption
            ) { query, units, sort ->
                Triple(query, units, sort)
            }.collect { (query, units, sort) ->
                applyFilters(query, units, sort)
            }
        }
    }

    private fun applyFilters(query: String, units: List<String>, sort: SortOption) {
        viewModelScope.launch(Dispatchers.Default) {
            var filteredList = allStaffList.toList()

            // Áp dụng tìm kiếm
            if (query.isNotEmpty()) {
                filteredList = filteredList.filter { staff ->
                    staff.fullName.contains(query, ignoreCase = true) ||
                            staff.position.contains(query, ignoreCase = true) ||
                            staff.staffId.contains(query, ignoreCase = true)
                }
            }

            // Áp dụng lọc theo đơn vị
            if (units.isNotEmpty()) {
                filteredList = filteredList.filter { staff ->
                    staff.unit?.let { unitRef ->
                        val unit = repository.getUnitDetailsSync(unitRef)
                        unit?.name?.let { name -> units.contains(name) } ?: false
                    } ?: false
                }
            }

            // Áp dụng sắp xếp
            filteredList = when (sort) {
                SortOption.NAME_ASC -> filteredList.sortedBy { it.fullName }
                SortOption.NAME_DESC -> filteredList.sortedByDescending { it.fullName }
                SortOption.POSITION_ASC -> filteredList.sortedBy { it.position }
                SortOption.POSITION_DESC -> filteredList.sortedByDescending { it.position }
                SortOption.UNIT_ASC -> filteredList.sortedBy { staff ->
                    staff.unit?.let { unitRef ->
                        repository.getUnitDetailsSync(unitRef)?.name ?: ""
                    } ?: ""
                }
                SortOption.UNIT_DESC -> filteredList.sortedByDescending { staff ->
                    staff.unit?.let { unitRef ->
                        repository.getUnitDetailsSync(unitRef)?.name ?: ""
                    } ?: ""
                }
            }

            withContext(Dispatchers.Main) {
                _staffList.value = filteredList
            }
        }
    }

    fun searchStaff(query: String) {
        searchQuery.value = query
    }

    fun filterStaffByUnits(units: List<String>) {
        selectedUnits.value = units
    }

    fun sortStaffByName(ascending: Boolean) {
        sortOption.value = if (ascending) SortOption.NAME_ASC else SortOption.NAME_DESC
    }

    fun sortStaffByPosition(ascending: Boolean) {
        sortOption.value = if (ascending) SortOption.POSITION_ASC else SortOption.POSITION_DESC
    }

    fun sortStaffByUnit(ascending: Boolean) {
        sortOption.value = if (ascending) SortOption.UNIT_ASC else SortOption.UNIT_DESC
    }
}
