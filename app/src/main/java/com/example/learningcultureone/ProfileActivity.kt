package com.example.learningcultureone

import android.content.Intent
import android.os.Bundle
import android.view.View // Import View for visibility changes
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Assuming CountryProgress is defined as:
// data class CountryProgress(val moduleName: String = "", val progress: Int = 0)
// and CountryProgressAdapter is your RecyclerView adapter.

class ProfileActivity : BaseActivity() {

    private lateinit var tvHello: TextView
    private lateinit var tvProgressTitle: TextView // Reference the progress title TextView
    private lateinit var rvCountryProgress: RecyclerView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val countryProgressList = mutableListOf<CountryProgress>() // List to hold data
    private lateinit var adapter: CountryProgressAdapter // Adapter for RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // 🔹 Setup common bottom navigation
        setupBottomNavigation(R.id.navigation_profile)

        tvHello = findViewById(R.id.tvHello)
        tvProgressTitle = findViewById(R.id.tvProgressTitle) // Initialize the title TextView
        rvCountryProgress = findViewById(R.id.rvCountryProgress)

        rvCountryProgress.layoutManager = LinearLayoutManager(this)
        adapter = CountryProgressAdapter(countryProgressList) // Initialize adapter with empty list
        rvCountryProgress.adapter = adapter

        val user = auth.currentUser
        if (user != null) {
            tvHello.text = "Hello, ${user.email}"
            fetchSelectedCountryAndProgress(user.uid) // Call the new fetching method
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            // Optionally redirect to login if user is null
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun fetchSelectedCountryAndProgress(uid: String) {
        // First, get the user's main document to find the selected country
        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    val selectedCountry = userDocument.getString("selectedCountry") // Assumes you store the last selected country here

                    if (selectedCountry != null && selectedCountry.isNotEmpty()) {
                        tvProgressTitle.text = getString(R.string.your_progress_title, selectedCountry)
                        tvProgressTitle.visibility = View.VISIBLE // Make title visible
                        fetchProgressForCountry(uid, selectedCountry)
                    } else {
                        tvProgressTitle.text = getString(R.string.no_country_selected)
                        tvProgressTitle.visibility = View.VISIBLE
                        Toast.makeText(this, "No country selected yet. Please select one.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchProgressForCountry(uid: String, country: String) {
        // Now, fetch the progress data for the specific country from a subcollection
        db.collection("users").document(uid)
            .collection("progress").document(country).get() // Assuming a subcollection "progress" with documents named by country
            .addOnSuccessListener { countryProgressDoc ->
                countryProgressList.clear() // Clear existing data

                if (countryProgressDoc.exists()) {
                    val data = countryProgressDoc.data
                    if (data != null && data.isNotEmpty()) {
                        data.forEach { (key, value) ->
                            // Assuming module progress is stored as key-value pairs (e.g., "Culture": 75)
                            if (value is Long) { // Check if the value is a Long (Firestore default for numbers)
                                countryProgressList.add(CountryProgress(key, value.toInt()))
                            }
                        }
                    }
                }

                if (countryProgressList.isEmpty()) {
                    Toast.makeText(this, "No progress found for $country.", Toast.LENGTH_SHORT).show()
                    tvProgressTitle.visibility = View.GONE // Hide title if no progress
                } else {
                    tvProgressTitle.visibility = View.VISIBLE // Ensure title is visible if progress found
                }
                adapter.notifyDataSetChanged() // Notify adapter about data changes
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching progress for $country: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
