package com.karaketir.coachingapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.adapter.AllStudentsRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityAllStudentsBinding
import com.karaketir.coachingapp.models.Student
import java.util.*

class AllStudentsActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerViewAllStudentsAdapter: AllStudentsRecyclerAdapter

    private lateinit var binding: ActivityAllStudentsBinding
    private lateinit var recyclerViewAllStudents: RecyclerView
    private var studentList = ArrayList<Student>()
    private lateinit var filteredList: ArrayList<Student>

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllStudentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore

        val searchEditText = binding.searchStudentAllStudentsActivityEditText
        recyclerViewAllStudents = binding.recyclerViewAllStudents
        setupRecyclerView(studentList)

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                filteredList = ArrayList()
                if (p0.toString() != "") {
                    for (item in studentList) {
                        if (item.studentName.lowercase(Locale.getDefault())
                                .contains(p0.toString().lowercase(Locale.getDefault()))
                        ) {
                            filteredList.sortBy { it.studentName }
                            filteredList.add(item)
                        }
                    }
                    setupRecyclerView(filteredList)
                } else {
                    setupRecyclerView(studentList)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
        var kurumKodu: Int

        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener { it ->
            kurumKodu = it.get("kurumKodu").toString().toInt()

            db.collection("School").document(kurumKodu.toString()).collection("Student")
                .whereEqualTo("teacher", "").addSnapshotListener { value2, _ ->
                    studentList.clear()
                    if (value2 != null) {
                        for (document in value2) {
                            val studentName = document.get("nameAndSurname").toString()
                            val teacher = document.get("teacher").toString()
                            val studentID = document.get("id").toString()
                            val currentStudent = Student(studentName, teacher, studentID)
                            studentList.add(currentStudent)
                        }
                        studentList.sortBy { it.studentName }
                        recyclerViewAllStudentsAdapter.notifyDataSetChanged()
                    }
                }

        }


    }

    private fun setupRecyclerView(list: ArrayList<Student>) {
        val layoutManager = LinearLayoutManager(applicationContext)
        recyclerViewAllStudents.layoutManager = layoutManager
        recyclerViewAllStudentsAdapter = AllStudentsRecyclerAdapter(list)
        recyclerViewAllStudents.adapter = recyclerViewAllStudentsAdapter
    }
}