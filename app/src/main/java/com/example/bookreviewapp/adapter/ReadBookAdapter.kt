package com.example.bookreviewapp.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.bookreviewapp.databinding.ItemBookReadingBinding
import com.example.bookreviewapp.model.room.Reading
import java.text.SimpleDateFormat
import java.util.*

class ReadBookAdapter(val itemClickedListener: (Reading) -> Unit) :
    ListAdapter<Reading, ReadBookAdapter.ReadBookViewHolder>(diffUtil) {

    inner class ReadBookViewHolder(private val binding: ItemBookReadingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(readingModel: Reading) {
            // 제목
            binding.titleTextView.text = readingModel.title
            // 독서 퍼센트 & 독서 날짜
            when(readingModel.state){
                "isReading" -> {
                    // 독서 퍼센트
                    val percent = (readingModel.readingPage!!.toFloat() / readingModel.totalPage!!.toFloat()) * 100
                    Log.d(TAG, "percent : $percent, ${readingModel.readingPage}, ${readingModel.totalPage} ")
                    binding.percentReadingTextView.text = "${percent.toInt()}%"
                    // 시작 날짜
                    binding.startDateTextView.text = "독서 시작 날짜 : ${readingModel.startDate}"
                    // 목표 날짜
                    binding.targetDateTextView.text = "독서 목표 날짜 : ${readingModel.targetDate}"
                }
                "finishReading" -> {
                    // 시작 날짜
                    binding.startDateTextView.text = "독서 시작 날짜 : ${readingModel.startDate}"
                    // 완독 날짜
                    val finishDate = SimpleDateFormat("yyyy-MM-dd", Locale("ko", "KR")).format(Date(readingModel.finishTime!!))
                    Log.d(TAG, "finishDate : $finishDate")
                    binding.targetDateTextView.text = "완독 날짜 : $finishDate"

                }
            }

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
            ItemBookReadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

        val TAG = "ReadBookAdapter"
    }

}