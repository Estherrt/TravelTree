package com.example.traveltree

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import android.app.AlertDialog
import java.util.*
import android.media.MediaPlayer
import android.view.View

class CultureInfoActivity : AppCompatActivity() {

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
        "Key Aspects of Emirati Culture:\n-Family and Community:\nFamily is central to Emirati life, and strong social bonds are maintained through frequent gatherings and celebrations.\n-Hospitality:\nExtremely warm and welcoming, Emiratis are known for their generosity and hospitality, often offering guests Arabic coffee (gahwa) and dates.",
        "-Respect:\nRespect for elders and authority figures is deeply ingrained in the culture.\n-Islamic Values:\nIslam is the predominant religion, influencing various aspects of life, including architecture, attire, and social customs.",
        "-Traditional Arts and Crafts:\nEmirati culture boasts a vibrant arts scene, with traditional crafts like calligraphy, henna, weaving (Sadu), and perfumery.\n-Traditional Sports:\nFalconry, camel racing, and dhow racing are important cultural practices that continue to be enjoyed."
    )

    private val forwardStages = listOf(0.0f, 0.11f, 0.22f, 0.33f)
    private var currentIndex = 0
    private var previousIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        setContentView(R.layout.activity_culture_info)

        // Find views
        lottieSeed = findViewById(R.id.lottie_seed)
        lottieCharacter = findViewById(R.id.lottie_character)
        lottiePopper = findViewById(R.id.lottie_popper)
        dialogueBox = findViewById(R.id.dialogue_box)
        nextButton = findViewById(R.id.btn_next)
        prevButton = findViewById(R.id.btn_prev)
        backButton = findViewById(R.id.btn_back)
        readAloudButton = findViewById(R.id.btn_read_aloud)
        pageIndicator = findViewById(R.id.page_indicator)

        // TTS Initialization
        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale.US
            }
        }

        updateUI()

        nextButton.setOnClickListener {
            stopTTS() // stop any speaking

            if (currentIndex < dialogueLines.size - 1) {
                previousIndex = currentIndex
                currentIndex++
                updateUI()
            } else if (currentIndex == dialogueLines.size - 1) {
                // When 'Done' clicked
                playPopperAnimationAndSound() // triggers popper, sound, and popup AFTER popper finishes
            }
        }

        prevButton.setOnClickListener {
            stopTTS() // stop any speaking

            if (currentIndex > 0) {
                previousIndex = currentIndex
                currentIndex--
                updateUI()
            }
        }

        backButton.setOnClickListener {
            stopTTS()
            finish()
        }

        readAloudButton.setOnClickListener {
            stopTTS() // stop any ongoing TTS before speaking new text
            tts.speak(dialogueLines[currentIndex], TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun updateUI() {
        dialogueBox.text = dialogueLines[currentIndex]

        // Page indicator dots
        val indicators = List(dialogueLines.size) { index ->
            if (index == currentIndex) "●" else "○"
        }
        pageIndicator.text = indicators.joinToString(" ")

        // Lottie Seed animation control
        if (currentIndex > previousIndex) {
            val startProgress = forwardStages[previousIndex]
            val endProgress = forwardStages[currentIndex]
            lottieSeed.setMinAndMaxProgress(startProgress, endProgress)
            lottieSeed.speed = 1f
        } else if (currentIndex < previousIndex) {
            val startProgress = forwardStages[currentIndex]
            val endProgress = forwardStages[previousIndex]
            lottieSeed.setMinAndMaxProgress(startProgress, endProgress)
            lottieSeed.speed = -1f
        } else {
            val progress = forwardStages[currentIndex]
            lottieSeed.setMinAndMaxProgress(progress, progress)
            lottieSeed.speed = 0f
        }

        lottieSeed.playAnimation()

        // Enable/Disable Prev button
        prevButton.isEnabled = currentIndex != 0

        // Change "Next" button to "Done" on last page
        nextButton.text = if (currentIndex == dialogueLines.size - 1) "Done" else "Next"
    }

    private fun playPopperAnimationAndSound() {
        lottiePopper.visibility = View.VISIBLE
        lottiePopper.playAnimation()

        // Play popper sound
        popperMediaPlayer = MediaPlayer.create(this, R.raw.success)
        popperMediaPlayer?.start()

        // Show popup ONLY AFTER popper animation finishes
        lottiePopper.addLottieOnCompositionLoadedListener { composition ->
            val duration = composition.duration.toLong() // duration in milliseconds
            lottiePopper.postDelayed({
                lottiePopper.visibility = View.GONE
                popperMediaPlayer?.release()
                popperMediaPlayer = null
                showCompletionPopup() // Now popup comes AFTER popper finishes
            }, duration)
        }

        // Release MediaPlayer after sound ends
        popperMediaPlayer?.setOnCompletionListener {
            it.release()
            popperMediaPlayer = null
        }
    }

    private fun showCompletionPopup() {
        AlertDialog.Builder(this)
            .setTitle("Congratulations!")
            .setMessage("You've completed the culture learning session!")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun stopTTS() {
        if (tts.isSpeaking) {
            tts.stop()
        }
    }

    override fun onDestroy() {
        stopTTS()
        tts.shutdown()
        popperMediaPlayer?.release()
        popperMediaPlayer = null
        super.onDestroy()
    }
}