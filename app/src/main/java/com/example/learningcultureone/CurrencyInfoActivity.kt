package com.example.learningcultureone

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.*
import com.airbnb.lottie.LottieAnimationView
import java.util.*

class CurrencyInfoActivity : BaseActivity() {

    private lateinit var lottieSeed: LottieAnimationView
    private lateinit var lottieCharacter: LottieAnimationView
    private lateinit var lottiePopper: LottieAnimationView
    private lateinit var dialogueBox: TextView
    private lateinit var nextButton: Button
    private lateinit var prevButton: Button
    private lateinit var backButton: ImageView
    private lateinit var readAloudButton: Button
    private lateinit var pageIndicator: TextView
    private lateinit var tts: TextToSpeech

    private var popperMediaPlayer: MediaPlayer? = null

    private val dialogueLines = listOf(
        "The official currency of the United Arab Emirates is the United Arab Emirates Dirham (AED). It is issued and regulated by the Central Bank of the UAE.",
        "Subdivision: 1 dirham = 100 fils.\n\nCoins: Commonly used coins include 1, 5, 10, 25, and 50 fils, as well as a 1 dirham coin.\n\nBanknotes: Denominations available are 5, 10, 20, 50, 100, 200, 500, and 1000 dirhams.",
        "The UAE Dirham (AED) is pegged to the US Dollar, which helps maintain its stability against other currencies.\n\nCurrently, 1 Dirham is approximately equal to 23.60 Indian Rupees (INR). This stable rate benefits trade and remittances between the UAE and India, two countries with strong economic ties.",
        "The UAE Dirham banknotes feature iconic landmarks and cultural symbols that reflect the nation's heritage.\n\nImages include the Burj Khalifa, showcasing Dubai’s architectural marvel, and the traditional dhow boats representing the country’s maritime history.\n\nThese visuals celebrate the blend of modernity and tradition that defines the UAE’s identity."
    )

    private val forwardStages = listOf(0.0f, 0.11f, 0.22f, 0.33f)
    private var currentIndex = 0
    private var previousIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency_info)
        setupBottomNavigation(R.id.navigation_home)
        supportActionBar?.hide()

        initViews()
        initTTS()
        updateUI()
        setupListeners()
    }

    private fun initViews() {
        lottieSeed = findViewById(R.id.lottie_seed)
        lottieCharacter = findViewById(R.id.lottie_character)
        lottiePopper = findViewById(R.id.lottie_popper)
        dialogueBox = findViewById(R.id.dialogue_box)
        nextButton = findViewById(R.id.btn_next)
        prevButton = findViewById(R.id.btn_prev)
        backButton = findViewById(R.id.btn_back)
        readAloudButton = findViewById(R.id.btn_read_aloud)
        pageIndicator = findViewById(R.id.page_indicator)
    }

    private fun initTTS() {
        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.US
                tts.setSpeechRate(1.0f)
                tts.setPitch(1.0f)
            }
        }
    }

    private fun setupListeners() {
        nextButton.setOnClickListener { onNextClicked() }
        prevButton.setOnClickListener { onPrevClicked() }
        backButton.setOnClickListener { finish() }
        readAloudButton.setOnClickListener { speakCurrentLine() }
    }

    private fun onNextClicked() {
        stopTTS()
        if (currentIndex < dialogueLines.size - 1) {
            previousIndex = currentIndex
            currentIndex++
            updateUI()
        } else {
            playPopperAnimationAndSound()
        }
    }

    private fun onPrevClicked() {
        stopTTS()
        if (currentIndex > 0) {
            previousIndex = currentIndex
            currentIndex--
            updateUI()
        }
    }

    private fun speakCurrentLine() {
        stopTTS()
        tts.speak(dialogueLines[currentIndex], TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun updateUI() {
        dialogueBox.text = dialogueLines[currentIndex]

        // Update page indicator
        pageIndicator.text = dialogueLines.indices.joinToString(" ") {
            if (it == currentIndex) "●" else "○"
        }

        // Animate plant growth
        when {
            currentIndex > previousIndex -> {
                lottieSeed.setMinAndMaxProgress(forwardStages[previousIndex], forwardStages[currentIndex])
                lottieSeed.speed = 1f
            }
            currentIndex < previousIndex -> {
                lottieSeed.setMinAndMaxProgress(forwardStages[currentIndex], forwardStages[previousIndex])
                lottieSeed.speed = -1f
            }
            else -> {
                lottieSeed.setMinAndMaxProgress(forwardStages[currentIndex], forwardStages[currentIndex])
                lottieSeed.speed = 0f
            }
        }
        lottieSeed.playAnimation()

        // Button state updates
        prevButton.isEnabled = currentIndex != 0
        nextButton.text = if (currentIndex == dialogueLines.size - 1) "Done" else "Next"
    }

    private fun playPopperAnimationAndSound() {
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
                showCompletionPopup()
            }, duration)
        }

        popperMediaPlayer?.setOnCompletionListener {
            it.release()
            popperMediaPlayer = null
        }
    }

    private fun showCompletionPopup() {
        AlertDialog.Builder(this)
            .setTitle("🎉 Congratulations!")
            .setMessage("You've completed the UAE currency learning session!")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun stopTTS() {
        if (tts.isSpeaking) tts.stop()
    }

    override fun onDestroy() {
        stopTTS()
        tts.shutdown()
        popperMediaPlayer?.release()
        popperMediaPlayer = null
        super.onDestroy()
    }
}
