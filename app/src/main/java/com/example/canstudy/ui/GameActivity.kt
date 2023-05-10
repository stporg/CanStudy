package com.example.canstudy.ui

import android.app.Dialog
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.example.canstudy.Constants
import com.example.canstudy.R
import com.example.canstudy.databinding.ActivityGameBinding
import com.example.canstudy.databinding.DialogExitGameBinding

class GameActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding : ActivityGameBinding
    private lateinit var tvDifficultySetting : TextView
    private lateinit var tvCountdown : TextView
    private lateinit var tvGameTime : TextView
    private lateinit var tvGameScore : TextView
    private lateinit var tvGameEnglishDescription : TextView
    private lateinit var btnEasyDifficulty: Button
    private lateinit var btnMediumDifficulty: Button
    private lateinit var btnHardDifficulty: Button
    private lateinit var llGameScore: LinearLayout
    private lateinit var llOptionA: LinearLayout
    private lateinit var llOptionB: LinearLayout
    private lateinit var llOptionC: LinearLayout
    private lateinit var llOptionD: LinearLayout

    private var difficultySetting = "Easy"

    private var countdownTimer: CountDownTimer? = null
    private var gameTimer: CountDownTimer? = null
    private var countdownTime = 3
    private var gameTime = 60

    private lateinit var progressBar: ProgressBar

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initialiseActivity()

        btnEasyDifficulty.setOnClickListener(this)
        btnMediumDifficulty.setOnClickListener(this)
        btnHardDifficulty.setOnClickListener(this)
    }

    private fun initialiseActivity() {
        setSupportActionBar(binding.toolbarGame)
        if(supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "Game"
        }

        //TODO: add a dialog box saying "are you sure you want to exit while the game is playing"
        binding.toolbarGame.setNavigationOnClickListener { onBackPressed() }

        tvDifficultySetting = binding.tvDifficultySetting
        tvCountdown = binding.tvCountdown
        tvGameTime = binding.tvGameTime
        tvGameScore = binding.tvGameScore
        tvGameEnglishDescription = binding.tvGameEnglishDescription
        btnEasyDifficulty = binding.btnEasyDifficulty
        btnMediumDifficulty = binding.btnMediumDifficulty
        btnHardDifficulty = binding.btnHardDifficulty
        llGameScore = binding.llGameScore
        llOptionA = binding.llOptionA
        llOptionB = binding.llOptionB
        llOptionC = binding.llOptionC
        llOptionD = binding.llOptionD

        progressBar = binding.progressBarGame
        mediaPlayer = MediaPlayer.create(this@GameActivity, R.raw.yummy_dim_sum)
    }

    override fun onClick(view: View?) {
        var button = view as Button

        if (button.text.toString() == "Easy") {
            difficultySetting = Constants.EASY_DIFFICULTY
        } else if (button.text.toString() == "Med") {
            difficultySetting = Constants.MEDIUM_DIFFICULTY
        } else {
            difficultySetting = Constants.HARD_DIFFICULTY
        }

        setupGame()
    }

    private fun setupGame() {
        tvDifficultySetting.visibility = View.GONE
        btnEasyDifficulty.visibility = View.GONE
        btnMediumDifficulty.visibility = View.GONE
        btnHardDifficulty.visibility = View.GONE
        tvCountdown.visibility = View.VISIBLE

        countdownTimer = object : CountDownTimer((countdownTime * 1000).toLong(), 1000) {
            override fun onTick(p0: Long) {
                tvCountdown.text = countdownTime.toString()
                countdownTime--
            }

            override fun onFinish() {
                tvCountdown.visibility = View.GONE
                startGame()
            }
        }.start()
    }

    private fun startGame() {
        // mediaPlayer.start()
        binding.toolbarGame.title = "Game - $difficultySetting"
        progressBar.visibility = View.VISIBLE
        tvGameTime.visibility = View.VISIBLE
        tvGameScore.visibility = View.VISIBLE
        tvGameEnglishDescription.visibility = View.VISIBLE
        llGameScore.visibility = View.VISIBLE
        llOptionA.visibility = View.VISIBLE
        llOptionB.visibility = View.VISIBLE
        llOptionC.visibility = View.VISIBLE
        llOptionD.visibility = View.VISIBLE

        progressBar.max = 60
        var currentTime = 60

        gameTimer = object : CountDownTimer((gameTime * 1000).toLong(), 1000) {
            override fun onTick(p0: Long) {
                progressBar.progress = currentTime
                tvGameTime.text = currentTime.toString()
                currentTime--
            }

            override fun onFinish() {
                 // mediaPlayer.stop()
            }
        }.start()
    }

    override fun onBackPressed() {
        var customDialog = Dialog(this)
        val dialogBinding = DialogExitGameBinding.inflate(layoutInflater)
        customDialog.setContentView(dialogBinding.root)
        customDialog.setCanceledOnTouchOutside(false)

        dialogBinding.btnGameDialogYes.setOnClickListener {
            this@GameActivity.finish()
            customDialog.dismiss()
        }
        dialogBinding.btnGameDialogNo.setOnClickListener {
            customDialog.dismiss()
        }
        customDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}