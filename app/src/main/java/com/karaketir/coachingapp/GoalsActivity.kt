package com.karaketir.coachingapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.adapter.GoalsRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityGoalsBinding
import com.karaketir.coachingapp.models.Goal

class GoalsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGoalsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerAdapter: GoalsRecyclerAdapter

    private var goalList = ArrayList<Goal>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityGoalsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val recyclerView = binding.recyclerGoal
        val addGoalButton = binding.addGoalButton

        auth = Firebase.auth
        db = Firebase.firestore

        val intent = intent
        val studentID = intent.getStringExtra("studentID").toString()
        val layoutManager = LinearLayoutManager(this)


        recyclerView.layoutManager = layoutManager
        recyclerAdapter = GoalsRecyclerAdapter(goalList)
        recyclerView.adapter = recyclerAdapter


        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            val kurumKodu = it.get("kurumKodu").toString()

            if (it.get("personType") == "Student") {
                addGoalButton.visibility = View.GONE
            } else {
                addGoalButton.visibility = View.VISIBLE
            }

            db.collection("School").document(kurumKodu).collection("Student").document(studentID)
                .collection("HaftalikHedefler").addSnapshotListener { value, _ ->
                    if (value != null) {
                        goalList.clear()
                        for (document in value) {

                            val dersAdi = document.get("dersAdi").toString()
                            val toplamCalismaHedef =
                                document.get("toplamCalisma").toString().toInt()
                            val cozulenSoruHedef = document.get("çözülenSoru").toString().toInt()

                            val currentGoal = Goal(
                                dersAdi,
                                toplamCalismaHedef,
                                cozulenSoruHedef,
                                document.id,
                                studentID
                            )
                            goalList.add(currentGoal)

                        }
                        recyclerAdapter.notifyDataSetChanged()
                    }

                }

        }

        addGoalButton.setOnClickListener {
            val gonder = Intent(this, GoalEnterActivity::class.java)
            gonder.putExtra("studentID", studentID)
            this.startActivity(gonder)
        }

    }
}