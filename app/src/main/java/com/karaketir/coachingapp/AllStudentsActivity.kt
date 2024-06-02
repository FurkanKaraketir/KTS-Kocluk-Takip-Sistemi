package com.karaketir.coachingapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerViewAllStudentsAdapter: AllStudentsRecyclerAdapter

    private lateinit var binding: ActivityAllStudentsBinding
    private var kurumKodu = 0
    private lateinit var recyclerViewAllStudents: RecyclerView
    private var gradeList = arrayOf("Bütün Sınıflar", "12", "11", "10", "9", "0")
    private var studentList = ArrayList<Student>()
    private lateinit var filteredList: ArrayList<Student>

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllStudentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore

        kurumKodu = intent.getStringExtra("kurumKodu")?.toIntOrNull() ?: 0

        val searchEditText = binding.searchStudentAllStudentsActivityEditText
        recyclerViewAllStudents = binding.recyclerViewAllStudents
        val gradeSpinner = binding.gradeAllSpinner
        setupRecyclerView(studentList)

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                filteredList = ArrayList()
                if (p0.toString().isNotEmpty()) {
                    for (item in studentList) {
                        if (item.studentName.lowercase(Locale.getDefault())
                                .contains(p0.toString().lowercase(Locale.getDefault()))
                        ) {
                            filteredList.add(item)
                        }
                    }
                    filteredList.sortBy { it.studentName }
                    setupRecyclerView(filteredList)
                } else {
                    setupRecyclerView(studentList)
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        val gradeAdapter = ArrayAdapter(
            this@AllStudentsActivity, android.R.layout.simple_spinner_item, gradeList
        )
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gradeSpinner.adapter = gradeAdapter
        gradeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedGrade = gradeList[p2]
                if (selectedGrade == "Bütün Sınıflar") {
                    db.collection("School").document(kurumKodu.toString()).collection("Student")
                        .whereEqualTo("teacher", "").addSnapshotListener { documents, _ ->
                            studentList.clear()
                            if (documents != null) {
                                for (document in documents) {
                                    val studentGrade = document.get("grade").toString().toInt()
                                    val studentName = document.getString("nameAndSurname").orEmpty()
                                    val teacher = document.getString("teacher").orEmpty()
                                    val id = document.getString("id").orEmpty()
                                    val currentStudent =
                                        Student(studentName, teacher, id, studentGrade)
                                    studentList.add(currentStudent)
                                }
                            }
                            studentList.sortBy { it.studentName }
                            setupRecyclerView(studentList)
                            recyclerViewAllStudentsAdapter.notifyDataSetChanged()
                        }
                } else {
                    db.collection("School").document(kurumKodu.toString()).collection("Student")
                        .whereEqualTo("teacher", "")
                        .whereEqualTo("grade", selectedGrade.toIntOrNull() ?: 0)
                        .addSnapshotListener { documents, _ ->
                            studentList.clear()
                            if (documents != null) {
                                for (document in documents) {
                                    val studentGrade = document.get("grade").toString().toInt()
                                    val studentName = document.getString("nameAndSurname").orEmpty()
                                    val teacher = document.getString("teacher").orEmpty()
                                    val id = document.getString("id").orEmpty()
                                    val currentStudent =
                                        Student(studentName, teacher, id, studentGrade)
                                    studentList.add(currentStudent)
                                }
                            }
                            studentList.sortBy { it.studentName }
                            setupRecyclerView(studentList)
                            recyclerViewAllStudentsAdapter.notifyDataSetChanged()
                        }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView(list: ArrayList<Student>) {
        val layoutManager = LinearLayoutManager(applicationContext)
        recyclerViewAllStudents.layoutManager = layoutManager
        recyclerViewAllStudentsAdapter = AllStudentsRecyclerAdapter(list, kurumKodu)
        recyclerViewAllStudents.adapter = recyclerViewAllStudentsAdapter
    }
}
