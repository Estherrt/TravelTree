package com.example.traveltree

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var treeAnimation: LottieAnimationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var menuBtn: ImageButton
    private val totalLevels = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        treeAnimation = findViewById(R.id.treeAnimation)
        drawerLayout = findViewById(R.id.drawerLayout)
        menuBtn = findViewById(R.id.menuBtn)

        menuBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        setupLevelButtons()
    }

    private fun setupLevelButtons() {
        findViewById<Button>(R.id.btnCulture).setOnClickListener {
            completeLevel(1)
            startActivity(Intent(this, CultureInfoActivity::class.java))
        }
        findViewById<Button>(R.id.btnCurrency).setOnClickListener {
            completeLevel(2)
            startActivity(Intent(this, CurrencyInfoActivity::class.java))
        }
        findViewById<Button>(R.id.btnEmergency).setOnClickListener {
            completeLevel(3)
            startActivity(Intent(this, EmergencyInfoActivity::class.java))
        }
        findViewById<Button>(R.id.btnTravel).setOnClickListener {
            completeLevel(4)
            startActivity(Intent(this, TravelreqInfoActivity::class.java))
        }
        findViewById<Button>(R.id.btnGreetings).setOnClickListener {
            completeLevel(5)
            startActivity(Intent(this, GreetingsActivity::class.java))
        }
    }

    private fun completeLevel(levelCompleted: Int) {
        treeAnimation.setMinAndMaxFrame(0, levelCompleted * 20)
        treeAnimation.playAnimation()
        val prefs = getSharedPreferences("module_progress", MODE_PRIVATE)
        prefs.edit().putInt("completed", levelCompleted).apply()
        if (levelCompleted == totalLevels) {
            Toast.makeText(this, "Tree fully grown! 🎉", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("module_progress", MODE_PRIVATE)
        val completedModules = prefs.getInt("completed", 0)
        treeAnimation.progress = completedModules * 0.2f
    }
}
