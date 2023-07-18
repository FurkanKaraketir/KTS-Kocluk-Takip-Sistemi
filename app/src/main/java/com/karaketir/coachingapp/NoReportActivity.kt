@file:Suppress("DEPRECATION")

package com.karaketir.coachingapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.adapter.StudentsRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityNoReportBinding
import com.karaketir.coachingapp.models.Student
import java.util.Calendar
import java.util.UUID


class NoReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoReportBinding
    private var secilenZaman = "Bugün"
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var kurumKodu = 763455
    private var studentList = ArrayList<String>()

    private lateinit var recyclerViewMyStudentsRecyclerAdapter: StudentsRecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityNoReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        val recyclerViewMyStudents = binding.noReportRecycler
        val noReportDayCounterButton = binding.noReportDayCounterButton


        val raporGondermeyenList = intent.getSerializableExtra("list") as ArrayList<Student>
        secilenZaman = intent.getStringExtra("secilenZaman").toString()
        if (secilenZaman == "Bugün") {
            secilenZaman = "Dün"
        }
        kurumKodu = intent.getStringExtra("kurumKodu")!!.toInt()


        db.collection("School").document(kurumKodu.toString()).collection("Student")
            .whereEqualTo("teacher", auth.uid.toString()).addSnapshotListener { value, _ ->
                if (value != null) {
                    for (i in value) {
                        studentList.add(i.get("id").toString())
                    }
                }
            }


        val layoutManager = LinearLayoutManager(applicationContext)

        recyclerViewMyStudents.layoutManager = layoutManager

        recyclerViewMyStudentsRecyclerAdapter =
            StudentsRecyclerAdapter(raporGondermeyenList, secilenZaman)

        recyclerViewMyStudents.adapter = recyclerViewMyStudentsRecyclerAdapter

        noReportDayCounterButton.setOnClickListener {
            val newIntent = Intent(this, NoReportDayCountActivity::class.java)
            newIntent.putExtra("kurumKodu", kurumKodu.toString())
            newIntent.putExtra("studentList", studentList)
            this.startActivity(newIntent)
        }

        for (i in raporGondermeyenList) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            cal.clear(Calendar.MINUTE)
            cal.clear(Calendar.SECOND)
            cal.clear(Calendar.MILLISECOND)

            val day = cal[Calendar.DAY_OF_MONTH]
            val month = cal[Calendar.MONTH]
            val year = cal[Calendar.YEAR]
            val newDocID = "${day}-${month}-${year}"
            val newData = hashMapOf("id" to newDocID, "timestamp" to cal.time)

            db.collection("School").document(kurumKodu.toString()).collection("Student")
                .document(i.id).collection("NoDayReport").document(newDocID)
                .set(newData as Map<String, Any>)


        }


    }


}
