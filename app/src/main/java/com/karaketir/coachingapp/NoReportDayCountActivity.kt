package com.karaketir.coachingapp

//noinspection SuspiciousImport
import android.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.adapter.NoReportDayCounterAdapter
import com.karaketir.coachingapp.databinding.ActivityNoReportDayCountBinding
import java.util.Calendar

class NoReportDayCountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoReportDayCountBinding
    private var secilenZaman = "Bugün"
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var kurumKodu = 763455
    private val zamanAraliklari =
        arrayOf("Dün", "Bu Hafta", "Geçen Hafta", "Bu Ay", "Geçen Ay", "Tüm Zamanlar")
    private var studentList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoReportDayCountBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = Firebase.auth
        db = Firebase.firestore

        kurumKodu = intent.getStringExtra("kurumKodu").toString().toInt()
        studentList = intent.getStringArrayListExtra("studentList") as ArrayList<String>

        val layoutManager = LinearLayoutManager(applicationContext)
        val recyclerViewPreviousStudies = binding.noReportDayCounterRecycler
        recyclerViewPreviousStudies.layoutManager = layoutManager
        val recyclerViewPreviousStudiesAdapter =
            NoReportDayCounterAdapter(studentList, secilenZaman, kurumKodu)
        recyclerViewPreviousStudies.adapter = recyclerViewPreviousStudiesAdapter

        val teacherSpinner = binding.studyZamanAraligiSpinner

        val tarihAdapter = ArrayAdapter(
            this@NoReportDayCountActivity, R.layout.simple_spinner_item, zamanAraliklari
        )

        val cal = Calendar.getInstance()
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)

        tarihAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        teacherSpinner.adapter = tarihAdapter
        teacherSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemSelected(
                p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
            ) {
                secilenZaman = zamanAraliklari[p2]
                recyclerViewPreviousStudiesAdapter.notifyDataSetChanged()
                testFunMine()

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }


    }

    fun testFunMine() {
        val layoutManager = LinearLayoutManager(applicationContext)
        val recyclerViewPreviousStudies = binding.noReportDayCounterRecycler
        recyclerViewPreviousStudies.layoutManager = layoutManager
        val recyclerViewPreviousStudiesAdapter =
            NoReportDayCounterAdapter(studentList, secilenZaman, kurumKodu)
        recyclerViewPreviousStudies.adapter = recyclerViewPreviousStudiesAdapter
    }


}