package com.karaketir.coachingapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.adapter.DutiesRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityDutiesBinding
import com.karaketir.coachingapp.models.Duty
import java.io.File

class DutiesActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

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


    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var dutiesRecyclerAdapter: DutiesRecyclerAdapter
    private var gorevTurleri =
        arrayOf("Tamamlanmayan Görevler", "Tamamlanan Görevler", "Tüm Görevler")
    private var dutyList = ArrayList<Duty>()
    private lateinit var dutyAddButton: FloatingActionButton

    private lateinit var binding: ActivityDutiesBinding

    private fun deleteCache(context: Context) {
        try {
            val dir: File = context.cacheDir
            deleteDir(dir)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children = dir.list()
            if (children != null) {
                for (i in children.indices) {
                    val success = deleteDir(File(dir, children[i]))
                    if (!success) {
                        return false
                    }
                }
            }
            dir.delete()
        } else if (dir != null && dir.isFile) {
            dir.delete()
        } else {
            false
        }
    }

    override fun onStart() {
        super.onStart()
        deleteCache(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDutiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
        val dutiesRecyclerView = binding.dutiesRecyclerView
        val gorevTuruSpinner = binding.gorevSpinner
        dutyAddButton = binding.addDutyButton
        val dutyAdapter = ArrayAdapter(
            this@DutiesActivity, android.R.layout.simple_spinner_item, gorevTurleri
        )
        dutyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gorevTuruSpinner.adapter = dutyAdapter
        gorevTuruSpinner.onItemSelectedListener = this

        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            if (it.get("personType").toString() == "Teacher") {
                dutyAddButton.visibility = View.VISIBLE
            } else {
                dutyAddButton.visibility = View.GONE
            }
        }


        val layoutManager = GridLayoutManager(applicationContext, 2)

        dutiesRecyclerView.layoutManager = layoutManager

        dutiesRecyclerAdapter = DutiesRecyclerAdapter(dutyList)

        dutiesRecyclerView.adapter = dutiesRecyclerAdapter


    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {


        val studentID = intent.getStringExtra("studentID")

        var kurumKodu: Int
        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            kurumKodu = it.get("kurumKodu").toString().toInt()

            when (p2) {
                0 -> {
                    db.collection("School").document(kurumKodu.toString()).collection("Student")
                        .document(studentID!!).collection("Duties")
                        .whereEqualTo("tamamlandi", false)
                        .orderBy("eklenmeTarihi", Query.Direction.DESCENDING)
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
                                    val dutyTamamlandi = document.get("tamamlandi") as Boolean

                                    val currentDuty = Duty(
                                        konuAdi,
                                        toplamCalisma,
                                        studentID,
                                        dersAdi,
                                        tur,
                                        cozulenSoru,
                                        bitisZamani,
                                        document.id,
                                        dutyTamamlandi
                                    )
                                    dutyList.add(currentDuty)
                                }
                                dutiesRecyclerAdapter.notifyDataSetChanged()


                            }
                        }
                }
                1 -> {
                    db.collection("School").document(kurumKodu.toString()).collection("Student")
                        .document(studentID!!).collection("Duties").whereEqualTo("tamamlandi", true)
                        .orderBy("eklenmeTarihi", Query.Direction.DESCENDING)
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
                                    val dutyTamamlandi = document.get("tamamlandi") as Boolean

                                    val currentDuty = Duty(
                                        konuAdi,
                                        toplamCalisma,
                                        studentID,
                                        dersAdi,
                                        tur,
                                        cozulenSoru,
                                        bitisZamani,
                                        document.id,
                                        dutyTamamlandi
                                    )
                                    dutyList.add(currentDuty)
                                }
                                dutiesRecyclerAdapter.notifyDataSetChanged()


                            }
                        }
                }
                2 -> {
                    db.collection("School").document(kurumKodu.toString()).collection("Student")
                        .document(studentID!!).collection("Duties")
                        .orderBy("eklenmeTarihi", Query.Direction.DESCENDING)
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
                                    val dutyTamamlandi = document.get("tamamlandi") as Boolean

                                    val currentDuty = Duty(
                                        konuAdi,
                                        toplamCalisma,
                                        studentID,
                                        dersAdi,
                                        tur,
                                        cozulenSoru,
                                        bitisZamani,
                                        document.id,
                                        dutyTamamlandi
                                    )
                                    dutyList.add(currentDuty)
                                }
                                dutiesRecyclerAdapter.notifyDataSetChanged()


                            }
                        }
                }

            }


        }
        dutyAddButton = binding.addDutyButton

        dutyAddButton.setOnClickListener {
            val sendIntent = Intent(this, EnterDutyActivity::class.java)
            sendIntent.putExtra("studentID", studentID)
            this.startActivity(sendIntent)
        }


    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }
}