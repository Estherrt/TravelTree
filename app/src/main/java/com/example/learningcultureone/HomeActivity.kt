package com.example.learningcultureone

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Linking the buttons with their IDs in activity_home.xml
        val cultureButton = findViewById<Button>(R.id.btn_culture)
        val emergencyButton = findViewById<Button>(R.id.btn_emergency)
        val currencyButton = findViewById<Button>(R.id.btn_currency)
        val travelButton = findViewById<Button>(R.id.btn_travel)

        // Button click to go to CultureInfoActivity
        cultureButton.setOnClickListener {
            val intent = Intent(this, CultureInfoActivity::class.java)
            startActivity(intent)
        }

        // Future navigation for these options:
        emergencyButton.setOnClickListener {
            val intent = Intent(this, EmergencyInfoActivity::class.java)
            startActivity(intent)
        }

        currencyButton.setOnClickListener {
            val intent = Intent(this, CurrencyInfoActivity::class.java)
            startActivity(intent)
        }

        travelButton.setOnClickListener {
            val intent = Intent(this, TravelreqInfoActivity::class.java)
            startActivity(intent)
        }
    }
}
