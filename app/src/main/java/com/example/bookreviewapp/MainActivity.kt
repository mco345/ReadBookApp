package com.example.bookreviewapp

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.bookreviewapp.adapter.BookAdapter
import com.example.bookreviewapp.adapter.ViewPagerAdapter
import com.example.bookreviewapp.api.BookService
import com.example.bookreviewapp.databinding.ActivityMainBinding
import com.example.bookreviewapp.model.restful.BestSellerDto
import com.example.bookreviewapp.sharedPreference.MyApplication
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

    private var isTimer: Boolean = false

    override fun onResume() {
        super.onResume()

        binding.bestSellerRecyclerView.adapter = adapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkTimer()

        initViewPager()
        initBookRecyclerView()

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

        setData()

    }

    private fun checkTimer() {
        Log.d(TAG, "time - "+MyApplication.prefs.getLong("isTimerTime", 0))
        isTimer = MyApplication.prefs.getBoolean("isTimer", false)
        if(isTimer){
            val builder = AlertDialog.Builder(this)
            builder.setTitle("알림")
                .setMessage("저장되지 않은 타이머가 있습니다. 진행 중이던 타이머로 이동하시겠습니까?")
                .setPositiveButton("네",
                    DialogInterface.OnClickListener { dialog, id ->
                        val intent = Intent(this, TimerActivity::class.java)
                        val isTimerISBN = MyApplication.prefs.getString("isTimerISBN", "no isbn")
                        intent.putExtra("selectedBookISBN", isTimerISBN)
                        startActivity(intent)
                    })
                .setNegativeButton("아니오",
                    DialogInterface.OnClickListener { dialog, id ->
                        MyApplication.prefs.setBoolean("isTimer", false)
                        MyApplication.prefs.setString("isTimerISBN", "no isbn")
                        MyApplication.prefs.setLong("isTimerTime", 0)
                    })
            // 다이얼로그를 띄워주기
            builder.show()
        }
    }

    private fun initViewPager() {
        binding.viewPagerMain.adapter = ViewPagerAdapter(getViewPagerList())    // 어댑터 생성
        binding.viewPagerMain.orientation = ViewPager2.ORIENTATION_HORIZONTAL   // 방향은 가로로
        initPagerListener()
    }

    private fun getViewPagerList(): MutableList<Int> {
        return mutableListOf(R.drawable.bestseller)
    }

    private fun initPagerListener(){
        binding.viewPagerMain.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d(TAG, "onPageSelected - position : $position")
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                Log.d(TAG, "onPageScrolled  - position : $position")
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


    private fun setData(){

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

    fun search(view: View) {
        val intent = Intent(this, SearchActivity::class.java)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.myPage -> {
                val intent = Intent(this, MyPageActivity::class.java)
                startActivity(intent)
            }
            R.id.search -> {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val TAG = "MainActivity"
    }



}