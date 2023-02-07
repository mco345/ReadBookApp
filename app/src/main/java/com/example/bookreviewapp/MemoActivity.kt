package com.example.bookreviewapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresPermission
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.bookreviewapp.adapter.BookAdapter
import com.example.bookreviewapp.adapter.ReviewAdapter
import com.example.bookreviewapp.databinding.ActivityMemoBinding
import com.example.bookreviewapp.model.room.Review

class MemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMemoBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: ReviewAdapter

    private lateinit var isbn: String
    private lateinit var reviewList: List<Review>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // getIntent
        isbn = intent.getStringExtra("thisBookIsbn").toString()

        // Local DB(History) 생성
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "BookSearchDB"
        ).build()

        Log.d(TAG, "이 책의 isbn : $isbn")

        initBookRecyclerView()

        loadReview(isbn)



    }

    private fun loadReview(isbn: String) {
        Thread{
            reviewList = db.reviewDao().getReview(isbn.toLong())
            adapter.submitList(reviewList)
            runOnUiThread {
                if(reviewList.isEmpty()){
                    binding.noReviewLayout.isVisible = true
                }
            }
        }.start()

    }

    private fun initBookRecyclerView(){
        adapter = ReviewAdapter(itemClickedListener = {
            val intent = Intent(this, WriteMemoActivity::class.java)
            intent.putExtra("thisBookIsbn", it.id.toString())
            intent.putExtra("isRevise", true)
            intent.putExtra("page", it.page)
            intent.putExtra("currentDate", it.currentDate)
            intent.putExtra("review", it.review)
            startActivity(intent)
        })

        binding.memoRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.memoRecyclerView.adapter = adapter

    }


    // onClick
    fun addMemo(view: View) {
        val intent = Intent(this, WriteMemoActivity::class.java)
        intent.putExtra("thisBookIsbn", isbn)
        startActivity(intent)

    }

    companion object{
        private const val TAG = "MemoActivity"
    }
}