package com.karaketir.coachingapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.karaketir.coachingapp.adapter.StudentPlacementAdapter
import com.karaketir.coachingapp.databinding.ActivityTopStudentsBinding
import com.karaketir.coachingapp.models.StudentPlacment
import com.karaketir.coachingapp.services.StudyQueryHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TopStudentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTopStudentsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val studentList = ArrayList<StudentPlacment>()
    private var kurumKodu = 0

    private lateinit var studentsRecyclerAdapter: StudentPlacementAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopStudentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        kurumKodu = intent.getStringExtra("kurumKodu").toString().toInt()

        binding.studentPlacementRecyclerView.layoutManager =
            LinearLayoutManager(applicationContext)

        studentsRecyclerAdapter = StudentPlacementAdapter(studentList)
        binding.studentPlacementRecyclerView.adapter = studentsRecyclerAdapter

        TransitionManager.beginDelayedTransition(binding.transitionsContainer)
        binding.progressCircular.visibility = View.VISIBLE
        loadRanking()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadRanking() {
        lifecycleScope.launch {
            try {
                val studentsSnap = db.collection("School")
                    .document(kurumKodu.toString())
                    .collection("Student")
                    .whereEqualTo("grade", 12)
                    .get()
                    .await()

                val studentIds = studentsSnap.documents
                    .filter { doc ->
                        val teacher = doc.getString("teacher").orEmpty()
                        teacher.isNotEmpty()
                    }
                    .map { it.id }

                if (studentIds.isEmpty()) {
                    showRankingComplete(emptyList())
                    return@launch
                }

                val totalsByStudent = StudyQueryHelper.fetchTotalMinutesPerStudent(
                    db,
                    kurumKodu.toString(),
                    studentIds,
                )

                val ranked = totalsByStudent.entries
                    .sortedBy { it.value }
                    .map { (id, minutes) -> StudentPlacment(id, minutes) }
                    .reversed()

                showRankingComplete(ranked)
            } catch (e: Exception) {
                println(e.localizedMessage)
                TransitionManager.beginDelayedTransition(binding.transitionsContainer)
                binding.progressCircular.visibility = View.GONE
                Toast.makeText(
                    this@TopStudentsActivity,
                    "Sıralama yüklenemedi",
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showRankingComplete(ranked: List<StudentPlacment>) {
        studentList.clear()
        studentList.addAll(ranked)
        studentsRecyclerAdapter.notifyDataSetChanged()
        TransitionManager.beginDelayedTransition(binding.transitionsContainer)
        binding.progressCircular.visibility = View.GONE
        if (ranked.isNotEmpty()) {
            Toast.makeText(this, "İşlem Başarılı", Toast.LENGTH_LONG).show()
        }
    }
}
