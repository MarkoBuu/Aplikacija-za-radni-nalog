package com.example.radninalog

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.radninalog.databinding.ActivityRegisterBinding
import com.example.radninalog.databinding.ActivityResetPassBinding
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.rxbinding2.widget.RxTextView

class ResetPassActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPassBinding
    private lateinit var auth : FirebaseAuth

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityResetPassBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Autentifikacija
        auth = FirebaseAuth.getInstance()

        val emailCheck = RxTextView.textChanges(binding.email)
            .skipInitialValue()
            .map { email -> !Patterns.EMAIL_ADDRESS.matcher(email).matches() }
        emailCheck.subscribe { showEmailValidAlert(it) }

        binding.btnReset.setOnClickListener {
            val email = binding.email.text.toString().trim()
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this){reset->
                    if(reset.isSuccessful){
                        Intent(this, LoginActivity::class.java).also {
                            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(it)
                            Toast.makeText(this, "Check your email for password reset!", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(this, reset.exception?.message ,Toast.LENGTH_SHORT).show()
                    }
                }
        }

        binding.tvBackLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun showEmailValidAlert(isNotValid: Boolean) {
        if(isNotValid){
        binding.email.error =  "Invalid email!"
        binding.btnReset.isEnabled = false
        binding.btnReset.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
    } else {
        binding.email.error = null
            binding.btnReset.isEnabled = true
            binding.btnReset.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_color)
        }
    }
}
