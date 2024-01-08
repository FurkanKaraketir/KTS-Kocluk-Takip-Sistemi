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
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.adapter.DersProgramiAdapter
import com.karaketir.coachingapp.databinding.ActivityProgramBinding
import com.karaketir.coachingapp.models.Ders

class ProgramActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProgramBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var pazartesiList = ArrayList<Ders>()
    private var saliList = ArrayList<Ders>()
    private var carsambaList = ArrayList<Ders>()
    private var persembeList = ArrayList<Ders>()
    private var cumaList = ArrayList<Ders>()
    private var cumartesiList = ArrayList<Ders>()
    private var pazarList = ArrayList<Ders>()

    private var kurumKodu = 0
    private var personType = "Student"


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgramBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
        val addLessonButton = binding.addLessonButton
        val layoutManager0 = LinearLayoutManager(this)
        val layoutManager1 = LinearLayoutManager(this)
        val layoutManager2 = LinearLayoutManager(this)
        val layoutManager3 = LinearLayoutManager(this)
        val layoutManager4 = LinearLayoutManager(this)
        val layoutManager5 = LinearLayoutManager(this)
        val layoutManager6 = LinearLayoutManager(this)

        var studentID = intent.getStringExtra("studentID").toString()
        kurumKodu = intent.getStringExtra("kurumKodu").toString().toInt()
        personType = intent.getStringExtra("personType").toString()


        val recyclerPazartesi = binding.recyclerPazartesi
        val recyclerPazartesiAdapter = DersProgramiAdapter(pazartesiList)
        recyclerPazartesi.layoutManager = layoutManager0
        recyclerPazartesi.adapter = recyclerPazartesiAdapter


        val recyclerSali = binding.recyclerSali
        val recyclerSaliAdapter = DersProgramiAdapter(saliList)
        recyclerSali.layoutManager = layoutManager1
        recyclerSali.adapter = recyclerSaliAdapter


        val recyclerCarsamba = binding.recyclerCarsamba
        val recyclerCarsambaAdapter = DersProgramiAdapter(carsambaList)
        recyclerCarsamba.layoutManager = layoutManager2
        recyclerCarsamba.adapter = recyclerCarsambaAdapter


        val recyclerPersembe = binding.recyclerPersembe
        val recyclerPersembeAdapter = DersProgramiAdapter(persembeList)
        recyclerPersembe.layoutManager = layoutManager3
        recyclerPersembe.adapter = recyclerPersembeAdapter


        val recyclerCuma = binding.recyclerCuma
        val recyclerCumaAdapter = DersProgramiAdapter(cumaList)
        recyclerCuma.layoutManager = layoutManager4
        recyclerCuma.adapter = recyclerCumaAdapter


        val recyclerCumartesi = binding.recyclerCumartesi
        val recyclerCumartesiAdapter = DersProgramiAdapter(cumartesiList)
        recyclerCumartesi.layoutManager = layoutManager5
        recyclerCumartesi.adapter = recyclerCumartesiAdapter


        val registerPazar = binding.recyclerPazar
        val recyclerPazarAdapter = DersProgramiAdapter(pazarList)
        registerPazar.layoutManager = layoutManager6
        registerPazar.adapter = recyclerPazarAdapter



        addLessonButton.setOnClickListener {
            val newIntent = Intent(this, AddProgramActivity::class.java)
            newIntent.putExtra("studentID", studentID)
            newIntent.putExtra("kurumKodu", kurumKodu.toString())
            this.startActivity(newIntent)
        }






        if (personType == "Student") {
            addLessonButton.visibility = View.VISIBLE
            studentID = auth.uid.toString()
        } else {
            addLessonButton.visibility = View.VISIBLE
        }



        db.collection("School").document(kurumKodu.toString()).collection("Student")
            .document(studentID).collection("DersProgrami").document("Pazartesi")
            .collection("Dersler").orderBy("id", Query.Direction.ASCENDING)
            .addSnapshotListener { value, _ ->

                if (value != null) {
                    pazartesiList.clear()
                    for (i in value) {

                        val newDersAdi = i.get("dersAdi").toString()
                        val newDersTuru = i.get("dersTuru").toString()
                        val newDersSure = i.get("dersSure").toString().toInt()
                        val newDersNumara = i.get("id").toString()
                        val newDers = Ders(newDersAdi, newDersTuru, newDersSure, newDersNumara)

                        pazartesiList.add(newDers)
                        recyclerPazartesiAdapter.notifyDataSetChanged()
                    }
                }


            }
        db.collection("School").document(kurumKodu.toString()).collection("Student")
            .document(studentID).collection("DersProgrami").document("Salı").collection("Dersler")
            .orderBy("id", Query.Direction.ASCENDING).addSnapshotListener { value, _ ->

                if (value != null) {
                    saliList.clear()
                    for (i in value) {

                        val newDersAdi = i.get("dersAdi").toString()
                        val newDersTuru = i.get("dersTuru").toString()
                        val newDersSure = i.get("dersSure").toString().toInt()
                        val newDersNumara = i.get("id").toString()
                        val newDers = Ders(newDersAdi, newDersTuru, newDersSure, newDersNumara)

                        saliList.add(newDers)
                        recyclerSaliAdapter.notifyDataSetChanged()


                    }
                }


            }

        db.collection("School").document(kurumKodu.toString()).collection("Student")
            .document(studentID).collection("DersProgrami").document("Çarşamba")
            .collection("Dersler").orderBy("id", Query.Direction.ASCENDING)
            .addSnapshotListener { value, _ ->

                if (value != null) {
                    carsambaList.clear()
                    for (i in value) {

                        val newDersAdi = i.get("dersAdi").toString()
                        val newDersTuru = i.get("dersTuru").toString()
                        val newDersSure = i.get("dersSure").toString().toInt()
                        val newDersNumara = i.get("id").toString()
                        val newDers = Ders(newDersAdi, newDersTuru, newDersSure, newDersNumara)

                        carsambaList.add(newDers)
                        recyclerCarsambaAdapter.notifyDataSetChanged()

                    }
                }


            }

        db.collection("School").document(kurumKodu.toString()).collection("Student")
            .document(studentID).collection("DersProgrami").document("Perşembe")
            .collection("Dersler").orderBy("id", Query.Direction.ASCENDING)
            .addSnapshotListener { value, _ ->

                if (value != null) {
                    persembeList.clear()
                    for (i in value) {

                        val newDersAdi = i.get("dersAdi").toString()
                        val newDersTuru = i.get("dersTuru").toString()
                        val newDersSure = i.get("dersSure").toString().toInt()
                        val newDersNumara = i.get("id").toString()
                        val newDers = Ders(newDersAdi, newDersTuru, newDersSure, newDersNumara)

                        persembeList.add(newDers)
                        recyclerPersembeAdapter.notifyDataSetChanged()

                    }
                }


            }


        db.collection("School").document(kurumKodu.toString()).collection("Student")
            .document(studentID).collection("DersProgrami").document("Cuma").collection("Dersler")
            .orderBy("id", Query.Direction.ASCENDING).addSnapshotListener { value, _ ->

                if (value != null) {
                    cumaList.clear()
                    for (i in value) {

                        val newDersAdi = i.get("dersAdi").toString()
                        val newDersTuru = i.get("dersTuru").toString()
                        val newDersSure = i.get("dersSure").toString().toInt()
                        val newDersNumara = i.get("id").toString()
                        val newDers = Ders(newDersAdi, newDersTuru, newDersSure, newDersNumara)

                        cumaList.add(newDers)
                        recyclerCumaAdapter.notifyDataSetChanged()

                    }
                }


            }

        db.collection("School").document(kurumKodu.toString()).collection("Student")
            .document(studentID).collection("DersProgrami").document("Cumartesi")
            .collection("Dersler").orderBy("id", Query.Direction.ASCENDING)
            .addSnapshotListener { value, _ ->

                if (value != null) {
                    cumartesiList.clear()
                    for (i in value) {

                        val newDersAdi = i.get("dersAdi").toString()
                        val newDersTuru = i.get("dersTuru").toString()
                        val newDersSure = i.get("dersSure").toString().toInt()
                        val newDersNumara = i.get("id").toString()
                        val newDers = Ders(newDersAdi, newDersTuru, newDersSure, newDersNumara)

                        cumartesiList.add(newDers)
                        recyclerCumartesiAdapter.notifyDataSetChanged()

                    }
                }


            }

        db.collection("School").document(kurumKodu.toString()).collection("Student")
            .document(studentID).collection("DersProgrami").document("Pazar").collection("Dersler")
            .orderBy("id", Query.Direction.ASCENDING).addSnapshotListener { value, _ ->

                if (value != null) {
                    pazarList.clear()
                    for (i in value) {

                        val newDersAdi = i.get("dersAdi").toString()
                        val newDersTuru = i.get("dersTuru").toString()
                        val newDersSure = i.get("dersSure").toString().toInt()
                        val newDersNumara = i.get("id").toString()
                        val newDers = Ders(newDersAdi, newDersTuru, newDersSure, newDersNumara)

                        pazarList.add(newDers)
                        recyclerPazarAdapter.notifyDataSetChanged()

                    }
                }


            }


    }

}