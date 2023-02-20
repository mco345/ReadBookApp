package com.example.bookreviewapp.fragment.my_page

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.bookreviewapp.*
import com.example.bookreviewapp.adapter.LikeBookAdapter
import com.example.bookreviewapp.databinding.FragmentLikeBookBinding
import com.example.bookreviewapp.model.room.Like

class LikeBookFragment : Fragment() {
    private lateinit var binding: FragmentLikeBookBinding
    private lateinit var adapter: LikeBookAdapter
    private lateinit var db: AppDatabase
    private lateinit var context: MyPageActivity

    private lateinit var likeList: List<Like>



    override fun onAttach(context: Context) {
        super.onAttach(context)

        this.context = context as MyPageActivity
    }

    override fun onResume() {
        super.onResume()

        loadLikeList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLikeBookBinding.inflate(layoutInflater)

        // Local DB(History) 생성
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "BookSearchDB"
        ).build()


        initBookRecyclerView()
        loadLikeList()

        return binding.root
    }

    private fun loadLikeList() {
        Thread{
            likeList = db.likeDao().getAllLikeList()
            Log.d(TAG, "찜 목록 : $likeList")
            context.runOnUiThread{
                if(likeList.isEmpty()){
                    binding.likeBookRecyclerView.isVisible = false
                    binding.emptyTextView.isVisible = true
                }else{
                    binding.likeBookRecyclerView.isVisible = true
                    binding.emptyTextView.isVisible = false
                    setLikeList()
                }
            }
        }.start()
    }

    private fun setLikeList() {
        adapter.submitList(likeList)
    }


    private fun initBookRecyclerView(){
        adapter = LikeBookAdapter(itemClickedListener = {
            val intent = Intent(context, DetailActivity::class.java)
            Log.d(TAG, "isbn : ${it.id}")
            intent.putExtra("selectedBookISBN", it.id.toString())
            startActivity(intent)
        })

        binding.likeBookRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.likeBookRecyclerView.adapter = adapter
    }

    companion object {
        private const val TAG = "ListBookFragment"
    }
}