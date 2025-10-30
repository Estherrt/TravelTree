package com.example.learningcultureone

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.*
import com.airbnb.lottie.LottieAnimationView
import java.util.*

class CultureInfoActivity : BaseActivity() {

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
        "The culture of the UAE is a rich blend of Arabian, Islamic, and Persian influences, with strong traditions of hospitality, family, and respect. It's a society that values both its deep-rooted heritage and its modern, cosmopolitan outlook.",
        "Key Aspects of Emirati Culture:\n\n• Family and Community:\nFamily is central to Emirati life, and strong social bonds are maintained through frequent gatherings and celebrations.\n\n• Hospitality:\nEmiratis are known for their generosity, often offering guests Arabic coffee (gahwa) and dates.",
        "• Respect:\nRespect for elders and authority figures is deeply ingrained in the culture.\n\n• Islamic Values:\nIslam is the predominant religion, influencing architecture, attire, and customs.",
        "• Traditional Arts and Crafts:\nEmirati culture features vibrant calligraphy, henna, weaving (Sadu), and perfumery.\n\n• Traditional Sports:\nFalconry, camel racing, and dhow racing remain key cultural practices."
    )

    private val forwardStages = listOf(0.0f, 0.11f, 0.22f, 0.33f)
    private var currentIndex = 0
    private var previousIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_culture_info)

        setupBottomNavigation(R.id.navigation_home)
        supportActionBar?.hide()

        initViews()
        initTTS()
        updateUI()
        setClickListeners()
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
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
                tts.setSpeechRate(1.0f)
                tts.setPitch(1.0f)
            } else {
                Toast.makeText(this, "TTS initialization failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setClickListeners() {
        nextButton.setOnClickListener { handleNext() }
        prevButton.setOnClickListener { handlePrev() }
        backButton.setOnClickListener {
            stopTTS()
            finish()
        }
        readAloudButton.setOnClickListener { speakCurrentLine() }
    }

    private fun handleNext() {
        stopTTS()
        if (currentIndex < dialogueLines.size - 1) {
            previousIndex = currentIndex
            currentIndex++
            updateUI()
        } else {
            playPopperAnimationAndSound()
        }
    }

    private fun handlePrev() {
        stopTTS()
        if (currentIndex > 0) {
            previousIndex = currentIndex
            currentIndex--
            updateUI()
        }
    }

    private fun speakCurrentLine() {
        stopTTS()
        val text = dialogueLines[currentIndex]
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun updateUI() {
        dialogueBox.text = dialogueLines[currentIndex]

        pageIndicator.text = dialogueLines.indices.joinToString(" ") { index ->
            if (index == currentIndex) "●" else "○"
        }

        if (currentIndex > previousIndex) {
            lottieSeed.setMinAndMaxProgress(forwardStages[previousIndex], forwardStages[currentIndex])
            lottieSeed.speed = 1f
        } else if (currentIndex < previousIndex) {
            lottieSeed.setMinAndMaxProgress(forwardStages[currentIndex], forwardStages[previousIndex])
            lottieSeed.speed = -1f
        } else {
            lottieSeed.setMinAndMaxProgress(forwardStages[currentIndex], forwardStages[currentIndex])
            lottieSeed.speed = 0f
        }
        lottieSeed.playAnimation()

        prevButton.isEnabled = currentIndex > 0
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
            .setMessage("You've completed the culture learning session!")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun stopTTS() {
        if (::tts.isInitialized && tts.isSpeaking) {
            tts.stop()
        }
    }

    override fun onDestroy() {
        stopTTS()
        if (::tts.isInitialized) {
            tts.shutdown()
        }
        popperMediaPlayer?.release()
        popperMediaPlayer = null
        super.onDestroy()
    }
}
