package com.example.learningcultureone

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions // Import SetOptions

class CountrySelectionActivity : BaseActivity() {

    private lateinit var spinner: Spinner
    private lateinit var btnNext: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_country_selection)

        // ✅ Setup bottom navigation (Home tab active)
        setupBottomNavigation(R.id.navigation_home)

        // Initialize views
        spinner = findViewById(R.id.spinnerCountry)
        btnNext = findViewById(R.id.btnNext)

        // Firebase initialization
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Populate spinner with countries from resources
        val countries = resources.getStringArray(R.array.country_list)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, countries)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Next button click
        btnNext.setOnClickListener {
            val selectedCountry = spinner.selectedItem?.toString() ?: ""

            // Currently only UAE supported
            if (selectedCountry != "United Arab Emirates") {
                Toast.makeText(
                    this,
                    "Currently only United Arab Emirates is supported.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save selection to Firestore with the correct field name and using merge
            val data = hashMapOf("selectedCountry" to selectedCountry) // <<< KEY CHANGE HERE
            db.collection("users").document(userId)
                .set(data, SetOptions.merge()) // <<< Using merge() to avoid overwriting other user data
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Country saved successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // ✅ Move to HomeActivity
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error saving selection: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}
