package com.karaketir.coachingapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.databinding.ActivityAddPrgrphPrblmBinding
import com.karaketir.coachingapp.services.WorldTimeApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class AddPrgrphPrblmActivity : AppCompatActivity() {

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


    private lateinit var binding: ActivityAddPrgrphPrblmBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var soruDk = 0
    private var soruSayi = 0
    private var kurumKodu = 763455


    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAddPrgrphPrblmBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        val cal = Calendar.getInstance()
        var studyTime: Calendar
        val cal2 = Calendar.getInstance()
        val paragrafTitle = binding.paragrafTitle
        val paragrafSaveButton = binding.paragrafSaveButton
        val currentTestsMinutesEditText = binding.paragrafDkEditText
        val currentTestsEditText = binding.paragrafSoruEditText
        val tarihSecButton = binding.studyDateButton

        val intent = intent
        val documentID = UUID.randomUUID().toString()
        val subjectType = "TYT"

        val dersAdi = intent.getStringExtra("dersAdi").toString()
        kurumKodu = intent.getStringExtra("kurumKodu").toString().toInt()
        val retrofit = Retrofit.Builder().baseUrl("http://worldtimeapi.org")
            .addConverterFactory(GsonConverterFactory.create()).build()

        val worldTimeApi = retrofit.create(WorldTimeApi::class.java)

        lifecycleScope.launch {
            val currentTime = try {
                worldTimeApi.getCurrentTime()
            } catch (e: Exception) {
                println(e.localizedMessage)
                null
            }




            tarihSecButton.setOnClickListener {

                val year = cal2.get(Calendar.YEAR)
                val month = cal2.get(Calendar.MONTH)
                val day = cal2.get(Calendar.DAY_OF_MONTH)

                val dpd = DatePickerDialog(this@AddPrgrphPrblmActivity, { _, year2, monthOfYear, dayOfMonth ->
                    tarihSecButton.text = ("Çalışma Tarihi: $dayOfMonth/${monthOfYear + 1}/$year2")
                    cal.set(year2, monthOfYear, dayOfMonth, 0, 0, 0)
                    cal2.set(year2, monthOfYear, dayOfMonth, 0, 0, 0)
                }, year, month, day)

                dpd.show()
            }

            if (currentTime != null) {
                //turn string to date
                val date = currentTime.datetime.split("T")[0]
                val time = currentTime.datetime.split("T")[1].split(".")[0]
                cal[Calendar.YEAR] = date.split("-")[0].toInt()
                cal[Calendar.MONTH] = date.split("-")[1].toInt() - 1
                cal[Calendar.DAY_OF_MONTH] = date.split("-")[2].toInt()
                cal[Calendar.HOUR_OF_DAY] = time.split(":")[0].toInt()
                cal[Calendar.MINUTE] = time.split(":")[1].toInt()
                cal[Calendar.SECOND] = time.split(":")[2].toInt()

                cal2[Calendar.YEAR] = date.split("-")[0].toInt()
                cal2[Calendar.MONTH] = date.split("-")[1].toInt() - 1
                cal2[Calendar.DAY_OF_MONTH] = date.split("-")[2].toInt()
                cal2[Calendar.HOUR_OF_DAY] = time.split(":")[0].toInt()
                cal2[Calendar.MINUTE] = time.split(":")[1].toInt()
                cal2[Calendar.SECOND] = time.split(":")[2].toInt()
                tarihSecButton.text =
                    ("Çalışma Tarihi: ${cal2[Calendar.DAY_OF_MONTH]}/${cal[Calendar.MONTH] + 1}/${cal2[Calendar.YEAR]}")

            } else {
                println("null")
            }
            paragrafTitle.text = dersAdi

            paragrafSaveButton.setOnClickListener {
                paragrafSaveButton.isClickable = false

                var stopper = false
                var stopper2 = false
                studyTime = cal2

                cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                cal.clear(Calendar.MINUTE)
                cal.clear(Calendar.SECOND)
                cal.clear(Calendar.MILLISECOND)

                if (currentTestsMinutesEditText.text.toString().isNotEmpty()) {
                    currentTestsMinutesEditText.error = null
                    soruDk = currentTestsMinutesEditText.text.toString().toInt()

                    if (currentTestsEditText.text.toString().isNotEmpty()) {
                        currentTestsEditText.error = null

                        soruSayi = currentTestsEditText.text.toString().toInt()

                        val study = hashMapOf(
                            "id" to documentID,
                            "timestamp" to studyTime.time,
                            "konuAnlatımı" to 0,
                            "konuTestiDK" to soruDk,
                            "dersAdi" to dersAdi,
                            "tür" to subjectType,
                            "konuAdi" to dersAdi,
                            "toplamCalisma" to soruDk,
                            "çözülenSoru" to soruSayi
                        )

                        val baslangicTarihi = cal.time


                        cal.add(Calendar.DAY_OF_YEAR, 1)
                        val bitisTarihi = cal.time
                        Toast.makeText(
                            this@AddPrgrphPrblmActivity, "Lütfen Bekleyiniz...", Toast.LENGTH_SHORT
                        ).show()

                        db.collection("School").document(kurumKodu.toString()).collection("Student")
                            .document(auth.uid.toString()).collection("Studies")
                            .whereEqualTo("konuAdi", dersAdi)
                            .whereGreaterThan("timestamp", baslangicTarihi)
                            .whereLessThan("timestamp", bitisTarihi)
                            .addSnapshotListener { value, _ ->

                                if (!stopper) {
                                    if (value != null) {
                                        if (!value.isEmpty) {
                                            for (document in value) {
                                                val studyUpdate = hashMapOf(
                                                    "id" to document.id,
                                                    "timestamp" to studyTime.time,
                                                    "konuAnlatımı" to document.get("konuAnlatımı")
                                                        .toString().toInt(),
                                                    "konuTestiDK" to soruDk + document.get("konuTestiDK")
                                                        .toString().toInt(),
                                                    "tür" to subjectType,
                                                    "dersAdi" to dersAdi,
                                                    "konuAdi" to dersAdi,
                                                    "toplamCalisma" to soruDk + document.get("konuAnlatımı")
                                                        .toString()
                                                        .toInt() + document.get("konuTestiDK")
                                                        .toString().toInt(),
                                                    "çözülenSoru" to soruSayi + document.get("çözülenSoru")
                                                        .toString().toInt()
                                                )
                                                stopper = true

                                                db.collection("School")
                                                    .document(kurumKodu.toString())
                                                    .collection("Student")
                                                    .document(auth.uid.toString())
                                                    .collection("Studies").document(document.id)
                                                    .update(studyUpdate as Map<String, Any>)
                                                    .addOnSuccessListener {

                                                        if (!stopper2) {
                                                            db.collection("School")
                                                                .document(kurumKodu.toString())
                                                                .collection("Student")
                                                                .document(auth.uid.toString())
                                                                .collection("Duties")
                                                                .whereGreaterThan(
                                                                    "bitisZamani", studyTime.time
                                                                ).whereEqualTo(
                                                                    "dersAdi", dersAdi
                                                                ).whereEqualTo(
                                                                    "tür", subjectType
                                                                ).whereEqualTo(
                                                                    "konuAdi", dersAdi
                                                                )
                                                                .addSnapshotListener { value5, e5 ->


                                                                    if (!stopper2) {
                                                                        if (e5 != null) println(
                                                                            e5.localizedMessage
                                                                        )

                                                                        if (value5 != null) {
                                                                            for (document5 in value5) {


                                                                                val gorevUpdate =
                                                                                    hashMapOf(
                                                                                        "toplamCalisma" to document5.get(
                                                                                            "toplamCalisma"
                                                                                        ).toString()
                                                                                            .toInt() - (soruDk),
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
                                                                                    )
                                                                                        .addOnSuccessListener {

                                                                                            if (document5.get(
                                                                                                    "toplamCalisma"
                                                                                                )
                                                                                                    .toString()
                                                                                                    .toInt() - (soruDk) <= 0 && document5.get(
                                                                                                    "çözülenSoru"
                                                                                                )
                                                                                                    .toString()
                                                                                                    .toInt() - soruSayi <= 0
                                                                                            ) {
                                                                                                db.collection(
                                                                                                    "School"
                                                                                                )
                                                                                                    .document(
                                                                                                        kurumKodu.toString()
                                                                                                    )
                                                                                                    .collection(
                                                                                                        "Student"
                                                                                                    )
                                                                                                    .document(
                                                                                                        auth.uid.toString()
                                                                                                    )
                                                                                                    .collection(
                                                                                                        "Duties"
                                                                                                    )
                                                                                                    .document(
                                                                                                        document5.id
                                                                                                    )
                                                                                                    .update(
                                                                                                        "tamamlandi",
                                                                                                        true
                                                                                                    )
                                                                                                    .addOnSuccessListener {
                                                                                                        stopper2 =
                                                                                                            true

                                                                                                        Toast.makeText(
                                                                                                            this@AddPrgrphPrblmActivity,
                                                                                                            "İşlem Başarılı!",
                                                                                                            Toast.LENGTH_SHORT
                                                                                                        )
                                                                                                            .show()
                                                                                                        finish()
                                                                                                    }
                                                                                            } else {
                                                                                                stopper2 =
                                                                                                    true
                                                                                                Toast.makeText(
                                                                                                    this@AddPrgrphPrblmActivity,
                                                                                                    "İşlem Başarılı!",
                                                                                                    Toast.LENGTH_SHORT
                                                                                                )
                                                                                                    .show()
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
                                                            this@AddPrgrphPrblmActivity,
                                                            "İşlem Başarılı!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        finish()


                                                    }


                                            }
                                        } else {
                                            stopper = true
                                            db.collection("School").document(kurumKodu.toString())
                                                .collection("Student").document(auth.uid.toString())
                                                .collection("Studies").document(documentID)
                                                .set(study)
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
                                                                "konuAdi", dersAdi
                                                            ).addSnapshotListener { value5, e5 ->


                                                                if (!stopper2) {
                                                                    if (e5 != null) println(
                                                                        e5.localizedMessage
                                                                    )

                                                                    if (value5 != null) {
                                                                        for (document5 in value5) {


                                                                            val gorevUpdate =
                                                                                hashMapOf(
                                                                                    "toplamCalisma" to document5.get(
                                                                                        "toplamCalisma"
                                                                                    ).toString()
                                                                                        .toInt() - (soruDk),
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
                                                                                )
                                                                                    .addOnSuccessListener {

                                                                                        if (document5.get(
                                                                                                "toplamCalisma"
                                                                                            )
                                                                                                .toString()
                                                                                                .toInt() - (soruDk) <= 0 && document5.get(
                                                                                                "çözülenSoru"
                                                                                            )
                                                                                                .toString()
                                                                                                .toInt() - soruSayi <= 0
                                                                                        ) {
                                                                                            db.collection(
                                                                                                "School"
                                                                                            )
                                                                                                .document(
                                                                                                    kurumKodu.toString()
                                                                                                )
                                                                                                .collection(
                                                                                                    "Student"
                                                                                                )
                                                                                                .document(
                                                                                                    auth.uid.toString()
                                                                                                )
                                                                                                .collection(
                                                                                                    "Duties"
                                                                                                )
                                                                                                .document(
                                                                                                    document5.id
                                                                                                )
                                                                                                .update(
                                                                                                    "tamamlandi",
                                                                                                    true
                                                                                                )
                                                                                                .addOnSuccessListener {
                                                                                                    stopper2 =
                                                                                                        true

                                                                                                    Toast.makeText(
                                                                                                        this@AddPrgrphPrblmActivity,
                                                                                                        "İşlem Başarılı!",
                                                                                                        Toast.LENGTH_SHORT
                                                                                                    )
                                                                                                        .show()
                                                                                                    finish()
                                                                                                }
                                                                                        } else {
                                                                                            stopper2 =
                                                                                                true
                                                                                            Toast.makeText(
                                                                                                this@AddPrgrphPrblmActivity,
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
                                                        this@AddPrgrphPrblmActivity, "İşlem Başarılı!", Toast.LENGTH_SHORT
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

                                                db.collection("School")
                                                    .document(kurumKodu.toString())
                                                    .collection("Student")
                                                    .document(auth.uid.toString())
                                                    .collection("Duties").whereGreaterThan(
                                                        "bitisZamani", studyTime.time
                                                    ).whereEqualTo("dersAdi", dersAdi)
                                                    .whereEqualTo("tür", subjectType).whereEqualTo(
                                                        "konuAdi", dersAdi
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
                                                                            .toInt() - (soruDk),
                                                                        "çözülenSoru" to document5.get(
                                                                            "çözülenSoru"
                                                                        ).toString()
                                                                            .toInt() - soruSayi
                                                                    )
                                                                    db.collection("School")
                                                                        .document(
                                                                            kurumKodu.toString()
                                                                        ).collection("Student")
                                                                        .document(auth.uid.toString())
                                                                        .collection("Duties")
                                                                        .document(
                                                                            document5.id
                                                                        ).update(
                                                                            gorevUpdate as Map<String, Any>
                                                                        ).addOnSuccessListener {
                                                                            if (document5.get(
                                                                                    "toplamCalisma"
                                                                                ).toString()
                                                                                    .toInt() - (soruDk) <= 0 && document5.get(
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
                                                                                        Toast.makeText(
                                                                                            this@AddPrgrphPrblmActivity,
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
                                    this@AddPrgrphPrblmActivity, "İşlem Başarılı", Toast.LENGTH_SHORT
                                ).show()

                                finish()


                            }


                    } else {
                        currentTestsEditText.error = "Bu Alanı Boş Bırakamazsın!"
                        paragrafSaveButton.isClickable = true

                    }


                } else {
                    currentTestsMinutesEditText.error = "Bu Alanı Boş Bırakamazsın!"
                    paragrafSaveButton.isClickable = true

                }
            }
        }


    }
}