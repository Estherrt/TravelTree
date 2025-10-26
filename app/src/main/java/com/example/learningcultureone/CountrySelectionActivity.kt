package com.example.learningcultureone

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class CountrySelectionActivity : AppCompatActivity() {

    private lateinit var countrySpinner: Spinner
    private lateinit var nextButton: Button
    private lateinit var selectedCountry: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_country_selection)

        countrySpinner = findViewById(R.id.spinnerCountry)
        nextButton = findViewById(R.id.btnNext)

        val countries = resources.getStringArray(R.array.country_list)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, countries)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        countrySpinner.adapter = adapter

        nextButton.setOnClickListener {
            selectedCountry = countrySpinner.selectedItem.toString()
            if (selectedCountry == "United Arab Emirates") {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Currently only 'United Arab Emirates' is supported", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
