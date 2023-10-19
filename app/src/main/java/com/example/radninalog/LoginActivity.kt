package com.example.radninalog

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.radninalog.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.rxbinding2.widget.RxTextView

@SuppressLint("CheckResult")

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Autentifikacija
        auth = FirebaseAuth.getInstance()

        val usernameCheck = RxTextView.textChanges(binding.email)
            .skipInitialValue()
            .map { user -> user.isEmpty() }
        usernameCheck.subscribe { showTextMinimalAlert(it, "Email") }

        val passCheck = RxTextView.textChanges(binding.password)
            .skipInitialValue()
            .map { pass -> pass.isEmpty() }
        passCheck.subscribe { showTextMinimalAlert(it, "Password") }

        val invalidFields = io.reactivex.Observable.combineLatest(
            usernameCheck,
            passCheck,
        ) { usernameInvalid: Boolean, passInvalid: Boolean ->
            !usernameInvalid && !passInvalid
        }
        invalidFields.subscribe { isValid ->
            if(isValid) {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_color)
            } else {
                binding.btnLogin.isEnabled = false
                binding.btnLogin.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
            }
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()
            loginUser(email, password)
        }
        binding.tvNoAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ResetPassActivity::class.java))
        }
    }

    private fun showTextMinimalAlert(isNotValid: Boolean, text : String) {
        if (text == "Email")
            binding.email.error = if(isNotValid) "$text Can not be empty!" else null
       else if (text == "Password")
            binding.password.error = if(isNotValid) "$text Can not be empty!" else null

    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { login ->
                if (login.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    val sharedPref = getSharedPreferences("WorkTrackr", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putString("userId", userId)
                    editor.apply()

                    Intent(this, HomeActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, login.exception?.message, Toast.LENGTH_SHORT).show()
                } }
}}
