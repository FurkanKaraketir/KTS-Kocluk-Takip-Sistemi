package com.karaketir.coachingapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.TransitionManager
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.adapter.StudentPlacementAdapter
import com.karaketir.coachingapp.databinding.ActivityTopStudentsBinding
import com.karaketir.coachingapp.models.StudentPlacment

class TopStudentsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTopStudentsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val mainHash = hashMapOf<String, Int>()
    private val idList = ArrayList<String>()
    private val studentList = ArrayList<StudentPlacment>()
    private var kurumKodu = 0
    private var arrayCount = 0
    private var check = false
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var studentsRecyclerAdapter: StudentPlacementAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopStudentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore


        val layoutManager = LinearLayoutManager(applicationContext)

        binding.studentPlacementRecyclerView.layoutManager = layoutManager

        studentsRecyclerAdapter = StudentPlacementAdapter(studentList)

        binding.studentPlacementRecyclerView.adapter = studentsRecyclerAdapter


        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            kurumKodu = it.get("kurumKodu").toString().toInt()
            TransitionManager.beginDelayedTransition(binding.transitionsContainer)
            binding.progressCircular.visibility = View.VISIBLE
            siralamaHazirla()

        }


        handler.post(object : Runnable {
            override fun run() {
                // Keep the postDelayed before the updateTime(), so when the event ends, the handler will stop too.
                handler.postDelayed(this, 1000)
                updateTime()
            }
        })

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateTime() {

        if (idList.isNotEmpty()) {
            if (idList.size >= arrayCount + 1) {
                valueAdding(arrayCount)
                arrayCount += 1
            } else {

                if (!check) {
                    Toast.makeText(this, "İşlem Başarılı", Toast.LENGTH_LONG).show()
                    studentsRecyclerAdapter.notifyDataSetChanged()
                    TransitionManager.beginDelayedTransition(binding.transitionsContainer)
                    binding.progressCircular.visibility = View.GONE


                    check = true
                }
            }
        }


    }

    private fun siralamaHazirla() {

        println(kurumKodu)
        db.collection("School").document(kurumKodu.toString()).collection("Student").get()
            .addOnSuccessListener {


                for (i in it.documents) {

                    if (i.get("teacher").toString() != "" && i.get("grade").toString() == "12") {
                        idList.add(i.id)

                    }


                }
                println(idList.size)
            }


    }


    @SuppressLint("NotifyDataSetChanged")
    private fun valueAdding(position: Int) {


        var toplamCalisma = 0
        db.collection("School").document(kurumKodu.toString()).collection("Student")
            .document(idList[position]).collection("Studies").get().addOnSuccessListener { my ->

                for (i in my.documents) {
                    toplamCalisma += i.get("toplamCalisma").toString().toInt()
                }
                mainHash[idList[position]] = toplamCalisma

                val sortedMap = mainHash.toList().sortedBy { (_, value) -> value }.toMap()

                studentList.clear()
                for (a in sortedMap.keys) {

                    val newStudent = StudentPlacment(a, sortedMap[a]!!)
                    studentList.add(newStudent)

                }
                studentList.reverse()
                studentsRecyclerAdapter.notifyDataSetChanged()

            }


    }


}