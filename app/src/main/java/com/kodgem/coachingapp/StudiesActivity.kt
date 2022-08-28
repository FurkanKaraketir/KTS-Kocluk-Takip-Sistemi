package com.kodgem.coachingapp

//noinspection SuspiciousImport
import android.R
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import java.util.*


class StudiesActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerViewStudiesAdapter: StudiesRecyclerAdapter

    private lateinit var recyclerViewStudies: RecyclerView
    private var studyList = ArrayList<Study>()
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date
    private lateinit var binding: ActivityStudiesBinding
    private lateinit var secilenZamanAraligi: String
    private lateinit var studentID: String
    private val zamanAraliklari = arrayOf("Bu Hafta", "Geçen Hafta", "Bu Ay", "Tüm Zamanlar")
    private lateinit var layoutManager: GridLayoutManager

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudiesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore

        layoutManager = GridLayoutManager(applicationContext, 2)
        val intent = intent
        val studyZamanSpinner = binding.studyZamanAraligiSpinner
        val gorevlerButton = binding.gorevTeacherButton


        val studyAdapter = ArrayAdapter(
            this@StudiesActivity, R.layout.simple_spinner_item, zamanAraliklari
        )

        studentID = intent.getStringExtra("studentID").toString()

        studyAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        studyZamanSpinner.adapter = studyAdapter
        studyZamanSpinner.onItemSelectedListener = this

        gorevlerButton.setOnClickListener {
            val intent2 = Intent(this, DutiesActivity::class.java)
            intent2.putExtra("studentID", studentID)
            this.startActivity(intent2)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        secilenZamanAraligi = zamanAraliklari[position]

        recyclerViewStudies = binding.recyclerViewStudies
        recyclerViewStudies.layoutManager = layoutManager
        recyclerViewStudiesAdapter = StudiesRecyclerAdapter(studyList, secilenZamanAraligi)
        recyclerViewStudies.adapter = recyclerViewStudiesAdapter
        recyclerViewStudiesAdapter.notifyDataSetChanged()

        var cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)

        when (position) {
            0 -> {
                cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek
                baslangicTarihi = cal.time


                cal.add(Calendar.WEEK_OF_YEAR, 1)
                bitisTarihi = cal.time

            }
            1 -> {
                cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek
                bitisTarihi = cal.time


                cal.add(Calendar.DAY_OF_YEAR, -7)
                baslangicTarihi = cal.time


            }
            2 -> {

                cal = Calendar.getInstance()
                cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                cal.clear(Calendar.MINUTE)
                cal.clear(Calendar.SECOND)
                cal.clear(Calendar.MILLISECOND)

                cal.set(Calendar.DAY_OF_MONTH, 1)
                baslangicTarihi = cal.time


                cal.add(Calendar.MONTH, 1)
                bitisTarihi = cal.time


            }
            3 -> {
                cal.set(1970, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                baslangicTarihi = cal.time


                cal.set(2077, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                bitisTarihi = cal.time

            }
        }
        println(baslangicTarihi)
        println(bitisTarihi)

        db.collection("School").document("SchoolIDDDD").collection("Student").document(studentID)
            .collection("Studies").whereGreaterThan("timestamp", baslangicTarihi)
            .whereLessThan("timestamp", bitisTarihi)
            .orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { value, _ ->
                if (value != null) {
                    studyList.clear()
                    if (!value.isEmpty) {
                        for (document in value) {
                            val studyName = document.get("konuAdi").toString()
                            val sure = document.get("toplamCalisma").toString()
                            val studyDersAdi = document.get("dersAdi").toString()
                            val studyTur = document.get("tür").toString()
                            val soruSayisi = document.get("çözülenSoru").toString()

                            val currentStudy = Study(
                                studyName, sure, studentID, studyDersAdi, studyTur, soruSayisi
                            )
                            studyList.add(currentStudy)
                            println(currentStudy.studyName)
                        }

                        recyclerViewStudiesAdapter.notifyDataSetChanged()

                    } else {
                        studyList.clear()
                        recyclerViewStudiesAdapter.notifyDataSetChanged()

                    }


                } else {
                    studyList.clear()
                    recyclerViewStudiesAdapter.notifyDataSetChanged()

                }

            }


    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }
}