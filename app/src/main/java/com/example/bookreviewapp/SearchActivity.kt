package com.example.bookreviewapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.bookreviewapp.adapter.BookAdapter
import com.example.bookreviewapp.adapter.HistoryAdapter
import com.example.bookreviewapp.api.BookService
import com.example.bookreviewapp.databinding.ActivitySearchBinding
import com.example.bookreviewapp.model.restful.Book
import com.example.bookreviewapp.model.restful.SearchBookDto
import com.example.bookreviewapp.model.room.History
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: BookAdapter
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var bookService: BookService

    private lateinit var searchKeyword: String

    var currentPage = 1
    var isTrySearch = false
    private lateinit var searchBookList: List<Book>

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBookRecyclerView()
        initHistoryRecyclerView()
        initSearchEditText()

        // Local DB(History) 생성
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "BookSearchDB"
        ).build()

        retrofitCreate()

        showHistoryView()
    }

    private fun retrofitCreate(){
        val retrofit = Retrofit.Builder()
            .baseUrl(getString(R.string.BaseUrl))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        bookService = retrofit.create(BookService::class.java)
    }

    private fun initBookRecyclerView(){
        adapter = BookAdapter(itemClickedListener = {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("selectedBookISBN", it.isbn13)
            startActivity(intent)
        })

        // 검색결과
        binding.searchRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchRecyclerView.adapter = adapter
    }


    private fun initHistoryRecyclerView(){
        historyAdapter = HistoryAdapter (
            historyDeleteClickedListener = {
                deleteSearchKeyword(it)
            },
            historyItemClickedListener = {
                search(it)
                binding.searchEditText.setText(it)
                afterSearch()
            }
        )

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter
    }

    private fun deleteSearchKeyword(keyword: String){
        Thread{
            db.historyDao().delete(keyword)
            showHistoryView()
        }.start()
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initSearchEditText(){
        // 키보드가 입력되었을 때
        binding.searchEditText.setOnKeyListener { v, keyCode, event ->
            if(keyCode == KeyEvent.KEYCODE_ENTER && event.action == MotionEvent.ACTION_DOWN){
                search(binding.searchEditText.text.toString())
                afterSearch()

                return@setOnKeyListener true
            }

            return@setOnKeyListener false
        }

        // EditText가 터치되었을 때
        binding.searchEditText.setOnTouchListener { v, event ->
            if(event.action == MotionEvent.ACTION_DOWN){
                showHistoryView()
            }else if(event.action == MotionEvent.ACTION_UP){
                if(event.getRawX() >= (binding.searchEditText.right - binding.searchEditText.compoundDrawables[2].bounds.width())){
                    binding.searchEditText.setText("")
                }
            }

            return@setOnTouchListener false
        }
    }



    private fun afterSearch(){
        //키보드 내리기
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)

    }

    private fun search(keyword: String) {
        resetRecyclerView()
        isTrySearch = true
        searchKeyword = keyword
        binding.searchEditText.clearFocus()

        bookService.getBooksByName(getString(R.string.APIKey), keyword, currentPage)
            .enqueue(object : Callback<SearchBookDto> {
                @RequiresApi(Build.VERSION_CODES.M)
                override fun onResponse(
                    call: Call<SearchBookDto>,
                    response: Response<SearchBookDto>
                ) {
                    // HistoryView 가리고 검색 keyword 저장
                    hideHistoryView()
                    saveSearchKeyword(keyword)

                    // 검색 결과 UI 표시
                    initSearchResult(response.body()?.totalResults)

                    // 예외 처리
                    if (response.isSuccessful.not()) {
                        Log.e(TAG, "NOT!! SUCCESS")
                        return
                    }

                    // searchRecyclerview visible
                    binding.searchRecyclerView.isVisible = true

                    // 전역변수 searchBookList에 검색 결과 저장
                    searchBookList = response.body()?.books.orEmpty()

                    // BookAdapter의 currentList에 searchBookList 저장
                    adapter.submitList(searchBookList)

                    // Recyclerview 최하단 스크롤 감지
                    isBottom()

                }

                override fun onFailure(call: Call<SearchBookDto>, t: Throwable) {
                    // todo 실패처리
                    hideHistoryView()
                    Log.e(TAG, t.toString())
                }

            })
    }

    // 검색 결과 개수 UI에 표시
    @SuppressLint("SetTextI18n")
    private fun initSearchResult(totalResults: Int?){
        binding.totalResultTextView.text = "검색결과 : ${totalResults ?: 0}개"
    }

    // 검색 결과 초기화
    private fun resetRecyclerView() {
        currentPage = 1
        adapter.submitList(null)
        binding.appbar.setExpanded(true)
    }



    // RecyclerView 최하단 스크롤 감지
    @RequiresApi(Build.VERSION_CODES.M)
    private fun isBottom(){
        binding.searchRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(!binding.searchRecyclerView.canScrollVertically(1)){
                    // 10개의 데이터 추가로 load
                    getMoreData()
                }
            }
        })
    }



    // 추가 데이터 불러오기
    private fun getMoreData(){
        currentPage++   //  시작 페이지 추가
        bookService.getBooksByName(getString(R.string.APIKey), searchKeyword, currentPage)
            .enqueue(object: Callback<SearchBookDto>{
                override fun onResponse(
                    call: Call<SearchBookDto>,
                    response: Response<SearchBookDto>
                ) {

                    // 기존 searchBookList에 추가로 받아온 데이터 추가
                    searchBookList = searchBookList + response.body()?.books.orEmpty()

                    // adapt
                    adapter.submitList(searchBookList)


                }

                override fun onFailure(call: Call<SearchBookDto>, t: Throwable) {
                    TODO("Not yet implemented")
                }
            })
    }




    private fun showHistoryView(){
        Thread{
            val keywords = db.historyDao().getAll().reversed()
            // 임의의 Thread에서 UI에 접근할 때
            runOnUiThread{
                binding.historyRecyclerView.isVisible = true
                historyAdapter.submitList(keywords)
            }
        }.start()
    }

    private fun hideHistoryView(){
        binding.historyRecyclerView.isVisible = false
    }

    private fun saveSearchKeyword(keyword: String) {
        Thread{
            // 이미 검색한 기록이 있으면 제거
            db.historyDao().delete(keyword)

            db.historyDao().insertHistory(History(null, keyword))
        }.start()
    }

    override fun onBackPressed() {
        // 검색할 때 뒤로가기 누르면 historyRecyclerView만 사라짐
        if(isTrySearch && binding.historyRecyclerView.isVisible){
            binding.searchEditText.clearFocus()
            hideHistoryView()
        }
        // 그 외
        else{
            super.onBackPressed()
        }
    }





    companion object {
        private const val TAG = "SearchActivity"
    }
}