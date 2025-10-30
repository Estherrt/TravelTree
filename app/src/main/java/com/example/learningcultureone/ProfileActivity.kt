package com.example.learningcultureone

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



class ProfileActivity : BaseActivity() {

    private lateinit var tvHello: TextView
    private lateinit var tvProgressTitle: TextView
    private lateinit var rvCountryProgress: RecyclerView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val countryProgressList = mutableListOf<CountryProgress>()
    private lateinit var adapter: CountryProgressAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setupBottomNavigation(R.id.navigation_profile)

        tvHello = findViewById(R.id.tvHello)
        tvProgressTitle = findViewById(R.id.tvProgressTitle)
        rvCountryProgress = findViewById(R.id.rvCountryProgress)

        rvCountryProgress.layoutManager = LinearLayoutManager(this)
        adapter = CountryProgressAdapter(countryProgressList)
        rvCountryProgress.adapter = adapter

        val user = auth.currentUser
        if (user != null) {
            tvHello.text = "Hello, ${user.email}"
            fetchSelectedCountryAndProgress(user.uid)
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun fetchSelectedCountryAndProgress(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    val selectedCountry = userDocument.getString("selectedCountry")

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
        db.collection("users").document(uid)
            .collection("progress").document(country).get()
            .addOnSuccessListener { countryProgressDoc ->
                countryProgressList.clear()

                if (countryProgressDoc.exists()) {
                    val data = countryProgressDoc.data
                    if (data != null && data.isNotEmpty()) {
                        data.forEach { (key, value) ->

                            if (value is Long) {
                                countryProgressList.add(CountryProgress(key, value.toInt()))
                            }
                        }
                    }
                }

                if (countryProgressList.isEmpty()) {
                    Toast.makeText(this, "No progress found for $country.", Toast.LENGTH_SHORT).show()
                    tvProgressTitle.visibility = View.GONE
                } else {
                    tvProgressTitle.visibility = View.VISIBLE
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching progress for $country: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
