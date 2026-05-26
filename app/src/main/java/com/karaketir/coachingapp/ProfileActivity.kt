package com.karaketir.coachingapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.giphy.sdk.ui.Giphy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.karaketir.coachingapp.curriculum.CurriculumProgram
import com.karaketir.coachingapp.curriculum.GradeCurriculumRepository
import com.karaketir.coachingapp.curriculum.StudyLabels
import com.karaketir.coachingapp.databinding.ActivityProfileBinding
import com.karaketir.coachingapp.services.openLink
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ProfileActivity : AppCompatActivity() {

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
        grade = intent.getStringExtra("grade")?.toIntOrNull() ?: 0
        personType = intent.getStringExtra("personType").toString()



        kurumKodu = intent.getStringExtra("kurumKodu")?.toIntOrNull() ?: 0

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
            loadCurriculumProgramSubtitle(grade)
        } else {
            gradeText.visibility = View.GONE
            gradeChangeEditText.visibility = View.GONE
            binding.curriculumProgramText.visibility = View.GONE
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

    private fun loadCurriculumProgramSubtitle(studentGrade: Int) {
        lifecycleScope.launch {
            val config = try {
                GradeCurriculumRepository.load(db)
            } catch (_: Exception) {
                GradeCurriculumRepository.defaultConfig()
            }
            val profileProgram = try {
                val snap = db.collection("User").document(auth.uid.toString()).get().await()
                CurriculumProgram.fromFirestore(snap.getString("curriculumProgram"))
            } catch (_: Exception) {
                null
            }
            val program = GradeCurriculumRepository.preferredProgram(
                config,
                studentGrade,
                profileProgram,
            )
            binding.curriculumProgramText.visibility = View.VISIBLE
            binding.curriculumProgramText.text =
                StudyLabels.programDisplayName(program, studentGrade)
        }
    }

}
