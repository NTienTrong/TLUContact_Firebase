package com.edu.tlucontact.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.edu.tlucontact.R
import com.edu.tlucontact.data.models.Unit
import com.edu.tlucontact.databinding.UnitItemBinding

class UnitsAdapter(
    private var units: MutableList<Unit>,
    private val onItemClick: (Unit) -> kotlin.Unit // Thêm listener
) : RecyclerView.Adapter<UnitsAdapter.UnitsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitsViewHolder {
        val binding = UnitItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UnitsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UnitsViewHolder, position: Int) {
        val unit = units[position]
        holder.bind(unit)
    }

    override fun getItemCount(): Int = units.size

    fun submitList(newList: List<Unit>) {
        units.clear()
        units.addAll(newList)
        notifyDataSetChanged()
    }

    inner class UnitsViewHolder(private val binding: UnitItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(unit: Unit) {
            binding.unitNameTextView.text = unit.name
            binding.unitCodeTextView.text = unit.code

            Glide.with(binding.root.context)
                .load(unit.logoURL)
                .placeholder(R.drawable.ic_default_unit_logo)
                .error(R.drawable.ic_default_unit_logo)
                .into(binding.unitLogoImageView)

            binding.root.setOnClickListener { // Xử lý sự kiện click
                onItemClick(unit)
            }
        }
    }
}