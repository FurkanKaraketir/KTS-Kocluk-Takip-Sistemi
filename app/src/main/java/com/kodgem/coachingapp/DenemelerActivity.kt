package com.kodgem.coachingapp

import android.R
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kodgem.coachingapp.adapter.DenemelerRecyclerAdapter
import com.kodgem.coachingapp.databinding.ActivityDenemelerBinding
import com.kodgem.coachingapp.models.Deneme
import com.kodgem.coachingapp.models.Study
import java.util.*

class DenemelerActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var binding: ActivityDenemelerBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: DenemelerRecyclerAdapter
    private lateinit var spinner: Spinner
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var denemeList = ArrayList<Deneme>()
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date
    private lateinit var secilenZamanAraligi: String
    private lateinit var studentID: String
    private val zamanAraliklari = arrayOf("Bu Hafta", "Geçen Hafta", "Bu Ay", "Tüm Zamanlar")
    private lateinit var layoutManager: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDenemelerBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore
        recyclerView = binding.denemelerRecyclerView
        spinner = binding.denemeSpinner
        layoutManager = GridLayoutManager(applicationContext, 2)

        val denemeAdapter = ArrayAdapter(
            this@DenemelerActivity, R.layout.simple_spinner_item, zamanAraliklari
        )

        studentID = intent.getStringExtra("studentID").toString()

        denemeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        spinner.adapter = denemeAdapter
        spinner.onItemSelectedListener = this

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        secilenZamanAraligi = zamanAraliklari[position]

        recyclerView = binding.denemelerRecyclerView
        recyclerView.layoutManager = layoutManager
        recyclerAdapter = DenemelerRecyclerAdapter(denemeList, secilenZamanAraligi)
        recyclerView.adapter = recyclerAdapter
        recyclerAdapter.notifyDataSetChanged()

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

        var kurumKodu: Int
        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            kurumKodu = it.get("kurumKodu").toString().toInt()

            db.collection("School").document(kurumKodu.toString()).collection("Student")
                .document(studentID).collection("Denemeler")
                .whereGreaterThan("denemeTarihi", baslangicTarihi)
                .whereLessThan("denemeTarihi", bitisTarihi)
                .orderBy("denemeTarihi", Query.Direction.DESCENDING)
                .addSnapshotListener { value, error ->

                    if (error != null) {
                        println(error.localizedMessage)
                    }
                    if (value != null) {
                        denemeList.clear()
                        if (!value.isEmpty) {
                            for (document in value) {
                                val denemeID = document.id
                                val denemeAdi = document.get("denemeAdi").toString()
                                val denemeToplamNet = document.get("toplamNet").toString().toFloat()
                                val denemeTarihi = document.get("denemeTarihi") as Timestamp

                                val currentDeneme = Deneme(
                                    denemeID, denemeAdi, denemeToplamNet, denemeTarihi, studentID
                                )
                                denemeList.add(currentDeneme)
                            }

                            recyclerAdapter.notifyDataSetChanged()

                        } else {
                            denemeList.clear()
                            recyclerAdapter.notifyDataSetChanged()

                        }


                    } else {
                        denemeList.clear()
                        recyclerAdapter.notifyDataSetChanged()

                    }

                }


        }

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }
}