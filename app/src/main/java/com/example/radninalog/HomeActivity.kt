package com.example.radninalog

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.example.radninalog.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var btnPunchIn: Button
    private lateinit var btnPause: Button
    private lateinit var btnPunchOut: Button
    private lateinit var tvElapsedTime: TextView
    private lateinit var tvPausedTime: TextView

    private var punchInTimeFormatted: String = ""
    private var timer: CountDownTimer? = null
    private var isTimerRunning = false
    private var startTime: Long = 0
    private var pausedTime: Long = 0
    private var pausedTimeStart: Long = 0
    private var punchInTime: Long = 0
    private var punchOutTime: Long = 0

    private lateinit var spinner: Spinner
    private lateinit var textView: TextView
    private lateinit var textViewLaw : TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        btnPunchIn = findViewById(R.id.btnPunchIn)
        btnPause = findViewById(R.id.btnPause)
        btnPunchOut = findViewById(R.id.btnPunchOut)
        tvElapsedTime = findViewById(R.id.tvElapsedTime)
        tvPausedTime = findViewById(R.id.tvPausedTime)

        btnPunchIn.setOnClickListener { startTimer() }
        btnPause.setOnClickListener { handlePauseButtonClick() }
        btnPunchOut.setOnClickListener { stopTimer() }

        auth = FirebaseAuth.getInstance()

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            Intent(this, MainActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it)
                Toast.makeText(this, "Logout successful!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCollection.setOnClickListener {
            startActivity(Intent(this, CollectionActivity::class.java))
        }

        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)


        binding.calendarBtn.setOnClickListener {
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, _, _, _ ->

            }, year, month, day)
            dpd.show()
        }
        FirebaseApp.initializeApp(this)

        spinner = findViewById(R.id.mainJob)
        textView = findViewById(R.id.mainJob_label)

        val jobOptions: MutableList<String> = mutableListOf()
        val mainJobsCollectionRef = FirebaseFirestore.getInstance().collection("mainJobs").orderBy("name", Query.Direction.ASCENDING)

        mainJobsCollectionRef.get()
            .addOnSuccessListener { snapshot ->
                for (document in snapshot.documents) {
                    val jobName = document.getString("name")
                    jobName?.let { jobOptions.add(it) }
                }
                jobOptions.add(0, "Select job type")
                val adapter = object : ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_item,
                    jobOptions
                ) {
                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getDropDownView(position, convertView, parent)
                        view.setBackgroundColor(Color.WHITE)

                        val textView = view.findViewById<TextView>(android.R.id.text1)
                        textView.setTextColor(Color.DKGRAY)

                        return view
                    }

                    override fun isEnabled(position: Int): Boolean {
                        return position != 0
                    }
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = super.getView(position, convertView, parent)
                        if (position == 0) {
                            (view as TextView).setTextColor(Color.GRAY)
                        } else {
                            (view as TextView).setTextColor(Color.BLACK)
                        }
                        return view
                    }
                }
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.e("Spinner", "Error retrieving job options: $exception")
            }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedOption = parent?.getItemAtPosition(position).toString()
                textView.text = selectedOption

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }


        val spinnerLaw = findViewById<Spinner>(R.id.jobs_second)
        textViewLaw = findViewById<TextView>(R.id.labe_pod_posao)


        val initialOptions: MutableList<String> = mutableListOf("Select your job")
        val initialAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            initialOptions
        )

        initialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLaw.adapter = initialAdapter


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedOption = parent?.getItemAtPosition(position).toString()
                textView.text = selectedOption

                when (selectedOption) {
                    "Law" -> {
                        val lawJobsCollectionRef = FirebaseFirestore.getInstance().collection("law").orderBy("nameLaw", Query.Direction.ASCENDING)

                        lawJobsCollectionRef.get()
                            .addOnSuccessListener { snapshot ->
                                val jobLawOptions: MutableList<String> = mutableListOf()

                                for (document in snapshot.documents) {
                                    val jobNameLaw = document.getString("nameLaw")
                                    jobNameLaw?.let { jobLawOptions.add(it) }
                                }
                                jobLawOptions.add(0, "Select your job")
                                val adapter = object : ArrayAdapter<String>(
                                    applicationContext,
                                    android.R.layout.simple_spinner_item,
                                    jobLawOptions
                                ) {
                                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getDropDownView(position, convertView, parent)
                                        view.setBackgroundColor(Color.WHITE)

                                        val textView = view.findViewById<TextView>(android.R.id.text1)
                                        textView.setTextColor(Color.DKGRAY)
                                        return view
                                    }

                                    override fun isEnabled(position: Int): Boolean {
                                        return position != 0
                                    }

                                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getView(position, convertView, parent)
                                        if (position == 0) {
                                            (view as TextView).setTextColor(Color.GRAY)
                                        } else {
                                            (view as TextView).setTextColor(Color.BLACK)
                                        }
                                        return view
                                    }
                                }
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinnerLaw.adapter = adapter
                                spinnerLaw.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                        val selectedOption = parent?.getItemAtPosition(position).toString()
                                        textViewLaw.text = selectedOption
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Spinner", "Error retrieving job options: $exception")
                            }
                    }
                    "Manufacturing" -> {
                        val lawJobsCollectionRef = FirebaseFirestore.getInstance().collection("manufacturing").orderBy("nameManu", Query.Direction.ASCENDING)

                        lawJobsCollectionRef.get()
                            .addOnSuccessListener { snapshot ->
                                val jobLawOptions: MutableList<String> = mutableListOf()

                                for (document in snapshot.documents) {
                                    val jobNameLaw = document.getString("nameManu")
                                    jobNameLaw?.let { jobLawOptions.add(it) }
                                }
                                jobLawOptions.add(0, "Select your job")

                                val adapter = object : ArrayAdapter<String>(
                                    applicationContext,
                                    android.R.layout.simple_spinner_item,
                                    jobLawOptions
                                ) {
                                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getDropDownView(position, convertView, parent)
                                        view.setBackgroundColor(Color.WHITE)

                                        val textView = view.findViewById<TextView>(android.R.id.text1)
                                        textView.setTextColor(Color.DKGRAY)
                                        return view
                                    }
                                    override fun isEnabled(position: Int): Boolean {

                                        return position != 0
                                    }

                                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getView(position, convertView, parent)
                                        if (position == 0) {
                                            (view as TextView).setTextColor(Color.GRAY)
                                        } else {
                                            (view as TextView).setTextColor(Color.BLACK)
                                        }
                                        return view
                                    }
                                }
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinnerLaw.adapter = adapter
                                spinnerLaw.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                        val selectedOption = parent?.getItemAtPosition(position).toString()
                                        textViewLaw.text = selectedOption
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Spinner", "Error retrieving job options: $exception")
                            }
                    }
                    "Hospitality and tourism" -> {
                        val lawJobsCollectionRef = FirebaseFirestore.getInstance().collection("hospitality").orderBy("nameHosp", Query.Direction.ASCENDING)

                        lawJobsCollectionRef.get()
                            .addOnSuccessListener { snapshot ->
                                val jobLawOptions: MutableList<String> = mutableListOf()

                                for (document in snapshot.documents) {
                                    val jobNameLaw = document.getString("nameHosp")
                                    jobNameLaw?.let { jobLawOptions.add(it) }
                                }
                                jobLawOptions.add(0, "Select your job")

                                val adapter = object : ArrayAdapter<String>(
                                    applicationContext,
                                    android.R.layout.simple_spinner_item,
                                    jobLawOptions
                                ) {
                                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getDropDownView(position, convertView, parent)
                                        view.setBackgroundColor(Color.WHITE)

                                        val textView = view.findViewById<TextView>(android.R.id.text1)
                                        textView.setTextColor(Color.DKGRAY)
                                        return view
                                    }
                                    override fun isEnabled(position: Int): Boolean {
                                        return position != 0
                                    }

                                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getView(position, convertView, parent)
                                        if (position == 0) {
                                            (view as TextView).setTextColor(Color.GRAY)
                                        } else {
                                            (view as TextView).setTextColor(Color.BLACK)
                                        }
                                        return view
                                    }
                                }
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinnerLaw.adapter = adapter
                                spinnerLaw.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                        val selectedOption = parent?.getItemAtPosition(position).toString()
                                        textViewLaw.text = selectedOption
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Spinner", "Error retrieving job options: $exception")
                            }
                    }
                    "Art, craft and design" -> {
                        val lawJobsCollectionRef = FirebaseFirestore.getInstance().collection("art").orderBy("nameArt", Query.Direction.ASCENDING)

                        lawJobsCollectionRef.get()
                            .addOnSuccessListener { snapshot ->
                                val jobLawOptions: MutableList<String> = mutableListOf()

                                for (document in snapshot.documents) {
                                    val jobNameLaw = document.getString("nameArt")
                                    jobNameLaw?.let { jobLawOptions.add(it) }
                                }
                                jobLawOptions.add(0, "Select your job")

                                val adapter = object : ArrayAdapter<String>(
                                    applicationContext,
                                    android.R.layout.simple_spinner_item,
                                    jobLawOptions
                                ) {
                                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getDropDownView(position, convertView, parent)
                                        view.setBackgroundColor(Color.WHITE)

                                        val textView = view.findViewById<TextView>(android.R.id.text1)
                                        textView.setTextColor(Color.DKGRAY)
                                        return view
                                    }
                                    override fun isEnabled(position: Int): Boolean {
                                        return position != 0
                                    }

                                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getView(position, convertView, parent)
                                        if (position == 0) {
                                            (view as TextView).setTextColor(Color.GRAY)
                                        } else {
                                            (view as TextView).setTextColor(Color.BLACK)
                                        }
                                        return view
                                    }
                                }
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinnerLaw.adapter = adapter
                                spinnerLaw.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                        val selectedOption = parent?.getItemAtPosition(position).toString()
                                        textViewLaw.text = selectedOption
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Spinner", "Error retrieving job options: $exception")
                            }
                    }
                    "Environmental and agricultural" -> {
                        val lawJobsCollectionRef = FirebaseFirestore.getInstance().collection("environment").orderBy("nameEnv", Query.Direction.ASCENDING)

                        lawJobsCollectionRef.get()
                            .addOnSuccessListener { snapshot ->
                                val jobLawOptions: MutableList<String> = mutableListOf()

                                for (document in snapshot.documents) {
                                    val jobNameLaw = document.getString("nameEnv")
                                    jobNameLaw?.let { jobLawOptions.add(it) }
                                }
                                jobLawOptions.add(0, "Select your job")

                                val adapter = object : ArrayAdapter<String>(
                                    applicationContext,
                                    android.R.layout.simple_spinner_item,
                                    jobLawOptions
                                ) {
                                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getDropDownView(position, convertView, parent)
                                        view.setBackgroundColor(Color.WHITE)

                                        val textView = view.findViewById<TextView>(android.R.id.text1)
                                        textView.setTextColor(Color.DKGRAY)
                                        return view
                                    }
                                    override fun isEnabled(position: Int): Boolean {
                                        return position != 0
                                    }

                                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getView(position, convertView, parent)
                                        if (position == 0) {
                                            (view as TextView).setTextColor(Color.GRAY)
                                        } else {
                                            (view as TextView).setTextColor(Color.BLACK)
                                        }
                                        return view
                                    }
                                }
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinnerLaw.adapter = adapter
                                spinnerLaw.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                        val selectedOption = parent?.getItemAtPosition(position).toString()
                                        textViewLaw.text = selectedOption
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Spinner", "Error retrieving job options: $exception")
                            }
                    }
                    "Health care" -> {
                        val lawJobsCollectionRef = FirebaseFirestore.getInstance().collection("health").orderBy("nameCare", Query.Direction.ASCENDING)

                        lawJobsCollectionRef.get()
                            .addOnSuccessListener { snapshot ->
                                val jobLawOptions: MutableList<String> = mutableListOf()

                                for (document in snapshot.documents) {
                                    val jobNameLaw = document.getString("nameCare")
                                    jobNameLaw?.let { jobLawOptions.add(it) }
                                }
                                jobLawOptions.add(0, "Select your job")

                                val adapter = object : ArrayAdapter<String>(
                                    applicationContext,
                                    android.R.layout.simple_spinner_item,
                                    jobLawOptions
                                ) {
                                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getDropDownView(position, convertView, parent)
                                        view.setBackgroundColor(Color.WHITE)

                                        val textView = view.findViewById<TextView>(android.R.id.text1)
                                        textView.setTextColor(Color.DKGRAY)
                                        return view
                                    }
                                    override fun isEnabled(position: Int): Boolean {
                                        return position != 0
                                    }

                                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getView(position, convertView, parent)
                                        if (position == 0) {
                                            (view as TextView).setTextColor(Color.GRAY)
                                        } else {
                                            (view as TextView).setTextColor(Color.BLACK)
                                        }
                                        return view
                                    }
                                }
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinnerLaw.adapter = adapter
                                spinnerLaw.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                        val selectedOption = parent?.getItemAtPosition(position).toString()
                                        textViewLaw.text = selectedOption
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Spinner", "Error retrieving job options: $exception")
                            }
                    }
                    "Building and construction" -> {
                        val lawJobsCollectionRef = FirebaseFirestore.getInstance().collection("build").orderBy("nameBuild", Query.Direction.ASCENDING)

                        lawJobsCollectionRef.get()
                            .addOnSuccessListener { snapshot ->
                                val jobLawOptions: MutableList<String> = mutableListOf()

                                for (document in snapshot.documents) {
                                    val jobNameLaw = document.getString("nameBuild")
                                    jobNameLaw?.let { jobLawOptions.add(it) }
                                }
                                jobLawOptions.add(0, "Select your job")

                                val adapter = object : ArrayAdapter<String>(
                                    applicationContext,
                                    android.R.layout.simple_spinner_item,
                                    jobLawOptions
                                ) {
                                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getDropDownView(position, convertView, parent)
                                        view.setBackgroundColor(Color.WHITE)

                                        val textView = view.findViewById<TextView>(android.R.id.text1)
                                        textView.setTextColor(Color.DKGRAY)
                                        return view
                                    }
                                    override fun isEnabled(position: Int): Boolean {
                                        return position != 0
                                    }

                                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getView(position, convertView, parent)
                                        if (position == 0) {
                                            (view as TextView).setTextColor(Color.GRAY)
                                        } else {
                                            (view as TextView).setTextColor(Color.BLACK)
                                        }
                                        return view
                                    }
                                }
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinnerLaw.adapter = adapter
                                spinnerLaw.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                        val selectedOption = parent?.getItemAtPosition(position).toString()
                                        textViewLaw.text = selectedOption
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Spinner", "Error retrieving job options: $exception")
                            }
                    }
                    "Business and management" -> {
                        val lawJobsCollectionRef = FirebaseFirestore.getInstance().collection("business").orderBy("nameMan", Query.Direction.ASCENDING)

                        lawJobsCollectionRef.get()
                            .addOnSuccessListener { snapshot ->
                                val jobLawOptions: MutableList<String> = mutableListOf()

                                for (document in snapshot.documents) {
                                    val jobNameLaw = document.getString("nameMan")
                                    jobNameLaw?.let { jobLawOptions.add(it) }
                                }
                                jobLawOptions.add(0, "Select your job")

                                val adapter = object : ArrayAdapter<String>(
                                    applicationContext,
                                    android.R.layout.simple_spinner_item,
                                    jobLawOptions
                                ) {
                                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getDropDownView(position, convertView, parent)
                                        view.setBackgroundColor(Color.WHITE)

                                        val textView = view.findViewById<TextView>(android.R.id.text1)
                                        textView.setTextColor(Color.DKGRAY)
                                        return view
                                    }
                                    override fun isEnabled(position: Int): Boolean {
                                        return position != 0
                                    }

                                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getView(position, convertView, parent)
                                        if (position == 0) {
                                            (view as TextView).setTextColor(Color.GRAY)
                                        } else {
                                            (view as TextView).setTextColor(Color.BLACK)
                                        }
                                        return view
                                    }
                                }
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinnerLaw.adapter = adapter
                                spinnerLaw.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                        val selectedOption = parent?.getItemAtPosition(position).toString()
                                        textViewLaw.text = selectedOption
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Spinner", "Error retrieving job options: $exception")
                            }
                    }
                    "Education" -> {
                        val lawJobsCollectionRef = FirebaseFirestore.getInstance().collection("education").orderBy("nameEdu", Query.Direction.ASCENDING)

                        lawJobsCollectionRef.get()
                            .addOnSuccessListener { snapshot ->
                                val jobLawOptions: MutableList<String> = mutableListOf()

                                for (document in snapshot.documents) {
                                    val jobNameLaw = document.getString("nameEdu")
                                    jobNameLaw?.let { jobLawOptions.add(it) }
                                }
                                jobLawOptions.add(0, "Select your job")

                                val adapter = object : ArrayAdapter<String>(
                                    applicationContext,
                                    android.R.layout.simple_spinner_item,
                                    jobLawOptions
                                ) {
                                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getDropDownView(position, convertView, parent)
                                        view.setBackgroundColor(Color.WHITE)

                                        val textView = view.findViewById<TextView>(android.R.id.text1)
                                        textView.setTextColor(Color.DKGRAY)
                                        return view
                                    }
                                    override fun isEnabled(position: Int): Boolean {
                                        return position != 0
                                    }

                                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getView(position, convertView, parent)
                                        if (position == 0) {
                                            (view as TextView).setTextColor(Color.GRAY)
                                        } else {
                                            (view as TextView).setTextColor(Color.BLACK)
                                        }
                                        return view
                                    }
                                }
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinnerLaw.adapter = adapter
                                spinnerLaw.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                        val selectedOption = parent?.getItemAtPosition(position).toString()
                                        textViewLaw.text = selectedOption
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Spinner", "Error retrieving job options: $exception")
                            }
                    }
                    "Financial services" -> {
                        val lawJobsCollectionRef = FirebaseFirestore.getInstance().collection("finance").orderBy("nameFin", Query.Direction.ASCENDING)

                        lawJobsCollectionRef.get()
                            .addOnSuccessListener { snapshot ->
                                val jobLawOptions: MutableList<String> = mutableListOf()

                                for (document in snapshot.documents) {
                                    val jobNameLaw = document.getString("nameFin")
                                    jobNameLaw?.let { jobLawOptions.add(it) }
                                }
                                jobLawOptions.add(0, "Select your job")

                                val adapter = object : ArrayAdapter<String>(
                                    applicationContext,
                                    android.R.layout.simple_spinner_item,
                                    jobLawOptions
                                ) {
                                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getDropDownView(position, convertView, parent)
                                        view.setBackgroundColor(Color.WHITE)

                                        val textView = view.findViewById<TextView>(android.R.id.text1)
                                        textView.setTextColor(Color.DKGRAY)
                                        return view
                                    }
                                    override fun isEnabled(position: Int): Boolean {
                                        return position != 0
                                    }

                                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getView(position, convertView, parent)
                                        if (position == 0) {
                                            (view as TextView).setTextColor(Color.GRAY)
                                        } else {
                                            (view as TextView).setTextColor(Color.BLACK)
                                        }
                                        return view
                                    }
                                }
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinnerLaw.adapter = adapter
                                spinnerLaw.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                        val selectedOption = parent?.getItemAtPosition(position).toString()
                                        textViewLaw.text = selectedOption
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Spinner", "Error retrieving job options: $exception")
                            }
                    }
                    "Hair and beauty" -> {
                        val lawJobsCollectionRef = FirebaseFirestore.getInstance().collection("hair").orderBy("nameHair", Query.Direction.ASCENDING)

                        lawJobsCollectionRef.get()
                            .addOnSuccessListener { snapshot ->
                                val jobLawOptions: MutableList<String> = mutableListOf()

                                for (document in snapshot.documents) {
                                    val jobNameLaw = document.getString("nameHair")
                                    jobNameLaw?.let { jobLawOptions.add(it) }
                                }
                                jobLawOptions.add(0, "Select your job")

                                val adapter = object : ArrayAdapter<String>(
                                    applicationContext,
                                    android.R.layout.simple_spinner_item,
                                    jobLawOptions
                                ) {
                                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getDropDownView(position, convertView, parent)
                                        view.setBackgroundColor(Color.WHITE)

                                        val textView = view.findViewById<TextView>(android.R.id.text1)
                                        textView.setTextColor(Color.DKGRAY)
                                        return view
                                    }
                                    override fun isEnabled(position: Int): Boolean {
                                        return position != 0
                                    }

                                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getView(position, convertView, parent)
                                        if (position == 0) {
                                            (view as TextView).setTextColor(Color.GRAY)
                                        } else {
                                            (view as TextView).setTextColor(Color.BLACK)
                                        }
                                        return view
                                    }
                                }
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinnerLaw.adapter = adapter
                                spinnerLaw.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                        val selectedOption = parent?.getItemAtPosition(position).toString()
                                        textViewLaw.text = selectedOption
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Spinner", "Error retrieving job options: $exception")
                            }
                    }
                    "IT and computer science" -> {
                        val lawJobsCollectionRef = FirebaseFirestore.getInstance().collection("IT").orderBy("nameIT", Query.Direction.ASCENDING)

                        lawJobsCollectionRef.get()
                            .addOnSuccessListener { snapshot ->
                                val jobLawOptions: MutableList<String> = mutableListOf()

                                for (document in snapshot.documents) {
                                    val jobNameLaw = document.getString("nameIT")
                                    jobNameLaw?.let { jobLawOptions.add(it) }
                                }
                                jobLawOptions.add(0, "Select your job")

                                val adapter = object : ArrayAdapter<String>(
                                    applicationContext,
                                    android.R.layout.simple_spinner_item,
                                    jobLawOptions
                                ) {
                                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getDropDownView(position, convertView, parent)
                                        view.setBackgroundColor(Color.WHITE)

                                        val textView = view.findViewById<TextView>(android.R.id.text1)
                                        textView.setTextColor(Color.DKGRAY)
                                        return view
                                    }
                                    override fun isEnabled(position: Int): Boolean {
                                        return position != 0
                                    }

                                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getView(position, convertView, parent)
                                        if (position == 0) {
                                            (view as TextView).setTextColor(Color.GRAY)
                                        } else {
                                            (view as TextView).setTextColor(Color.BLACK)
                                        }
                                        return view
                                    }
                                }
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinnerLaw.adapter = adapter
                                spinnerLaw.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                        val selectedOption = parent?.getItemAtPosition(position).toString()
                                        textViewLaw.text = selectedOption
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Spinner", "Error retrieving job options: $exception")
                            }
                    }
                    "Marketing and advertising" -> {
                        val lawJobsCollectionRef = FirebaseFirestore.getInstance().collection("marketing").orderBy("nameMark", Query.Direction.ASCENDING)

                        lawJobsCollectionRef.get()
                            .addOnSuccessListener { snapshot ->
                                val jobLawOptions: MutableList<String> = mutableListOf()

                                for (document in snapshot.documents) {
                                    val jobNameLaw = document.getString("nameMark")
                                    jobNameLaw?.let { jobLawOptions.add(it) }
                                }
                                jobLawOptions.add(0, "Select your job")

                                val adapter = object : ArrayAdapter<String>(
                                    applicationContext,
                                    android.R.layout.simple_spinner_item,
                                    jobLawOptions
                                ) {
                                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getDropDownView(position, convertView, parent)
                                        view.setBackgroundColor(Color.WHITE)

                                        val textView = view.findViewById<TextView>(android.R.id.text1)
                                        textView.setTextColor(Color.DKGRAY)
                                        return view
                                    }
                                    override fun isEnabled(position: Int): Boolean {
                                        return position != 0
                                    }

                                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getView(position, convertView, parent)
                                        if (position == 0) {
                                            (view as TextView).setTextColor(Color.GRAY)
                                        } else {
                                            (view as TextView).setTextColor(Color.BLACK)
                                        }
                                        return view
                                    }
                                }
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinnerLaw.adapter = adapter
                                spinnerLaw.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                        val selectedOption = parent?.getItemAtPosition(position).toString()
                                        textViewLaw.text = selectedOption
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Spinner", "Error retrieving job options: $exception")
                            }
                    }
                    "Retail and customer service" -> {
                        val lawJobsCollectionRef = FirebaseFirestore.getInstance().collection("retail").orderBy("nameRet", Query.Direction.ASCENDING)

                        lawJobsCollectionRef.get()
                            .addOnSuccessListener { snapshot ->
                                val jobLawOptions: MutableList<String> = mutableListOf()

                                for (document in snapshot.documents) {
                                    val jobNameLaw = document.getString("nameRet")
                                    jobNameLaw?.let { jobLawOptions.add(it) }
                                }
                                jobLawOptions.add(0, "Select your job")

                                val adapter = object : ArrayAdapter<String>(
                                    applicationContext,
                                    android.R.layout.simple_spinner_item,
                                    jobLawOptions
                                ) {
                                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getDropDownView(position, convertView, parent)
                                        view.setBackgroundColor(Color.WHITE)

                                        val textView = view.findViewById<TextView>(android.R.id.text1)
                                        textView.setTextColor(Color.DKGRAY)
                                        return view
                                    }
                                    override fun isEnabled(position: Int): Boolean {
                                        return position != 0
                                    }

                                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getView(position, convertView, parent)
                                        if (position == 0) {
                                            (view as TextView).setTextColor(Color.GRAY)
                                        } else {

                                            (view as TextView).setTextColor(Color.BLACK)
                                        }
                                        return view
                                    }
                                }
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinnerLaw.adapter = adapter
                                spinnerLaw.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                        val selectedOption = parent?.getItemAtPosition(position).toString()
                                        textViewLaw.text = selectedOption
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Spinner", "Error retrieving job options: $exception")
                            }
                    }
                    "Science and mathematics" -> {
                        val lawJobsCollectionRef = FirebaseFirestore.getInstance().collection("science").orderBy("nameSci", Query.Direction.ASCENDING)

                        lawJobsCollectionRef.get()
                            .addOnSuccessListener { snapshot ->
                                val jobLawOptions: MutableList<String> = mutableListOf()

                                for (document in snapshot.documents) {
                                    val jobNameLaw = document.getString("nameSci")
                                    jobNameLaw?.let { jobLawOptions.add(it) }
                                }
                                jobLawOptions.add(0, "Select your job")

                                val adapter = object : ArrayAdapter<String>(
                                    applicationContext,
                                    android.R.layout.simple_spinner_item,
                                    jobLawOptions
                                ) {
                                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getDropDownView(position, convertView, parent)
                                        view.setBackgroundColor(Color.WHITE)

                                        val textView = view.findViewById<TextView>(android.R.id.text1)
                                        textView.setTextColor(Color.DKGRAY)
                                        return view
                                    }
                                    override fun isEnabled(position: Int): Boolean {
                                        return position != 0
                                    }

                                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getView(position, convertView, parent)
                                        if (position == 0) {
                                            (view as TextView).setTextColor(Color.GRAY)
                                        } else {
                                            (view as TextView).setTextColor(Color.BLACK)
                                        }
                                        return view
                                    }
                                }
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinnerLaw.adapter = adapter
                                spinnerLaw.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                        val selectedOption = parent?.getItemAtPosition(position).toString()
                                        textViewLaw.text = selectedOption
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Spinner", "Error retrieving job options: $exception")
                            }
                    }
                    "Transport and logistics" -> {
                        val lawJobsCollectionRef = FirebaseFirestore.getInstance().collection("transport").orderBy("nameTrans", Query.Direction.ASCENDING)

                        lawJobsCollectionRef.get()
                            .addOnSuccessListener { snapshot ->
                                val jobLawOptions: MutableList<String> = mutableListOf()

                                for (document in snapshot.documents) {
                                    val jobNameLaw = document.getString("nameTrans")
                                    jobNameLaw?.let { jobLawOptions.add(it) }
                                }
                                jobLawOptions.add(0, "Select your job")

                                val adapter = object : ArrayAdapter<String>(
                                    applicationContext,
                                    android.R.layout.simple_spinner_item,
                                    jobLawOptions
                                ) {
                                    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getDropDownView(position, convertView, parent)
                                        view.setBackgroundColor(Color.WHITE)

                                        val textView = view.findViewById<TextView>(android.R.id.text1)
                                        textView.setTextColor(Color.DKGRAY)
                                        return view
                                    }
                                    override fun isEnabled(position: Int): Boolean {
                                        return position != 0
                                    }

                                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                                        val view = super.getView(position, convertView, parent)
                                        if (position == 0) {
                                            (view as TextView).setTextColor(Color.GRAY)
                                        } else {
                                            (view as TextView).setTextColor(Color.BLACK)
                                        }
                                        return view
                                    }
                                }
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinnerLaw.adapter = adapter
                                spinnerLaw.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                        val selectedOption = parent?.getItemAtPosition(position).toString()
                                        textViewLaw.text = selectedOption
                                    }

                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("Spinner", "Error retrieving job options: $exception")
                            }
                    }
                    else -> {

                        val initialAdapter = object : ArrayAdapter<String>(
                            applicationContext,
                            android.R.layout.simple_spinner_item,
                            initialOptions
                        ) {
                            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                                val view = super.getDropDownView(position, convertView, parent)
                                view.setBackgroundColor(Color.WHITE)

                                val textView = view.findViewById<TextView>(android.R.id.text1)
                                textView.setTextColor(Color.DKGRAY)

                                return view
                            }
                        }

                        initialAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerLaw.adapter = initialAdapter
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

    }


    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun startTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true

            punchInTime = System.currentTimeMillis()
            val format = SimpleDateFormat("dd/MM/yyyy; hh:mm:ss a")
            punchInTimeFormatted = format.format(Date(punchInTime))

            startTime = System.currentTimeMillis() - pausedTime
            btnPunchIn.visibility = View.GONE
            btnPause.visibility = View.VISIBLE
            tvPausedTime.visibility = View.VISIBLE
            tvElapsedTime.visibility = View.VISIBLE
            btnPause.text = "Pause"
            btnPunchOut.visibility = View.VISIBLE

            timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val currentTime = System.currentTimeMillis()
                    val elapsedTime = currentTime - startTime
                    updateElapsedTime(elapsedTime)
                }
                override fun onFinish() {
                }
            }.start()
        } else {
            resumeTimer()
        }
    }

    private var pausedTimeTimer: CountDownTimer? = null

    @SuppressLint("SetTextI18n")
    private fun pauseTimer() {
        if (isTimerRunning) {
            timer?.cancel()
            isTimerRunning = false
            btnPause.text = "Continue"

            val currentTime = System.currentTimeMillis()
            pausedTimeStart = currentTime // zapocni mjeriti vrijeme

            pausedTimeTimer?.cancel()

            pausedTimeTimer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val currentTimer = System.currentTimeMillis()
                    val currentPausedTime = currentTimer - pausedTimeStart + pausedTime
                    updatePausedTime(currentPausedTime) // Update the paused time display
                }

                override fun onFinish() {
                }
            }.start()
        }
    }


    @SuppressLint("SetTextI18n")
    private fun resumeTimer() {
        if (!isTimerRunning) {
            isTimerRunning = true
            btnPause.text = "Pause"

            pausedTimeTimer?.cancel()

            val currentTime = System.currentTimeMillis()
            pausedTime += currentTime - pausedTimeStart

            timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val currentTimer = System.currentTimeMillis()
                    val elapsedTime = currentTimer - startTime - pausedTime
                    updateElapsedTime(elapsedTime)
                }

                override fun onFinish() {
                }
            }.start()
        }
    }

    private fun handlePauseButtonClick() {
        if (isTimerRunning) {
            pauseTimer()
        } else {
            resumeTimer()
        }
    }


    @SuppressLint("SimpleDateFormat")
    private fun stopTimer() {
        timer?.cancel()
        isTimerRunning = false
        punchOutTime = System.currentTimeMillis()
        val format = SimpleDateFormat("dd/MM/yyyy; hh:mm:ss a")
        val punchOutTimeFormatted = format.format(Date(punchOutTime))
        //vremena i poslovi
        val intent = Intent(this, WorkInfoActivity::class.java)
        intent.putExtra("startTime", "Start time: $punchInTimeFormatted")
        intent.putExtra("endTime", "End time: $punchOutTimeFormatted")
        intent.putExtra("elapsedTime", tvElapsedTime.text.toString())
        intent.putExtra("pausedTime", tvPausedTime.text.toString())
        intent.putExtra("selectedOption", "Branch of job: " + textView.text.toString())
        intent.putExtra("selectedJob", "Your job: " + textViewLaw.text.toString())
        //Materijali
        val materialsUsedText = findViewById<TextInputEditText>(R.id.materials_used_text)
        val textMaterial = materialsUsedText.text.toString()
        DataHolder.materialsUsed = "Materials used: $textMaterial"
        //Toolovi
        val toolsUsedText = findViewById<TextInputEditText>(R.id.tools_used_text)
        val textTool = toolsUsedText.text.toString()
        DataToolsHolder.toolsUsed = "Tools used: $textTool"
        startActivity(intent)

        startTime = 0
        pausedTime = 0
        pausedTimeStart = 0

        pausedTimeTimer?.cancel()
        pausedTimeTimer = null
        pausedTimeStart = System.currentTimeMillis()
        updateElapsedTime(0)
        updatePausedTime(0)

        btnPunchIn.visibility = View.VISIBLE
        btnPause.visibility = View.GONE
        btnPunchOut.visibility = View.GONE
        tvPausedTime.visibility = View.GONE
        tvElapsedTime.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun updateElapsedTime(elapsedTime: Long) {
        val seconds = (elapsedTime / 1000) % 60
        val minutes = (elapsedTime / (1000 * 60)) % 60
        val hours = (elapsedTime / (1000 * 60 * 60)) % 24

        val elapsedTimeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        tvElapsedTime.text = "Time spent working: $elapsedTimeString"
    }

    @SuppressLint("SetTextI18n")
    private fun updatePausedTime(pausedTime: Long) {
        val seconds = (pausedTime / 1000) % 60
        val minutes = (pausedTime / (1000 * 60)) % 60
        val hours = (pausedTime / (1000 * 60 * 60)) % 24

        val pausedTimeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        tvPausedTime.text = "Time spent on break: $pausedTimeString"
    }

}
