@file:Suppress("DEPRECATION")

package com.karaketir.coachingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.adapter.StudiesRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityStudiesByClassesBinding
import com.karaketir.coachingapp.models.Study
import java.util.*
import kotlin.collections.ArrayList

class ActivityStudiesByClasses : AppCompatActivity() {

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


    private lateinit var binding: ActivityStudiesByClassesBinding
    private lateinit var recyclerAdapter: StudiesRecyclerAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var studyList = ArrayList<Study>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityStudiesByClassesBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
        val intent = intent

        val studentID = intent.getStringExtra("studentID").toString()
        val secilenDersAdi = intent.getStringExtra("dersAdi").toString()
        val baslangicTarihi = intent.getSerializableExtra("baslangicTarihi") as Date
        val bitisTarihi = intent.getSerializableExtra("bitisTarihi") as Date
        val secilenZamanAraligi = intent.getStringExtra("secilenZamanAraligi").toString()
        val recyclerView = binding.recyclerStudy

        val button = binding.studyGraphButton

        button.setOnClickListener {

            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Tür Seçiniz")
            alertDialog.setMessage("Konuların Türünü Seçiniz")
            alertDialog.setPositiveButton("TYT") { _, _ ->

                val newIntent = Intent(this, ClassAllStudiesGraphActivity::class.java)
                newIntent.putExtra("dersAdi", secilenDersAdi)
                newIntent.putExtra("secilenZamanAraligi", secilenZamanAraligi)
                newIntent.putExtra("studentID", studentID)
                newIntent.putExtra("tür", "TYT")
                this.startActivity(newIntent)

            }
            alertDialog.setNegativeButton("AYT") { _, _ ->
                val newIntent = Intent(this, ClassAllStudiesGraphActivity::class.java)
                newIntent.putExtra("dersAdi", secilenDersAdi)
                newIntent.putExtra("secilenZamanAraligi", secilenZamanAraligi)
                newIntent.putExtra("studentID", studentID)
                newIntent.putExtra("tür", "AYT")
                this.startActivity(newIntent)
            }
            alertDialog.show()


        }

        val title = binding.dersAdiStudyTextView
        title.text = secilenDersAdi
        val layoutManager = GridLayoutManager(applicationContext, 2)

        recyclerView.layoutManager = layoutManager

        recyclerAdapter = StudiesRecyclerAdapter(studyList, secilenZamanAraligi)

        recyclerView.adapter = recyclerAdapter
        var kurumKodu: Int
        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            kurumKodu = it.get("kurumKodu").toString().toInt()
            db.collection("School").document(kurumKodu.toString()).collection("Student")
                .document(studentID).collection("Studies").whereEqualTo("dersAdi", secilenDersAdi)
                .whereGreaterThan("timestamp", baslangicTarihi)
                .whereLessThan("timestamp", bitisTarihi)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { value, error ->

                    if (error != null) {
                        println(error.localizedMessage)
                    }

                    if (value != null) {
                        studyList.clear()
                        if (!value.isEmpty) {
                            for (document in value) {
                                val studyName = document.get("konuAdi").toString()
                                val sure = document.get("toplamCalisma").toString()
                                val studyDersAdi = document.get("dersAdi").toString()
                                val studyTur = document.get("tür").toString()
                                val soruSayisi = document.get("çözülenSoru").toString()
                                val timestamp = document.get("timestamp") as Timestamp

                                val currentStudy = Study(
                                    studyName,
                                    sure,
                                    studentID,
                                    studyDersAdi,
                                    studyTur,
                                    soruSayisi,
                                    timestamp,
                                    document.id
                                )
                                studyList.add(currentStudy)
                            }

                            recyclerAdapter.notifyDataSetChanged()

                        } else {
                            studyList.clear()
                            recyclerAdapter.notifyDataSetChanged()

                        }


                    } else {
                        studyList.clear()
                        recyclerAdapter.notifyDataSetChanged()
                    }

                }

        }

    }
}