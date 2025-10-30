package com.example.learningcultureone

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etRePassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnGoLogin: Button
    private lateinit var ivPasswordToggle: ImageView
    private lateinit var ivRePasswordToggle: ImageView

    private lateinit var auth: FirebaseAuth
    private var passwordVisible = false
    private var rePasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etRePassword = findViewById(R.id.etRePassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnGoLogin = findViewById(R.id.btnGoLogin)
        ivPasswordToggle = findViewById(R.id.ivPasswordToggle)
        ivRePasswordToggle = findViewById(R.id.ivRePasswordToggle)

        auth = FirebaseAuth.getInstance()

        ivPasswordToggle.setOnClickListener {
            passwordVisible = !passwordVisible
            if (passwordVisible) {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivPasswordToggle.setImageResource(R.drawable.ic_visibility_off)
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivPasswordToggle.setImageResource(R.drawable.ic_visibility)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        ivRePasswordToggle.setOnClickListener {
            rePasswordVisible = !rePasswordVisible
            if (rePasswordVisible) {
                etRePassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivRePasswordToggle.setImageResource(R.drawable.ic_visibility_off)
            } else {
                etRePassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivRePasswordToggle.setImageResource(R.drawable.ic_visibility)
            }
            etRePassword.setSelection(etRePassword.text.length)
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val rePassword = etRePassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
                Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != rePassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }

        btnGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
