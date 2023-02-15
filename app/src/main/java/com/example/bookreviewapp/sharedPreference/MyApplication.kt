package com.example.bookreviewapp.sharedPreference

import android.app.Application

// 앱에 있는 다른 액티비티보다 먼저 생성되어야 다른 곳에 데이터를 넘겨줄 수 있으므로 Application에 해당하는 클래스를 생성
class MyApplication: Application() {
    companion object {
        lateinit var prefs: PreferenceUtil
    }

    override fun onCreate() {
        prefs = PreferenceUtil(applicationContext)
        super.onCreate()
    }
}