package com.karaketir.coachingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.databinding.ActivityAddUserDataBinding

class AddUserDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddUserDataBinding
    private lateinit var nameAndSurname: String
    private var grade: Int = 0
    private lateinit var branch: String
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isTeacher: Boolean = false
    private var documentID: String = ""
    private var email: String = ""
    private var studentList = arrayOf("Sayısal", "Sözel", "Eşit Ağırlık", "Dil")
    private var teacherList = arrayOf(
        "Türk Dili ve Edebiyatı",
        "Matematik",
        "Fizik",
        "Kimya",
        "Biyoloji",
        "Tarih",
        "Coğrafya",
        "Felsefe",
        "Din Kültürü ve Ahlak Bilgisi",
        "İngilizce",
        "Arapça"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddUserDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
        val branchSpinner = binding.branchSpinner

        intent?.let {
            nameAndSurname = it.getStringExtra("nameAndSurname").toString()
            documentID = it.getStringExtra("documentID").toString()
            email = it.getStringExtra("email").toString()

        }

        isTeacher = false
        binding.TextInputGrade.visibility = View.VISIBLE
        var branchAdapter = ArrayAdapter(
            this@AddUserDataActivity, android.R.layout.simple_spinner_item, studentList
        )
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        branchSpinner.adapter = branchAdapter
        branchSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
            ) {
                branch = studentList[p2]

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        branch = ""

        val nameAndSurnameEditText = binding.nameAndSurnameEditText
        val kurumKoduEditText = binding.kurumKoduEditText
        nameAndSurnameEditText.setText(nameAndSurname)

        val gradeEditText = binding.gradeEditText

        val teacherSwitch = binding.teacherSwitch
        val saveButton = binding.saveButton
        teacherSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                isTeacher = true
                binding.TextInputGrade.visibility = View.GONE

                branchAdapter = ArrayAdapter(
                    this@AddUserDataActivity, android.R.layout.simple_spinner_item, teacherList
                )
                branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                branchSpinner.adapter = branchAdapter
                branchSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
                    ) {
                        branch = teacherList[p2]

                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }

                }
            } else {
                isTeacher = false
                binding.TextInputGrade.visibility = View.VISIBLE
                branchAdapter = ArrayAdapter(
                    this@AddUserDataActivity, android.R.layout.simple_spinner_item, studentList
                )
                branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                branchSpinner.adapter = branchAdapter
                branchSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
                    ) {
                        branch = studentList[p2]

                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }

                }
            }
        }


        saveButton.setOnClickListener {

            if (isTeacher) {

                if (nameAndSurnameEditText.text.toString().isNotEmpty()) {
                    nameAndSurnameEditText.error = null
                    nameAndSurname = nameAndSurnameEditText.text.toString()

                    if (kurumKoduEditText.text.toString().isNotEmpty()) {
                        kurumKoduEditText.error = null

                        val userData = hashMapOf(
                            "nameAndSurname" to nameAndSurname,
                            "subject" to branch,
                            "kurumKodu" to kurumKoduEditText.text.toString().toInt(),
                            "personType" to "Teacher",
                            "id" to documentID,
                            "email" to email
                        )


                        db.collection("User").document(documentID).set(userData)
                            .addOnSuccessListener {

                                db.collection("School").document(kurumKoduEditText.text.toString())
                                    .collection("Teacher").document(documentID).set(userData)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Kayıt Başarılı", Toast.LENGTH_SHORT)
                                            .show()

                                        val newIntent = Intent(this, MainActivity::class.java)
                                        startActivity(newIntent)
                                        finish()
                                    }.addOnFailureListener {
                                        Toast.makeText(this, "Bir Hata Oluştu", Toast.LENGTH_SHORT)
                                            .show()
                                    }

                            }.addOnFailureListener {
                                Toast.makeText(this, "Bir Hata Oluştu", Toast.LENGTH_SHORT).show()
                            }


                    } else {
                        kurumKoduEditText.error = "Bu Alan Boş Bırakılamaz"
                    }


                } else {
                    nameAndSurnameEditText.error = "Bu Alan Boş Bırakılamaz"
                }


            } else {

                if (nameAndSurnameEditText.text.toString().isNotEmpty()) {
                    nameAndSurnameEditText.error = null
                    nameAndSurname = nameAndSurnameEditText.text.toString()

                    if (kurumKoduEditText.text.toString().isNotEmpty()) {
                        kurumKoduEditText.error = null

                        if (gradeEditText.text.toString().isNotEmpty()) {
                            gradeEditText.error = null
                            grade = gradeEditText.text.toString().toInt()


                            val userData = hashMapOf(
                                "nameAndSurname" to nameAndSurname,
                                "subject" to branch,
                                "kurumKodu" to kurumKoduEditText.text.toString().toInt(),
                                "personType" to "Student",
                                "grade" to grade,
                                "teacher" to "",
                                "id" to documentID,
                                "email" to email
                            )


                            db.collection("User").document(documentID).set(userData)
                                .addOnSuccessListener {

                                    db.collection("School")
                                        .document(kurumKoduEditText.text.toString())
                                        .collection("Student").document(documentID).set(userData)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                this, "Kayıt Başarılı", Toast.LENGTH_SHORT
                                            ).show()

                                            val newIntent = Intent(this, MainActivity::class.java)
                                            startActivity(newIntent)
                                            finish()
                                        }.addOnFailureListener {
                                            Toast.makeText(
                                                this, "Bir Hata Oluştu", Toast.LENGTH_SHORT
                                            ).show()
                                        }

                                }.addOnFailureListener {
                                    Toast.makeText(this, "Bir Hata Oluştu", Toast.LENGTH_SHORT)
                                        .show()
                                }


                        } else {
                            gradeEditText.error = "Bu Alan Boş Bırakılamaz"
                        }


                    } else {
                        kurumKoduEditText.error = "Bu Alan Boş Bırakılamaz"
                    }


                } else {
                    nameAndSurnameEditText.error = "Bu Alan Boş Bırakılamaz"
                }


            }


        }


    }
}