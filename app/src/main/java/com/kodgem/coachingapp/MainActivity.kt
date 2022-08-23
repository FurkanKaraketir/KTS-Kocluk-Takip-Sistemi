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
import com.kodgem.coachingapp.adapter.StudentsRecyclerAdapter
import com.kodgem.coachingapp.adapter.StudiesRecyclerAdapter
import com.kodgem.coachingapp.databinding.ActivityMainBinding
import com.kodgem.coachingapp.models.Student
import com.kodgem.coachingapp.models.Study


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerViewPreviousStudies: RecyclerView
    private lateinit var recyclerViewMyStudents: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerViewPreviousStudiesAdapter: StudiesRecyclerAdapter
    private lateinit var recyclerViewMyStudentsRecyclerAdapter: StudentsRecyclerAdapter
    private var studyList = ArrayList<Study>()
    private var studentList = ArrayList<Student>()

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
        val allStudentsBtn = binding.allStudentsBtn

        auth = Firebase.auth
        db = Firebase.firestore

        recyclerViewPreviousStudies = binding.previousStudies
        recyclerViewMyStudents = binding.myStudents

        val layoutManager = LinearLayoutManager(applicationContext)





        var visible = false
        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            nameAndSurnameTextView.text = it.get("nameAndSurname").toString()

            TransitionManager.beginDelayedTransition(transitionsContainer)
            visible = !visible
            nameAndSurnameTextView.visibility = if (visible) View.VISIBLE else View.GONE


            if (it.get("personType").toString() == "Student") {
                recyclerViewPreviousStudies.visibility = View.VISIBLE
                allStudentsBtn.visibility = View.GONE

                recyclerViewPreviousStudies.layoutManager = layoutManager
                recyclerViewPreviousStudiesAdapter = StudiesRecyclerAdapter(studyList)
                recyclerViewPreviousStudies.adapter = recyclerViewPreviousStudiesAdapter

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
                        recyclerViewPreviousStudiesAdapter.notifyDataSetChanged()

                    }


            } else if (it.get("personType").toString() == "Teacher") {
                recyclerViewMyStudents.visibility = View.VISIBLE
                allStudentsBtn.visibility = View.VISIBLE
                recyclerViewMyStudents.layoutManager = layoutManager

                recyclerViewMyStudentsRecyclerAdapter = StudentsRecyclerAdapter(studentList)

                recyclerViewMyStudents.adapter = recyclerViewMyStudentsRecyclerAdapter
                contentTextView.text = "Öğrencilerim"
                db.collection("School").document("SchoolIDDDD").collection("Student")
                    .whereEqualTo("teacher", auth.uid.toString()).addSnapshotListener { it2, _ ->

                        studentList.clear()
                        val documents = it2!!.documents
                        for (document in documents){
                            val studentName = document.get("nameAndSurname").toString()
                            val teacher = document.get("teacher").toString()
                            val id = document.get("id").toString()
                            val currentStudent = Student(studentName,teacher,id)
                            studentList.add(currentStudent)

                        }

                        recyclerViewMyStudentsRecyclerAdapter.notifyDataSetChanged()

                }


            }
            TransitionManager.beginDelayedTransition(sayacContainer)
            sayacVisible = !sayacVisible
            contentTextView.visibility = if (sayacVisible) View.VISIBLE else View.GONE

        }


        allStudentsBtn.setOnClickListener {
            val intent = Intent(this,AllStudentsActivity::class.java)
            this.startActivity(intent)
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