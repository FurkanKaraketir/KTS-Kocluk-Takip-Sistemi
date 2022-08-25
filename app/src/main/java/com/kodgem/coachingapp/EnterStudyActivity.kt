package com.kodgem.coachingapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kodgem.coachingapp.databinding.ActivityEnterStudyBinding
import java.util.*
import kotlin.collections.HashMap

class EnterStudyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEnterStudyBinding
    private lateinit var auth: FirebaseAuth
    private var konuAdlari = ArrayList<String>()

    private lateinit var db: FirebaseFirestore
    private lateinit var secilenKonu: String
    private lateinit var secilenDocumentID: String

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterStudyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore
        val spinner = binding.studySpinner
        val documentID = UUID.randomUUID().toString()
        val subjectType = intent.getStringExtra("studyType")
        val dersAdi = intent.getStringExtra("dersAdi")
        secilenKonu = ""
        db.collection("Lessons").document(dersAdi.toString()).collection(subjectType.toString())
            .addSnapshotListener { value, _ ->
                if (value != null) {
                    konuAdlari.clear()
                    for (document in value) {
                        val konuAdi = document.get("konuAdi").toString()
                        konuAdlari.add(konuAdi)
                    }
                    val studyAdapter = ArrayAdapter(
                        this@EnterStudyActivity,
                        android.R.layout.simple_spinner_dropdown_item, konuAdlari
                    )

                    studyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = studyAdapter
                    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            position: Int,
                            p3: Long
                        ) {
                            secilenKonu = konuAdlari[position]


                            db.collection("Lessons").document(dersAdi.toString())
                                .collection(subjectType.toString())
                                .whereEqualTo("konuAdi", secilenKonu)
                                .addSnapshotListener { value1, _ ->
                                    if (value1 != null) {
                                        for (document in value1) {
                                            secilenDocumentID =
                                                document.get("id").toString()
                                        }
                                    }
                                }

                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {

                        }

                    }


                }
            }


        val subjectTypeTitle = binding.subjectTypeTitle
        val textInputCurrentMinutes = binding.TextInputCurrentMinutes
        val textInputCurrentTestsMinutes = binding.TextInputCurrentTestsMinutes
        val textInputCurrentTests = binding.TextInputCurrentTests
        val studySaveButton = binding.studySaveButton

        val currentMinutesEditText = binding.currentMinutesEditText
        val currentTestsMinutesEditText = binding.currentTestsMinutesEditText
        val currentTestsEditText = binding.currentTestsEditText

        subjectTypeTitle.text = "Tür: $subjectType"

        textInputCurrentMinutes.hint = "Kaç Dakika Konu Çalıştın?"
        textInputCurrentMinutes.helperText = "Kaç Dakika Konu Çalıştın?"

        textInputCurrentTestsMinutes.hint = "Bu Konuda Kaç Dakika Test Çözdün?"
        textInputCurrentTestsMinutes.helperText = "Bu Konuda Kaç Dakika Test Çözdün?"

        textInputCurrentTests.hint = "Bu Konuda Kaç Tane Soru Çözdün?"
        textInputCurrentTests.helperText = "Bu Konuda Kaç Tane Sorusu Çözdün?"

        studySaveButton.setOnClickListener {
            var stopper = false
            studySaveButton.isClickable = false
            if (currentMinutesEditText.text.toString().isNotEmpty()) {
                currentMinutesEditText.error = null

                if (currentTestsMinutesEditText.text.toString().isNotEmpty()) {
                    currentTestsMinutesEditText.error = null

                    if (currentTestsEditText.text.toString().isNotEmpty()) {
                        currentTestsEditText.error = null
                        val study = hashMapOf(
                            "id" to documentID,
                            "timestamp" to FieldValue.serverTimestamp(),
                            "konuAnlatımı" to currentMinutesEditText.text.toString().toInt(),
                            "konuTestiDK" to currentTestsMinutesEditText.text.toString().toInt(),
                            "dersAdi" to dersAdi,
                            "konuAdi" to secilenKonu,
                            "toplamCalisma" to currentMinutesEditText.text.toString()
                                .toInt() + currentTestsMinutesEditText.text.toString()
                                .toInt(),
                            "çözülenSoru" to currentTestsEditText.text.toString().toInt()
                        )

                        db.collection("School").document("SchoolIDDDD").collection("Student")
                            .document(auth.uid.toString()).collection("Studies")
                            .whereEqualTo("konuAdi", secilenKonu)
                            .addSnapshotListener { value, _ ->

                                if (!stopper) {
                                    if (value != null) {
                                        if (!value.isEmpty) {
                                            for (document in value) {
                                                val studyUpdate = hashMapOf(
                                                    "id" to document.id,
                                                    "timestamp" to FieldValue.serverTimestamp(),
                                                    "konuAnlatımı" to currentMinutesEditText.text.toString()
                                                        .toInt() + document.get("konuAnlatımı")
                                                        .toString()
                                                        .toInt(),
                                                    "konuTestiDK" to currentTestsMinutesEditText.text.toString()
                                                        .toInt() + document.get("konuTestiDK")
                                                        .toString()
                                                        .toInt(),
                                                    "dersAdi" to dersAdi,
                                                    "konuAdi" to secilenKonu,
                                                    "toplamCalisma" to currentMinutesEditText.text.toString()
                                                        .toInt() + currentTestsMinutesEditText.text.toString()
                                                        .toInt() + document.get("konuAnlatımı")
                                                        .toString()
                                                        .toInt() + document.get("konuTestiDK")
                                                        .toString()
                                                        .toInt(),
                                                    "çözülenSoru" to currentTestsEditText.text.toString()
                                                        .toInt() + document.get("çözülenSoru")
                                                        .toString()
                                                        .toInt()
                                                )
                                                stopper = true

                                                db.collection("School").document("SchoolIDDDD")
                                                    .collection("Student")
                                                    .document(auth.uid.toString())
                                                    .collection("Studies")
                                                    .document(document.id).update(studyUpdate)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(
                                                            this,
                                                            "İşlem Başarılı!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        finish()

                                                    }


                                            }
                                        } else {
                                            stopper = true

                                            db.collection("School").document("SchoolIDDDD")
                                                .collection("Student")
                                                .document(auth.uid.toString()).collection("Studies")
                                                .document(documentID).set(study)
                                                .addOnSuccessListener {
                                                    Toast.makeText(
                                                        this,
                                                        "İşlem Başarılı!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    finish()
                                                }.addOnFailureListener {
                                                    println(it.localizedMessage)
                                                }


                                        }


                                    }

                                }

                            }


                    } else {
                        currentTestsEditText.error = "Bu Alanı Boş Bırakamazsın!"
                        studySaveButton.isClickable = true

                    }


                } else {
                    currentTestsMinutesEditText.error = "Bu Alanı Boş Bırakamazsın!"
                    studySaveButton.isClickable = true

                }


            } else {
                currentMinutesEditText.error = "Bu Alanı Boş Bırakamazsın!"
                studySaveButton.isClickable = true

            }


        }

    }


}
