package com.example.bookreviewapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.bookreviewapp.api.BookService
import com.example.bookreviewapp.databinding.ActivityDetailBinding
import com.example.bookreviewapp.model.restful.Book
import com.example.bookreviewapp.model.restful.SelectedBookDto
import com.example.bookreviewapp.model.room.Like
import com.example.bookreviewapp.model.room.Rating
import com.example.bookreviewapp.model.room.Review
import com.willy.ratingbar.BaseRatingBar
import com.willy.ratingbar.ScaleRatingBar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding // view binding
    private lateinit var bookService: BookService
    private lateinit var db: AppDatabase

    private lateinit var thisBook: Book

    private lateinit var isbn: String

    var isAllFabsVisible: Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isbn = intent.getStringExtra("selectedBookISBN").toString()

        // Local DB(History) 생성
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "BookSearchDB"
        ).build()

        retrofitCreate()
        setUI(isbn!!)
        setFloatingActionButton()


        // 찜
        loadLike(isbn)
        saveLike(isbn)

        // 별점
        loadRating(isbn)
        saveRating(isbn)


    }

    private fun retrofitCreate() {
        val retrofit = Retrofit.Builder()
            .baseUrl(getString(R.string.BaseUrl))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        bookService = retrofit.create(BookService::class.java)
    }

    private fun setFloatingActionButton() {
        // FAB - GONE
        binding.addMemoFAB.isVisible = false
        binding.addMemoText.isVisible = false
        binding.startReadingFAB.isVisible = false
        binding.startReadingText.isVisible = false

        binding.extendedFAB.shrink()

        binding.extendedFAB.setOnClickListener {
            if(!isAllFabsVisible){
                binding.addMemoFAB.show()
                binding.addMemoText.isVisible = true
                binding.startReadingFAB.show()
                binding.startReadingText.isVisible = true

                binding.extendedFAB.extend()
                binding.extendedFAB.setIconResource(R.drawable.ic_clear)

                isAllFabsVisible = true
            }else{
                binding.addMemoFAB.hide()
                binding.addMemoText.isVisible = false
                binding.startReadingFAB.hide()
                binding.startReadingText.isVisible = false

                binding.extendedFAB.shrink()
                binding.extendedFAB.setIconResource(R.drawable.ic_plus)

                isAllFabsVisible = false
            }
        }


    }

    private fun setUI(isbn: String) {
        // getData
        bookService.getSelectedBook(getString(R.string.APIKey), isbn)
            .enqueue(object : Callback<SelectedBookDto> {
                override fun onResponse(
                    call: Call<SelectedBookDto>,
                    response: Response<SelectedBookDto>
                ) {
                    val item = response.body()?.item?.get(0)
                    // UI
                    binding.titleTextView.text = item?.title.orEmpty()
                    binding.authorTextView.text = item?.author.orEmpty()
                    binding.descriptionTextView.text =
                        if (item?.description != "") item?.description.orEmpty() else "x"
                    binding.publisherTextView.text = item?.publisher.orEmpty()
                    binding.ISBNTextView.text = if(item?.isbn13 != "") item?.isbn13.orEmpty() else item?.isbn10.orEmpty()
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



    // 찜한 책인지 아닌지 불러오기
    private fun loadLike(isbn: String) {
        Thread {
            val isLike = db.likeDao().getIsLike(isbn.toLong())
            runOnUiThread {
                binding.likeButton.rating = if (isLike == true) 1.0f else 0f
            }
        }.start()

    }

    // 찜 or 찜 제거
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    private fun saveLike(isbn: String) {
        binding.likeButton.setOnTouchListener { v, event ->
            binding.likeButton.setOnRatingChangeListener { ratingBar, rating, fromUser ->
                if (rating.toInt() == 1) {
                    Thread {
                        db.likeDao().saveLike(
                            Like(
                                isbn.toLong(),
                                true,
                                thisBook.title,
                                thisBook.coverSmallUrl,
                                thisBook.author,
                                thisBook.description,
                                System.currentTimeMillis()
                            )
                        )
                    }.start()

                    Toast.makeText(this, "찜한 목록에 책을 추가하였습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Thread {
                        db.likeDao().deleteLike(isbn.toLong())
                    }.start()

                    Toast.makeText(this, "찜한 목록에 책을 제외하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            return@setOnTouchListener false
        }
    }

    // 별점 불러오기
    private fun loadRating(isbn: String) {
        Thread{
            val rating = db.ratingDao().getRating(isbn.toLong())
            binding.ratingBar.rating = rating
        }.start()
    }

    // 별점 저장
    private fun saveRating(isbn: String) {
        binding.ratingBar.setOnRatingChangeListener { ratingBar, rating, fromUser ->
            Thread{
                db.ratingDao().saveRating(
                    Rating(
                        isbn.toLong(),
                        rating
                    )
                )
            }.start()
        }


    }

    fun addMemo(view: View) {
        val intent = Intent(this, MemoActivity::class.java)
        intent.putExtra("thisBookIsbn", isbn)
        startActivity(intent)

    }

    fun startReading(view: View) {

    }


    companion object {
        private const val TAG = "DetailActivity"
    }


}