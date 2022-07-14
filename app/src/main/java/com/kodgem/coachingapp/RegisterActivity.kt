package com.kodgem.coachingapp

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
import com.kodgem.coachingapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private val alanlar = arrayOf("Dil", "Eşit Ağırlık", "Sözel", "Sayısal")
    private val dersler =
        arrayOf("Matematik", "Türk Dili ve Edebiyatı", "Fizik", "Kimya", "Biyoloji")

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


        signUpButton.setOnClickListener {
            if (emailEditText.text.toString().isNotEmpty()) {
                emailEditText.error = null

                if (passwordEditText.text.toString().isNotEmpty()) {
                    passwordEditText.error = null
                    if (gradeText.text.toString().isNotEmpty() || selection == 2) {
                        gradeText.error = null
                        if (nameAndSurnameEditText.text.toString().isNotEmpty()) {
                            nameAndSurnameEditText.error = null

                            nameAndSurname = nameAndSurnameEditText.text.toString()

                            grade = gradeText.text.toString().toInt()
                            signUp(emailEditText.text.toString(), passwordEditText.text.toString())

                        } else {
                            nameAndSurnameEditText.error = "Bu Alan Boş Bırakılamaz"

                        }


                    } else {
                        gradeText.error = "Bu Alan Boş Bırakılamaz"
                    }

                } else {
                    passwordEditText.error = "Bu Alan Boş Bırakılamaz"
                }
            } else {
                emailEditText.error = "Bu Alan Boş Bırakılamaz"
            }
        }


        val studentAdapter = ArrayAdapter(
            this@RegisterActivity,
            android.R.layout.simple_spinner_item, alanlar
        )

        val teacherAdapter = ArrayAdapter(
            this@RegisterActivity,
            android.R.layout.simple_spinner_item, dersler
        )

        teacherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        teacherSpinner.adapter = teacherAdapter
        teacherSpinner.onItemSelectedListener = this

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

    private fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    documentID = auth.uid!!

                    if (selection == 1) {
                        val user = hashMapOf(
                            "email" to email,
                            "grade" to grade,
                            "id" to documentID,
                            "nameAndSurname" to nameAndSurname,
                            "personType" to "Student",
                            "subjectType" to branch
                        )

                        db.collection("User").document(documentID)
                            .set(user)
                            .addOnSuccessListener {

                                db.collection("School").document("SchoolIDDDD")
                                    .collection("Student").document(documentID).set(user)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Başarılı", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, MainActivity::class.java)
                                        this.startActivity(intent)
                                        finish()
                                    }


                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
                            }
                    } else if (selection == 2) {
                        val user = hashMapOf(
                            "email" to email,
                            "id" to documentID,
                            "nameAndSurname" to nameAndSurname,
                            "personType" to "Teacher",
                            "subjectType" to branch
                        )

                        db.collection("User").document(documentID)
                            .set(user)
                            .addOnSuccessListener {

                                db.collection("School").document("SchoolIDDDD")
                                    .collection("Teacher").document(documentID).set(user)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Başarılı", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, MainActivity::class.java)
                                        this.startActivity(intent)
                                        finish()
                                    }


                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
                            }
                    }


                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        baseContext, "Kayıt Başarısız!",
                        Toast.LENGTH_SHORT
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
            when (position) {
                0 -> {
                    branch = "Matematik"
                }
                1 -> {
                    branch = "Türk Dili ve Edebiyatı"
                }
                2 -> {
                    branch = "Fizik"

                }
                3 -> {
                    branch = "Kimya"
                }
                4 -> {
                    branch = "Biyoloji"

                }
            }
        }


    }


    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

}