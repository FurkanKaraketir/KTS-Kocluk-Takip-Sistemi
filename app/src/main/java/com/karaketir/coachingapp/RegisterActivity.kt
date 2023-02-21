package com.karaketir.coachingapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

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


    private val alanlar = arrayOf("Dil", "Eşit Ağırlık", "Sözel", "Sayısal")
    private val dersler = ArrayList<String>()

    private lateinit var documentID: String
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityRegisterBinding
    private var nameAndSurname = ""
    private var grade = 0
    private var selection = 0
    private var branch = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        val studentSpinner = binding.studentSpinner
        val teacherSpinner = binding.teacherSpinner
        val registerLayout = binding.registerLayout
        val studentButton = binding.studentButton
        val teacherButton = binding.teacherButton
        val studentSpinnerLayout = binding.studentSpinnerLayout
        val teacherSpinnerLayout = binding.teacherSpinnerLayout
        val textInputClass = binding.TextInputClass
        val signUpButton = binding.signUpButton
        val gradeText = binding.classEditText
        val emailEditText = binding.emailRegisterEditText
        val passwordEditText = binding.passwordRegisterEditText
        val nameAndSurnameEditText = binding.nameAndSurnameEditText
        val kurumKoduEditText = binding.kurumKoduEditText
        val kullanici = binding.kullanimBtn
        val gizlilik = binding.gizBtn
        val cerez = binding.cerezBtn
        kullanici.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra(
                "link",
                "https://docs.google.com/document/d/1mzyFHaD6UUrB85BkXRO9r3fS8_jo_sUxoDPbn0Qi2kk/edit?usp=sharing"
            )
            this.startActivity(intent)
        }
        gizlilik.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra(
                "link",
                "https://docs.google.com/document/d/1cDKP_HRTnQhVJS3u1uYQeLqU4x3_4HCNQreBE5JCIag/edit?usp=sharing"
            )
            this.startActivity(intent)
        }
        cerez.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra(
                "link",
                "https://docs.google.com/document/d/1jFbZ8IW4AEb8ZMBy57YLQAJAwl5uQ34W4q_yMhqc8aU/edit?usp=sharing"
            )
            this.startActivity(intent)
        }

        signUpButton.setOnClickListener {
            Toast.makeText(this, "Lütfen Bekleyiniz...", Toast.LENGTH_SHORT).show()
            if (emailEditText.text.toString().isNotEmpty()) {
                emailEditText.error = null

                if (passwordEditText.text.toString().isNotEmpty()) {

                    if (passwordEditText.text.toString().length >= 6) {
                        passwordEditText.error = null
                        if (gradeText.text.toString().isNotEmpty() || selection == 2) {
                            gradeText.error = null
                            if (nameAndSurnameEditText.text.toString().isNotEmpty()) {
                                nameAndSurnameEditText.error = null

                                if (kurumKoduEditText.text.toString().isNotEmpty()) {
                                    kurumKoduEditText.error = null


                                    nameAndSurname = nameAndSurnameEditText.text.toString()

                                    grade = try {
                                        gradeText.text.toString().toInt()
                                    } catch (e: Exception) {
                                        0
                                    }

                                    signUp(
                                        emailEditText.text.toString(),
                                        passwordEditText.text.toString(),
                                        kurumKoduEditText.text.toString().toInt()
                                    )


                                } else {
                                    kurumKoduEditText.error = "Bu Alan Boş Bırakılamaz"
                                }


                            } else {
                                nameAndSurnameEditText.error = "Bu Alan Boş Bırakılamaz"

                            }


                        } else {
                            gradeText.error = "Bu Alan Boş Bırakılamaz"
                        }
                    } else {
                        passwordEditText.error = "Şifre En Az 6 Karakter Uzunluğunda Olmalı"
                    }


                } else {
                    passwordEditText.error = "Bu Alan Boş Bırakılamaz"
                }
            } else {
                emailEditText.error = "Bu Alan Boş Bırakılamaz"
            }
        }


        val studentAdapter = ArrayAdapter(
            this@RegisterActivity, android.R.layout.simple_spinner_item, alanlar
        )


        db.collection("Lessons").addSnapshotListener { value, _ ->
            if (value != null) {
                for (document in value) {
                    dersler.add(document.get("dersAdi").toString())
                }
                val teacherAdapter = ArrayAdapter(
                    this@RegisterActivity, android.R.layout.simple_spinner_item, dersler
                )
                teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                teacherSpinner.adapter = teacherAdapter
                teacherSpinner.onItemSelectedListener = this
            }
        }

        studentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        studentSpinner.adapter = studentAdapter
        studentSpinner.onItemSelectedListener = this


        studentButton.setOnClickListener {
            selection = 1
            registerLayout.visibility = View.VISIBLE
            teacherSpinnerLayout.visibility = View.GONE
            textInputClass.visibility = View.VISIBLE
            studentSpinnerLayout.visibility = View.VISIBLE
        }

        teacherButton.setOnClickListener {
            registerLayout.visibility = View.VISIBLE
            textInputClass.visibility = View.GONE
            selection = 2
            teacherSpinnerLayout.visibility = View.VISIBLE
            studentSpinnerLayout.visibility = View.GONE
        }


    }

    private fun signUp(email: String, password: String, kurumKodu: Int) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                documentID = auth.uid!!

                if (selection == 1) {
                    val user = hashMapOf(
                        "email" to email,
                        "grade" to grade,
                        "id" to documentID,
                        "nameAndSurname" to nameAndSurname,
                        "personType" to "Student",
                        "subjectType" to branch,
                        "kurumKodu" to kurumKodu,
                        "teacher" to "",
                    )

                    db.collection("User").document(documentID).set(user).addOnSuccessListener {

                        db.collection("School").document(kurumKodu.toString()).collection("Student")
                            .document(documentID).set(user).addOnSuccessListener {
                                Toast.makeText(this, "Başarılı", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                this.startActivity(intent)
                                finish()
                            }


                    }.addOnFailureListener { e ->
                        Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                } else if (selection == 2) {
                    val user = hashMapOf(
                        "email" to email,
                        "id" to documentID,
                        "nameAndSurname" to nameAndSurname,
                        "personType" to "Teacher",
                        "subjectType" to branch,
                        "kurumKodu" to kurumKodu,
                    )

                    db.collection("User").document(documentID).set(user).addOnSuccessListener {

                        db.collection("School").document(kurumKodu.toString()).collection("Teacher")
                            .document(documentID).set(user).addOnSuccessListener {
                                Toast.makeText(this, "Başarılı", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                this.startActivity(intent)
                                finish()
                            }


                    }.addOnFailureListener { e ->
                        Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
                }


            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(
                    baseContext, "Kayıt Başarısız!", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {

        if (selection == 1) {
            when (position) {
                0 -> {
                    branch = "Dil"

                }
                1 -> {
                    branch = "Eşit Ağırlık"

                }
                2 -> {
                    branch = "Sözel"

                }
                3 -> {
                    branch = "Sayısal"

                }
            }
        }
        if (selection == 2) {

            branch = dersler[position]
        }


    }


    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

}