package com.example.canstudy.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.canstudy.CanStudyApp
import com.example.canstudy.databinding.ActivityReviewBinding
import com.example.canstudy.db.adapter.WordAdapter
import com.example.canstudy.db.dao.WordDao
import com.example.canstudy.db.entity.WordEntity
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ReviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewBinding
    private lateinit var tvNoWrongWordsFound: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialiseActivity()

        val intent = intent
        val wrongWordList = intent.getIntegerArrayListExtra("key")
        val dao = (application as CanStudyApp).db.wordDao()

        if (wrongWordList != null) {
            setupWordRecyclerView(dao, wrongWordList)
        } else {
            tvNoWrongWordsFound.visibility = VISIBLE
        }
    }

    private fun initialiseActivity() {
        setSupportActionBar(binding.toolbarReviewActivity)
        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Review"
        }

        tvNoWrongWordsFound = binding.tvNoWrongWordsFound

        binding?.toolbarReviewActivity?.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupWordRecyclerView(wordDao: WordDao, wrongWordList: ArrayList<Int>) {
        val wordList = ArrayList<WordEntity>()
        lifecycleScope.launch {
            wordDao.readAll().collect { allWordsList ->
                if (allWordsList.isNotEmpty()) {
                    binding.rvReview.layoutManager = LinearLayoutManager(this@ReviewActivity)
                    for (word in allWordsList) {
                        val newWord = WordEntity(
                            word.ID,
                            word.CANTO_WORD,
                            word.ENGLISH_WORD,
                            word.CORRECT_STATUS
                        )
                        wordList.add(newWord)
                    }
                    attachAdapter(wordList)
                }
            }
        }
    }

    private fun attachAdapter(list: ArrayList<WordEntity>) {
        val wordAdapter = WordAdapter(list)
        binding.rvReview.adapter = wordAdapter
    }
}