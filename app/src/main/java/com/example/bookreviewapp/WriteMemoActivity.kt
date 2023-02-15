package com.example.bookreviewapp

import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.room.Room
import com.example.bookreviewapp.databinding.ActivityWriteMemoBinding
import com.example.bookreviewapp.model.room.Review
import java.time.LocalDateTime

class WriteMemoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWriteMemoBinding
    private lateinit var db: AppDatabase

    private lateinit var isbn: String

    private var isRevise: Boolean = false
    private var currentDate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteMemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isbn = intent.getStringExtra("selectedBookISBN").toString()
        isRevise = intent.getBooleanExtra("isRevise", false)
        if (isRevise) {
            val page = intent.getStringExtra("page")
            val review = intent.getStringExtra("review")
            currentDate = intent.getLongExtra("currentDate", 0)

            setUI(page, review)
        }

        Log.d(TAG, "isbn : $isbn")

        // Local DB(History) 생성
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "BookSearchDB"
        ).build()

        initToolbarNavigation()

    }


    // 수정 시에만
    private fun setUI(page: String?, review: String?) {
        binding.pageEditText.setText(page)
        binding.reviewEditText.setText(review)
        binding.deleteButton.isVisible = true
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun saveMemo(view: View) {
        if (binding.reviewEditText.text.isEmpty() || binding.pageEditText.text.isEmpty()) {
            Toast.makeText(this, "페이지 번호 또는 메모를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }
        Thread {
            db.reviewDao().saveReview(
                Review(
                    if (!isRevise) System.currentTimeMillis() else currentDate,
                    isbn.toLong(),
                    binding.reviewEditText.text.toString(),
                    binding.pageEditText.text.toString(),

                    )
            )

            val intent = Intent(this, MemoActivity::class.java)
            intent.putExtra("selectedBookISBN", isbn)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }.start()
    }

    fun deleteMemo(view: View) {
        // 다이얼로그를 생성하기 위해 Builder 클래스 생성자를 이용해 줍니다.
        val builder = AlertDialog.Builder(this)
        builder.setTitle("삭제?")
            .setMessage("정말 삭제하시겠습니까?")
            .setPositiveButton("네",
                DialogInterface.OnClickListener { dialog, id ->
                    Thread {
                        db.reviewDao().deleteReview(currentDate)

                        val intent = Intent(this, MemoActivity::class.java)
                        intent.putExtra("selectedBookISBN", isbn)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                    }.start()
                })
            .setNegativeButton("아니오",
                DialogInterface.OnClickListener { dialog, id ->
                })
        // 다이얼로그를 띄워주기
        builder.show()

    }

    private fun initToolbarNavigation() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    companion object {
        private const val TAG = "WriteMemoActivity"
    }


}