package com.example.learningcultureone

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : BaseActivity() {

    private lateinit var tvHello: TextView
    private lateinit var rvCountryProgress: RecyclerView
    private lateinit var btnLogout: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // 🔹 Setup common bottom navigation
        setupBottomNavigation(R.id.navigation_profile)

        tvHello = findViewById(R.id.tvHello)
        rvCountryProgress = findViewById(R.id.rvCountryProgress)
        btnLogout = findViewById(R.id.btnLogout)

        rvCountryProgress.layoutManager = LinearLayoutManager(this)

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val user = auth.currentUser
        if (user != null) {
            tvHello.text = "Hello, ${user.email}"
            fetchProgress(user.uid)
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchProgress(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val data = document.data
                    if (data != null) {
                        val progressList = mutableListOf<CountryProgress>()
                        // Only display progress modules
                        data.forEach { (key, value) ->
                            if (key != "country" && value is Long) {
                                progressList.add(CountryProgress(key, value.toInt()))
                            }
                        }

                        if (progressList.isEmpty()) {
                            Toast.makeText(this, "No progress found", Toast.LENGTH_SHORT).show()
                        } else {
                            rvCountryProgress.adapter = CountryProgressAdapter(progressList)
                        }
                    } else {
                        Toast.makeText(this, "No progress data found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "No progress found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching progress: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
