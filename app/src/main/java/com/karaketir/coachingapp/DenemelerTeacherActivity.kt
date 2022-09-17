package com.karaketir.coachingapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.adapter.DenemelerTeacherRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityDenemelerTeacherBinding

class DenemelerTeacherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDenemelerTeacherBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var denemelerList = ArrayList<String>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDenemelerTeacherBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        val layoutManager = LinearLayoutManager(this)

        val recyclerView = binding.recyclerViewDenemeler
        val recyclerViewAdapter = DenemelerTeacherRecyclerAdapter(denemelerList)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = recyclerViewAdapter

        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            val kurumKodu = it.get("kurumKodu")?.toString()?.toInt()

            auth.currentUser?.let { it1 ->
                db.collection("School").document(kurumKodu.toString()).collection("Teacher")
                    .document(
                        it1.uid
                    ).collection("Denemeler").addSnapshotListener { value, _ ->
                        if (value != null) {
                            denemelerList.clear()
                            for (deneme in value) {
                                denemelerList.add(deneme.get("denemeAdi").toString())
                            }
                            recyclerViewAdapter.notifyDataSetChanged()
                        }
                    }
            }

        }

    }
}