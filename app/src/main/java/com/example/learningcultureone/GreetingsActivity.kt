package com.example.learningcultureone

import android.app.AlertDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.*
import com.airbnb.lottie.LottieAnimationView
import java.util.*

class GreetingsActivity : BaseActivity() {

    private lateinit var dialogueBox: TextView
    private lateinit var pageIndicator: TextView
    private lateinit var listenButton: Button
    private lateinit var nextButton: Button
    private lateinit var prevButton: Button
    private lateinit var lottieSeed: LottieAnimationView
    private lateinit var lottiePopper: LottieAnimationView
    private lateinit var backButton: ImageView

    private var currentIndex = 0
    private var previousIndex = 0

    private lateinit var tts: TextToSpeech
    private var popperMediaPlayer: MediaPlayer? = null

    private val greetings = listOf(
        "السلام عليكم\nPeace be upon you\nSalam Alaikum",
        "صباح الخير\nGood morning\nSabah alkhayr",
        "مساء الخير\nGood evening\nMasa alkhayr",
        "مرحبا\nHello\nMarhaban",
        "كيف الحال؟\nHow are you?\nKayfa alhaal",
        "أنا بخير\nI am fine\nAna bekhayr",
        "شكرًا\nThank you\nShukran",
        "عفوًا\nYou're welcome\nAfwan",
        "نعم\nYes\nNa'am",
        "لا\nNo\nLa",
        "مع السلامة\nGoodbye\nMa’a as-salama",
        "تصبح على خير\nGood night\nTusbih ‘ala khayr",
        "من فضلك\nPlease\nMin fadlik",
        "آسف\nSorry\nAasif",
        "ما اسمك؟\nWhat is your name?\nMa ismuk?"
    )

    private val totalStages = greetings.size
    private val forwardStages = List(totalStages) { i ->
        i.toFloat() / (totalStages - 1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_greetings)

        setupBottomNavigation(R.id.navigation_home)
        supportActionBar?.hide()

        bindViews()
        initTTS()
        setupListeners()

        lottieSeed.progress = 0f
        lottieSeed.pauseAnimation()

        updatePage()
    }

    private fun bindViews() {
        dialogueBox = findViewById(R.id.dialogue_box)
        pageIndicator = findViewById(R.id.page_indicator)
        listenButton = findViewById(R.id.btn_read_aloud)
        nextButton = findViewById(R.id.btn_next)
        prevButton = findViewById(R.id.btn_prev)
        lottieSeed = findViewById(R.id.lottie_seed)
        lottiePopper = findViewById(R.id.lottie_popper)
        backButton = findViewById(R.id.btn_back)
    }

    private fun initTTS() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
                tts.setSpeechRate(1.0f)
                tts.setPitch(1.0f)
            } else {
                Toast.makeText(this, "TTS initialization failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            markLevelAsCompleted()
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        listenButton.setOnClickListener {
            val pronunciation = greetings[currentIndex].split("\n").getOrNull(2) ?: ""
            tts.speak(pronunciation, TextToSpeech.QUEUE_FLUSH, null, null)
        }

        nextButton.setOnClickListener {
            if (currentIndex < greetings.lastIndex) {
                previousIndex = currentIndex
                currentIndex++
                updatePage()
            } else {
                showCompletionEffect()
            }
        }

        prevButton.setOnClickListener {
            if (currentIndex > 0) {
                previousIndex = currentIndex
                currentIndex--
                updatePage()
            }
        }
    }

    private fun updatePage() {
        dialogueBox.text = greetings[currentIndex]

        pageIndicator.text = greetings.indices.joinToString(" ") {
            if (it == currentIndex) "●" else "○"
        }

        val startProgress = forwardStages[previousIndex]
        val endProgress = forwardStages[currentIndex]

        if (endProgress > startProgress) {
            lottieSeed.setMinAndMaxProgress(startProgress, endProgress)
            lottieSeed.speed = 1f
            lottieSeed.playAnimation()
        } else {
            lottieSeed.pauseAnimation()
            lottieSeed.progress = endProgress
        }

        nextButton.text = if (currentIndex == greetings.lastIndex) "Done" else "Next"
        prevButton.isEnabled = currentIndex > 0
    }

    private fun showCompletionEffect() {
        lottiePopper.visibility = View.VISIBLE
        lottiePopper.playAnimation()

        popperMediaPlayer = MediaPlayer.create(this, R.raw.success)
        popperMediaPlayer?.start()

        lottiePopper.addLottieOnCompositionLoadedListener { composition ->
            val duration = composition.duration.toLong()
            lottiePopper.postDelayed({
                lottiePopper.visibility = View.GONE
                popperMediaPlayer?.release()
                popperMediaPlayer = null
                showCompletionDialog()
            }, duration)
        }
    }

    private fun showCompletionDialog() {
        AlertDialog.Builder(this)
            .setTitle("🎉 Congratulations!")
            .setMessage("You've completed the greetings learning session!")
            .setPositiveButton("OK") { dialog, _ ->
                markLevelAsCompleted()
                dialog.dismiss()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .show()
    }

    private fun markLevelAsCompleted() {
        val prefs = getSharedPreferences("module_progress", MODE_PRIVATE)
        val completed = prefs.getInt("completed", 0)
        if (completed < 5) {
            prefs.edit().putInt("completed", 5).apply()
        }
    }

    override fun onBackPressed() {
        markLevelAsCompleted()
        super.onBackPressed()
    }

    override fun onDestroy() {
        tts.shutdown()
        popperMediaPlayer?.release()
        popperMediaPlayer = null
        super.onDestroy()
    }
}
