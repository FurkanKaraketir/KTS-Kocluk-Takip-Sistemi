package com.karaketir.coachingapp

//noinspection SuspiciousImport
import android.R
import android.annotation.SuppressLint
import android.app.DatePickerDialog
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
import com.karaketir.coachingapp.databinding.ActivityAddDenemeTeacherBinding
import java.util.*

class AddDenemeTeacherActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

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



    private lateinit var binding: ActivityAddDenemeTeacherBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var turler = arrayOf("TYT", "AYT")
    private var secilenTur = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAddDenemeTeacherBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)


        val denemeAdiEditText = binding.denemeAdiEditText
        val denemeSinifEditText = binding.denemeSinifEditText
        val denemeTuruSpinner = binding.denemeTurSpinner
        val denemeSave = binding.saveDeneme
        val tarihSecButton = binding.denemeTarihSecButton

        tarihSecButton.setOnClickListener {


            val dpd = DatePickerDialog(this, { _, year2, monthOfYear, dayOfMonth ->
                tarihSecButton.text = ("Bitiş Tarihi: $dayOfMonth/${monthOfYear + 1}/$year2")
                c.set(year2, monthOfYear, dayOfMonth, 0, 0, 0)
            }, year, month, day)

            dpd.show()
        }

        val denemeAdapter = ArrayAdapter(
            this@AddDenemeTeacherActivity, R.layout.simple_spinner_item, turler
        )

        denemeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        denemeTuruSpinner.adapter = denemeAdapter
        denemeTuruSpinner.onItemSelectedListener = this



        denemeSave.setOnClickListener {
            val documentID = UUID.randomUUID().toString()

            if (denemeAdiEditText.text.toString().isNotEmpty()) {
                denemeAdiEditText.error = null

                if (denemeSinifEditText.text.toString().isNotEmpty()) {
                    denemeSinifEditText.error = null
                    val data = hashMapOf(
                        "denemeAdi" to denemeAdiEditText.text.toString(),
                        "grade" to denemeSinifEditText.text.toString().toInt(),
                        "id" to documentID,
                        "bitisTarihi" to c.time,
                        "tür" to secilenTur
                    )

                    db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
                        val kurumKodu = it.get("kurumKodu").toString().toInt()

                        db.collection("School").document(kurumKodu.toString()).collection("Teacher")
                            .document(auth.uid.toString()).collection("Denemeler")
                            .document(documentID).set(data).addOnSuccessListener {
                                Toast.makeText(this, "İşlem Başarılı!", Toast.LENGTH_SHORT).show()
                                finish()
                            }

                    }


                } else {
                    denemeSinifEditText.error = "Bu Alan Boş Bırakılamaz"

                }


            } else {
                denemeAdiEditText.error = "Bu Alan Boş Bırakılamaz"
            }

        }


    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

        when (p2) {

            0 -> {
                secilenTur = "TYT"
            }
            1 -> {
                secilenTur = "AYT"
            }

        }

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }
}