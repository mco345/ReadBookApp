package com.example.bookreviewapp

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.room.Room
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.bookreviewapp.databinding.ActivityTimerBinding
import com.example.bookreviewapp.model.room.Reading
import com.example.bookreviewapp.sharedPreference.MyApplication
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.*


class TimerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimerBinding
    private lateinit var db: AppDatabase
    private lateinit var isbn: String
    private lateinit var thisBook: Reading
    private var timerTask: Timer? = null

    private var currentTime: Long = 0
    private var isStartTimer = false
    private var currentMilliSecond: Long = 0
    private var isTimer = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // getIntent
        isbn = intent.getStringExtra("selectedBookISBN").toString()



        // AppDatabase
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "BookSearchDB"
        ).build()


        checkIsTimer()  // check isTimer
        initBookInfo()
        setTimer()

        initToolbarNavigation()

    }

    // ???????????? ?????? ???????????? ????????? ????????? ??? UI ??????
    private fun checkIsTimer() {
        isTimer = MyApplication.prefs.getBoolean("isTimer", false)

        if(isTimer){
            currentTime = MyApplication.prefs.getLong("isTimerTime", 0)
            binding.timerTextView.text = timeToText(currentTime)
        }
    }

    // ??? ?????? ??????
    private fun initBookInfo() {
        Thread {
            thisBook = db.readingDao().getAllFromId(isbn.toLong())
            val bookImage = thisBook.coverUrl
            val bookTitle = thisBook.title
            val bookAuthor = thisBook.author
            val bookCurrentPage = thisBook.readingPage

            runOnUiThread {
                // bookImage
                Glide.with(this)
                    .load(bookImage)
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                    .into(binding.bookImage)

                // book title
                binding.titleTextView.text = bookTitle

                // book author
                binding.authorTextView.text = bookAuthor

                // book current page
                binding.pageTextView.text = "?????? ????????? : $bookCurrentPage"
            }
        }.start()
    }


    // db?????? ????????? ??? ????????? ??????
    // ???????????? ?????? ???????????? ????????? ?????? UI ?????????????????? ??????
    private fun setTimer() {
        if(isTimer) return

        Thread{
            currentTime = db.readingDao().getAllFromId(isbn.toLong()).readingTime ?: 0
            runOnUiThread {
                binding.timerTextView.text = timeToText(currentTime)
            }
        }.start()
    }

    // OnClick : ????????? ?????? ???
    fun timer(view: View) {
        if(!isTimer) {
            initPreferences()
            isTimer = true
        }

        if(isStartTimer){
            pauseTimer()
        }else{
            startTimer()
        }
    }

    // ????????? ?????? -> SharedPreference setting
    private fun initPreferences(){
        MyApplication.prefs.setBoolean("isTimer", true)
        MyApplication.prefs.setString("isTimerISBN", isbn)
        MyApplication.prefs.setLong("isTimerTime", currentTime)
    }

    // ????????? ??????
    private fun startTimer() {
        isStartTimer = true

        binding.playButton.setImageResource(R.drawable.pause_button)

        timerTask = kotlin.concurrent.timer(period = 10) {	// timer() ??????
            currentMilliSecond++

            // 1?????????
            if(currentMilliSecond == 100L){
                currentMilliSecond = 0
                currentTime++	// period=10, 0.01????????? currentTime??? 1??? ??????

                MyApplication.prefs.setLong("isTimerTime", currentTime) // Pref ??????
                Log.d(TAG, "isTimerTime : ${MyApplication.prefs.getLong("isTimerTime", 0)}")

                var currentTimeText = timeToText(currentTime)   // ??? -> ?????????

                // UI????????? ?????? ?????????
                runOnUiThread {
                    binding.timerTextView.text = currentTimeText
                }
            }

        }
    }

    // ????????? ??????
    private fun pauseTimer(){
        isStartTimer = false

        binding.playButton.setImageResource(R.drawable.play_button)

        timerTask?.cancel()
    }

    // OnClick : ?????? ??? ??????
    fun saveTimer(view: View) {
        // TODO currentTime db??? ??????
        val startTimerDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialog)
        startTimerDialog.setContentView(R.layout.dialog_timer)
        startTimerDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        startTimerDialog.show()

        save(startTimerDialog)

    }

    private fun save(dialog: BottomSheetDialog) {
        val isReadingButton: LinearLayout = dialog.findViewById(R.id.isReading)!!
        val isReadingTextView: TextView = dialog.findViewById(R.id.isReadingText)!!
        val afterReadingButton: LinearLayout = dialog.findViewById(R.id.afterReading)!!
        val afterReadingTextView: TextView = dialog.findViewById(R.id.afterReadingText)!!
        val timerTextView: TextView = dialog.findViewById(R.id.timerTextView)!!
        val pageEditText: EditText = dialog.findViewById(R.id.pageEditText)!!
        val totalPageTextView: TextView = dialog.findViewById(R.id.totalPage)!!
        val readingLayout: LinearLayout = dialog.findViewById(R.id.readingLayout)!!
        val okButton: Button = dialog.findViewById(R.id.OkButton)!!

        // ????????? ??????
        timerTask?.cancel()

        // state ??????
        var state = "isReading"
        isReadingButton.setOnClickListener {
            state = "isReading"

            isReadingButton.setBackgroundResource(R.drawable.background_radius_20_stroke_orange)
            isReadingTextView.setTextColor(ContextCompat.getColor(this, R.color.orange))
            afterReadingButton.setBackgroundResource(R.drawable.background_radius_20_stroke_gray)
            afterReadingTextView.setTextColor(ContextCompat.getColor(this, R.color.textPrimaryColor))

            readingLayout.isVisible = true
        }
        afterReadingButton.setOnClickListener {
            state = "finishReading"

            isReadingButton.setBackgroundResource(R.drawable.background_radius_20_stroke_gray)
            isReadingTextView.setTextColor(ContextCompat.getColor(this, R.color.textPrimaryColor))
            afterReadingButton.setBackgroundResource(R.drawable.background_radius_20_stroke_orange)
            afterReadingTextView.setTextColor(ContextCompat.getColor(this, R.color.orange))

            readingLayout.isVisible = false
        }

        // ?????????
        pageEditText.setText(thisBook.readingPage.toString())

        // ??? ?????????
        val totalPage = thisBook.totalPage!!
        totalPageTextView.text = "/$totalPage"

        // ????????? TextView
        timerTextView.text = timeToText(currentTime)

        // ?????? ?????? ?????????
        okButton.setOnClickListener {
            when(state){
                "isReading" -> {
                    if (pageEditText.length() == 0) {
                        Toast.makeText(this, "?????? ???????????? ??????????????????.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    } else if (pageEditText.text.toString().toInt() > totalPage) {
                        Toast.makeText(this, "?????? ???????????? ?????? ??????????????????.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    Thread{
                        // db ??????
                        db.readingDao().updatePage(isbn.toLong(), pageEditText.text.toString().toInt())
                        db.readingDao().updateTimer(isbn.toLong(), currentTime)
                    }.start()
                    Toast.makeText(this, "???????????? ??????????????? ?????????????????????.", Toast.LENGTH_SHORT).show()
                }
                "finishReading" -> {
                    Thread{
                        // db ??????
                        db.readingDao().updateTimer(isbn.toLong(), currentTime)
                        db.readingDao().updateReadingState(isbn.toLong(), "finishReading", System.currentTimeMillis())
                    }.start()
                    Toast.makeText(this, "????????? ??? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show()
                }
            }

            // Preference ?????????
            MyApplication.prefs.setBoolean("isTimer", false)
            MyApplication.prefs.setString("isTimerISBN", "no isbn")
            MyApplication.prefs.setLong("isTimerTime", 0)


            finish()
        }


    }

    fun addMemo(view: View) {
        val intent = Intent(this, MemoActivity::class.java)
        intent.putExtra("selectedBookISBN", isbn)
        startActivity(intent)
    }

    override fun onBackPressed() {
        if(MyApplication.prefs.getBoolean("isTimer", false)){
            val builder = AlertDialog.Builder(this)

            builder.setTitle("????????? ?????? ??????")
                .setMessage("?????? ???????????? ???????????? ?????? ????????????????????????? ??????????????? ?????? ?????? ????????? ????????? ???????????? ?????? ????????? ??? ????????????.")
                .setPositiveButton("???",
                    DialogInterface.OnClickListener { dialog, id ->
                        timerTask?.cancel() // ????????? ??????

                        // Preference ?????????
                        MyApplication.prefs.setBoolean("isTimer", false)
                        MyApplication.prefs.setString("isTimerISBN", "no isbn")
                        MyApplication.prefs.setLong("isTimerTime", 0)

                        // ??????
                        finish()
                    })
                .setNegativeButton("?????????", null)
            // ?????????????????? ????????????
            builder.show()
        }else{
            super.onBackPressed()
        }
    }

    private fun timeToText(time: Long): String{
        var hour = 0
        var minute = 0
        var second = 0

        hour = (time / 3600).toInt()
        minute = (time / 60 % 60).toInt()
        second = (time % 60).toInt()

        Log.d(TAG, "time : $time, hour : $hour, minute : $minute, second : $second")

        return String.format("%02d:%02d:%02d", hour, minute, second)
    }


    private fun initToolbarNavigation() {
        binding.toolbar.setNavigationOnClickListener {
            if(MyApplication.prefs.getBoolean("isTimer", false)){
                val builder = AlertDialog.Builder(this)

                builder.setTitle("????????? ?????? ??????")
                    .setMessage("?????? ???????????? ???????????? ?????? ????????????????????????? ??????????????? ?????? ?????? ????????? ????????? ???????????? ?????? ????????? ??? ????????????.")
                    .setPositiveButton("???",
                        DialogInterface.OnClickListener { dialog, id ->
                            timerTask?.cancel() // ????????? ??????

                            // Preference ?????????
                            MyApplication.prefs.setBoolean("isTimer", false)
                            MyApplication.prefs.setString("isTimerISBN", "no isbn")
                            MyApplication.prefs.setLong("isTimerTime", 0)

                            // ??????
                            finish()
                        })
                    .setNegativeButton("?????????", null)
                // ?????????????????? ????????????
                builder.show()
            }else{
                finish()
            }
        }
    }

    companion object{
        private const val TAG = "TimerActivity"
    }




}