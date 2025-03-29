package com.edu.tlucontact.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.edu.tlucontact.R
import com.edu.tlucontact.data.models.Staff
import com.edu.tlucontact.data.repositories.StaffRepository
import com.edu.tlucontact.databinding.StaffItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StaffAdapter(
    private val onItemClick: (Staff) -> Unit
) : ListAdapter<Staff, StaffAdapter.StaffViewHolder>(StaffDiffCallback()) {

    private val staffRepository = StaffRepository()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val binding = StaffItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StaffViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        val staff = getItem(position)
        holder.bind(staff)
    }

    inner class StaffViewHolder(private val binding: StaffItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val coroutineScope = CoroutineScope(Dispatchers.Main)

        fun bind(staff: Staff) {
            binding.staffNameTextView.text = staff.fullName
            binding.staffPositionTextView.text = staff.position

            // Lấy và hiển thị tên đơn vị sử dụng coroutines
            binding.staffUnitTextView.text = "Đang tải..."
            staff.unit?.let { unitRef ->
                coroutineScope.launch {
                    val unit = withContext(Dispatchers.IO) {
                        staffRepository.getUnitDetailsCoroutine(unitRef)
                    }
                    binding.staffUnitTextView.text = unit?.name ?: "Chưa rõ đơn vị"
                }
            } ?: run {
                binding.staffUnitTextView.text = "Chưa rõ đơn vị"
            }

            // Tải ảnh với Glide và animation
            Glide.with(binding.root.context)
                .load(staff.photoURL)
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .transition(DrawableTransitionOptions.withCrossFade())
                .circleCrop()
                .into(binding.staffImageView)

            binding.root.setOnClickListener {
                onItemClick(staff)
            }
        }
    }

    class StaffDiffCallback : DiffUtil.ItemCallback<Staff>() {
        override fun areItemsTheSame(oldItem: Staff, newItem: Staff): Boolean {
            return oldItem.staffId == newItem.staffId
        }

        override fun areContentsTheSame(oldItem: Staff, newItem: Staff): Boolean {
            return oldItem == newItem
        }
    }
}
