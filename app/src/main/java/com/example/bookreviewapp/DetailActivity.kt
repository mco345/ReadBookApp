package com.example.bookreviewapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.example.bookreviewapp.model.room.Reading
import com.google.gson.internal.bind.util.ISO8601Utils.format
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.String.format
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding // view binding
    private lateinit var bookService: BookService
    private lateinit var db: AppDatabase

    private lateinit var thisBook: Book
    private lateinit var isbn: String

    var thisBookState: String = "noRead"
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
        setIsRead()
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
                    binding.ISBNTextView.text =
                        if (item?.isbn13 != "") item?.isbn13.orEmpty() else item?.isbn10.orEmpty()
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

    private fun setIsRead(){
        // 읽는 중인 책인지 아닌지
        Thread{
            thisBookState = if(db.readingDao().getState(isbn.toLong()) == null) thisBookState else db.readingDao().getState(isbn.toLong())
            Log.d(TAG, "state : $thisBookState")
            if(thisBookState == "isReading"){
                runOnUiThread {
                    binding.isReadTextView.text = "읽는 중"
                    binding.isReadTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
                    binding.isReadTextView.setBackgroundResource(R.drawable.background_radius_orange)
                }
            }
        }.start()
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun startReading(view: View) {
        val startReadingDialog = Dialog(this)

        initReadingDialog(startReadingDialog)
        saveReadingDialog(startReadingDialog)



    }

    private fun initReadingDialog(dialog: Dialog){

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_start_reading)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveReadingDialog(dialog: Dialog){
        val pageEditText: EditText = dialog.findViewById(R.id.pageEditText)
        val totalPage: TextView = dialog.findViewById(R.id.totalPage)
        val startReadingDateText: TextView = dialog.findViewById(R.id.startReadingDateText)
        val targetReadingDateText: TextView = dialog.findViewById(R.id.targetReadingDateText)
        val okButton: Button = dialog.findViewById(R.id.OkButton)

        // 총 페이지 TextView
        totalPage.text = "/ ${thisBook.subInfo.itemPage}"


        val currentDate = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE)    // 현재 날짜(yyyy-MM-dd 형식)
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // 독서 시작 날짜
        var startDate = currentDate
        startReadingDateText.text = currentDate
        startReadingDateText.setOnClickListener {
            val cal = Calendar.getInstance()    //캘린더뷰 만들기
            val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                startDate = dateFormatter.format(Date(year-1900, month, dayOfMonth))
                startReadingDateText.text = startDate
            }
            DatePickerDialog(
                this, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(
                    Calendar.DAY_OF_MONTH
                )
            ).show()
        }

        // 목표 완독 날짜
        targetReadingDateText.text = currentDate
        var targetDate = currentDate
        targetReadingDateText.setOnClickListener {
            val cal = Calendar.getInstance()    //캘린더뷰 만들기
            val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                targetDate = dateFormatter.format(Date(year-1900, month, dayOfMonth))
                targetReadingDateText.text = targetDate
            }
            DatePickerDialog(
                this, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(
                    Calendar.DAY_OF_MONTH
                )
            ).show()
        }


        okButton.setOnClickListener {
            val startDateToInt = startDate.replace("-","").toInt()
            val targetDateToInt = targetDate.replace("-","").toInt()
            if(startDateToInt > targetDateToInt){
                Toast.makeText(this, "목표 완독 날짜를 다시 설정해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(pageEditText.length() == 0){
                Toast.makeText(this, "현재 페이지를 설정해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else if (pageEditText.text.toString().toInt() > thisBook.subInfo.itemPage.toInt()){
                Toast.makeText(this, "현재 페이지를 다시 설정해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Thread{
                db.readingDao().insertReading(
                    Reading(
                        isbn.toLong(),
                        "isReading",
                        0,
                        pageEditText.text.toString().toInt(),
                        startDate,
                        targetDate,
                        thisBook.title,
                        thisBook.author,
                        thisBook.coverSmallUrl,
                        thisBook.description
                    )
                )
                runOnUiThread {
                    binding.isReadTextView.text = "읽는 중"
                    binding.isReadTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
                    binding.isReadTextView.setBackgroundResource(R.drawable.background_radius_orange)
                }
            }.start()

            Toast.makeText(this, "읽는 중인 책에 추가되었습니다.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }

    fun startTimer(view: View) {
        val intent = Intent(this, TimerActivity::class.java)
        intent.putExtra("thisBookIsbn", isbn)
        startActivity(intent)
    }


    companion object {
        private const val TAG = "DetailActivity"
    }




}