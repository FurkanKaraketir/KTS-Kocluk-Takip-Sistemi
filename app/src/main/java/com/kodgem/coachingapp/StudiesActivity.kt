package com.kodgem.coachingapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kodgem.coachingapp.adapter.StudiesRecyclerAdapter
import com.kodgem.coachingapp.databinding.ActivityStudiesBinding
import com.kodgem.coachingapp.models.Study

class StudiesActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerViewStudiesAdapter: StudiesRecyclerAdapter

    private lateinit var recyclerViewStudies: RecyclerView
    private var studyList = ArrayList<Study>()

    private lateinit var binding: ActivityStudiesBinding

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudiesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore
        val layoutManager = GridLayoutManager(applicationContext, 2)
        val intent = intent


        val studentID = intent.getStringExtra("studentID")

        recyclerViewStudies = binding.recyclerViewStudies
        recyclerViewStudies.layoutManager = layoutManager
        recyclerViewStudiesAdapter = StudiesRecyclerAdapter(studyList)
        recyclerViewStudies.adapter = recyclerViewStudiesAdapter

        if (studentID != null) {
            db.collection("School").document("SchoolIDDDD").collection("Student")
                .document(studentID).collection("Studies")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { value, _ ->
                    if (value != null) {
                        studyList.clear()
                        for (document in value) {
                            val studyName = document.get("konuAdi").toString()
                            val sure = document.get("toplamCalisma").toString()
                            val currentStudy = Study(studyName, sure)
                            studyList.add(currentStudy)
                        }
                        recyclerViewStudiesAdapter.notifyDataSetChanged()
                    }
                }
        }


    }
}