package com.kodgem.coachingapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kodgem.coachingapp.databinding.ActivityEnterStudyBinding
import java.util.*

class EnterStudyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEnterStudyBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterStudyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore
        val documentID = UUID.randomUUID().toString()
        val subjectType = intent.getStringExtra("studyType")
        val subjectTypeTitle = binding.subjectTypeTitle
        val textInputCurrentMinutes = binding.TextInputCurrentMinutes
        val textInputCurrentTestsMinutes = binding.TextInputCurrentTestsMinutes
        val textInputCurrentTests = binding.TextInputCurrentTests
        val studySaveButton = binding.studySaveButton

        val currentMinutesEditText = binding.currentMinutesEditText
        val currentTestsMinutesEditText = binding.currentTestsMinutesEditText
        val currentTestsEditText = binding.currentTestsEditText

        subjectTypeTitle.text = "Konu: $subjectType"

        textInputCurrentMinutes.hint = "Kaç Dakika $subjectType Konu Çalıştın?"
        textInputCurrentMinutes.helperText = "Kaç Dakika $subjectType Konu Çalıştın?"

        textInputCurrentTestsMinutes.hint = "Kaç Dakika $subjectType Testi Çözdün?"
        textInputCurrentTestsMinutes.helperText = "Kaç Dakika $subjectType Testi Çözdün?"

        textInputCurrentTests.hint = "Kaç Tane $subjectType Sorusu Çözdün?"
        textInputCurrentTests.helperText = "Kaç Tane $subjectType Sorusu Çözdün?"

        studySaveButton.setOnClickListener {
            studySaveButton.isClickable = false
            if (currentMinutesEditText.text.toString().isNotEmpty()) {
                currentMinutesEditText.error = null

                if (currentTestsMinutesEditText.text.toString().isNotEmpty()) {
                    currentTestsMinutesEditText.error = null

                    if (currentTestsEditText.text.toString().isNotEmpty()) {
                        currentTestsEditText.error = null
                        val study = hashMapOf(
                            "id" to documentID,
                            "konuAnlatımı" to currentMinutesEditText.text.toString(),
                            "konuTestiDK" to currentTestsMinutesEditText.text.toString(),
                            "subjectTheme" to subjectType,
                            "toplamCalisma" to (currentMinutesEditText.text.toString()
                                .toInt() + currentTestsMinutesEditText.text.toString()
                                .toInt()).toString(),
                            "çözülenSoru" to currentTestsEditText.text.toString()
                        )
                        db.collection("School").document("SchoolIDDDD").collection("Student")
                            .document(auth.uid.toString()).collection("Studies")
                            .document(documentID).set(study).addOnSuccessListener {
                                Toast.makeText(this, "İşlem Başarılı!", Toast.LENGTH_SHORT).show()
                                finish()
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