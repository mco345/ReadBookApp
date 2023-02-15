package com.example.bookreviewapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.bookreviewapp.AppDatabase
import com.example.bookreviewapp.R
import com.example.bookreviewapp.databinding.ItemBookBinding
import com.example.bookreviewapp.model.restful.Book
import com.example.bookreviewapp.model.room.Like

class LikeBookAdapter(val itemClickedListener: (Like) -> Unit) :
    ListAdapter<Like, LikeBookAdapter.LikeBookViewHolder>(diffUtil) {

    inner class LikeBookViewHolder(private val binding: ItemBookBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(likeModel: Like) {
            // 상태
            val db = Room.databaseBuilder(
                binding.root.context,
                AppDatabase::class.java,
                "BookSearchDB"
            ).build()

            Thread{
                val state = db.readingDao().getState(likeModel.id!!.toLong()).orEmpty()
                Log.d(TAG, "id : ${likeModel.id}, state : $state")

                binding.root.post {
                    when(state){
                        "isReading" -> {
                            Log.d(TAG, "isReading id : ${likeModel.id}")
                            binding.stateImageView.isVisible = true
                            binding.stateImageView.setImageResource(R.drawable.shape_oval_orange)
                        }
                        "finishReading" -> {
                            Log.d(TAG, "finishReading id : ${likeModel.id}")
                            binding.stateImageView.isVisible = true
                            binding.stateImageView.setImageResource(R.drawable.shape_oval_violet)
                        }
                    }
                }

            }.start()
            // 제목
            binding.titleTextView.text = likeModel.title
            // 설명
            binding.descriptionTextView.text = likeModel.description
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
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

        private const val TAG = "LikeBookAdapter"
    }
}