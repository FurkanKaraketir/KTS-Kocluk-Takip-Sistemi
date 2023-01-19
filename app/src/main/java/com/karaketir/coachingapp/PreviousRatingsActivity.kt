package com.karaketir.coachingapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.adapter.RatingsRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityPreviousRatingsBinding
import com.karaketir.coachingapp.models.Rating

class PreviousRatingsActivity : AppCompatActivity() {

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


    private lateinit var binding: ActivityPreviousRatingsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerAdapter: RatingsRecyclerAdapter
    private var ratingsList = ArrayList<Rating>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityPreviousRatingsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        val recyclerView = binding.previousRatingsRecyclerView

        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        recyclerAdapter = RatingsRecyclerAdapter(ratingsList)
        recyclerView.adapter = recyclerAdapter

        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener { snapshot ->
            val kurumKodu = snapshot.get("kurumKodu")?.toString()?.toInt()

            db.collection("School").document(kurumKodu.toString()).collection("Student")
                .document(auth.uid.toString()).collection("Degerlendirme")
                .orderBy("degerlendirmeDate", Query.Direction.DESCENDING)
                .addSnapshotListener { degerlendirmeler, _ ->
                    if (degerlendirmeler != null) {
                        ratingsList.clear()
                        for (degerlendirme in degerlendirmeler) {
                            val yildizSayisi = degerlendirme.get("yildizSayisi").toString().toInt()
                            val date = degerlendirme.get("degerlendirmeDate") as Timestamp
                            val currentRating = Rating(yildizSayisi, date)
                            ratingsList.add(currentRating)
                        }
                        recyclerAdapter.notifyDataSetChanged()
                    }
                }

        }


    }
}