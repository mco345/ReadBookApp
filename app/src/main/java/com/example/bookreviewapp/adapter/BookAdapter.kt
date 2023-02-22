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
import com.example.bookreviewapp.model.retrofit.Book

/*
ListAdapter란 백그라운드 스레드에서 기존 리스트와 새로 들어온 리스트 간 차이를 계산하여 RecyclerView에 데이터를 표시하기 위한 어댑터 기본 클래스이다.
ListAdapter에서는 백 그라운드에서 아이템을 비교하고 업데이트 해주기 때문에 데이터를 삽입하거나 지울 때,
RecyclerView.Adapter.NotifiyDataSetChanged()와 같은 명령어를 사용하지 않아 편리함
*/
class BookAdapter(val itemClickedListener: (Book) -> Unit): ListAdapter<Book, BookAdapter.BookItemViewHolder>(diffUtil) {

    inner class BookItemViewHolder(private val binding: ItemBookBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(bookModel: Book){
            // 상태, 찜
            val db = Room.databaseBuilder(
                binding.root.context,
                AppDatabase::class.java,
                "BookSearchDB"
            ).build()

            Thread{
                val isbn = bookModel.isbn13
                val state = db.readingDao().getState(if (isbn.isEmpty()) 0 else isbn.toLong()).orEmpty()
                Log.d(TAG, "state : $state")

                val isLike = db.likeDao().getIsLike(if (isbn.isEmpty()) 0 else isbn.toLong()) ?: false
                Log.d(TAG, "isLike : $isLike")

                binding.root.post {
                    when(state){
                        "isReading" -> {
                            Log.d(TAG, "isReading id : ${bookModel.isbn13}")
                            binding.stateImageView.isVisible = true
                            binding.stateImageView.setImageResource(R.drawable.shape_oval_orange)
                        }
                        "finishReading" -> {
                            Log.d(TAG, "finishReading id : ${bookModel.isbn13}")
                            binding.stateImageView.isVisible = true
                            binding.stateImageView.setImageResource(R.drawable.shape_oval_violet)
                        }
                    }

                    if(isLike){
                        binding.likeImageView.isVisible = true
                    }
                }


            }.start()



            // 제목
            binding.titleTextView.text = bookModel.title
            //작가
            binding.authorTextView.text = bookModel.author
            // 설명
            binding.descriptionTextView.text = bookModel.description
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
            // 표지 사진
            Glide
                .with(binding.coverImageView.context)
                .load(bookModel.coverSmallUrl)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                .into(binding.coverImageView)

            // itemClickedListener
            binding.root.setOnClickListener {
                itemClickedListener(bookModel)
            }
        }
    }

    // 미리 만들어진 ViewHolder가 없을 경우 새로 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookItemViewHolder {
        return BookItemViewHolder(ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false))




    }

    // ViewHolder가 View에 그려졌을 때 data binding
    override fun onBindViewHolder(holder: BookItemViewHolder, position: Int) {
        // currentList: 해당 Adapter에 "submitList()"를 통해 삽입한 아이템 리스트
        holder.bind(currentList[position])
    }

    companion object {
        // diffUtil: currentList에 있는 각 아이템들을 비교하여 최신 상태를 유지하도록 한다.
        val diffUtil = object: DiffUtil.ItemCallback<Book>(){
            override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem.id == newItem.id
            }

        }

        private const val TAG = "BookAdapter"
    }
}