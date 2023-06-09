package com.example.canstudy.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.canstudy.CanStudyApp
import com.example.canstudy.db.adapter.WordAdapter
import com.example.canstudy.databinding.ActivitySearchBinding
import com.example.canstudy.databinding.DialogAddWordBinding
import com.example.canstudy.db.dao.WordDao
import com.example.canstudy.db.entity.WordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList

/**
 * A class that allows the user to search words in English or Cantonese.
 */

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var languageSelected: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialiseActivity()

        val dao = (application as CanStudyApp).db.wordDao()

        binding.btnAddWord.setOnClickListener {
            addWordDialog(dao)
        }
    }

    /**
     * A function that initialises the word list and UI components.
     */
    private fun initialiseActivity() {
        setSupportActionBar(binding.toolbarSearchActivity)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Search word/phrase"
        }
        binding.toolbarSearchActivity?.setNavigationOnClickListener { onBackPressed() }

        val dao = (application as CanStudyApp).db.wordDao()

        languageSelected = "English"

        binding.etSearchBar.requestFocus()

        setupRadioGroupListener()
        setupWordRecyclerView(dao)
        setupSearchListener()
    }

    /**
     * A function to add a word to the Word Database.
     */
    private fun addWordDialog(wordDao: WordDao) {
        val wordDialog = Dialog(this)
        val dialogBinding = DialogAddWordBinding.inflate(layoutInflater)
        wordDialog.setContentView(dialogBinding.root)
        wordDialog.setCanceledOnTouchOutside(true)

        // Make dialog match the width of the screen
        val window = wordDialog.window
        val layoutParams = window?.attributes
        layoutParams?.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = layoutParams

        dialogBinding.btnYes.setOnClickListener {
            val cantoneseWord = dialogBinding.etAddCantoneseWord.text.toString()
            val englishWord = dialogBinding.etAddEnglishWord.text.toString()
            val numberOfEnglishWords = englishWord.trim().split("\\s+".toRegex())
            when {
                dialogBinding.etAddCantoneseWord.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please enter a Cantonese word/phrase", Toast.LENGTH_SHORT).show()
                }
                dialogBinding.etAddEnglishWord.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please enter a English word/phrase", Toast.LENGTH_SHORT).show()
                } else -> {
                // TODO: add number_of_words textbox for user to enter, or calculate the number of words yourself
                    val newWord = WordEntity(0, cantoneseWord, englishWord, 1, numberOfEnglishWords.size)
                    lifecycleScope.launch {
                        // withContext(Dispatchers.IO) suspends the coroutine and switches the execution to a background thread pool provided by the 'Dispatchesr.IO' dispatcher.
                        // This avoids blocking the main thread which could cause the UI to freeze or become unresponsive.
                        withContext(Dispatchers.IO) {
                            wordDao.addWord(newWord)
                            setupWordRecyclerView(wordDao)
                        }
                    }
                    Toast.makeText(this@SearchActivity, "Successfully added", Toast.LENGTH_SHORT).show()
                    wordDialog.dismiss()
                }
            }
        }
        dialogBinding.btnNo.setOnClickListener {
            wordDialog.dismiss()
        }

        wordDialog.show()
    }

    /**
     * A function to set up the English and Cantonese radio buttons.
     */
    private fun setupRadioGroupListener() {
        binding.rgLanguage.setOnCheckedChangeListener { _, checkedId: Int ->
            if (checkedId == binding.rbEnglish.id) {
                binding.etSearchBar.setText("")
                setupSearchListener()
            } else {
                binding.etSearchBar.setText("")
                languageSelected = "Cantonese"
                setupSearchListener()
            }
        }
    }

    /**
     * A function to search and filter the English/Cantonese words by the user input.
     */
    private fun setupSearchListener() {
        binding.etSearchBar.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                val query = s.toString()
                filterWithQuery(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    /**
     * A function that initialises the RecyclerView for the search list.
     */
    private fun setupWordRecyclerView(wordDao: WordDao) {
        val wordList = ArrayList<WordEntity>()
        lifecycleScope.launch {
            wordDao.readAll().collect { allWordsList ->
                if (allWordsList.isNotEmpty()) {
                    binding.rvSearch.layoutManager = LinearLayoutManager(this@SearchActivity)
                    for (word in allWordsList) {
                        val newWord = WordEntity(
                            word.ID,
                            word.CANTO_WORD,
                            word.ENGLISH_WORD,
                            word.NEW_STATUS,
                            word.NUMBER_OF_WORDS
                        )
                        wordList.add(newWord)
                    }
                    attachAdapter(wordList)
                }
            }
        }
    }

    /**
     * A function that filters the word list by the user input.
     */
    private fun filterWithQuery(query: String) {
        val dao = (application as CanStudyApp).db.wordDao()
        getWordList(dao) { wordList ->
            val filteredList: ArrayList<WordEntity> = ArrayList()
            for (word in wordList) {
                if (languageSelected.equals("English")) {
                    if (word.getEnglishWord().lowercase().contains(query.lowercase())) {
                        filteredList.add(word)
                    }
                } else {
                    if (word.getCantoWord().lowercase().contains(query.lowercase())) {
                        filteredList.add(word)
                    }
                }
            }
            toggleRecyclerView(filteredList)
            attachAdapter(filteredList)
        }
    }

    /**
     * A function that filters the word list by the user input.
     */
    private fun getWordList(wordDao: WordDao, callback: (ArrayList<WordEntity>) -> Unit) {
        lifecycleScope.launch {
            wordDao.readAll().collect { allWordsList ->
                if (allWordsList.isNotEmpty()) {
                    val wordList = ArrayList<WordEntity>()
                    binding.rvSearch.layoutManager = LinearLayoutManager(this@SearchActivity)
                    for (word in allWordsList) {
                        val newWord = WordEntity(
                            word.ID,
                            word.CANTO_WORD,
                            word.ENGLISH_WORD,
                            word.NEW_STATUS,
                            word.NUMBER_OF_WORDS
                        )
                        wordList.add(newWord)
                    }
                    callback(wordList)
                }
            }
        }
    }

    /**
     * A function that passes the current array list to the RecyclerView adapter.
     */
    private fun attachAdapter(list: ArrayList<WordEntity>) {
        val wordAdapter = WordAdapter(list)
        binding.rvSearch.adapter = wordAdapter
    }

    /**
     * A function that changes the RecyclerView visibility based on the current word list
     */
    private fun toggleRecyclerView(wordList: ArrayList<WordEntity>) {
        if (wordList.isEmpty()) {
            binding.rvSearch.visibility = View.INVISIBLE
            binding.tvNoResultsFound.visibility = View.VISIBLE
        } else {
            binding.rvSearch.visibility = View.VISIBLE
            binding.tvNoResultsFound.visibility = View.INVISIBLE
        }
    }
}