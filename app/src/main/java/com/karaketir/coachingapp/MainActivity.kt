package com.karaketir.coachingapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.karaketir.coachingapp.databinding.ActivityMainBinding
import com.karaketir.coachingapp.fragments.DenemelerFragment
import com.karaketir.coachingapp.fragments.DenemelerTeacherFragment
import com.karaketir.coachingapp.fragments.DutiesFragment
import com.karaketir.coachingapp.fragments.MainFragment
import com.karaketir.coachingapp.fragments.SettingsFragment
import com.karaketir.coachingapp.fragments.StatsFragment

class MainActivity : AppCompatActivity() {

    init {
        // Setting system properties for XML parsing with Apache POI
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        )
    }

    private lateinit var auth: FirebaseAuth
    private var kurumKodu = 763455
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: FirebaseFirestore
    private var isTeacher = false

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            subscribeToFirebaseTopics()
        }
    }

    private fun subscribeToFirebaseTopics() {
        FirebaseMessaging.getInstance().subscribeToTopic("all")
        FirebaseMessaging.getInstance().subscribeToTopic(auth.uid.toString())
    }

    private fun replaceFragmentTeacher(fragment: Fragment) {
        if (!isFinishing && !supportFragmentManager.isStateSaved) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_teacher, fragment).commit()
        }
    }

    private fun replaceFragmentStudent(fragment: Fragment) {
        if (!isFinishing && !supportFragmentManager.isStateSaved) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_student, fragment).commit()
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        setupUI()
        checkAndRequestNotificationPermission()

        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener { snapshot ->
            handleUserType(snapshot)
        }.addOnFailureListener {
            // Handle potential failure in retrieving user data
        }
    }

    private fun setupUI() {
        binding.bottomNavigationTeacher.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> replaceFragmentTeacher(MainFragment(this))
                R.id.navigation_stats -> replaceFragmentTeacher(StatsFragment(this))
                R.id.navigation_denemeler -> replaceFragmentTeacher(DenemelerTeacherFragment(this))
                R.id.navigation_settings -> replaceFragmentTeacher(SettingsFragment(this))
                else -> { /* No action needed */
                }
            }
            true
        }

        binding.bottomNavigationStudent.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> replaceFragmentStudent(MainFragment(this))
                R.id.navigation_denemeler -> replaceFragmentStudent(DenemelerFragment(this))
                R.id.navigation_duties -> replaceFragmentStudent(DutiesFragment(this))
                R.id.navigation_settings -> replaceFragmentStudent(SettingsFragment(this))
                else -> { /* No action needed */
                }
            }
            true
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun handleUserType(snapshot: DocumentSnapshot) {
        kurumKodu = try {
            snapshot["kurumKodu"].toString().toInt()
        } catch (e: Exception) {
            763455
        }

        isTeacher = snapshot["personType"].toString() == "Teacher"
        if (isTeacher) {
            setupTeacherUI()
        } else {
            setupStudentUI()
        }
    }

    private fun setupTeacherUI() {
        binding.fragmentContainerStudent.visibility = View.GONE
        binding.fragmentContainerTeacher.visibility = View.VISIBLE
        binding.bottomNavigationTeacher.visibility = View.VISIBLE
        binding.bottomNavigationStudent.visibility = View.GONE
        replaceFragmentTeacher(MainFragment(this))
    }

    private fun setupStudentUI() {
        binding.fragmentContainerStudent.visibility = View.VISIBLE
        binding.fragmentContainerTeacher.visibility = View.GONE
        binding.bottomNavigationTeacher.visibility = View.GONE
        binding.bottomNavigationStudent.visibility = View.VISIBLE
        replaceFragmentStudent(MainFragment(this))
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _: Boolean ->
            // Handle the permission request response
        }
}
