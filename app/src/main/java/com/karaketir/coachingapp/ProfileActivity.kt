@file:Suppress("DEPRECATION")

package com.karaketir.coachingapp

import android.Manifest.permission.CAMERA
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.giphy.sdk.core.models.Media
import com.giphy.sdk.ui.GPHContentType
import com.giphy.sdk.ui.GPHSettings
import com.giphy.sdk.ui.Giphy
import com.giphy.sdk.ui.themes.GPHTheme
import com.giphy.sdk.ui.themes.GridType
import com.giphy.sdk.ui.views.GiphyDialogFragment
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.karaketir.coachingapp.databinding.ActivityProfileBinding
import com.karaketir.coachingapp.services.glide
import com.karaketir.coachingapp.services.openLink
import com.karaketir.coachingapp.services.placeHolderYap


class ProfileActivity : AppCompatActivity(), GiphyDialogFragment.GifSelectionListener {

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
    private var istenen: String = ""

    private lateinit var binding: ActivityProfileBinding
    private lateinit var storage: FirebaseStorage
    private lateinit var secilenGorsel: ImageView
    private lateinit var spaceRef: StorageReference

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        secilenGorsel = binding.secilenGorsel

        db.collection("UserPhotos").document(auth.uid.toString()).get().addOnSuccessListener {
            if (it.get("photoURL").toString() != " ") {
                secilenGorsel.visibility = View.VISIBLE
                secilenGorsel.glide(it.get("photoURL").toString(), placeHolderYap(this))
            } else {
                secilenGorsel.visibility = View.GONE
            }
        }.addOnFailureListener {
            secilenGorsel.visibility = View.GONE
        }


        val developerButton = binding.developerButtonProfile

        developerButton.setOnClickListener {
            openLink(
                "https://www.linkedin.com/in/furkankaraketir/", this
            )
        }

        storage = Firebase.storage
        db.collection("School").document("763455").collection("Student")
            .document("1BfRKDOOQmb3FAJqv6IQZpWyqyt1").get().addOnSuccessListener {
                Giphy.configure(this, it.get("giphyKey").toString())
            }
        val settings = GPHSettings(GridType.waterfall, GPHTheme.Dark)
        settings.mediaTypeConfig = arrayOf(GPHContentType.gif)


        val storageRef = storage.reference
        val imagesRef = storageRef.child("userBackgroundPhotos")

        val fileName = "${auth.uid.toString()}.jpg"
        spaceRef = imagesRef.child(fileName)

        val saveButton = binding.saveProfileButton
        val deleteUser = binding.deleteAccountButton
        val nameText = binding.currentNameTextView
        val gradeText = binding.currentGradeTextView
        val nameChangeEditText = binding.changeNameEditText
        val addPhoto = binding.addPhoto
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

        addPhoto.setOnClickListener {


            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("GIF ya da Resim")
            alertDialog.setMessage("Seçim Yapınız")
            alertDialog.setNegativeButton("GIF") { _, _ ->
                GiphyDialogFragment.newInstance(settings)
                    .show(supportFragmentManager, "giphy_dialog")
            }
            alertDialog.setPositiveButton("Resim") { _, _ ->
                if (ContextCompat.checkSelfPermission(
                        this, READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                        this, CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    //İzin Verilmedi, iste
                    ActivityCompat.requestPermissions(
                        this, arrayOf(READ_EXTERNAL_STORAGE, CAMERA), 1
                    )


                } else {
                    ImagePicker.with(this@ProfileActivity)
                        .crop(10f, 8f) //Crop square image, its same as crop(1f, 1f)
                        .start()
                }
            }
            alertDialog.show()


        }

        binding.removePhoto.setOnClickListener {
            db.collection("UserPhotos").document(auth.uid.toString()).update("photoURL", " ")
                .addOnSuccessListener {
                    Toast.makeText(this, "Resim Kaldırıldı", Toast.LENGTH_SHORT).show()
                    secilenGorsel.visibility = View.GONE
                }
            spaceRef.delete()
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                val uri: Uri = data?.data!!
                secilenGorsel.glide("", placeHolderYap(this))

                Toast.makeText(this, "Lütfen Bekleyiniz", Toast.LENGTH_SHORT).show()


                spaceRef.putFile(uri).addOnSuccessListener {

                    val yuklenenGorselReference =
                        FirebaseStorage.getInstance().reference.child("userBackgroundPhotos")
                            .child(auth.uid.toString() + ".jpg")

                    yuklenenGorselReference.downloadUrl.addOnSuccessListener { downloadURL ->

                        val refFile = hashMapOf("photoURL" to downloadURL.toString())

                        db.collection("UserPhotos").document(auth.uid.toString()).set(refFile)
                            .addOnSuccessListener {
                                Toast.makeText(this, "İşlem Başarılı", Toast.LENGTH_SHORT).show()
                                secilenGorsel.visibility = View.VISIBLE
                                secilenGorsel.setImageURI(uri)
                            }
                    }


                }

            }

            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun didSearchTerm(term: String) {

    }

    override fun onDismissed(selectedContentType: GPHContentType) {
    }

    override fun onGifSelected(
        media: Media, searchTerm: String?, selectedContentType: GPHContentType
    ) {

        val url = media.embedUrl!!
        val hepsi: List<String> = url.split('/')

        istenen = hepsi[hepsi.size - 1]
        val a = "https://media.giphy.com/media/$istenen/giphy.gif"
        secilenGorsel.glide(a, placeHolderYap(this))
        Toast.makeText(this, "Lütfen Bekleyiniz", Toast.LENGTH_SHORT).show()

        val newData = hashMapOf("photoURL" to a)
        db.collection("UserPhotos").document(auth.uid.toString()).set(newData)
            .addOnSuccessListener {
                secilenGorsel.visibility = View.VISIBLE

                Toast.makeText(this, "İşlem Başarılı!", Toast.LENGTH_SHORT).show()
            }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        //İzin Yeni Verildi
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImagePicker.with(this@ProfileActivity)
                    .crop(10f, 8f) //Crop square image, its same as crop(1f, 1f)
                    .start()
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}
