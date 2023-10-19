package com.example.radninalog

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.example.radninalog.databinding.ActivityWorkInfoBinding
import com.google.firebase.firestore.FirebaseFirestore


class WorkInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWorkInfoBinding
    private lateinit var nameFull: String
    private lateinit var nameFirm: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityWorkInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.homeBtn.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            Toast.makeText(this, "Work order deleted!", Toast.LENGTH_SHORT).show()
        }


        val startTime = intent.getStringExtra("startTime")
        val endTime = intent.getStringExtra("endTime")
        val elapsedTime = intent.getStringExtra("elapsedTime")
        val pausedTime = intent.getStringExtra("pausedTime")

        val tvStartTime = findViewById<TextView>(R.id.pocetak)
        val tvEndTime = findViewById<TextView>(R.id.kraj)
        val tvElapsedTime = findViewById<TextView>(R.id.elaps)
        val tvPausedTime = findViewById<TextView>(R.id.pauza)

        tvStartTime.text = startTime
        tvEndTime.text = endTime
        tvElapsedTime.text = elapsedTime
        tvPausedTime.text = pausedTime

        val sharedPref = getSharedPreferences("WorkTrackr", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("userId", "")

        if (userId != null) {
            retrieveFullName(userId)
            retrieveFirmName(userId)
        }
        val materialsUsed = DataHolder.materialsUsed
        val materialTextView = findViewById<TextView>(R.id.material)
        materialTextView.text = materialsUsed

        val toolsUsed = DataToolsHolder.toolsUsed
        val toolTextView = findViewById<TextView>(R.id.tool)
        toolTextView.text = toolsUsed

        val selectedOption = intent.getStringExtra("selectedOption")
        val optionSelected = findViewById<TextView>(R.id.jobType)
        optionSelected.text = selectedOption

        val selectedJob = intent.getStringExtra("selectedJob")
        val jobSelected = findViewById<TextView>(R.id.job)
        jobSelected.text = selectedJob

        binding.addBtn.setOnClickListener {
            val firestore = FirebaseFirestore.getInstance()
            val usersCollectionRef = firestore.collection("users")
            val userDocumentRef = if (userId != null) usersCollectionRef.document(userId) else null

            if (userDocumentRef != null) {
                val naloziCollectionRef = userDocumentRef.collection("nalozi")
                val workEntryDocumentRef = naloziCollectionRef.document()

                val workEntryInfo = hashMapOf(
                    "startTime" to startTime,
                    "endTime" to endTime,
                    "elapsedTime" to elapsedTime,
                    "pausedTime" to pausedTime,
                    "materialsUsed" to materialsUsed,
                    "toolsUsed" to toolsUsed,
                    "jobType" to selectedOption,
                    "yourJob" to selectedJob,
                    "firmName" to nameFirm,
                    "fullName" to nameFull
                )

                workEntryDocumentRef.set(workEntryInfo)
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener { exception ->
                        Log.e("firestore", "Error adding document: $exception")
                    }

                naloziCollectionRef.get()
                    .addOnSuccessListener { querySnapshot ->
                        for (documentSnapshot in querySnapshot.documents) {
                            documentSnapshot.getString("startTime")
                            documentSnapshot.getString("endTime")
                            documentSnapshot.getString("elapsedTime")
                            documentSnapshot.getString("pausedTime")
                            documentSnapshot.getString("materialsUsed")
                            documentSnapshot.getString("toolsUsed")
                            documentSnapshot.getString("jobType")
                            documentSnapshot.getString("yourJob")
                            documentSnapshot.getString("firmName")
                            documentSnapshot.getString("fullName")
                        }
                        Toast.makeText(this, "Work order saved!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { exception ->
                        Log.e("firestore", "Error retrieving documents: $exception")
                    }
            }
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun retrieveFullName(userId: String) {
        val fullNameRef = FirebaseFirestore.getInstance().collection("fullName").document(userId)

        fullNameRef.get()
            .addOnSuccessListener { documentSnapshot ->
                val fullName = documentSnapshot.getString("fullName")
                val nameTextView = findViewById<TextView>(R.id.name)
                nameTextView.text = "Full name: $fullName"
                if (fullName != null) {
                    nameFull = fullName
                }
            }
            .addOnFailureListener { exception ->
                Log.e("firestore", "Error retrieving: $exception")
            }
    }

    @SuppressLint("SetTextI18n")
    private fun retrieveFirmName(userId: String) {
        val firmNameRef = FirebaseFirestore.getInstance().collection("firmName").document(userId)

        firmNameRef.get()
            .addOnSuccessListener { documentSnapshot ->
                val firmName = documentSnapshot.getString("firmName")
                val firmTextView = findViewById<TextView>(R.id.firm)
                firmTextView.text = "Firm name: $firmName"
                if (firmName != null) {
                    nameFirm = firmName
                }
            }
            .addOnFailureListener { exception ->
                Log.e("firestore", "Error retrieving: $exception")
            }
    }
}
