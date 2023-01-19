package com.karaketir.coachingapp

//noinspection SuspiciousImport
import android.R
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.adapter.DenemelerTeacherRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityDenemelerTeacherBinding
import com.karaketir.coachingapp.models.DenemeTeacher

class DenemelerTeacherActivity : AppCompatActivity() {

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


    private lateinit var binding: ActivityDenemelerTeacherBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var denemelerList = ArrayList<DenemeTeacher>()
    private var gradeList = arrayOf("Bütün Sınıflar", "12", "11", "10", "9", "0")
    private val turler = arrayOf("Tüm Denemeler", "TYT", "AYT")
    private var secilenGrade = ""
    private var secilenTur = ""


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDenemelerTeacherBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        val layoutManager = LinearLayoutManager(this)

        val recyclerView = binding.recyclerViewDenemeler
        val addDenemeButton = binding.addDeneme
        val recyclerViewAdapter = DenemelerTeacherRecyclerAdapter(denemelerList)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = recyclerViewAdapter
        val gradeSpinner = binding.denemeTeacherGradeSpinner
        val turSpinner = binding.denemeTeacherTurSpinner
        addDenemeButton.setOnClickListener {
            val intent = Intent(this, AddDenemeTeacherActivity::class.java)
            this.startActivity(intent)
        }

        val gradeAdapter = ArrayAdapter(
            this@DenemelerTeacherActivity, R.layout.simple_spinner_item, gradeList
        )
        val turAdapter = ArrayAdapter(
            this@DenemelerTeacherActivity, R.layout.simple_spinner_item, turler
        )

        gradeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        gradeSpinner.adapter = gradeAdapter
        gradeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {

                secilenGrade = gradeList[position]

                turAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                turSpinner.adapter = turAdapter
                turSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?, view: View?, position: Int, id: Long
                    ) {

                        secilenTur = turler[position]

                        if (secilenGrade == "Bütün Sınıflar" && secilenTur == "Tüm Denemeler") {
                            denemelerList.clear()
                            db.collection("User").document(auth.uid.toString()).get()
                                .addOnSuccessListener {
                                    val kurumKodu = it.get("kurumKodu")?.toString()?.toInt()

                                    auth.currentUser?.let { it1 ->
                                        db.collection("School").document(kurumKodu.toString())
                                            .collection("Teacher").document(
                                                it1.uid
                                            ).collection("Denemeler")
                                            .orderBy("bitisTarihi", Query.Direction.DESCENDING)
                                            .addSnapshotListener { value, _ ->
                                                if (value != null) {
                                                    denemelerList.clear()
                                                    for (deneme in value) {
                                                        val currentDeneme = DenemeTeacher(
                                                            deneme.get("denemeAdi").toString(),
                                                            deneme.id
                                                        )
                                                        denemelerList.add(currentDeneme)
                                                    }
                                                    recyclerViewAdapter.notifyDataSetChanged()
                                                }
                                            }
                                    }

                                }
                        }
                        if (secilenGrade == "Bütün Sınıflar" && secilenTur != "Tüm Denemeler") {
                            denemelerList.clear()
                            db.collection("User").document(auth.uid.toString()).get()
                                .addOnSuccessListener {
                                    val kurumKodu = it.get("kurumKodu")?.toString()?.toInt()

                                    auth.currentUser?.let { it1 ->
                                        db.collection("School").document(kurumKodu.toString())
                                            .collection("Teacher").document(
                                                it1.uid
                                            ).collection("Denemeler")
                                            .whereEqualTo("tür", secilenTur)
                                            .orderBy("bitisTarihi", Query.Direction.DESCENDING)
                                            .addSnapshotListener { value, _ ->
                                                if (value != null) {
                                                    denemelerList.clear()
                                                    for (deneme in value) {
                                                        val currentDeneme = DenemeTeacher(
                                                            deneme.get("denemeAdi").toString(),
                                                            deneme.id
                                                        )
                                                        denemelerList.add(currentDeneme)
                                                    }
                                                    recyclerViewAdapter.notifyDataSetChanged()
                                                }
                                            }
                                    }

                                }
                        }
                        if (secilenGrade != "Bütün Sınıflar" && secilenTur == "Tüm Denemeler") {
                            denemelerList.clear()
                            db.collection("User").document(auth.uid.toString()).get()
                                .addOnSuccessListener {
                                    val kurumKodu = it.get("kurumKodu")?.toString()?.toInt()

                                    auth.currentUser?.let { it1 ->
                                        db.collection("School").document(kurumKodu.toString())
                                            .collection("Teacher").document(
                                                it1.uid
                                            ).collection("Denemeler")
                                            .whereEqualTo("grade", secilenGrade.toInt())
                                            .orderBy("bitisTarihi", Query.Direction.DESCENDING)
                                            .addSnapshotListener { value, _ ->
                                                if (value != null) {
                                                    denemelerList.clear()
                                                    for (deneme in value) {
                                                        val currentDeneme = DenemeTeacher(
                                                            deneme.get("denemeAdi").toString(),
                                                            deneme.id
                                                        )
                                                        denemelerList.add(currentDeneme)
                                                    }
                                                    recyclerViewAdapter.notifyDataSetChanged()
                                                }
                                            }
                                    }

                                }
                        }
                        if (secilenGrade != "Bütün Sınıflar" && secilenTur != "Tüm Denemeler") {
                            denemelerList.clear()
                            db.collection("User").document(auth.uid.toString()).get()
                                .addOnSuccessListener {
                                    val kurumKodu = it.get("kurumKodu")?.toString()?.toInt()

                                    auth.currentUser?.let { it1 ->
                                        db.collection("School").document(kurumKodu.toString())
                                            .collection("Teacher").document(
                                                it1.uid
                                            ).collection("Denemeler")
                                            .whereEqualTo("grade", secilenGrade.toInt())
                                            .whereEqualTo("tür", secilenTur)
                                            .orderBy("bitisTarihi", Query.Direction.DESCENDING)
                                            .addSnapshotListener { value, _ ->
                                                if (value != null) {
                                                    denemelerList.clear()
                                                    for (deneme in value) {
                                                        val currentDeneme = DenemeTeacher(
                                                            deneme.get("denemeAdi").toString(),
                                                            deneme.id
                                                        )
                                                        denemelerList.add(currentDeneme)
                                                    }
                                                    recyclerViewAdapter.notifyDataSetChanged()
                                                }
                                            }
                                    }

                                }
                        }

                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }

                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }


    }
}