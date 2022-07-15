package com.kodgem.coachingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kodgem.coachingapp.adapter.StudiesRecyclerAdapter
import com.kodgem.coachingapp.databinding.ActivityMainBinding
import com.kodgem.coachingapp.models.Study


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerViewAdapter: StudiesRecyclerAdapter
    private var studyList = ArrayList<Study>()

    public override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            this.startActivity(intent)
            finish()
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val nameAndSurnameTextView = binding.nameAndSurnameTextView
        val transitionsContainer = binding.transitionsContainer
        val sayacContainer = binding.sayacContainer

        var sayacVisible = false

        val contentTextView = binding.contentTextView
        val addStudyButton = binding.addStudyButton
        val signOutButton = binding.signOutButton
        val sayacButton = binding.sayacButton

        auth = Firebase.auth
        db = Firebase.firestore

        recyclerView = binding.previousStudies
        val layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = layoutManager
        recyclerViewAdapter = StudiesRecyclerAdapter(studyList)
        recyclerView.adapter = recyclerViewAdapter

        var visible = false
        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            nameAndSurnameTextView.text = it.get("nameAndSurname").toString()

            TransitionManager.beginDelayedTransition(transitionsContainer)
            visible = !visible
            nameAndSurnameTextView.visibility = if (visible) View.VISIBLE else View.GONE


            if (it.get("personType").toString() == "Student") {
                sayacButton.visibility = View.VISIBLE
                contentTextView.text = "Geçmiş Çalışmalarım"

                db.collection("School").document("SchoolIDDDD").collection("Student")
                    .document(auth.uid.toString()).collection("Studies")
                    .orderBy("subjectTheme", Query.Direction.ASCENDING)
                    .addSnapshotListener { it1, _ ->
                        studyList.clear()
                        val documents = it1!!.documents
                        for (document in documents) {
                            val subjectTheme = document.get("subjectTheme").toString()
                            val subjectCount = document.get("toplamCalisma").toString()
                            val currentStudy = Study(subjectTheme, subjectCount)

                            studyList.add(currentStudy)

                        }
                        recyclerViewAdapter.notifyDataSetChanged()

                    }


            } else if (it.get("personType").toString() == "Teacher") {
                contentTextView.text = "Öğrencilerim"

            }
            TransitionManager.beginDelayedTransition(sayacContainer)
            sayacVisible = !sayacVisible
            contentTextView.visibility = if (sayacVisible) View.VISIBLE else View.GONE

        }



        addStudyButton.setOnClickListener {
            val intent = Intent(this, ClassesActivity::class.java)
            this.startActivity(intent)
        }
        signOutButton.setOnClickListener {
            signOut()
        }

        sayacButton.setOnClickListener {
            val intent = Intent(this, ChronometerActivity::class.java)
            this.startActivity(intent)
        }


    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        this.startActivity(intent)
        finish()
    }
}