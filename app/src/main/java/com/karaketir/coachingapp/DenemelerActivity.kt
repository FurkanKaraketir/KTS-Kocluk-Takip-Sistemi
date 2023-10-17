package com.karaketir.coachingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.adapter.DenemelerRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityDenemelerBinding
import com.karaketir.coachingapp.models.Deneme
import java.util.*

class DenemelerActivity : AppCompatActivity() {

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


    private lateinit var binding: ActivityDenemelerBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: DenemelerRecyclerAdapter
    private lateinit var spinner: Spinner
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var denemeList = ArrayList<Deneme>()
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date
    private var kurumKodu = 763455
    private var secilenZamanAraligi = ""
    private lateinit var studentID: String
    private val zamanAraliklari =
        arrayOf("Bugün", "Dün", "Bu Hafta", "Geçen Hafta", "Bu Ay", "Geçen Ay", "Tüm Zamanlar")
    private val turler = arrayOf("Tüm Denemeler", "TYT", "AYT")
    private lateinit var layoutManager: GridLayoutManager
    private var grade = 0
    private var teacher = ""
    private var personType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDenemelerBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore
        recyclerView = binding.denemelerRecyclerView
        spinner = binding.denemeSpinner
        val turSpinner = binding.denemeTurSpinner
        val denemeAddButton = binding.denemeAddButton

        kurumKodu = intent.getStringExtra("kurumKodu").toString().toInt()
        grade = intent.getStringExtra("grade").toString().toInt()
        teacher = intent.getStringExtra("teacher").toString()
        personType = intent.getStringExtra("personType").toString()
        layoutManager = GridLayoutManager(applicationContext, 2)

        if (personType == "Student") {
            denemeAddButton.visibility = View.VISIBLE
        } else {
            denemeAddButton.visibility = View.GONE
        }


        val denemeAdapter = ArrayAdapter(
            this@DenemelerActivity, android.R.layout.simple_spinner_item, zamanAraliklari
        )
        val turAdapter = ArrayAdapter(
            this@DenemelerActivity, android.R.layout.simple_spinner_item, turler
        )

        studentID = intent.getStringExtra("studentID").toString()

        denemeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = denemeAdapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                secilenZamanAraligi = zamanAraliklari[position]

                recyclerView = binding.denemelerRecyclerView
                recyclerView.layoutManager = layoutManager
                recyclerAdapter =
                    DenemelerRecyclerAdapter(denemeList, secilenZamanAraligi, kurumKodu)
                recyclerView.adapter = recyclerAdapter
                recyclerAdapter.notifyDataSetChanged()

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
                        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

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
                        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

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

                turAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                turSpinner.adapter = turAdapter
                turSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                        val secilenTur = turler[p2]
                        if (secilenTur != "Tüm Denemeler") {


                            db.collection("School").document(kurumKodu.toString())
                                .collection("Student").document(studentID).collection("Denemeler")
                                .whereEqualTo("denemeTür", secilenTur)
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
                                                val denemeToplamNet =
                                                    document.get("toplamNet").toString().toFloat()
                                                val denemeTarihi =
                                                    document.get("denemeTarihi") as Timestamp
                                                val denemeTur = document.get("denemeTür").toString()

                                                val currentDeneme = Deneme(
                                                    denemeID,
                                                    denemeAdi,
                                                    denemeToplamNet,
                                                    denemeTarihi,
                                                    studentID,
                                                    denemeTur
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


                        } else {
                            db.collection("School").document(kurumKodu.toString())
                                .collection("Student").document(studentID).collection("Denemeler")
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
                                                val denemeToplamNet =
                                                    document.get("toplamNet").toString().toFloat()
                                                val denemeTarihi =
                                                    document.get("denemeTarihi") as Timestamp
                                                val denemeTur = document.get("denemeTür").toString()

                                                val currentDeneme = Deneme(
                                                    denemeID,
                                                    denemeAdi,
                                                    denemeToplamNet,
                                                    denemeTarihi,
                                                    studentID,
                                                    denemeTur
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
                                    val denemeToplamNet =
                                        document.get("toplamNet").toString().toFloat()
                                    val denemeTarihi = document.get("denemeTarihi") as Timestamp
                                    val denemeTur = document.get("denemeTür").toString()

                                    val currentDeneme = Deneme(
                                        denemeID,
                                        denemeAdi,
                                        denemeToplamNet,
                                        denemeTarihi,
                                        studentID,
                                        denemeTur
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

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
        denemeAddButton.setOnClickListener {


            if (teacher.isNotEmpty()) {
                val popup = PopupMenu(applicationContext, it)
                //inflate menu with layout mainmenu
                popup.inflate(R.menu.subject_context)
                popup.show()

                popup.setOnMenuItemClickListener { item ->
                    if (item.itemId == R.id.TYT) {
                        val intent = Intent(this, EnterTytActivity::class.java)
                        intent.putExtra("studyType", "TYT")
                        intent.putExtra("grade", grade.toString())
                        intent.putExtra("teacher", teacher)
                        intent.putExtra("kurumKodu", kurumKodu.toString())
                        this.startActivity(intent)
                    }

                    if (item.itemId == R.id.AYT) {
                        val intent = Intent(this, EnterTytActivity::class.java)
                        intent.putExtra("studyType", "AYT")
                        intent.putExtra("grade", grade.toString())
                        intent.putExtra("teacher", teacher)
                        intent.putExtra("kurumKodu", kurumKodu.toString())
                        this.startActivity(intent)
                    }
                    false
                }
            } else {
                Toast.makeText(
                    this, "Koç Öğretmeniniz Bulunmamaktadır.", Toast.LENGTH_SHORT
                ).show()
            }


        }

    }
}