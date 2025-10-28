package com.example.learningcultureone

import android.app.AlertDialog
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.*
import com.airbnb.lottie.LottieAnimationView
import java.util.*

class TravelreqInfoActivity : BaseActivity() {

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

    // Travel Requirements content
    private val dialogueLines = listOf(
        """
        - For Tourists / Short-Term Visitors:
        1. Valid Passport:
        Must be valid for at least 6 months from the date of entry.

        2. Visa Requirement:
        Visa on Arrival / Visa-Free for certain nationalities.
        Pre-arranged Tourist Visa required for others (apply via embassy, airline, or UAE travel portals).

        3. Return/Onward Ticket:
        Proof of departure is required to ensure you do not overstay.
        """.trimIndent(),

        """
        4. Accommodation Details:
        Hotel booking confirmation or an invitation letter from a UAE resident.

        5. Sufficient Funds:
        Evidence of the ability to support yourself during your stay (bank statements or credit cards).

        6. Travel Insurance:
        Highly recommended — especially for medical coverage, including COVID-19 treatment.

        7. COVID-19 Rules:
        Requirements like vaccination proof or PCR tests may apply depending on latest UAE travel guidelines.
        """.trimIndent(),

        """
        - For Long-Term Stay / Residency (Work, Study, Family):
        1. Valid Passport:
        At least 6 months validity required for processing residence permits.

        2. Entry Permit / Residency Visa:
        Required for jobs, business, study, or family reunification.
        Often sponsored by an employer, educational institution, or family member.

        3. Medical Fitness Test:
        Mandatory health check-up (including screenings for communicable diseases) as part of residence visa processing.
        """.trimIndent(),

        """
        4. Emirates ID Registration:
        Compulsory for residents — serves as your official identification card.

        5. Accommodation / Tenancy Contract:
        Proof of legal housing arrangement (rent contract or company-provided housing).

        6. Health Insurance:
        Required in most Emirates — proof of valid UAE-compliant health coverage is mandatory for residence visa holders.

        7. Financial Proof (if investing or self-sponsoring):
        Must demonstrate sufficient financial resources or investment in UAE.
        """.trimIndent()
    )

    // Growth animation stages
    private val forwardStages = listOf(0.0f, 0.11f, 0.22f, 0.33f)
    private var currentIndex = 0
    private var previousIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        // Inflate layout into BaseActivity’s container
        setContentView(R.layout.activity_travelreq_info)
        setupBottomNavigation(R.id.navigation_home)

        initViews()
        initTTS()
        updateUI()
        setClickListeners()
    }

    /** Initialize all views **/
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

    /** Initialize Text-to-Speech **/
    private fun initTTS() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.US
                tts.setSpeechRate(1.0f)
            }
        }
    }

    /** Set up all button click listeners **/
    private fun setClickListeners() {
        nextButton.setOnClickListener { handleNext() }
        prevButton.setOnClickListener { handlePrev() }
        backButton.setOnClickListener { finish() }
        readAloudButton.setOnClickListener { speakCurrentLine() }
    }

    /** Handle next button **/
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

    /** Handle previous button **/
    private fun handlePrev() {
        stopTTS()
        if (currentIndex > 0) {
            previousIndex = currentIndex
            currentIndex--
            updateUI()
        }
    }

    /** Speak the current dialogue **/
    private fun speakCurrentLine() {
        stopTTS()
        tts.speak(dialogueLines[currentIndex], TextToSpeech.QUEUE_FLUSH, null, null)
    }

    /** Update UI and animation **/
    private fun updateUI() {
        dialogueBox.text = dialogueLines[currentIndex]

        // Update page indicators
        pageIndicator.text = dialogueLines.indices.joinToString(" ") { i ->
            if (i == currentIndex) "●" else "○"
        }

        // Handle Lottie seed growth animation
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

    /** Play success animation and sound **/
    private fun playPopperAnimationAndSound() {
        lottiePopper.visibility = View.VISIBLE
        lottiePopper.playAnimation()

        popperMediaPlayer = MediaPlayer.create(this, R.raw.success)
        popperMediaPlayer?.start()

        // Show popup after animation completes
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

    /** Completion popup **/
    private fun showCompletionPopup() {
        AlertDialog.Builder(this)
            .setTitle("🎉 Congratulations!")
            .setMessage("You've completed the travel requirements learning session!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    /** Stop TTS safely **/
    private fun stopTTS() {
        if (::tts.isInitialized && tts.isSpeaking) {
            tts.stop()
        }
    }

    /** Release resources **/
    override fun onDestroy() {
        stopTTS()
        if (::tts.isInitialized) tts.shutdown()
        popperMediaPlayer?.release()
        popperMediaPlayer = null
        super.onDestroy()
    }
}
