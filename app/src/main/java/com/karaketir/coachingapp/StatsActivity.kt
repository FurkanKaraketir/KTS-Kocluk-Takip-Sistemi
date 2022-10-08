package com.karaketir.coachingapp

//noinspection SuspiciousImport
import android.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
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
import com.karaketir.coachingapp.adapter.StatisticsRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityStatsBinding
import com.karaketir.coachingapp.models.Statistic
import java.util.*

class StatsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityStatsBinding
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date
    private lateinit var layoutManager: GridLayoutManager
    private var secilenZamanAraligi = ""
    private val handler = Handler(Looper.getMainLooper())
    private var secilenGrade = ""
    private lateinit var recyclerViewStats: RecyclerView
    private lateinit var recyclerViewStatsAdapter: StatisticsRecyclerAdapter
    private var dersSoruHash = hashMapOf<String, Float>()
    private var dersSureHash = hashMapOf<String, Float>()
    private var statsList = ArrayList<Statistic>()
    private var ogrenciSayisi = 0
    private var gradeList = arrayOf("Bütün Sınıflar", "12", "11", "10", "9")

    private val zamanAraliklari =
        arrayOf("Bugün", "Dün", "Bu Hafta", "Geçen Hafta", "Bu Ay", "Geçen Ay", "Tüm Zamanlar")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        layoutManager = GridLayoutManager(applicationContext, 2)
        val statsZamanSpinner = binding.statsZamanAraligiSpinner

        val statsGradeSpinner = binding.statsGradeSpinner

        val statsAdapter = ArrayAdapter(
            this@StatsActivity, R.layout.simple_spinner_item, zamanAraliklari
        )

        val gradeAdapter = ArrayAdapter(
            this@StatsActivity, R.layout.simple_spinner_item, gradeList
        )

        statsAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        statsZamanSpinner.adapter = statsAdapter






        statsZamanSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {

                gradeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                statsGradeSpinner.adapter = gradeAdapter
                statsGradeSpinner.onItemSelectedListener = object : OnItemSelectedListener {
                    @SuppressLint("SetTextI18n")
                    override fun onItemSelected(
                        p0: AdapterView<*>?, p1: View?, position2: Int, p3: Long
                    ) {
                        secilenGrade = gradeList[position2]
                        secilenZamanAraligi = zamanAraliklari[position]
                        statsList.clear()
                        dersSureHash.clear()
                        dersSoruHash.clear()

                        recyclerViewStats = binding.statsRecyclerView
                        recyclerViewStats.layoutManager = layoutManager

                        recyclerViewStatsAdapter = StatisticsRecyclerAdapter(statsList)

                        recyclerViewStats.adapter = recyclerViewStatsAdapter
                        recyclerViewStatsAdapter.notifyDataSetChanged()


                        var cal = Calendar.getInstance()
                        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                        cal.clear(Calendar.MINUTE)
                        cal.clear(Calendar.SECOND)
                        cal.clear(Calendar.MILLISECOND)

                        when (position) {
                            0 -> {
                                baslangicTarihi = cal.time


                                cal.add(Calendar.DAY_OF_YEAR, 1)
                                bitisTarihi = cal.time
                            }
                            1 -> {
                                bitisTarihi = cal.time

                                cal.add(Calendar.DAY_OF_YEAR, -1)
                                baslangicTarihi = cal.time
                            }
                            2 -> {
                                cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek
                                baslangicTarihi = cal.time


                                cal.add(Calendar.WEEK_OF_YEAR, 1)
                                bitisTarihi = cal.time

                            }
                            3 -> {
                                cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek
                                bitisTarihi = cal.time


                                cal.add(Calendar.DAY_OF_YEAR, -7)
                                baslangicTarihi = cal.time


                            }
                            4 -> {

                                cal = Calendar.getInstance()
                                cal[Calendar.HOUR_OF_DAY] =
                                    0 // ! clear would not reset the hour of day !

                                cal.clear(Calendar.MINUTE)
                                cal.clear(Calendar.SECOND)
                                cal.clear(Calendar.MILLISECOND)

                                cal.set(Calendar.DAY_OF_MONTH, 1)
                                baslangicTarihi = cal.time


                                cal.add(Calendar.MONTH, 1)
                                bitisTarihi = cal.time


                            }
                            5 -> {
                                cal = Calendar.getInstance()
                                cal[Calendar.HOUR_OF_DAY] =
                                    0 // ! clear would not reset the hour of day !

                                cal.clear(Calendar.MINUTE)
                                cal.clear(Calendar.SECOND)
                                cal.clear(Calendar.MILLISECOND)

                                cal.set(Calendar.DAY_OF_MONTH, 1)
                                bitisTarihi = cal.time


                                cal.add(Calendar.MONTH, -1)
                                baslangicTarihi = cal.time

                            }

                            6 -> {
                                cal.set(1970, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                                baslangicTarihi = cal.time


                                cal.set(2077, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                                bitisTarihi = cal.time

                            }
                        }


                        var kurumKodu: Int
                        val dersListesi = kotlin.collections.ArrayList<String>()

                        db.collection("User").document(auth.uid.toString()).get()
                            .addOnSuccessListener {
                                kurumKodu = it.get("kurumKodu").toString().toInt()

                                db.collection("Lessons")
                                    .orderBy("dersAdi", Query.Direction.ASCENDING)
                                    .addSnapshotListener { dersler, _ ->
                                        if (dersler != null) {
                                            for (i in dersler) {
                                                dersListesi.add(i.id)
                                            }
                                        }
                                        for (dersIndex in dersListesi) {

                                            if (secilenGrade == "Bütün Sınıflar") {
                                                db.collection("School")
                                                    .document(kurumKodu.toString())
                                                    .collection("Student")
                                                    .whereEqualTo("teacher", auth.uid.toString())
                                                    .addSnapshotListener { ogrencliler, _ ->
                                                        statsList.clear()

                                                        if (ogrencliler != null) {
                                                            ogrenciSayisi = ogrencliler.size()

                                                            for (ogrenci in ogrencliler) {
                                                                var toplamCalisma = 0
                                                                var cozulenSoru = 0
                                                                db.collection("School")
                                                                    .document(kurumKodu.toString())
                                                                    .collection("Student")
                                                                    .document(ogrenci.id)
                                                                    .collection("Studies")
                                                                    .whereEqualTo(
                                                                        "dersAdi", dersIndex
                                                                    ).whereGreaterThan(
                                                                        "timestamp", baslangicTarihi
                                                                    ).whereLessThan(
                                                                        "timestamp", bitisTarihi
                                                                    )
                                                                    .addSnapshotListener { studies, _ ->


                                                                        if (studies != null && ogrencliler.size() != 0) {

                                                                            for (study in studies) {
                                                                                toplamCalisma += study.get(
                                                                                    "toplamCalisma"
                                                                                ).toString().toInt()
                                                                                cozulenSoru += study.get(
                                                                                    "çözülenSoru"
                                                                                ).toString().toInt()


                                                                            }
                                                                            if (dersIndex in dersSoruHash.keys) {

                                                                                val currentValue =
                                                                                    dersSoruHash[dersIndex]

                                                                                if (currentValue != null) {
                                                                                    dersSoruHash[dersIndex] =
                                                                                        currentValue + cozulenSoru
                                                                                }


                                                                            } else {
                                                                                dersSoruHash[dersIndex] =
                                                                                    cozulenSoru.toFloat()
                                                                            }

                                                                            if (dersIndex in dersSureHash.keys) {

                                                                                val currentValue =
                                                                                    dersSureHash[dersIndex]

                                                                                if (currentValue != null) {
                                                                                    dersSureHash[dersIndex] =
                                                                                        currentValue + toplamCalisma
                                                                                }


                                                                            } else {
                                                                                dersSureHash[dersIndex] =
                                                                                    toplamCalisma.toFloat()
                                                                            }




                                                                            statsList.clear()
                                                                            for (i in dersSureHash.keys) {
                                                                                val currentStatistic =
                                                                                    Statistic(
                                                                                        i,
                                                                                        (dersSureHash[i]?.div(
                                                                                            ogrenciSayisi
                                                                                        )).toString(),
                                                                                        (dersSoruHash[i]?.div(
                                                                                            ogrenciSayisi
                                                                                        )).toString()
                                                                                    )

                                                                                statsList.add(
                                                                                    currentStatistic
                                                                                )
                                                                                recyclerViewStatsAdapter.notifyDataSetChanged()
                                                                            }
                                                                        }


                                                                    }


                                                            }

                                                        }


                                                    }
                                            } else {
                                                db.collection("School")
                                                    .document(kurumKodu.toString())
                                                    .collection("Student")
                                                    .whereEqualTo("teacher", auth.uid.toString())
                                                    .whereEqualTo("grade", secilenGrade.toInt())
                                                    .addSnapshotListener { ogrencliler, error ->
                                                        statsList.clear()
                                                        if (error != null) {
                                                            println(error.localizedMessage)
                                                        }

                                                        if (ogrencliler != null) {
                                                            ogrenciSayisi = ogrencliler.size()

                                                            for (ogrenci in ogrencliler) {
                                                                var toplamCalisma = 0
                                                                var cozulenSoru = 0
                                                                db.collection("School")
                                                                    .document(kurumKodu.toString())
                                                                    .collection("Student")
                                                                    .document(ogrenci.id)
                                                                    .collection("Studies")
                                                                    .whereEqualTo(
                                                                        "dersAdi", dersIndex
                                                                    ).whereGreaterThan(
                                                                        "timestamp", baslangicTarihi
                                                                    ).whereLessThan(
                                                                        "timestamp", bitisTarihi
                                                                    )
                                                                    .addSnapshotListener { studies, _ ->


                                                                        if (studies != null && ogrencliler.size() != 0) {

                                                                            for (study in studies) {
                                                                                toplamCalisma += study.get(
                                                                                    "toplamCalisma"
                                                                                ).toString().toInt()
                                                                                cozulenSoru += study.get(
                                                                                    "çözülenSoru"
                                                                                ).toString().toInt()


                                                                            }
                                                                            if (dersIndex in dersSoruHash.keys) {

                                                                                val currentValue =
                                                                                    dersSoruHash[dersIndex]

                                                                                if (currentValue != null) {
                                                                                    dersSoruHash[dersIndex] =
                                                                                        currentValue + cozulenSoru
                                                                                }


                                                                            } else {
                                                                                dersSoruHash[dersIndex] =
                                                                                    cozulenSoru.toFloat()
                                                                            }

                                                                            if (dersIndex in dersSureHash.keys) {

                                                                                val currentValue =
                                                                                    dersSureHash[dersIndex]

                                                                                if (currentValue != null) {
                                                                                    dersSureHash[dersIndex] =
                                                                                        currentValue + toplamCalisma
                                                                                }


                                                                            } else {
                                                                                dersSureHash[dersIndex] =
                                                                                    toplamCalisma.toFloat()
                                                                            }



                                                                            statsList.clear()
                                                                            for (i in dersSureHash.keys) {
                                                                                val currentStatistic =
                                                                                    Statistic(
                                                                                        i,
                                                                                        (dersSureHash[i]?.div(
                                                                                            ogrenciSayisi
                                                                                        )).toString(),
                                                                                        (dersSoruHash[i]?.div(
                                                                                            ogrenciSayisi
                                                                                        )).toString()
                                                                                    )
                                                                                statsList.add(
                                                                                    currentStatistic
                                                                                )
                                                                                recyclerViewStatsAdapter.notifyDataSetChanged()
                                                                            }


                                                                        }


                                                                    }


                                                            }

                                                        }


                                                    }
                                            }
                                        }

                                    }


                            }


                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }

                }


            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }

        handler.post(object : Runnable {
            override fun run() {
                // Keep the postDelayed before the updateTime(), so when the event ends, the handler will stop too.
                handler.postDelayed(this, 2000)
                showSum()
            }
        })


    }

    @SuppressLint("SetTextI18n")
    private fun showSum() {
        var toplamSure = 0f
        var toplamSoru = 0f
        if (statsList.isNotEmpty()){
            for (i in statsList){
                toplamSure += i.toplamCalisma.toFloat()
                toplamSoru += i.cozulenSoru.toFloat()
            }
        }


        val toplamSureSaat = toplamSure / 60
        binding.toplamSure.text = toplamSure.format(2) + "dk " + "(${
            toplamSureSaat.format(2)
        } Saat)"
        binding.toplamSoru.text = "${toplamSoru.format(2)} Soru"
    }

    private fun Float.format(digits: Int) = "%.${digits}f".format(this)


}