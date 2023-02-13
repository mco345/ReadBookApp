package com.example.bookreviewapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
                        if (item?.description != "")
                            item?.description.orEmpty()
                            .replace("&lt;", "<")
                            .replace("&gt;", ">")
                            .replace("&amp;", "&")
                            .replace("&quot;", "\"")
                        else "x"
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
            thisBookState = if(db.readingDao().getAllFromId(isbn.toLong())?.state == null) thisBookState else db.readingDao().getAllFromId(
                isbn.toLong()
            )?.state.toString()
            Log.d(TAG, "state : $thisBookState")
            runOnUiThread {
                when(thisBookState){
                    "noRead" -> {
                        binding.isReadTextView.text = "독서 시작하기"
                        binding.isReadTextView.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.orange
                            )
                        )
                        binding.isReadTextView.setBackgroundResource(R.drawable.background_radius_20_stroke_orange)
                    }
                    "isReading" -> {
                        binding.isReadTextView.text = "읽는 중"
                        binding.isReadTextView.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.white
                            )
                        )
                        binding.isReadTextView.setBackgroundResource(R.drawable.background_radius_orange)
                    }
                    "finishReading" -> {
                        binding.isReadTextView.text = "완독함"
                        binding.isReadTextView.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.white
                            )
                        )
                        binding.isReadTextView.setBackgroundResource(R.drawable.background_radius_violet)
                    }
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
        val startReadingDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialog)

        initReadingDialog(startReadingDialog)
        saveReadingDialog(startReadingDialog)

    }

    private fun initReadingDialog(dialog: BottomSheetDialog){

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        when(thisBookState){
            "noRead" -> dialog.setContentView(R.layout.dialog_start_reading)
            "isReading" -> dialog.setContentView(R.layout.dialog_reading)
            "finishReading" -> dialog.setContentView(R.layout.dialog_finish_reading)
        }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveReadingDialog(dialog: BottomSheetDialog){
        when(thisBookState){
            "noRead" -> startReadingDialog(dialog)
            "isReading" -> isReadingDialog(dialog)
            "finishReading" -> finishReadingDialog(dialog)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startReadingDialog(dialog: BottomSheetDialog) {
        val pageEditText: EditText = dialog.findViewById(R.id.pageEditText)!!
        val totalPage: TextView = dialog.findViewById(R.id.totalPage)!!
        val startReadingDateText: TextView = dialog.findViewById(R.id.startReadingDateText)!!
        val targetDateTextView: TextView = dialog.findViewById(R.id.targetDateTextView)!!
        val targetReadingDateText: TextView = dialog.findViewById(R.id.targetReadingDateText)!!
        val targetDateCheckBox: CheckBox = dialog.findViewById(R.id.targetDateCheckBox)!!
        val okButton: Button = dialog.findViewById(R.id.OkButton)!!

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
                startDate = dateFormatter.format(Date(year - 1900, month, dayOfMonth))
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
                targetDate = dateFormatter.format(Date(year - 1900, month, dayOfMonth))
                targetReadingDateText.text = targetDate
            }
            DatePickerDialog(
                this, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(
                    Calendar.DAY_OF_MONTH
                )
            ).show()
        }

        // 목료 완독 날짜 CheckBox
        var targetChecked = true
        targetDateCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            when(isChecked){
                true -> {
                    targetDateTextView.isEnabled = true
                    targetReadingDateText.isEnabled = true
                    targetDateTextView.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black
                        )
                    )
                    targetReadingDateText.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black
                        )
                    )
                    targetChecked = true
                }
                false -> {
                    targetDateTextView.isEnabled = false
                    targetReadingDateText.isEnabled = false
                    targetDateTextView.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.textPrimaryColor
                        )
                    )
                    targetReadingDateText.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.textPrimaryColor
                        )
                    )
                    targetChecked = false
                }
            }
        }


        okButton.setOnClickListener {
            if(targetChecked){
                val startDateToInt = startDate.replace("-", "").toInt()
                val targetDateToInt = targetDate.replace("-", "").toInt()
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
            }

            Thread{
                thisBookState = "isReading"
                db.readingDao().insertReading(
                    Reading(
                        isbn.toLong(),
                        thisBookState,
                        0,
                        pageEditText.text.toString().toInt(),
                        thisBook.subInfo.itemPage.toInt(),
                        System.currentTimeMillis(),
                        startDate,
                        targetChecked,
                        if (targetChecked) targetDate else null,
                        null,
                        thisBook.title,
                        thisBook.author,
                        thisBook.coverSmallUrl,
                        thisBook.description
                    )
                )
            }.start()

            Toast.makeText(this, "읽는 중인 책에 추가되었습니다.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()

            renewActivity()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isReadingDialog(dialog: BottomSheetDialog) {
        val beforeReadingButton: LinearLayout = dialog.findViewById(R.id.beforeReading)!!
        val beforeReadingText: TextView = dialog.findViewById(R.id.beforeReadingText)!!
        val isReadingButton: LinearLayout = dialog.findViewById(R.id.isReading)!!
        val isReadingText: TextView = dialog.findViewById(R.id.isReadingText)!!
        val afterReadingButton: LinearLayout = dialog.findViewById(R.id.afterReading)!!
        val afterReadingText: TextView = dialog.findViewById(R.id.afterReadingText)!!
        val isReadingLayout: LinearLayout = dialog.findViewById(R.id.isReadingLayout)!!
        val pageEditText: EditText = dialog.findViewById(R.id.pageEditText)!!
        val totalPage: TextView = dialog.findViewById(R.id.totalPage)!!
        val startReadingDateText: TextView = dialog.findViewById(R.id.startReadingDateText)!!
        val targetDateTextView: TextView = dialog.findViewById(R.id.targetDateTextView)!!
        val targetReadingDateText: TextView = dialog.findViewById(R.id.targetReadingDateText)!!
        val okButton: Button = dialog.findViewById(R.id.OkButton)!!

        // Reading DB 불러오기
        var page: Int? = null
        var startDate: String? = null
        var targetChecked: Boolean? = null
        var targetDate: String? = null
        Thread{
            val thisBookReading = db.readingDao().getAllFromId(isbn.toLong())
            page = thisBookReading.readingPage
            startDate = thisBookReading.startDate
            targetChecked = thisBookReading.targetChecked
            targetDate = thisBookReading.targetDate
            runOnUiThread {
                pageEditText.setText(page.toString())
                startReadingDateText.text = startDate
                if(targetChecked == true)
                    targetReadingDateText.text = targetDate
                else {
                    targetReadingDateText.isVisible = false
                    targetDateTextView.isVisible = false
                }

            }

        }.start()

        // state 선택
        var stateSelected = thisBookState
        beforeReadingButton.setOnClickListener {
            stateSelected = "noRead"
            // 선택창 UI Setting
            beforeReadingButton.setBackgroundResource(R.drawable.background_radius_20_stroke_orange)
            beforeReadingText.setTextColor(ContextCompat.getColor(this, R.color.orange))
            isReadingButton.setBackgroundResource(R.drawable.background_radius_20_stroke_gray)
            isReadingText.setTextColor(ContextCompat.getColor(this, R.color.textPrimaryColor))
            afterReadingButton.setBackgroundResource(R.drawable.background_radius_20_stroke_gray)
            afterReadingText.setTextColor(ContextCompat.getColor(this, R.color.textPrimaryColor))
            // 나머지 View
            isReadingLayout.isVisible = false
        }
        isReadingButton.setOnClickListener {
            stateSelected = "isReading"
            // 선택창 UI Setting
            beforeReadingButton.setBackgroundResource(R.drawable.background_radius_20_stroke_gray)
            beforeReadingText.setTextColor(ContextCompat.getColor(this, R.color.textPrimaryColor))
            isReadingButton.setBackgroundResource(R.drawable.background_radius_20_stroke_orange)
            isReadingText.setTextColor(ContextCompat.getColor(this, R.color.orange))
            afterReadingButton.setBackgroundResource(R.drawable.background_radius_20_stroke_gray)
            afterReadingText.setTextColor(ContextCompat.getColor(this, R.color.textPrimaryColor))
            // 나머지 View
            isReadingLayout.isVisible = true
        }
        afterReadingButton.setOnClickListener {
            stateSelected = "finishReading"
            // 선택창 UI Setting
            beforeReadingButton.setBackgroundResource(R.drawable.background_radius_20_stroke_gray)
            beforeReadingText.setTextColor(ContextCompat.getColor(this, R.color.textPrimaryColor))
            isReadingButton.setBackgroundResource(R.drawable.background_radius_20_stroke_gray)
            isReadingText.setTextColor(ContextCompat.getColor(this, R.color.textPrimaryColor))
            afterReadingButton.setBackgroundResource(R.drawable.background_radius_20_stroke_orange)
            afterReadingText.setTextColor(ContextCompat.getColor(this, R.color.orange))
            // 나머지 View
            isReadingLayout.isVisible = false
        }

        // 페이지
        totalPage.text = "/ ${thisBook.subInfo.itemPage}"

        val currentDate = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE)    // 현재 날짜(yyyy-MM-dd 형식)
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // 독서 시작 날짜
        startReadingDateText.setOnClickListener {
            val cal = Calendar.getInstance()    //캘린더뷰 만들기
            val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                startDate = dateFormatter.format(Date(year - 1900, month, dayOfMonth))
                startReadingDateText.text = startDate
            }
            DatePickerDialog(
                this, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(
                    Calendar.DAY_OF_MONTH
                )
            ).show()
        }

        // 목표 완독 날짜
        targetReadingDateText.setOnClickListener {
            val cal = Calendar.getInstance()    //캘린더뷰 만들기
            val dateSetListener =
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    targetDate = dateFormatter.format(Date(year - 1900, month, dayOfMonth))
                    targetReadingDateText.text = targetDate
                }
            DatePickerDialog(
                this, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(
                    Calendar.DAY_OF_MONTH
                )
            ).show()
        }

        // 확인 버튼
        okButton.setOnClickListener {
            Log.d(TAG, "state : $stateSelected")
            when(stateSelected){
                "noRead" -> {
                    Thread {
                        db.readingDao().delete(isbn.toLong())
                    }.start()
                    Toast.makeText(this, "읽는 책 목록에서 삭제했습니다.", Toast.LENGTH_SHORT).show()

                }
                "isReading" -> {
                    if (targetChecked == true) {
                        var startDateToInt = startDate?.replace("-", "")?.toInt()
                        var targetDateToInt = targetDate?.replace("-", "")?.toInt()
                        if (startDateToInt!! > targetDateToInt!!) {
                            Toast.makeText(this, "목표 완독 날짜를 다시 설정해주세요.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        if (pageEditText.length() == 0) {
                            Toast.makeText(this, "현재 페이지를 설정해주세요.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        } else if (pageEditText.text.toString()
                                .toInt() > thisBook.subInfo.itemPage.toInt()
                        ) {
                            Toast.makeText(this, "현재 페이지를 다시 설정해주세요.", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                    }
                    Log.d(TAG, "현재 페이지 : ${pageEditText.text.toString().toInt()}")
                    Thread {
                        db.readingDao().updateReading(
                            isbn.toLong(),
                            pageEditText.text.toString().toInt(),
                            startDate,
                            targetDate
                        )
                    }.start()
                    Toast.makeText(this, "수정이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                }
                "finishReading" -> {
                    Thread {
                        db.readingDao().updateReadingState(
                            isbn.toLong(),
                            stateSelected,
                            System.currentTimeMillis()
                        )
                    }.start()
                    Toast.makeText(this, "완독한 책 목록에 추가하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            renewActivity()

            dialog.dismiss()
        }
    }

    private fun finishReadingDialog(dialog: BottomSheetDialog) {
        val resetButton: ImageButton = dialog.findViewById(R.id.resetButton)!!
        val startDateTextView: TextView = dialog.findViewById(R.id.startDateTextView)!!
        val targetDateTextView: TextView = dialog.findViewById(R.id.targetDateTextView)!!
        val finishDateTextView: TextView = dialog.findViewById(R.id.finishDateTextView)!!
        val timerTextView: TextView = dialog.findViewById(R.id.timerTextView)!!

        // 리셋 버튼
        resetButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("초기화")
                .setMessage("초기화하시면 독서 정보가 모두 삭제됩니다. 정말 초기화하시겠습니까?")
                .setPositiveButton("확인",
                    DialogInterface.OnClickListener { dialog, id ->
                        Thread{
                            db.readingDao().delete(isbn.toLong())
                        }.start()
                        renewActivity()
                    })
                .setNegativeButton("취소", null)
            // 다이얼로그를 띄워주기
            builder.show()
        }

        Thread{
            val thisBookReading = db.readingDao().getAllFromId(isbn.toLong())
            val startDate = thisBookReading.startDate
            val targetDate = thisBookReading.targetDate
            val finishDate = SimpleDateFormat("yyyy-MM-dd", Locale("ko", "KR")).format(Date(thisBookReading.finishTime!!))
            runOnUiThread {
                startDateTextView.text = startDate
                targetDateTextView.text = targetDate
                finishDateTextView.text = finishDate
            }
        }.start()

    }

    fun startTimer(view: View) {
        val intent = Intent(this, TimerActivity::class.java)
        intent.putExtra("thisBookIsbn", isbn)
        startActivity(intent)
    }

    fun renewActivity(){
        finish() //인텐트 종료
        overridePendingTransition(0, 0) //인텐트 효과 없애기
        val intent = intent //인텐트
        intent.putExtra("selectedBookISBN", isbn)
        startActivity(intent) //액티비티 열기
        overridePendingTransition(0, 0) //인텐트 효과 없애기
    }

    fun backButtonClicked(view: View) {
        finish()
    }


    companion object {
        private const val TAG = "DetailActivity"
    }




}