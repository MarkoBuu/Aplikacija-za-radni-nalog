package com.example.radninalog

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Observable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.radninalog.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jakewharton.rxbinding2.widget.RxTextView

@SuppressLint("CheckResult")
class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Autentifikacija
        auth = FirebaseAuth.getInstance()

        val nameCheck = RxTextView.textChanges(binding.fullname)
            .skipInitialValue()
            .map { name -> name.isEmpty() }
        nameCheck.subscribe { showNameExistsAlert(it) }

        val emailCheck = RxTextView.textChanges(binding.etEmail)
            .skipInitialValue()
            .map { email -> !Patterns.EMAIL_ADDRESS.matcher(email).matches() }
        emailCheck.subscribe { showEmailValidAlert(it) }

        val passCheck = RxTextView.textChanges(binding.etPassword)
            .skipInitialValue()
            .map { pass -> pass.length < 8 }
        passCheck.subscribe { showTextMinimalAlert(it, "Password") }

        val passConfirmCheck = io.reactivex.Observable.merge(
            RxTextView.textChanges(binding.etPassword)
                .skipInitialValue()
                .map { pass -> pass.toString() != binding.etConfirmPassword.text.toString()
                },
            RxTextView.textChanges(binding.etConfirmPassword)
                .skipInitialValue()
                .map { confirmPass -> confirmPass.toString() != binding.etPassword.text.toString() })
        passConfirmCheck.subscribe {
            showPasswordConfirmAlert(it)
        }

        val invalidFields = io.reactivex.Observable.combineLatest(
            nameCheck,
            emailCheck,
            passCheck,
            passConfirmCheck
        ) { nameInvalid: Boolean, emailInvalid: Boolean, passInvalid: Boolean, passConfirmInvalid: Boolean ->
            !nameInvalid && !emailInvalid && !passInvalid && !passConfirmInvalid
        }
        invalidFields.subscribe { isValid ->
            if(isValid) {
                binding.btnRegister.isEnabled = true
                binding.btnRegister.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_color)
            } else {
                binding.btnRegister.isEnabled = false
                binding.btnRegister.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
            }
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val fullName = binding.fullname.text.toString().trim()
            val firmName = binding.etFirmName.text.toString().trim()
            registerUser(email, password, fullName, firmName )
        }
        binding.tvHasAccount.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
    private fun showNameExistsAlert(isNotValid: Boolean){
        binding.fullname.error = if (isNotValid) "Can not be empty!" else null
    }

    private fun showTextMinimalAlert(isNotValid: Boolean, text : String) {
         if (text == "Password")
            binding.etPassword.error = if(isNotValid) "$text too weak!" else null
    }

    private fun showEmailValidAlert(isNotValid: Boolean) {
        binding.etEmail.error = if(isNotValid) "Invalid email format!" else null
    }

    private fun showPasswordConfirmAlert(isNotValid: Boolean) {
        binding.etConfirmPassword.error = if (isNotValid) "Passwords are not the same!" else null
    }

    private fun registerUser(email: String, password: String, fullName: String, firmName: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid

                    val sharedPref = getSharedPreferences("WorkTrackr", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putString("userId", userId)
                    editor.apply()

                    val db = FirebaseFirestore.getInstance()
                    val fullNameData = hashMapOf(
                        "fullName" to fullName
                    )
                    val firmNameData = hashMapOf(
                        "firmName" to firmName
                    )
                    if (userId != null) {
                        db.collection("fullName").document(userId)
                            .set(fullNameData)
                        db.collection("firmName").document(userId)
                            .set(firmNameData)

                            .addOnSuccessListener {
                                startActivity(Intent(this, LoginActivity::class.java))
                                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Registration successful, but failed to save data: $exception", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }

            }
    }
}