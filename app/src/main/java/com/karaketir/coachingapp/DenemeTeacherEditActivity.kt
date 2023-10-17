package com.karaketir.coachingapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.databinding.ActivityDenemeTeacherEditBinding
import java.text.SimpleDateFormat
import java.util.*

class DenemeTeacherEditActivity : AppCompatActivity() {

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


    private lateinit var binding: ActivityDenemeTeacherEditBinding
    private lateinit var db: FirebaseFirestore
    private var kurumKodu = 0
    private lateinit var auth: FirebaseAuth

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDenemeTeacherEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
        kurumKodu = intent.getStringExtra("kurumKodu").toString().toInt()

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)


        val intent = intent
        val denemeID = intent.getStringExtra("denemeID").toString()
        binding.denemeTitle.text = intent.getStringExtra("denemeAdi").toString()




        db.collection("School").document(kurumKodu.toString()).collection("Teacher")
            .document(auth.uid.toString()).collection("Denemeler").document(denemeID).get()
            .addOnSuccessListener {
                val denemeTarihi = it.get("bitisTarihi") as Timestamp

                val dateFormated =
                    SimpleDateFormat("dd/MM/yyyy").format(denemeTarihi.toDate())

                binding.denemeEditPickDate.text = "Bitiş Tarihi: $dateFormated"
                binding.denemeEditPickDate.setOnClickListener {


                    val dpd = DatePickerDialog(this, { _, year2, monthOfYear, dayOfMonth ->
                        binding.denemeEditPickDate.text =
                            ("Bitiş Tarihi: $dayOfMonth /${monthOfYear + 1}/$year2")
                        c.set(year2, monthOfYear, dayOfMonth, 0, 0, 0)
                    }, year, month, day)

                    dpd.show()
                }


            }



        binding.denemeEditSave.setOnClickListener {
            val updateData = hashMapOf<String, Any>("bitisTarihi" to c.time)


            db.collection("School").document(kurumKodu.toString()).collection("Teacher")
                .document(auth.uid.toString()).collection("Denemeler").document(denemeID)
                .update(updateData).addOnSuccessListener {
                    Toast.makeText(this, "İşlem Başarılı!", Toast.LENGTH_SHORT).show()
                    finish()
                }


        }


    }
}