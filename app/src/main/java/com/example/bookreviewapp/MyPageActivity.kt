package com.example.bookreviewapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.room.Room
import androidx.viewpager2.widget.ViewPager2
import com.example.bookreviewapp.databinding.ActivityDetailBinding
import com.example.bookreviewapp.databinding.ActivityMyPageBinding
import com.example.bookreviewapp.fragment.my_page.DoneBookFragment
import com.example.bookreviewapp.fragment.my_page.LikeBookFragment
import com.example.bookreviewapp.fragment.my_page.MyPageViewPagerAdapter
import com.example.bookreviewapp.fragment.my_page.ReadBookFragment
import com.example.bookreviewapp.model.restful.Book
import com.example.bookreviewapp.model.room.Like
import com.google.android.material.tabs.TabLayoutMediator

class MyPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyPageBinding // view binding

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewPager()

        // Local DB(History) 생성
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "BookSearchDB"
        ).build()
    }

    private fun initViewPager() {
        //ViewPager2 Adapter 셋팅
        var myPageViewPagerAdapter = MyPageViewPagerAdapter(this)
        myPageViewPagerAdapter.addFragment(ReadBookFragment())
        myPageViewPagerAdapter.addFragment(DoneBookFragment())
        myPageViewPagerAdapter.addFragment(LikeBookFragment())

        //Adapter 연결
        binding.viewPager.apply {

            adapter = myPageViewPagerAdapter

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                }
            })
        }

        //ViewPager, TabLayout 연결
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "내가 읽는 중인 책"
                1 -> tab.text = "내가 읽은 책"
                2 -> tab.text = "내가 찜한 책"
            }
        }.attach()
    }

    companion object{
        private const val TAG = "MyPageActivity"
    }
}