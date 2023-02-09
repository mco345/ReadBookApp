package com.example.bookreviewapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.bookreviewapp.databinding.ItemBookBinding
import com.example.bookreviewapp.model.restful.Book
import com.example.bookreviewapp.model.room.Like
import com.example.bookreviewapp.model.room.Reading

class ReadBookAdapter(val itemClickedListener: (Reading) -> Unit) :
    ListAdapter<Reading, ReadBookAdapter.ReadBookViewHolder>(diffUtil) {

    inner class ReadBookViewHolder(private val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(readingModel: Reading) {
            // 제목
            binding.titleTextView.text = readingModel.title
            // 설명
            binding.descriptionTextView.text = readingModel.description
            // 표지 사진
            Glide
                .with(binding.coverImageView.context)
                .load(readingModel.coverUrl)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                .into(binding.coverImageView)

            // itemClickListener
            binding.root.setOnClickListener {
                itemClickedListener(readingModel)
            }

        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadBookViewHolder {
        return ReadBookViewHolder(
            ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ReadBookViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        // diffUtil: currentList에 있는 각 아이템들을 비교하여 최신 상태를 유지하도록 한다.
        val diffUtil = object : DiffUtil.ItemCallback<Reading>() {
            override fun areItemsTheSame(oldItem: Reading, newItem: Reading): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Reading, newItem: Reading): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }

}