package com.example.learningcultureone

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : BaseActivity() {

    private lateinit var treeAnimation: LottieAnimationView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val totalModules = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Setup bottom navigation (highlight Home)
        setupBottomNavigation(R.id.navigation_home)

        // Initialize Firebase and animation view
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        treeAnimation = findViewById(R.id.treeAnimation)

        // Initialize buttons for each learning module
        setupModuleButton(R.id.btnCulture, 1, CultureInfoActivity::class.java)
        setupModuleButton(R.id.btnCurrency, 2, CurrencyInfoActivity::class.java)
        setupModuleButton(R.id.btnEmergency, 3, EmergencyInfoActivity::class.java)
        setupModuleButton(R.id.btnTravel, 4, TravelreqInfoActivity::class.java)
        setupModuleButton(R.id.btnGreetings, 5, GreetingsActivity::class.java)

        // Restore any previously saved progress
        restoreProgress()
    }

    /**
     * Sets up button click listeners for each module
     */
    private fun setupModuleButton(buttonId: Int, module: Int, targetActivity: Class<*>) {
        findViewById<Button>(buttonId)?.setOnClickListener {
            updateProgress(module)
            startActivity(Intent(this, targetActivity))
        }
    }

    /**
     * Updates animation progress and saves progress to Firestore
     */
    private fun updateProgress(module: Int) {
        val progress = module / totalModules.toFloat()

        // Animate growth of the tree
        treeAnimation.setMinAndMaxProgress(treeAnimation.progress, progress)
        treeAnimation.playAnimation()

        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)

        // Use 'set' with merge = true to avoid overwriting other user data
        val data = hashMapOf("completedModule" to module)
        userRef.set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Progress saved ✅", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving progress: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Restores saved module progress and updates tree animation
     */
    private fun restoreProgress() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val completedModule = document.getLong("completedModule")?.toInt() ?: 0
                    val progress = completedModule / totalModules.toFloat()
                    treeAnimation.progress = progress
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to restore progress", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        restoreProgress()
    }
}
