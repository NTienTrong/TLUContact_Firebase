package com.edu.tlucontact.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.edu.tlucontact.R
import com.edu.tlucontact.data.models.StudentWithDisplayName
import com.edu.tlucontact.databinding.StudentItemBinding

class StudentsAdapter(
    private var students: MutableList<StudentWithDisplayName>,
    private val onItemClick: (StudentWithDisplayName) -> Unit
) : RecyclerView.Adapter<StudentsAdapter.StudentsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentsViewHolder {
        val binding = StudentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentsViewHolder, position: Int) {
        val studentWithDisplayName = students[position]
        holder.bind(studentWithDisplayName)
    }

    override fun getItemCount(): Int = students.size

    fun submitList(newList: List<StudentWithDisplayName>) {
        students.clear()
        students.addAll(newList)
        notifyDataSetChanged()
    }

    inner class StudentsViewHolder(private val binding: StudentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(studentWithDisplayName: StudentWithDisplayName) {
            binding.studentNameTextView.text = studentWithDisplayName.student.fullName // Hiển thị fullName
            binding.studentClassTextView.text = studentWithDisplayName.student.className
            binding.studentIdTextView.text = studentWithDisplayName.student.studentId

            Glide.with(binding.root.context)
                .load(studentWithDisplayName.student.photoURL)
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .into(binding.studentImageView)

            binding.root.setOnClickListener {
                onItemClick(studentWithDisplayName)
            }
        }
    }
}