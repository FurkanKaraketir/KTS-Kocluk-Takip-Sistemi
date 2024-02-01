package com.karaketir.coachingapp.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.databinding.FragmentSettingsBinding
import com.karaketir.coachingapp.services.openLink

class SettingsFragment : Fragment() {

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

    private var kurumKodu = 0
    private var name = ""
    private var personType = ""
    private var grade = 0

    private var _binding: FragmentSettingsBinding? = null
    private var isViewCreated = false

    // This property is only valid between onCreateView and
// onDestroyView.


    private val binding get() = _binding ?: throw IllegalStateException("Binding is null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        isViewCreated = false
    }

    private fun isBindingAvailable(): Boolean {
        return isViewCreated && _binding != null
    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        db = Firebase.firestore
        isViewCreated = true

        if (isBindingAvailable()) {
            val mBinding = binding

            db.collection("User").document(auth.uid.toString()).get()
                .addOnSuccessListener { snapshot ->
                    name = snapshot.get("nameAndSurname").toString()
                    grade = try {
                        snapshot.get("grade").toString().toInt()
                    } catch (e: Exception) {
                        0
                    }
                    personType = snapshot.get("personType").toString()
                    kurumKodu = snapshot.get("kurumKodu").toString().toInt()


                    val developerButton = mBinding.developerButtonProfile

                    developerButton.setOnClickListener {
                        openLink(
                            "https://www.linkedin.com/in/furkankaraketir/", requireContext()
                        )
                    }


                    val saveButton = mBinding.saveProfileButton
                    val deleteUser = mBinding.deleteAccountButton
                    val nameText = mBinding.currentNameTextView
                    val gradeText = mBinding.currentGradeTextView
                    val nameChangeEditText = mBinding.changeNameEditText
                    val gradeChangeEditText = mBinding.changeGradeEditText

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

                        val alertDialog = AlertDialog.Builder(requireActivity())
                        alertDialog.setTitle("Kaydet")
                        alertDialog.setMessage("Değişiklikleri Kaydetmek İstediğinize Emin misiniz?")
                        alertDialog.setPositiveButton("Kaydet") { _, _ ->

                            if (nameChangeEditText.text.toString().isNotEmpty()) {


                                db.collection("User").document(auth.uid.toString())
                                    .update("nameAndSurname", nameChangeEditText.text.toString())

                                db.collection("School").document(kurumKodu.toString())
                                    .collection(personType).document(auth.uid.toString())
                                    .update("nameAndSurname", nameChangeEditText.text.toString())


                            }
                            if (gradeChangeEditText.text.toString().isNotEmpty()) {
                                db.collection("User").document(auth.uid.toString())
                                    .update("grade", gradeChangeEditText.text.toString().toInt())

                                db.collection("School").document(kurumKodu.toString())
                                    .collection(personType).document(auth.uid.toString())
                                    .update("grade", gradeChangeEditText.text.toString().toInt())
                            }
                            Toast.makeText(requireContext(), "İşlem Başarılı!", Toast.LENGTH_SHORT)
                                .show()


                        }
                        alertDialog.setNegativeButton("İptal") { _, _ ->

                        }
                        alertDialog.show()

                    }

                    deleteUser.setOnClickListener {
                        val alertDialog = AlertDialog.Builder(requireActivity())
                        alertDialog.setTitle("Hesabı Sil")
                        alertDialog.setMessage("Hesabınızı Silmek İstediğinize Emin misiniz?\nBu İşlem Geri Alınamaz!!")
                        alertDialog.setPositiveButton("Sil") { _, _ ->

                            db.collection("School").document(kurumKodu.toString())
                                .collection(personType).document(auth.uid.toString()).delete()
                                .addOnSuccessListener {
                                    db.collection("User").document(auth.uid.toString()).delete()
                                        .addOnSuccessListener {
                                            Firebase.auth.currentUser!!.delete()
                                                .addOnSuccessListener {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "İşlem Başarılı!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
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


    }
}