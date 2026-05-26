@file:Suppress("DEPRECATION")

package com.karaketir.coachingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.karaketir.coachingapp.adapter.StudiesRecyclerAdapter
import com.karaketir.coachingapp.curriculum.CurriculumProgram
import com.karaketir.coachingapp.curriculum.GradeCurriculumConfig
import com.karaketir.coachingapp.curriculum.GradeCurriculumRepository
import com.karaketir.coachingapp.databinding.ActivityStudiesByClassesBinding
import com.karaketir.coachingapp.models.Study
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class ActivityStudiesByClasses : AppCompatActivity() {
    private lateinit var binding: ActivityStudiesByClassesBinding
    private lateinit var recyclerAdapter: StudiesRecyclerAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var kurumKodu = 0
    private var studyList = ArrayList<Study>()
    private var studentProgram = CurriculumProgram.LEGACY
    private var studentGrade = 12

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityStudiesByClassesBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
        val intent = intent

        kurumKodu = intent.getStringExtra("kurumKodu").toString().toInt()
        val studentID = intent.getStringExtra("studentID").toString()
        val secilenDersAdi = intent.getStringExtra("dersAdi").toString()
        val baslangicTarihi = intent.getSerializableExtra("baslangicTarihi") as Date
        val bitisTarihi = intent.getSerializableExtra("bitisTarihi") as Date
        val secilenZamanAraligi = intent.getStringExtra("secilenZamanAraligi").toString()
        val recyclerView = binding.recyclerStudy

        binding.studyGraphButton.setOnClickListener {
            lifecycleScope.launch {
                loadStudentProgram(studentID)
                if (studentProgram == CurriculumProgram.TYMM) {
                    openClassGraph(
                        secilenDersAdi,
                        secilenZamanAraligi,
                        studentID,
                        program = CurriculumProgram.TYMM.firestoreValue,
                        sinif = studentGrade,
                    )
                } else {
                    showLegacyTurDialog(secilenDersAdi, secilenZamanAraligi, studentID)
                }
            }
        }

        binding.dersAdiStudyTextView.text = secilenDersAdi
        recyclerView.layoutManager = GridLayoutManager(applicationContext, 2)
        recyclerAdapter =
            StudiesRecyclerAdapter(studyList, secilenZamanAraligi, kurumKodu, "Teacher")
        recyclerView.adapter = recyclerAdapter

        db.collection("School").document(kurumKodu.toString()).collection("Student")
            .document(studentID).collection("Studies").whereEqualTo("dersAdi", secilenDersAdi)
            .whereGreaterThan("timestamp", baslangicTarihi).whereLessThan("timestamp", bitisTarihi)
            .orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { value, error ->
                if (error != null) {
                    println(error.localizedMessage)
                }
                studyList.clear()
                if (value != null && !value.isEmpty) {
                    for (document in value) {
                        studyList.add(Study.fromDocument(document, studentID))
                    }
                }
                recyclerAdapter.notifyDataSetChanged()
            }
    }

    private suspend fun loadStudentProgram(studentID: String) {
        try {
            val studentSnap = db.collection("User").document(studentID).get().await()
            studentGrade = studentSnap.getLong("grade")?.toInt()
                ?: studentSnap.getString("grade")?.toIntOrNull()
                ?: 12
            val config = GradeCurriculumRepository.load(db)
            studentProgram = GradeCurriculumRepository.programForGrade(config, studentGrade)
        } catch (_: Exception) {
            studentProgram = GradeCurriculumRepository.programForGrade(
                GradeCurriculumConfig(GradeCurriculumRepository.defaultGradePrograms),
                studentGrade,
            )
        }
    }

    private fun showLegacyTurDialog(dersAdi: String, zamanAraligi: String, studentID: String) {
        AlertDialog.Builder(this)
            .setTitle("Tür Seçiniz")
            .setMessage("Konuların Türünü Seçiniz")
            .setPositiveButton("TYT") { _, _ ->
                openClassGraph(dersAdi, zamanAraligi, studentID, tur = "TYT")
            }
            .setNegativeButton("AYT") { _, _ ->
                openClassGraph(dersAdi, zamanAraligi, studentID, tur = "AYT")
            }
            .show()
    }

    private fun openClassGraph(
        dersAdi: String,
        zamanAraligi: String,
        studentID: String,
        tur: String? = null,
        program: String? = null,
        sinif: Int? = null,
    ) {
        startActivity(
            Intent(this, ClassAllStudiesGraphActivity::class.java).apply {
                putExtra("kurumKodu", kurumKodu.toString())
                putExtra("dersAdi", dersAdi)
                putExtra("secilenZamanAraligi", zamanAraligi)
                putExtra("studentID", studentID)
                tur?.let { putExtra("tür", it) }
                program?.let { putExtra("program", it) }
                sinif?.let { putExtra("sinif", it) }
            }
        )
    }
}
