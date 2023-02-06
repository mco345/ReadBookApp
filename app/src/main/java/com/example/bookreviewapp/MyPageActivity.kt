package com.example.bookreviewapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.room.Room
import com.example.bookreviewapp.model.restful.Book
import com.example.bookreviewapp.model.room.Like

class MyPageActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        // Local DB(History) 생성
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "BookSearchDB"
        ).build()

        Thread{
            val likeList:List<Like> = db.likeDao().getAllLikeList()
            Log.d(TAG, "찜 목록 : $likeList")
        }.start()
    }

    companion object{
        private const val TAG = "MyPageActivity"
    }
}