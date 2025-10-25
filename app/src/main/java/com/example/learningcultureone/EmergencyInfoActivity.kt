package com.example.learningcultureone

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import android.app.AlertDialog
import java.util.*
import android.media.MediaPlayer
import android.view.View

class EmergencyInfoActivity : AppCompatActivity() {

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
        "The UAE has a unified emergency number (112) that connects you to police, ambulance, and fire services.\n" +"\n" +"Emergency services in the UAE offer multilingual support to assist residents and visitors.",
        "Police, ambulance, and fire departments have dedicated direct numbers for faster assistance.\n" + "\n" + "It is important to know the emergency numbers and keep them handy for quick response in critical situations.",
        "Police: 999 — For all law enforcement emergencies.\n" + "\n" + "Ambulance: 998 — For urgent medical assistance.\n" + "\n" + "Fire Department: 997 — For fire emergencies and rescue.",
        "Coast Guard: 996 — For maritime emergencies.\n" + "\n" + "Civil Defence: 997 — Handles various civil emergencies including fires and disasters.\n" + "\n" + "Traffic Accidents: 901 — For reporting road accidents."
    )

    private val forwardStages = listOf(0.0f, 0.11f, 0.22f, 0.33f)
    private var currentIndex = 0
    private var previousIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_info)

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
            .setMessage("You've completed the emergency contacts learning session!")
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


