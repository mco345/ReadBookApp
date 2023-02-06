package com.example.bookreviewapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.bookreviewapp.api.BookService
import com.example.bookreviewapp.databinding.ActivityDetailBinding
import com.example.bookreviewapp.model.restful.Book
import com.example.bookreviewapp.model.restful.SelectedBookDto
import com.example.bookreviewapp.model.room.Like
import com.example.bookreviewapp.model.room.Review
import com.willy.ratingbar.BaseRatingBar
import com.willy.ratingbar.ScaleRatingBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DetailActivity : AppCompatActivity() {
    // view binding
    private lateinit var binding: ActivityDetailBinding
    private lateinit var bookService: BookService
    private lateinit var db: AppDatabase

    private lateinit var thisBook: Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isbn = intent.getStringExtra("selectedBookISBN")

        // Local DB(History) 생성
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "BookSearchDB"
        ).build()

        retrofitCreate()
        setUI(isbn!!)

        // 리뷰
        loadReview(isbn)
        saveReview(isbn)

        // 찜
        loadLike(isbn)
        saveLike(isbn)

        // 별점
        loadRating(isbn)
        saveRating(isbn)


    }



    private fun retrofitCreate(){
        val retrofit = Retrofit.Builder()
            .baseUrl(getString(R.string.BaseUrl))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        bookService = retrofit.create(BookService::class.java)
    }

    private fun setUI(isbn: String){
        // getData
        bookService.getSelectedBook(getString(R.string.APIKey), isbn)
            .enqueue(object: Callback<SelectedBookDto>{
                override fun onResponse(
                    call: Call<SelectedBookDto>,
                    response: Response<SelectedBookDto>
                ) {
                    val item = response.body()?.item?.get(0)
                    // UI
                    binding.titleTextView.text = item?.title.orEmpty()
                    binding.authorTextView.text = item?.author.orEmpty()
                    binding.descriptionTextView.text = if(item?.description != "") item?.description.orEmpty() else "x"
                    binding.publisherTextView.text = item?.publisher.orEmpty()
                    binding.ISBNTextView.text = item?.isbn13.orEmpty()
                    binding.pageTextView.text = item?.subInfo?.itemPage.orEmpty()
                    binding.pubDateTextView.text = item?.pubDate.orEmpty()
                    Glide.with(binding.coverImageView.context)
                        .load(item?.coverSmallUrl.orEmpty())
                        .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                        .into(binding.coverImageView)

                    thisBook = item!!
                }

                override fun onFailure(call: Call<SelectedBookDto>, t: Throwable) {
                }
            })
    }

    // 리뷰 불러오기
    private fun loadReview(isbn: String) {
        Thread{
            val review = db.reviewDao().getOneReview(isbn.toLong())
            runOnUiThread {
                binding.reviewEditText.setText(review?.review.orEmpty())
            }
        }.start()
    }

    // 리뷰 저장
    private fun saveReview(isbn: String) {
        // 리뷰 작성하는 동시에 저장
        binding.reviewEditText.setOnKeyListener { v, keyCode, event ->
            if(event.action == MotionEvent.ACTION_DOWN){
                Thread{
                    db.reviewDao().saveReview(
                        Review(
                            isbn.toLong(),
                            binding.reviewEditText.text.toString()
                        )
                    )
                }.start()
            }
            return@setOnKeyListener false
        }
    }

    // 찜한 책인지 아닌지 불러오기
    private fun loadLike(isbn: String) {
        Thread{
            val isLike = db.likeDao().getIsLike(isbn.toLong())
            runOnUiThread {
                binding.likeButton.rating = if(isLike == true) 1.0f else 0f
            }
        }.start()

    }

    // 찜 or 찜 제거
    @SuppressLint("ClickableViewAccessibility")
    private fun saveLike(isbn: String) {
        binding.likeButton.setOnTouchListener { v, event ->
            binding.likeButton.setOnRatingChangeListener { ratingBar, rating, fromUser ->
                if(rating.toInt() == 1){
                    Thread{
                        db.likeDao().saveLike(
                            Like(
                                isbn.toLong(),
                                true,
                                thisBook.title,
                                thisBook.coverSmallUrl,
                                thisBook.author,
                                thisBook.description
                            )
                        )
                    }.start()

                    Toast.makeText(this, "찜한 목록에 책을 추가하였습니다.", Toast.LENGTH_SHORT).show()
                }else{
                    Thread{
                        db.likeDao().deleteLike(isbn.toLong())
                    }.start()

                    Toast.makeText(this, "찜한 목록에 책을 제외하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            return@setOnTouchListener false
        }
    }

    // 별점 저장
    private fun loadRating(isbn: String) {


    }

    private fun saveRating(isbn: String) {

    }




    companion object{
        private const val TAG = "DetailActivity"
    }
}