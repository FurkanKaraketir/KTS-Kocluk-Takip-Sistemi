package com.karaketir.coachingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.databinding.ActivityStudentClassUpdateBinding

class StudentClassUpdateActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var binding: ActivityStudentClassUpdateBinding
    private var name = ""
    private var kurumKodu = 0
    private var grade = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentClassUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore


        val nameEdit = binding.nameAndSurnameEditText
        val gradeEdit = binding.gradeEditText
        val saveButton = binding.signUpButton

        name = intent.getStringExtra("name").toString()
        grade = intent.getStringExtra("grade").toString().toInt()
        kurumKodu = intent.getStringExtra("kurumKodu").toString().toInt()


        nameEdit.setText(name)
        gradeEdit.setText(grade.toString())

        saveButton.setOnClickListener {

            name = nameEdit.text.toString()
            grade = gradeEdit.text.toString().toInt()


            val data = hashMapOf(
                "nameAndSurname" to name, "grade" to grade
            )

            db.collection("User").document(intent.getStringExtra("id").toString())
                .update(data as Map<String, Any>).addOnSuccessListener {
                    db.collection("School").document(kurumKodu.toString()).collection("Student")
                        .document(intent.getStringExtra("id").toString()).update(
                            data as Map<String, Any>
                        ).addOnSuccessListener {
                            Toast.makeText(this, "İşlem Başarılı", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                }


        }


    }
}