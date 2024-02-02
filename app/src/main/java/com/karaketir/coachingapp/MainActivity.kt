package com.karaketir.coachingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
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
            this.startActivity(intent)
            finish()
        } else {
            FirebaseMessaging.getInstance().subscribeToTopic("all")
            FirebaseMessaging.getInstance().subscribeToTopic(auth.uid.toString())
        }
    }

    private fun replaceFragmentTeacher(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container_teacher, fragment)
        transaction.commit()
    }

    private fun replaceFragmentStudent(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container_student, fragment)
        transaction.commit()
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        val bottomNavigationTeacher = binding.bottomNavigationTeacher
        val bottomNavigationStudent = binding.bottomNavigationStudent
        val fragmentContainerTeacher = binding.fragmentContainerTeacher
        val fragmentContainerStudent = binding.fragmentContainerStudent


        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener { snapshot ->
            kurumKodu = snapshot["kurumKodu"].toString().toInt()
            if (snapshot["personType"].toString() == "Teacher") {
                isTeacher = true

                fragmentContainerStudent.visibility = View.GONE
                fragmentContainerTeacher.visibility = View.VISIBLE
                bottomNavigationTeacher.visibility = View.VISIBLE
                bottomNavigationStudent.visibility = View.GONE
                replaceFragmentTeacher(MainFragment())

            } else {
                isTeacher = false

                fragmentContainerStudent.visibility = View.VISIBLE
                fragmentContainerTeacher.visibility = View.GONE
                bottomNavigationTeacher.visibility = View.GONE
                bottomNavigationStudent.visibility = View.VISIBLE

                replaceFragmentStudent(MainFragment())
            }

            binding.bottomNavigationTeacher.setOnItemSelectedListener {

                when (it.itemId) {
                    R.id.navigation_home -> {
                        replaceFragmentTeacher(MainFragment())

                    }

                    R.id.navigation_stats -> {
                        replaceFragmentTeacher(StatsFragment())

                    }

                    R.id.navigation_denemeler -> {
                        replaceFragmentTeacher(DenemelerTeacherFragment())


                    }

                    R.id.navigation_settings -> {
                        replaceFragmentTeacher(SettingsFragment())
                    }

                    else -> {

                    }
                }

                true
            }

            binding.bottomNavigationStudent.setOnItemSelectedListener {

                when (it.itemId) {
                    R.id.navigation_home -> {
                        replaceFragmentStudent(MainFragment())

                    }

                    R.id.navigation_denemeler -> {
                        replaceFragmentStudent(DenemelerFragment())

                    }
                    R.id.navigation_duties -> {
                        replaceFragmentStudent(DutiesFragment())
                    }
                    R.id.navigation_settings -> {
                        replaceFragmentStudent(SettingsFragment())
                    }

                    else -> {

                    }
                }

                true
            }


        }


    }


}