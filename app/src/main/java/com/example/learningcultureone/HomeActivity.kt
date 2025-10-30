package com.example.learningcultureone

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class HomeActivity : BaseActivity() {

    private lateinit var treeAnimation: LottieAnimationView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val totalModules = 5

    private val moduleNames = mapOf(
        1 to "Culture",
        2 to "Currency",
        3 to "Emergency",
        4 to "Travel",
        5 to "Greetings"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setupBottomNavigation(R.id.navigation_home)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        treeAnimation = findViewById(R.id.treeAnimation)

        setupModuleButton(R.id.btnCulture, 1, CultureInfoActivity::class.java)
        setupModuleButton(R.id.btnCurrency, 2, CurrencyInfoActivity::class.java)
        setupModuleButton(R.id.btnEmergency, 3, EmergencyInfoActivity::class.java)
        setupModuleButton(R.id.btnTravel, 4, TravelreqInfoActivity::class.java)
        setupModuleButton(R.id.btnGreetings, 5, GreetingsActivity::class.java)

        restoreOverallProgress() // Keep this for tree animation
    }


    private fun setupModuleButton(buttonId: Int, moduleNumber: Int, targetActivity: Class<*>) {
        findViewById<Button>(buttonId)?.setOnClickListener {
            startActivity(Intent(this, targetActivity))
            updateOverallProgress(moduleNumber)
            updateModuleProgressForCountry(moduleNumber)
        }
    }

    private fun updateOverallProgress(clickedModule: Int) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)

        db.runTransaction { transaction ->
            val userDocument = transaction.get(userRef)
            val currentCompletedModule = userDocument.getLong("completedModule")?.toInt() ?: 0
            val newHighestCompletedModule = maxOf(currentCompletedModule, clickedModule)

            if (newHighestCompletedModule > currentCompletedModule) {
                val data = hashMapOf("completedModule" to newHighestCompletedModule)
                transaction.set(userRef, data, SetOptions.merge())

                val startProgress = treeAnimation.progress
                val targetProgress = newHighestCompletedModule / totalModules.toFloat()

                val animator = ValueAnimator.ofFloat(startProgress, targetProgress)
                animator.addUpdateListener { animation ->
                    treeAnimation.progress = animation.animatedValue as Float
                }
                animator.duration = 500
                animator.start()
            } else {
                treeAnimation.progress = newHighestCompletedModule / totalModules.toFloat()
            }
            null
        }.addOnSuccessListener {
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error updating overall progress: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateModuleProgressForCountry(moduleNumber: Int) {
        val userId = auth.currentUser?.uid ?: return
        val moduleName = moduleNames[moduleNumber] ?: return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { userDocument ->
                val selectedCountry = userDocument.getString("selectedCountry")

                if (selectedCountry != null && selectedCountry.isNotEmpty()) {
                    val progressRef = db.collection("users").document(userId)
                        .collection("progress").document(selectedCountry)

                    val moduleProgressData = hashMapOf(moduleName to 100)
                    progressRef.set(moduleProgressData, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "$moduleName progress updated for $selectedCountry ✅", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error saving $moduleName progress: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "No country selected to save module progress.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to get selected country for module progress: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun restoreOverallProgress() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val completedModule = document.getLong("completedModule")?.toInt() ?: 0
                    val progress = completedModule / totalModules.toFloat()
                    treeAnimation.progress = progress
                } else {
                    treeAnimation.progress = 0f
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to restore overall progress", Toast.LENGTH_SHORT).show()
                treeAnimation.progress = 0f
            }
    }

    override fun onResume() {
        super.onResume()
        restoreOverallProgress()
    }
}
