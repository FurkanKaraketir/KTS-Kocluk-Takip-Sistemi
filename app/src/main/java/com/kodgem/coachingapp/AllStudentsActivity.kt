package com.kodgem.coachingapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kodgem.coachingapp.adapter.AllStudentsRecyclerAdapter
import com.kodgem.coachingapp.adapter.StudentsRecyclerAdapter
import com.kodgem.coachingapp.databinding.ActivityAllStudentsBinding
import com.kodgem.coachingapp.models.Student

class AllStudentsActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerViewAllStudentsAdapter: AllStudentsRecyclerAdapter

    private lateinit var binding: ActivityAllStudentsBinding
    private lateinit var recyclerViewAllStudents: RecyclerView
    private var studentList = ArrayList<Student>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllStudentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore

        recyclerViewAllStudents = binding.recyclerViewAllStudents
        val layoutManager = LinearLayoutManager(applicationContext)
        recyclerViewAllStudents.layoutManager = layoutManager
        recyclerViewAllStudentsAdapter = AllStudentsRecyclerAdapter(studentList)
        recyclerViewAllStudents.adapter = recyclerViewAllStudentsAdapter
        db.collection("School").document("SchoolIDDDD").collection("Student")
            .whereEqualTo("teacher", "").addSnapshotListener { value, _ ->
                studentList.clear()
                if (value != null) {
                    for (document in value) {
                        val studentName = document.get("nameAndSurname").toString()
                        val teacher = document.get("teacher").toString()
                        val studentID = document.get("id").toString()
                        val currentStudent = Student(studentName, teacher,studentID)
                        studentList.add(currentStudent)
                    }
                }

                db.collection("School").document("SchoolIDDDD").collection("Student")
                    .whereEqualTo("teacher", auth.uid.toString()).addSnapshotListener { value2, _ ->
                        if (value2 != null) {
                            for (document in value2) {
                                val studentName = document.get("nameAndSurname").toString()
                                val teacher = document.get("teacher").toString()
                                val studentID = document.get("id").toString()
                                val currentStudent = Student(studentName,teacher,studentID)
                                studentList.add(currentStudent)
                            }
                            recyclerViewAllStudentsAdapter.notifyDataSetChanged()
                        }
                    }
            }


    }
}