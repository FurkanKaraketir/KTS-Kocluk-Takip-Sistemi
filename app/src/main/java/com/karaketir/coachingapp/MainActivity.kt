package com.karaketir.coachingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.karaketir.coachingapp.notifications.NotificationDeepLink
import com.karaketir.coachingapp.notifications.NotificationPermissionHelper
import com.karaketir.coachingapp.notifications.NotificationType
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.karaketir.coachingapp.databinding.ActivityMainBinding
import com.karaketir.coachingapp.fragments.*

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var kurumKodu = 763455
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: FirebaseFirestore
    private var isTeacher = false
    private var studentWeeklyGoalsDirty = false
    private var pendingNotificationType: NotificationType? = null
    private var mainShellReady = false

    fun onStudentWeeklyGoalsSaved() {
        studentWeeklyGoalsDirty = true
        val main = supportFragmentManager.findFragmentById(R.id.fragment_container_student)
        if (main is MainFragment && !isTeacher) {
            main.reloadStudentWeeklyGoalsFromPreferences()
            studentWeeklyGoalsDirty = false
        }
    }

    fun consumeStudentWeeklyGoalsDirty(): Boolean {
        val dirty = studentWeeklyGoalsDirty
        studentWeeklyGoalsDirty = false
        return dirty
    }

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
        if (!isFinishing && !supportFragmentManager.isStateSaved) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container_teacher, fragment)
            transaction.commit()
        }
    }

    private fun replaceFragmentStudent(fragment: Fragment) {
        if (!isFinishing && !supportFragmentManager.isStateSaved) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container_student, fragment)
            transaction.commit()
        }
    }

    fun openStudentSettings() {
        if (!isTeacher) {
            selectStudentBottomNav(R.id.navigation_settings)
        }
    }

    fun selectStudentBottomNav(itemId: Int) {
        if (isTeacher) return
        binding.bottomNavigationStudent.selectedItemId = itemId
    }

    fun selectTeacherBottomNav(itemId: Int) {
        if (!isTeacher) return
        binding.bottomNavigationTeacher.selectedItemId = itemId
    }

    private fun captureNotificationIntent(intent: Intent?) {
        NotificationDeepLink.parseType(intent)?.let { pendingNotificationType = it }
    }

    private fun deliverPendingNotificationNavigation() {
        if (!mainShellReady) return
        val type = pendingNotificationType ?: return
        pendingNotificationType = null
        NotificationDeepLink.apply(this, type, kurumKodu, isTeacher)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        captureNotificationIntent(intent)
        deliverPendingNotificationNavigation()
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
        captureNotificationIntent(intent)

        supportFragmentManager.setFragmentResultListener(REQUEST_WEEKLY_GOALS_CHANGED, this) { _, _ ->
            onStudentWeeklyGoalsSaved()
        }

        val bottomNavigationTeacher = binding.bottomNavigationTeacher
        val bottomNavigationStudent = binding.bottomNavigationStudent
        val fragmentContainerTeacher = binding.fragmentContainerTeacher
        val fragmentContainerStudent = binding.fragmentContainerStudent

        if (NotificationPermissionHelper.shouldRequestPostNotificationsPermission() &&
            !NotificationPermissionHelper.hasPostNotificationsPermission(this)
        ) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener { snapshot ->
            kurumKodu = try {
                snapshot["kurumKodu"].toString().toInt()
            } catch (e: Exception) {
                763455
            }
            if (snapshot["personType"].toString() == "Teacher") {
                isTeacher = true

                fragmentContainerStudent.visibility = View.GONE
                fragmentContainerTeacher.visibility = View.VISIBLE
                bottomNavigationTeacher.visibility = View.VISIBLE
                bottomNavigationStudent.visibility = View.GONE
                binding.root.requestLayout()
                replaceFragmentTeacher(MainFragment().apply { setMainActivity(this@MainActivity) })

            } else {
                isTeacher = false

                fragmentContainerStudent.visibility = View.VISIBLE
                fragmentContainerTeacher.visibility = View.GONE
                bottomNavigationTeacher.visibility = View.GONE
                bottomNavigationStudent.visibility = View.VISIBLE

                binding.root.requestLayout()
                replaceFragmentStudent(MainFragment().apply { setMainActivity(this@MainActivity) })
            }

            binding.bottomNavigationTeacher.setOnItemSelectedListener {

                when (it.itemId) {
                    R.id.navigation_home -> {
                        replaceFragmentTeacher(MainFragment().apply { setMainActivity(this@MainActivity) })

                    }

                    R.id.navigation_stats -> {
                        replaceFragmentTeacher(StatsFragment().apply { setMainActivity(this@MainActivity) })

                    }

                    R.id.navigation_settings -> {
                        replaceFragmentTeacher(SettingsFragment().apply { setMainActivity(this@MainActivity) })
                    }

                    else -> {

                    }
                }

                true
            }

            binding.bottomNavigationStudent.setOnItemSelectedListener {

                when (it.itemId) {
                    R.id.navigation_home -> {
                        replaceFragmentStudent(MainFragment().apply { setMainActivity(this@MainActivity) })

                    }

                    R.id.navigation_duties -> {
                        replaceFragmentStudent(DutiesFragment().apply { setMainActivity(this@MainActivity) })
                    }

                    R.id.navigation_settings -> {
                        replaceFragmentStudent(SettingsFragment().apply { setMainActivity(this@MainActivity) })
                    }

                    else -> {

                    }
                }

                true
            }

            mainShellReady = true
            deliverPendingNotificationNavigation()
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _: Boolean ->

        }

    companion object {
        const val REQUEST_WEEKLY_GOALS_CHANGED = "student_weekly_goals_changed"
    }
}
