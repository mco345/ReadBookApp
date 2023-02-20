package com.example.bookreviewapp.fragment.my_page

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.bookreviewapp.*
import com.example.bookreviewapp.adapter.BookAdapter
import com.example.bookreviewapp.adapter.LikeBookAdapter
import com.example.bookreviewapp.adapter.ReadBookAdapter
import com.example.bookreviewapp.databinding.FragmentReadBookBinding
import com.example.bookreviewapp.model.room.Reading

class ReadBookFragment : Fragment() {
    private lateinit var binding: FragmentReadBookBinding
    private lateinit var adapter: ReadBookAdapter
    private lateinit var db: AppDatabase
    private lateinit var context: MyPageActivity

    private lateinit var readingList: List<Reading>

    override fun onAttach(context: Context) {
        super.onAttach(context)

        this.context = context as MyPageActivity
    }

    override fun onResume() {
        super.onResume()

        loadReadingList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReadBookBinding.inflate(layoutInflater)

        // Local DB(History) 생성
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "BookSearchDB"
        ).build()

        initBookRecyclerView()
        loadReadingList()

        return binding.root


    }

    private fun initBookRecyclerView() {
        Log.d(TAG, "initBookRecyclerView")
        adapter = ReadBookAdapter(itemClickedListener = {
            val intent = Intent(context, DetailActivity::class.java)
            Log.d(TAG, "isbn : ${it.id}")
            intent.putExtra("selectedBookISBN", it.id.toString())
            startActivity(intent)
        })

        binding.readBookRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.readBookRecyclerView.adapter = adapter
    }

    private fun loadReadingList() {
        Thread {
            readingList = db.readingDao().getAll("isReading").orEmpty()
            context.runOnUiThread {
                if(readingList.isEmpty()){
                    binding.readBookRecyclerView.isVisible = false
                    binding.emptyTextView.isVisible = true
                }else{
                    binding.readBookRecyclerView.isVisible = true
                    binding.emptyTextView.isVisible = false
                    setReadingList()
                }
            }
        }.start()
    }

    private fun setReadingList() {
        adapter.submitList(readingList)

    }

    companion object {
        private const val TAG = "ReadBookFragment"
    }
}
