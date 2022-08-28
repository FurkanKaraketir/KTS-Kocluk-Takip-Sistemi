package com.kodgem.coachingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kodgem.coachingapp.adapter.DutiesRecyclerAdapter
import com.kodgem.coachingapp.databinding.ActivityDutiesBinding
import com.kodgem.coachingapp.models.Duty

class DutiesActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var dutiesRecyclerAdapter: DutiesRecyclerAdapter
    private var dutyList = ArrayList<Duty>()

    private lateinit var binding: ActivityDutiesBinding

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDutiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
        val dutyAddButton = binding.addDutyButton
        val intent = intent
        val studentID = intent.getStringExtra("studentID")
        val dutiesRecyclerView = binding.dutiesRecyclerView

        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            if (it.get("personType").toString() == "Teacher") {
                dutyAddButton.visibility = View.VISIBLE
            } else {
                dutyAddButton.visibility = View.GONE
            }
        }

        dutyAddButton.setOnClickListener {
            val sendIntent = Intent(this, EnterDutyActivity::class.java)
            sendIntent.putExtra("studentID", studentID)
            this.startActivity(sendIntent)
        }


        val layoutManager = GridLayoutManager(applicationContext, 2)

        dutiesRecyclerView.layoutManager = layoutManager

        dutiesRecyclerAdapter = DutiesRecyclerAdapter(dutyList)

        dutiesRecyclerView.adapter = dutiesRecyclerAdapter

        var kurumKodu: Int
        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            kurumKodu = it.get("kurumKodu").toString().toInt()
            db.collection("School").document(kurumKodu.toString()).collection("Student").document(studentID!!)
                .collection("Duties").orderBy("eklenmeTarihi", Query.Direction.DESCENDING)
                .addSnapshotListener { value, e ->
                    if (e != null) {
                        println(e.localizedMessage)
                    }
                    if (value != null) {
                        dutyList.clear()
                        for (document in value) {
                            val konuAdi = document.get("konuAdi").toString()
                            val tur = document.get("tür").toString()
                            val dersAdi = document.get("dersAdi").toString()
                            val toplamCalisma = document.get("toplamCalisma").toString()
                            val cozulenSoru = document.get("çözülenSoru").toString()
                            val bitisZamani = document.get("bitisZamani") as Timestamp

                            val currentDuty = Duty(
                                konuAdi,
                                toplamCalisma,
                                studentID,
                                dersAdi,
                                tur,
                                cozulenSoru,
                                bitisZamani,
                                document.id
                            )
                            dutyList.add(currentDuty)
                        }
                        dutiesRecyclerAdapter.notifyDataSetChanged()


                    }
                }

        }




    }
}