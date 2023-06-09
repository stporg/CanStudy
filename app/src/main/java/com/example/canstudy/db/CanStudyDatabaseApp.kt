package com.example.canstudy

import android.app.Application
import com.example.canstudy.db.WordDatabase
import kotlin.random.Random

class CanStudyApp: Application() {
    val db by lazy {
        WordDatabase.getInstance(this)
    }
}