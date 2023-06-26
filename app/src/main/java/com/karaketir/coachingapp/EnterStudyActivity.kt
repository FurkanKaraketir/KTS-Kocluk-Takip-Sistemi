package com.karaketir.coachingapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.databinding.ActivityEnterStudyBinding
import java.util.*
import kotlin.collections.ArrayList

@Suppress("UNCHECKED_CAST")
class EnterStudyActivity : AppCompatActivity() {

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


    private lateinit var binding: ActivityEnterStudyBinding
    private lateinit var auth: FirebaseAuth
    private var konuAdlari = ArrayList<String>()
    private var values = arrayOf(
        "0",
        "30",
        "40",
        "50",
        "60",
        "70",
        "80",
        "90",
        "100",
        "110",
        "120",
        "130",
        "140",
        "150",
        "160",
        "170",
        "180"
    )


    private var konuDk = 0
    private var soruDk = 0
    private var soruSayi = 0

    private lateinit var db: FirebaseFirestore
    private var secilenKonu = ""
    private var secilenDocumentID = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterStudyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore

        setupNumberPickerForStringValues()


        val spinner = binding.studySpinner
        val documentID = UUID.randomUUID().toString()
        val subjectType = intent.getStringExtra("studyType")
        val dersAdi = intent.getStringExtra("dersAdi")
        secilenKonu = ""
        db.collection("Lessons").document(dersAdi.toString()).collection(subjectType.toString())
            .orderBy("konuAdi", Query.Direction.ASCENDING).addSnapshotListener { value, _ ->
                if (value != null) {
                    konuAdlari.clear()
                    for (document in value) {
                        try {
                            val arrayType = document.get("arrayType") as ArrayList<String>
                            if ("konu" in arrayType) {
                                val konuAdi = document.get("konuAdi").toString()
                                konuAdlari.add(konuAdi)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
                        }


                    }
                    val studyAdapter = ArrayAdapter(
                        this@EnterStudyActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        konuAdlari
                    )

                    studyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = studyAdapter
                    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            p0: AdapterView<*>?, p1: View?, position: Int, p3: Long
                        ) {
                            secilenKonu = konuAdlari[position]


                            db.collection("Lessons").document(dersAdi.toString())
                                .collection(subjectType.toString())
                                .whereEqualTo("konuAdi", secilenKonu)
                                .addSnapshotListener { value1, _ ->
                                    if (value1 != null) {
                                        for (document in value1) {
                                            secilenDocumentID = document.get("id").toString()
                                        }
                                    }
                                }

                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {

                        }

                    }


                }
            }


        val subjectTypeTitle = binding.subjectTypeTitle
        val studySaveButton = binding.studySaveButton





        subjectTypeTitle.text = "Tür: $subjectType"

        studySaveButton.setOnClickListener {
            studySaveButton.isClickable = false
            var stopper = false
            var stopper2 = false
            val cal = Calendar.getInstance()
            val studyTime = Calendar.getInstance()
            studyTime.add(Calendar.HOUR_OF_DAY, -5)

            cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

            cal.clear(Calendar.MINUTE)
            cal.clear(Calendar.SECOND)
            cal.clear(Calendar.MILLISECOND)


            val study = hashMapOf(
                "id" to documentID,
                "timestamp" to studyTime.time,
                "konuAnlatımı" to konuDk,
                "konuTestiDK" to soruDk,
                "dersAdi" to dersAdi,
                "tür" to subjectType,
                "konuAdi" to secilenKonu,
                "toplamCalisma" to konuDk + soruDk,
                "çözülenSoru" to soruSayi
            )

            val baslangicTarihi = cal.time


            cal.add(Calendar.DAY_OF_YEAR, 1)
            val bitisTarihi = cal.time
            Toast.makeText(
                this, "Lütfen Bekleyiniz...", Toast.LENGTH_SHORT
            ).show()
            var kurumKodu: Int
            db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener { it2 ->
                kurumKodu = it2.get("kurumKodu").toString().toInt()
                db.collection("School").document(kurumKodu.toString()).collection("Student")
                    .document(auth.uid.toString()).collection("Studies")
                    .whereEqualTo("dersAdi", dersAdi).whereEqualTo("tür", subjectType)
                    .whereEqualTo("konuAdi", secilenKonu)
                    .whereGreaterThan("timestamp", baslangicTarihi)
                    .whereLessThan("timestamp", bitisTarihi).addSnapshotListener { value, error ->
                        if (error != null) {
                            println(error.localizedMessage)
                        }
                        if (!stopper) {
                            if (value != null) {
                                if (!value.isEmpty) {
                                    for (document in value) {
                                        val studyUpdate = hashMapOf(
                                            "id" to document.id,
                                            "timestamp" to studyTime.time,
                                            "konuAnlatımı" to konuDk + document.get(
                                                "konuAnlatımı"
                                            ).toString().toInt(),
                                            "konuTestiDK" to soruDk + document.get("konuTestiDK")
                                                .toString().toInt(),
                                            "tür" to subjectType,
                                            "dersAdi" to dersAdi,
                                            "konuAdi" to secilenKonu,
                                            "toplamCalisma" to konuDk + soruDk + document.get("konuAnlatımı")
                                                .toString().toInt() + document.get("konuTestiDK")
                                                .toString().toInt(),
                                            "çözülenSoru" to soruSayi + document.get("çözülenSoru")
                                                .toString().toInt()
                                        )
                                        stopper = true

                                        db.collection("School").document(kurumKodu.toString())
                                            .collection("Student").document(auth.uid.toString())
                                            .collection("Studies").document(document.id)
                                            .update(studyUpdate as Map<String, Any>)
                                            .addOnSuccessListener {

                                                if (!stopper2) {
                                                    db.collection("School")
                                                        .document(kurumKodu.toString())
                                                        .collection("Student")
                                                        .document(auth.uid.toString())
                                                        .collection("Duties").whereGreaterThan(
                                                            "bitisZamani", studyTime.time
                                                        ).whereEqualTo(
                                                            "dersAdi", dersAdi
                                                        ).whereEqualTo(
                                                            "tür", subjectType
                                                        ).whereEqualTo(
                                                            "konuAdi", secilenKonu
                                                        ).addSnapshotListener { value5, e5 ->


                                                            if (!stopper2) {
                                                                if (e5 != null) println(
                                                                    e5.localizedMessage
                                                                )

                                                                if (value5 != null) {
                                                                    for (document5 in value5) {


                                                                        val gorevUpdate = hashMapOf(
                                                                            "toplamCalisma" to document5.get(
                                                                                "toplamCalisma"
                                                                            ).toString()
                                                                                .toInt() - (konuDk + soruDk),
                                                                            "çözülenSoru" to document5.get(
                                                                                "çözülenSoru"
                                                                            ).toString()
                                                                                .toInt() - soruSayi
                                                                        )

                                                                        if (!stopper2) {
                                                                            stopper2 = true

                                                                            db.collection(
                                                                                "School"
                                                                            ).document(
                                                                                kurumKodu.toString()
                                                                            ).collection(
                                                                                "Student"
                                                                            ).document(
                                                                                auth.uid.toString()
                                                                            ).collection(
                                                                                "Duties"
                                                                            ).document(
                                                                                document5.id
                                                                            ).update(
                                                                                gorevUpdate as Map<String, Any>
                                                                            ).addOnSuccessListener {

                                                                                if (document5.get(
                                                                                        "toplamCalisma"
                                                                                    ).toString()
                                                                                        .toInt() - (konuDk + soruDk) <= 0 && document5.get(
                                                                                        "çözülenSoru"
                                                                                    ).toString()
                                                                                        .toInt() - soruSayi <= 0
                                                                                ) {
                                                                                    db.collection(
                                                                                        "School"
                                                                                    ).document(
                                                                                        kurumKodu.toString()
                                                                                    ).collection(
                                                                                        "Student"
                                                                                    ).document(
                                                                                        auth.uid.toString()
                                                                                    ).collection(
                                                                                        "Duties"
                                                                                    ).document(
                                                                                        document5.id
                                                                                    ).update(
                                                                                        "tamamlandi",
                                                                                        true
                                                                                    )
                                                                                        .addOnSuccessListener {
                                                                                            stopper2 =
                                                                                                true

                                                                                            Toast.makeText(
                                                                                                this,
                                                                                                "İşlem Başarılı!",
                                                                                                Toast.LENGTH_SHORT
                                                                                            ).show()
                                                                                            finish()
                                                                                        }
                                                                                } else {
                                                                                    stopper2 = true
                                                                                    Toast.makeText(
                                                                                        this,
                                                                                        "İşlem Başarılı!",
                                                                                        Toast.LENGTH_SHORT
                                                                                    ).show()
                                                                                    finish()
                                                                                }

                                                                            }

                                                                        }
                                                                    }
                                                                }
                                                            }


                                                        }
                                                }
                                                Toast.makeText(
                                                    this, "İşlem Başarılı!", Toast.LENGTH_SHORT
                                                ).show()
                                                finish()


                                            }


                                    }
                                } else {
                                    stopper = true
                                    db.collection("School").document(kurumKodu.toString())
                                        .collection("Student").document(auth.uid.toString())
                                        .collection("Studies").document(documentID).set(study)
                                        .addOnSuccessListener {

                                            if (!stopper2) {
                                                db.collection("School")
                                                    .document(kurumKodu.toString())
                                                    .collection("Student")
                                                    .document(auth.uid.toString())
                                                    .collection("Duties").whereGreaterThan(
                                                        "bitisZamani", studyTime.time
                                                    ).whereEqualTo(
                                                        "dersAdi", dersAdi
                                                    ).whereEqualTo(
                                                        "tür", subjectType
                                                    ).whereEqualTo(
                                                        "konuAdi", secilenKonu
                                                    ).addSnapshotListener { value5, e5 ->


                                                        if (!stopper2) {
                                                            if (e5 != null) println(
                                                                e5.localizedMessage
                                                            )

                                                            if (value5 != null) {
                                                                for (document5 in value5) {


                                                                    val gorevUpdate = hashMapOf(
                                                                        "toplamCalisma" to document5.get(
                                                                            "toplamCalisma"
                                                                        ).toString()
                                                                            .toInt() - (konuDk + soruDk),
                                                                        "çözülenSoru" to document5.get(
                                                                            "çözülenSoru"
                                                                        ).toString()
                                                                            .toInt() - soruSayi
                                                                    )

                                                                    if (!stopper2) {
                                                                        stopper2 = true

                                                                        db.collection(
                                                                            "School"
                                                                        ).document(
                                                                            kurumKodu.toString()
                                                                        ).collection(
                                                                            "Student"
                                                                        ).document(
                                                                            auth.uid.toString()
                                                                        ).collection(
                                                                            "Duties"
                                                                        ).document(
                                                                            document5.id
                                                                        ).update(
                                                                            gorevUpdate as Map<String, Any>
                                                                        ).addOnSuccessListener {

                                                                            if (document5.get(
                                                                                    "toplamCalisma"
                                                                                ).toString()
                                                                                    .toInt() - (konuDk + soruDk) <= 0 && document5.get(
                                                                                    "çözülenSoru"
                                                                                ).toString()
                                                                                    .toInt() - soruSayi <= 0
                                                                            ) {
                                                                                db.collection(
                                                                                    "School"
                                                                                ).document(
                                                                                    kurumKodu.toString()
                                                                                ).collection(
                                                                                    "Student"
                                                                                ).document(
                                                                                    auth.uid.toString()
                                                                                ).collection(
                                                                                    "Duties"
                                                                                ).document(
                                                                                    document5.id
                                                                                ).update(
                                                                                    "tamamlandi",
                                                                                    true
                                                                                )
                                                                                    .addOnSuccessListener {
                                                                                        stopper2 =
                                                                                            true

                                                                                        Toast.makeText(
                                                                                            this,
                                                                                            "İşlem Başarılı!",
                                                                                            Toast.LENGTH_SHORT
                                                                                        ).show()
                                                                                        finish()
                                                                                    }
                                                                            } else {
                                                                                stopper2 = true
                                                                                Toast.makeText(
                                                                                    this,
                                                                                    "İşlem Başarılı!",
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()
                                                                                finish()
                                                                            }

                                                                        }

                                                                    }
                                                                }
                                                            }
                                                        }


                                                    }
                                            }

                                            Toast.makeText(
                                                this, "İşlem Başarılı!", Toast.LENGTH_SHORT
                                            ).show()
                                            finish()


                                        }


                                        .addOnFailureListener {
                                            println(it.localizedMessage)
                                        }


                                }


                            } else {

                                db.collection("School").document(kurumKodu.toString())
                                    .collection("Student").document(auth.uid.toString())
                                    .collection("Studies").document(documentID).set(study)
                                    .addOnSuccessListener {

                                        db.collection("School").document(kurumKodu.toString())
                                            .collection("Student").document(auth.uid.toString())
                                            .collection("Duties").whereGreaterThan(
                                                "bitisZamani", studyTime.time
                                            ).whereEqualTo("dersAdi", dersAdi)
                                            .whereEqualTo("tür", subjectType).whereEqualTo(
                                                "konuAdi", secilenKonu
                                            ).addSnapshotListener { value5, e5 ->
                                                if (e5 != null) println(e5.localizedMessage)

                                                if (!stopper2) {
                                                    if (value5 != null) {
                                                        for (document5 in value5) {

                                                            stopper2 = true
                                                            val gorevUpdate = hashMapOf(
                                                                "toplamCalisma" to document5.get(
                                                                    "toplamCalisma"
                                                                ).toString()
                                                                    .toInt() - (konuDk + soruDk),
                                                                "çözülenSoru" to document5.get(
                                                                    "çözülenSoru"
                                                                ).toString().toInt() - soruSayi
                                                            )
                                                            db.collection("School").document(
                                                                kurumKodu.toString()
                                                            ).collection("Student")
                                                                .document(auth.uid.toString())
                                                                .collection("Duties").document(
                                                                    document5.id
                                                                ).update(
                                                                    gorevUpdate as Map<String, Any>
                                                                ).addOnSuccessListener {
                                                                    if (document5.get(
                                                                            "toplamCalisma"
                                                                        ).toString()
                                                                            .toInt() - (konuDk + soruDk) <= 0 && document5.get(
                                                                            "çözülenSoru"
                                                                        ).toString()
                                                                            .toInt() - soruSayi <= 0
                                                                    ) {
                                                                        db.collection(
                                                                            "School"
                                                                        ).document(
                                                                            kurumKodu.toString()
                                                                        ).collection(
                                                                            "Student"
                                                                        ).document(
                                                                            auth.uid.toString()
                                                                        ).collection(
                                                                            "Duties"
                                                                        ).document(
                                                                            document5.id
                                                                        ).update(
                                                                            "tamamlandi", true
                                                                        ).addOnSuccessListener {
                                                                            Toast.makeText(
                                                                                this,
                                                                                "İşlem Başarılı!",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                            finish()
                                                                        }
                                                                    } else {

                                                                        finish()
                                                                    }
                                                                }


                                                        }
                                                    }

                                                }


                                            }


                                    }.addOnFailureListener {
                                        println(it.localizedMessage)
                                    }
                            }

                        }
                        Toast.makeText(
                            this, "İşlem Başarılı", Toast.LENGTH_SHORT
                        ).show()

                        finish()


                    }
            }


        }

    }


    private fun setupNumberPickerForStringValues() {
        val numberPicker = binding.numberPickerKonuDk
        numberPicker.minValue = 0
        numberPicker.maxValue = values.size - 1
        numberPicker.displayedValues = values
        numberPicker.wrapSelectorWheel = true
        numberPicker.setOnValueChangedListener { _, _, newVal ->
            konuDk = values[newVal].toInt()
        }

        val soruDkPicker = binding.numberPickerSoruDk
        soruDkPicker.minValue = 0
        soruDkPicker.maxValue = values.size - 1
        soruDkPicker.displayedValues = values
        soruDkPicker.wrapSelectorWheel = true
        soruDkPicker.setOnValueChangedListener { _, _, newVal ->
            soruDk = values[newVal].toInt()
        }

        val soruSayiPicker = binding.numberPickerSoruSayi
        soruSayiPicker.minValue = 0
        soruSayiPicker.maxValue = values.size - 1
        soruSayiPicker.displayedValues = values
        soruSayiPicker.wrapSelectorWheel = true
        soruSayiPicker.setOnValueChangedListener { _, _, newVal ->
            soruSayi = values[newVal].toInt()
        }


    }


}
