package com.edu.tlucontact.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.edu.tlucontact.data.models.Unit
import com.edu.tlucontact.data.repositories.UnitRepository

class UnitViewModel : ViewModel() {

    private val repository = UnitRepository()
    private val _allUnits = MutableLiveData<List<Unit>>()
    private val _filteredUnits = MutableLiveData<List<Unit>>()
    val units: LiveData<List<Unit>> = _filteredUnits

    init {
        loadUnits()
    }

    private fun loadUnits() {
        repository.getUnits().observeForever { units ->
            _allUnits.value = units
            _filteredUnits.value = units
        }
    }

    fun filterUnitsByType(type: String?) {
        Log.d("UnitViewModel", "Lọc theo loại: $type")
        val allUnits = _allUnits.value ?: emptyList()
        val filteredUnits = if (type.isNullOrEmpty()) {
            allUnits
        } else {
            allUnits.filter { it.type.equals(type, ignoreCase = true) && (it.type.equals("Khoa", ignoreCase = true) || it.type.equals("Phòng", ignoreCase = true)) }
        }
        _filteredUnits.value = filteredUnits
        Log.d("UnitViewModel", "Số lượng đơn vị sau lọc: ${filteredUnits.size}")
    }

    fun searchUnits(query: String?) {
        Log.d("UnitViewModel", "Tìm kiếm: $query")
        val allUnits = _allUnits.value ?: emptyList()
        val searchedUnits = if (query.isNullOrEmpty()) {
            allUnits
        } else {
            allUnits.filter { it.name.contains(query, ignoreCase = true) || it.code.contains(query, ignoreCase = true) }
        }
        _filteredUnits.value = searchedUnits
        Log.d("UnitViewModel", "Số lượng đơn vị sau tìm kiếm: ${searchedUnits.size}")
    }

    fun sortUnitsByName(ascending: Boolean) {
        Log.d("UnitViewModel", "Sắp xếp theo tên (tăng dần: $ascending)")
        val allUnits = _filteredUnits.value ?: emptyList()
        val sortedUnits = if (ascending) {
            allUnits.sortedBy { it.name }
        } else {
            allUnits.sortedByDescending { it.name }
        }
        _filteredUnits.value = sortedUnits
        Log.d("UnitViewModel", "Số lượng đơn vị sau sắp xếp: ${sortedUnits.size}")
    }

    val allUnitsLiveData: LiveData<List<Unit>> = _allUnits // Thêm biến allUnitsLiveData
}