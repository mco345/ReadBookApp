package com.example.bookreviewapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bookreviewapp.databinding.ItemHistoryBinding
import com.example.bookreviewapp.databinding.ItemViewpagerMainBinding

class ViewPagerAdapter(list: MutableList<Int>) : RecyclerView.Adapter<ViewPagerAdapter.PagerViewHolder>() {
    val list = list

    inner class PagerViewHolder(private val binding: ItemViewpagerMainBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(image: Int){
            binding.viewPagerImageViewMain.setImageResource(image)

            itemView.setOnClickListener {
                Toast.makeText(binding.root.context, "$position", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewPagerAdapter.PagerViewHolder {
        return PagerViewHolder(
            ItemViewpagerMainBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewPagerAdapter.PagerViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }


}