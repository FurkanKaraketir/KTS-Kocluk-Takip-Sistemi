@file:Suppress("DEPRECATION")

package com.karaketir.coachingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.karaketir.coachingapp.adapter.StudentsRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityNoReportBinding
import com.karaketir.coachingapp.models.Student


class NoReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoReportBinding
    private var secilenZaman = "Bugün"

    private lateinit var recyclerViewMyStudentsRecyclerAdapter: StudentsRecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityNoReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerViewMyStudents = binding.noReportRecycler


        val raporGondermeyenList = intent.getSerializableExtra("list") as ArrayList<Student>
        secilenZaman = intent.getStringExtra("secilenZaman").toString()

        val layoutManager = LinearLayoutManager(applicationContext)

        recyclerViewMyStudents.layoutManager = layoutManager

        recyclerViewMyStudentsRecyclerAdapter =
            StudentsRecyclerAdapter(raporGondermeyenList, secilenZaman)

        recyclerViewMyStudents.adapter = recyclerViewMyStudentsRecyclerAdapter


    }


}
