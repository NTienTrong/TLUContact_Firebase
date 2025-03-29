package com.edu.tlucontact.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.edu.tlucontact.R
import com.edu.tlucontact.databinding.StaffListActivityBinding
import com.edu.tlucontact.ui.adapter.StaffAdapter
import com.edu.tlucontact.ui.viewmodels.StaffViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class StaffListActivity : AppCompatActivity() {

    private lateinit var binding: StaffListActivityBinding
    private lateinit var staffAdapter: StaffAdapter
    private lateinit var staffViewModel: StaffViewModel
    private var currentSortOption: SortOption = SortOption.NAME_ASC
    private val selectedUnits = mutableSetOf<String>()

    enum class SortOption {
        NAME_ASC, NAME_DESC, POSITION_ASC, POSITION_DESC, UNIT_ASC, UNIT_DESC
    }

    companion object {
        private const val EDIT_REQUEST_CODE = 1001 // Định nghĩa EDIT_REQUEST_CODE ở đây
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StaffListActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        setupRecyclerView()
        setupViewModel()
        setupSearch()
        setupSortAndFilter()
    }

    private fun setupRecyclerView() {
        binding.staffRecyclerView.layoutManager = LinearLayoutManager(this)
        staffAdapter = StaffAdapter { staff ->
            val intent = Intent(this, StaffDetailActivity::class.java).apply {
                putExtra("staffId", staff.staffId)
                putExtra("fullName", staff.fullName)
                putExtra("position", staff.position)
                putExtra("phone", staff.phone)
                putExtra("email", staff.email)
                putExtra("photoURL", staff.photoURL)
                putExtra("unitPath", staff.unit?.path)
                putExtra("userId", staff.userId)
            }
            startActivity(intent)
        }
        binding.staffRecyclerView.adapter = staffAdapter
    }

    private fun setupViewModel() {
        staffViewModel = ViewModelProvider(this)[StaffViewModel::class.java]

        // Quan sát dữ liệu từ ViewModel
        staffViewModel.staffList.observe(this) { staffList ->
            staffAdapter.submitList(staffList)
        }

        // Quan sát danh sách đơn vị
        staffViewModel.unitList.observe(this) { unitList ->
            setupUnitFilterChips(unitList)
        }

        // Quan sát trạng thái loading
        staffViewModel.isLoading.observe(this) { isLoading ->
            // Hiển thị loading indicator nếu cần
        }
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.clearSearchImageView.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                staffViewModel.searchStaff(s?.toString() ?: "")
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.clearSearchImageView.setOnClickListener {
            binding.searchEditText.text.clear()
            binding.clearSearchImageView.visibility = View.GONE
        }
    }

    private fun setupSortAndFilter() {
        // Thiết lập nút sắp xếp
        binding.sortButton.setOnClickListener {
            showSortOptions()
        }

        // Thiết lập nút lọc
        binding.filterButton.setOnClickListener {
            showFilterOptions()
        }

        // Thiết lập các nút trong sheet
        binding.cancelButton.setOnClickListener {
            hideFilterSortSheet()
        }

        binding.applyButton.setOnClickListener {
            applyFilterAndSort()
            hideFilterSortSheet()
        }
    }

    private fun showSortOptions() {
        binding.sheetTitle.text = "Sắp xếp theo"
        binding.sortOptionsGroup.visibility = View.VISIBLE
        binding.filterOptionsLayout.visibility = View.GONE

        // Chọn radio button tương ứng với lựa chọn sắp xếp hiện tại
        val radioButtonId = when (currentSortOption) {
            SortOption.NAME_ASC -> R.id.sortByNameAsc
            SortOption.NAME_DESC -> R.id.sortByNameDesc
            SortOption.POSITION_ASC -> R.id.sortByPositionAsc
            SortOption.POSITION_DESC -> R.id.sortByPositionDesc
            SortOption.UNIT_ASC -> R.id.sortByUnitAsc
            SortOption.UNIT_DESC -> R.id.sortByUnitDesc
        }
        binding.sortOptionsGroup.check(radioButtonId)

        showFilterSortSheet()
    }

    private fun showFilterOptions() {
        binding.sheetTitle.text = "Lọc theo"
        binding.sortOptionsGroup.visibility = View.GONE
        binding.filterOptionsLayout.visibility = View.VISIBLE

        showFilterSortSheet()
    }

    private fun showFilterSortSheet() {
        // Hiển thị sheet với animation
        binding.filterSortSheet.visibility = View.VISIBLE
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.silde_up)
        binding.filterSortSheet.startAnimation(slideUp)
    }

    private fun hideFilterSortSheet() {
        // Ẩn sheet với animation
        val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        binding.filterSortSheet.startAnimation(slideDown)
        binding.filterSortSheet.visibility = View.GONE
    }

    private fun applyFilterAndSort() {
        // Áp dụng lựa chọn sắp xếp
        if (binding.sortOptionsGroup.visibility == View.VISIBLE) {
            val selectedId = binding.sortOptionsGroup.checkedRadioButtonId
            currentSortOption = when (selectedId) {
                R.id.sortByNameAsc -> SortOption.NAME_ASC
                R.id.sortByNameDesc -> SortOption.NAME_DESC
                R.id.sortByPositionAsc -> SortOption.POSITION_ASC
                R.id.sortByPositionDesc -> SortOption.POSITION_DESC
                R.id.sortByUnitAsc -> SortOption.UNIT_ASC
                R.id.sortByUnitDesc -> SortOption.UNIT_DESC
                else -> SortOption.NAME_ASC
            }

            when (currentSortOption) {
                SortOption.NAME_ASC -> staffViewModel.sortStaffByName(true)
                SortOption.NAME_DESC -> staffViewModel.sortStaffByName(false)
                SortOption.POSITION_ASC -> staffViewModel.sortStaffByPosition(true)
                SortOption.POSITION_DESC -> staffViewModel.sortStaffByPosition(false)
                SortOption.UNIT_ASC -> staffViewModel.sortStaffByUnit(true)
                SortOption.UNIT_DESC -> staffViewModel.sortStaffByUnit(false)
            }

            updateSortChip()
        }

        // Áp dụng bộ lọc
        if (binding.filterOptionsLayout.visibility == View.VISIBLE) {
            staffViewModel.filterStaffByUnits(selectedUnits.toList())
            updateFilterChips()
        }
    }

    private fun setupUnitFilterChips(units: List<String>) {
        binding.unitFilterChipGroup.removeAllViews()

        units.forEach { unit ->
            val chip = layoutInflater.inflate(
                R.layout.item_filter_chip,
                binding.unitFilterChipGroup,
                false
            ) as Chip

            chip.text = unit
            chip.isCheckable = true
            chip.isChecked = selectedUnits.contains(unit)

            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedUnits.add(unit)
                } else {
                    selectedUnits.remove(unit)
                }
            }

            binding.unitFilterChipGroup.addView(chip)
        }
    }

    private fun updateFilterChips() {
        binding.activeFiltersChipGroup.removeAllViews()

        if (selectedUnits.isNotEmpty()) {
            binding.activeFiltersChipGroup.visibility = View.VISIBLE

            selectedUnits.forEach { unit ->
                val chip = layoutInflater.inflate(
                    R.layout.item_active_filter_chip,
                    binding.activeFiltersChipGroup,
                    false
                ) as Chip

                chip.text = unit
                chip.isCloseIconVisible = true

                chip.setOnCloseIconClickListener {
                    selectedUnits.remove(unit)
                    binding.activeFiltersChipGroup.removeView(chip)
                    staffViewModel.filterStaffByUnits(selectedUnits.toList())

                    if (selectedUnits.isEmpty()) {
                        binding.activeFiltersChipGroup.visibility = View.GONE
                    }
                }

                binding.activeFiltersChipGroup.addView(chip)
            }
        } else {
            binding.activeFiltersChipGroup.visibility = View.GONE
        }
    }

    private fun updateSortChip() {
        // Có thể thêm chip hiển thị lựa chọn sắp xếp hiện tại
    }

    override fun onResume() {
        super.onResume()
        // Tải lại dữ liệu mỗi khi StaffListActivity được hiển thị lại
        staffViewModel.refreshStaffList()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Dữ liệu đã được cập nhật, tải lại danh sách nhân viên
                staffViewModel.refreshStaffList()
            }
        }
    }
}
