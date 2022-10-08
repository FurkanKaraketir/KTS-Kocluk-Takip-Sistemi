package com.karaketir.coachingapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var binding: ActivityProfileBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        val saveButton = binding.saveProfileButton
        val deleteUser = binding.deleteAccountButton
        val nameText = binding.currentNameTextView
        val gradeText = binding.currentGradeTextView
        val nameChangeEditText = binding.changeNameEditText
        val gradeChangeEditText = binding.changeGradeEditText

        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            nameText.text = "İsim: " + it.get("nameAndSurname").toString()
            if (it.get("personType").toString() == "Student") {
                gradeText.visibility = View.VISIBLE
                gradeChangeEditText.visibility = View.VISIBLE
                gradeText.text = "Sınıf: " + it.get("grade").toString()
            } else {
                gradeText.visibility = View.GONE
                gradeChangeEditText.visibility = View.GONE
            }

        }

        saveButton.setOnClickListener {

            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Kaydet")
            alertDialog.setMessage("Değişiklikleri Kaydetmek İstediğinize Emin misiniz?")
            alertDialog.setPositiveButton("Kaydet") { _, _ ->
                db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
                    val kurumKodu = it.get("kurumKodu").toString()
                    val personType = it.get("personType").toString()
                    if (nameChangeEditText.text.toString().isNotEmpty()) {


                        db.collection("User").document(auth.uid.toString())
                            .update("nameAndSurname", nameChangeEditText.text.toString())

                        db.collection("School").document(kurumKodu).collection(personType)
                            .document(auth.uid.toString())
                            .update("nameAndSurname", nameChangeEditText.text.toString())


                    }
                    if (gradeChangeEditText.text.toString().isNotEmpty()) {
                        db.collection("User").document(auth.uid.toString())
                            .update("grade", gradeChangeEditText.text.toString().toInt())

                        db.collection("School").document(kurumKodu).collection(personType)
                            .document(auth.uid.toString())
                            .update("grade", gradeChangeEditText.text.toString().toInt())
                    }
                    Toast.makeText(this, "İşlem Başarılı!", Toast.LENGTH_SHORT).show()
                    finish()

                }

            }
            alertDialog.setNegativeButton("İptal") { _, _ ->

            }
            alertDialog.show()

        }

        deleteUser.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Hesabı Sil")
            alertDialog.setMessage("Hesabınızı Silmek İstediğinize Emin misiniz?\nBu İşlem Geri Alınamaz!!")
            alertDialog.setPositiveButton("Sil") { _, _ ->
                db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
                    val kurumKodu = it.get("kurumKodu").toString()
                    val personType = it.get("personType").toString()

                    db.collection("School").document(kurumKodu).collection(personType)
                        .document(auth.uid.toString()).delete().addOnSuccessListener {
                            db.collection("User").document(auth.uid.toString()).delete()
                                .addOnSuccessListener {
                                    Firebase.auth.currentUser!!.delete().addOnSuccessListener {
                                        Toast.makeText(this, "İşlem Başarılı!", Toast.LENGTH_SHORT)
                                            .show()
                                        finish()
                                    }
                                }
                        }


                }


            }
            alertDialog.setNegativeButton("İptal") { _, _ ->

            }
            alertDialog.show()
        }


    }
}