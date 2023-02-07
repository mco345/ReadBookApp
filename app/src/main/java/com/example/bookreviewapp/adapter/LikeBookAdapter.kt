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

class LikeBookAdapter(val itemClickedListener: (Like) -> Unit) :
    ListAdapter<Like, LikeBookAdapter.LikeBookViewHolder>(diffUtil) {
    inner class LikeBookViewHolder(private val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(likeModel: Like) {
            // 제목
            binding.titleTextView.text = likeModel.title
            // 설명
            binding.descriptionTextView.text = likeModel.description
            // 표지 사진
            Glide
                .with(binding.coverImageView.context)
                .load(likeModel.coverUrl)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                .into(binding.coverImageView)

            // itemClickListener
            binding.root.setOnClickListener {
                itemClickedListener(likeModel)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeBookViewHolder {
        return LikeBookViewHolder(
            ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: LikeBookViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        // diffUtil: currentList에 있는 각 아이템들을 비교하여 최신 상태를 유지하도록 한다.
        val diffUtil = object : DiffUtil.ItemCallback<Like>() {
            override fun areItemsTheSame(oldItem: Like, newItem: Like): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Like, newItem: Like): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }
}