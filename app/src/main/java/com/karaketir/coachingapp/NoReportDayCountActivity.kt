package com.karaketir.coachingapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.adapter.NoReportDayCounterAdapter
import com.karaketir.coachingapp.databinding.ActivityNoReportDayCountBinding

class NoReportDayCountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoReportDayCountBinding
    private var secilenZaman = "Bugün"
    private var secilenGrade = "Bütün Sınıflar"
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var kurumKodu = 763455
    private var studentList = ArrayList<String>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoReportDayCountBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = Firebase.auth
        db = Firebase.firestore

        kurumKodu = intent.getStringExtra("kurumKodu").toString().toInt()
        studentList = intent.getStringArrayListExtra("studentList") as ArrayList<String>
        secilenZaman = intent.getStringExtra("secilenZaman").toString()
        secilenGrade = intent.getStringExtra("grade").toString()
        val titleNoReport = binding.titleNoReport
        titleNoReport.text = "Zaman Aralığı: $secilenZaman \nSeçilen Sınıf: $secilenGrade"

        val layoutManager = LinearLayoutManager(applicationContext)
        val recyclerViewPreviousStudies = binding.noReportDayCounterRecycler
        recyclerViewPreviousStudies.layoutManager = layoutManager
        val recyclerViewPreviousStudiesAdapter =
            NoReportDayCounterAdapter(studentList, secilenZaman, kurumKodu)
        recyclerViewPreviousStudies.adapter = recyclerViewPreviousStudiesAdapter

        testFunMine()


    }

    private fun testFunMine() {
        val layoutManager = LinearLayoutManager(applicationContext)
        val recyclerViewPreviousStudies = binding.noReportDayCounterRecycler
        recyclerViewPreviousStudies.layoutManager = layoutManager
        val recyclerViewPreviousStudiesAdapter =
            NoReportDayCounterAdapter(studentList, secilenZaman, kurumKodu)
        recyclerViewPreviousStudies.adapter = recyclerViewPreviousStudiesAdapter
    }


}