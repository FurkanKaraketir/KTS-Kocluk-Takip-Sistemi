package com.karaketir.coachingapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.giphy.sdk.ui.Giphy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.karaketir.coachingapp.databinding.ActivityProfileBinding
import com.karaketir.coachingapp.services.openLink


class ProfileActivity : AppCompatActivity() {

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

    private lateinit var binding: ActivityProfileBinding
    private lateinit var storage: FirebaseStorage
    private var kurumKodu = 0
    private var name = ""
    private var personType = ""
    private var grade = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        name = intent.getStringExtra("name").toString()
        grade = intent.getStringExtra("grade").toString().toInt()
        personType = intent.getStringExtra("personType").toString()



        kurumKodu = intent.getStringExtra("kurumKodu").toString().toInt()

        val developerButton = binding.developerButtonProfile

        developerButton.setOnClickListener {
            openLink(
                "https://www.linkedin.com/in/furkankaraketir/", this
            )
        }

        storage = Firebase.storage
        db.collection("VersionCode").document("giphyKey").get().addOnSuccessListener {
            Giphy.configure(this, it.get("key").toString())
        }


        val saveButton = binding.saveProfileButton
        val deleteUser = binding.deleteAccountButton
        val nameText = binding.currentNameTextView
        val gradeText = binding.currentGradeTextView
        val nameChangeEditText = binding.changeNameEditText
        val gradeChangeEditText = binding.changeGradeEditText

        nameText.text = "İsim: $name"
        if (personType == "Student") {
            gradeText.visibility = View.VISIBLE
            gradeChangeEditText.visibility = View.VISIBLE
            gradeText.text = "Sınıf: $grade"
        } else {
            gradeText.visibility = View.GONE
            gradeChangeEditText.visibility = View.GONE
        }



        saveButton.setOnClickListener {

            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Kaydet")
            alertDialog.setMessage("Değişiklikleri Kaydetmek İstediğinize Emin misiniz?")
            alertDialog.setPositiveButton("Kaydet") { _, _ ->

                if (nameChangeEditText.text.toString().isNotEmpty()) {


                    db.collection("User").document(auth.uid.toString())
                        .update("nameAndSurname", nameChangeEditText.text.toString())

                    db.collection("School").document(kurumKodu.toString()).collection(personType)
                        .document(auth.uid.toString())
                        .update("nameAndSurname", nameChangeEditText.text.toString())


                }
                if (gradeChangeEditText.text.toString().isNotEmpty()) {
                    db.collection("User").document(auth.uid.toString())
                        .update("grade", gradeChangeEditText.text.toString().toInt())

                    db.collection("School").document(kurumKodu.toString()).collection(personType)
                        .document(auth.uid.toString())
                        .update("grade", gradeChangeEditText.text.toString().toInt())
                }
                Toast.makeText(this, "İşlem Başarılı!", Toast.LENGTH_SHORT).show()
                finish()


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

                db.collection("School").document(kurumKodu.toString()).collection(personType)
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
            alertDialog.setNegativeButton("İptal") { _, _ ->

            }
            alertDialog.show()
        }


    }


}
