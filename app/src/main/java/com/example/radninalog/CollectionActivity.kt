package com.example.radninalog

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.radninalog.databinding.ActivityCollectionBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class CollectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCollectionBinding
    private lateinit var recyclerViewCollection: RecyclerView
    private lateinit var adapter: CollectionAdapter
    private val collectionItems: MutableList<CollectionItem> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityCollectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toHomeBtn.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        recyclerViewCollection = findViewById(R.id.recyclerViewCollection)

        val sharedPref = getSharedPreferences("WorkTrackr", Context.MODE_PRIVATE)
        val userID = sharedPref.getString("userId", "")

        if (userID != null) {
            retrieveDocumentsFromCollection(userID)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun retrieveDocumentsFromCollection(userID: String) {
        val collectionRef = FirebaseFirestore.getInstance().collection("users").document(userID).collection("nalozi").orderBy("startTime", Query.Direction.ASCENDING)
        collectionRef.get()
            .addOnSuccessListener { querySnapshot ->
                for (documentSnapshot in querySnapshot) {
                    val startTime = documentSnapshot.getString("startTime")
                    val endTime = documentSnapshot.getString("endTime")
                    val elapsedTime = documentSnapshot.getString("elapsedTime")
                    val pausedTime = documentSnapshot.getString("pausedTime")
                    val materialsUsed = documentSnapshot.getString("materialsUsed")
                    val toolsUsed = documentSnapshot.getString("toolsUsed")
                    val jobType = documentSnapshot.getString("jobType")
                    val yourJob = documentSnapshot.getString("yourJob")
                    val firmName = documentSnapshot.getString("firmName")
                    val fullName = documentSnapshot.getString("fullName")

                    val item = CollectionItem(
                        startTime.toString(), endTime.toString(), elapsedTime.toString(),
                        pausedTime.toString(), materialsUsed.toString(), toolsUsed.toString(),
                        jobType.toString(), yourJob.toString(), firmName.toString(), fullName.toString()
                    )
                    collectionItems.add(item)
                }
                adapter = CollectionAdapter(collectionItems)
                recyclerViewCollection.adapter = adapter
                recyclerViewCollection.apply {
                    layoutManager = LinearLayoutManager(this@CollectionActivity)

                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error retrieving documents: $exception")
            }
    }
}
