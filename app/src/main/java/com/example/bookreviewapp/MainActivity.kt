package com.example.bookreviewapp

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.bookreviewapp.adapter.BookAdapter
import com.example.bookreviewapp.api.BookService
import com.example.bookreviewapp.databinding.ActivityMainBinding
import com.example.bookreviewapp.model.restful.BestSellerDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: BookAdapter
    private lateinit var bookService: BookService

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBookRecyclerView()
        initTransparentStatusBar()

        // 툴바 사용 설정
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        // Local DB(History) 생성
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "BookSearchDB"
        ).build()

        val retrofit = Retrofit.Builder()
                .baseUrl(getString(R.string.BaseUrl))
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        bookService = retrofit.create(BookService::class.java)

        bookService.getBestSellerBooks(getString(R.string.APIKey))
                .enqueue(object: Callback<BestSellerDto>{
                    override fun onResponse(call: Call<BestSellerDto>, response: Response<BestSellerDto>) {
                        // todo 성공처리

                        if(response.isSuccessful.not()){
                            Log.e(TAG, "NOT!! SUCCESS")
                            return
                        }

                        Log.d(TAG, "결과 : ${response.body()?.books}")

                        response.body()?.let{
                            adapter.submitList(it.books)
                        }
                    }

                    override fun onFailure(call: Call<BestSellerDto>, t: Throwable) {
                        // todo 실패처리
                        Log.e(TAG, t.toString())
                    }

                })

    }

    private fun initBookRecyclerView(){
        adapter = BookAdapter(itemClickedListener = {
            val intent = Intent(this, DetailActivity::class.java)
            Log.d(TAG, "isbn : ${it.isbn13}")
            intent.putExtra("selectedBookISBN", if(it.isbn13 != "") it.isbn13 else it.isbn10)
            startActivity(intent)
        })

        // 베스트셀러
        binding.bestSellerRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bestSellerRecyclerView.adapter = adapter

    }

    private fun initTransparentStatusBar(){
        if(Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            if(Build.VERSION.SDK_INT < 21) {
                setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
            } else {
                setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
                window.statusBarColor = Color.TRANSPARENT
            }
        }
    }

    private fun setWindowFlag(bits: Int, on: Boolean) {
        val winAttr = window.attributes
        winAttr.flags = if(on) winAttr.flags or bits else winAttr.flags and bits.inv()
        window.attributes = winAttr
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.search -> {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
            }
            R.id.myPage -> {
                val intent = Intent(this, MyPageActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val TAG = "MainActivity"
    }

}