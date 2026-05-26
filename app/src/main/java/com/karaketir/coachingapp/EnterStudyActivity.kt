package com.karaketir.coachingapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.karaketir.coachingapp.curriculum.CurriculumProgram
import com.karaketir.coachingapp.databinding.ActivityEnterStudyBinding
import kotlinx.coroutines.tasks.await
import com.karaketir.coachingapp.services.WorldTimeApi
import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar
import java.util.UUID

@Suppress("UNCHECKED_CAST")
class EnterStudyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEnterStudyBinding
    private lateinit var auth: FirebaseAuth
    private var konuAdlari = mutableListOf<String>()
    private var konuDk = 0
    private var soruDk = 0
    private var soruSayi = 0

    private lateinit var db: FirebaseFirestore
    private var kurumKodu = 0
    private var secilenKonu = ""
    private var secilenDocumentID = ""
    private var program = CurriculumProgram.LEGACY.firestoreValue
    private var isMaarif = false
    private var sinif = 0
    private var temaId = ""
    private var temaAdi = ""

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterStudyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore

        kurumKodu = intent.getStringExtra("kurumKodu")?.toIntOrNull() ?: 0

        val textInputCurrentMinutes = binding.TextInputCurrentMinutes
        val textInputCurrentTestsMinutes = binding.TextInputCurrentTestsMinutes
        val textInputCurrentTests = binding.TextInputCurrentTests

        val currentMinutesEditText = binding.currentMinutesEditText
        val currentTestsMinutesEditText = binding.currentTestsMinutesEditText
        val currentTestsEditText = binding.currentTestsEditText
        val tarihSecButton = binding.studyDateButton


        val spinner = binding.studySpinner
        val documentID = UUID.randomUUID().toString()
        var subjectType = intent.getStringExtra("studyType")
        val dersAdi = intent.getStringExtra("dersAdi")
        program = intent.getStringExtra("program") ?: CurriculumProgram.LEGACY.firestoreValue
        if (subjectType == "Maarif" && program != CurriculumProgram.TYMM.firestoreValue) {
            program = CurriculumProgram.TYMM.firestoreValue
        }
        isMaarif = program == CurriculumProgram.TYMM.firestoreValue
        sinif = intent.getIntExtra("sinif", intent.getIntExtra("grade", 0))
        temaId = intent.getStringExtra("temaId").orEmpty()
        temaAdi = intent.getStringExtra("temaAdi").orEmpty()
        secilenKonu = if (isMaarif) temaAdi.ifBlank { dersAdi.orEmpty() } else ""

        val retrofit = Retrofit.Builder().baseUrl("http://worldtimeapi.org")
            .addConverterFactory(GsonConverterFactory.create()).build()

        val worldTimeApi = retrofit.create(WorldTimeApi::class.java)
        val cal = Calendar.getInstance()
        val cal2 = Calendar.getInstance()


        lifecycleScope.launch {
            if (sinif == 0) {
                sinif = resolveProfileGrade()
            }

            val currentTime = try {
                worldTimeApi.getCurrentTime()
            } catch (e: Exception) {
                println(e.localizedMessage)
                null
            }
            // Use currentTime here or update UI


            tarihSecButton.setOnClickListener {

                val year = cal2.get(Calendar.YEAR)
                val month = cal2.get(Calendar.MONTH)
                val day = cal2.get(Calendar.DAY_OF_MONTH)

                val dpd =
                    DatePickerDialog(this@EnterStudyActivity, { _, year2, monthOfYear, dayOfMonth ->
                        tarihSecButton.text =
                            ("Çalışma Tarihi: $dayOfMonth/${monthOfYear + 1}/$year2")
                        cal.set(year2, monthOfYear, dayOfMonth, 0, 0, 0)
                        cal2.set(year2, monthOfYear, dayOfMonth, 0, 0, 0)
                    }, year, month, day)

                dpd.show()
            }
            if (currentTime != null) {

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
            textInputCurrentMinutes.hint = "Kaç Dakika Konu Çalıştın?"
            textInputCurrentMinutes.helperText = "Kaç Dakika Konu Çalıştın?"

            textInputCurrentTestsMinutes.hint = "Bu Konuda Kaç Dakika Test Çözdün?"
            textInputCurrentTestsMinutes.helperText = "Bu Konuda Kaç Dakika Test Çözdün?"

            textInputCurrentTests.hint = "Bu Konuda Kaç Tane Soru Çözdün?"
            textInputCurrentTests.helperText = "Bu Konuda Kaç Tane Sorusu Çözdün?"

            if (isMaarif) {
                binding.studySpinnerLayout.visibility = View.GONE
                binding.temaLabelText.visibility = View.VISIBLE
                binding.temaLabelText.text = if (temaAdi.isNotBlank()) {
                    getString(R.string.tema_seciniz) + " " + temaAdi
                } else {
                    dersAdi
                }
                binding.temaLabelText.isSelected = true
                val dateParams = binding.studyDateButton.layoutParams as android.widget.RelativeLayout.LayoutParams
                dateParams.addRule(android.widget.RelativeLayout.BELOW, R.id.temaLabelText)
                binding.studyDateButton.layoutParams = dateParams
            } else {
                binding.temaLabelText.visibility = View.GONE
            }

            if (!isMaarif && !subjectType.isNullOrBlank()) {
            db.collection("Lessons").document(dersAdi.toString()).collection(subjectType)
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
                                Toast.makeText(
                                    this@EnterStudyActivity,
                                    e.localizedMessage,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }


                        }
                        val studyAdapter = ArrayAdapter(
                            this@EnterStudyActivity,
                            android.R.layout.simple_spinner_dropdown_item,
                            konuAdlari
                        )

                        studyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinner.adapter = studyAdapter
                        spinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
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
                                                    secilenDocumentID =
                                                        document.get("id").toString()
                                                }
                                            }
                                        }

                                }

                                override fun onNothingSelected(p0: AdapterView<*>?) {

                                }

                            }


                    }
                }
            }

            val subjectTypeTitle = binding.subjectTypeTitle
            val studySaveButton = binding.studySaveButton





            subjectTypeTitle.text = if (isMaarif) {
                val temaPart = temaAdi.takeIf { it.isNotBlank() }?.let { " · $it" }.orEmpty()
                "$dersAdi · Maarif$temaPart"
            } else {
                "Tür: $subjectType"
            }
            subjectTypeTitle.isSelected = true

            studySaveButton.setOnClickListener {
                studySaveButton.isClickable = false
                var stopper = false
                var stopper2 = false

                cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                cal.clear(Calendar.MINUTE)
                cal.clear(Calendar.SECOND)
                cal.clear(Calendar.MILLISECOND)


                if (currentMinutesEditText.text.toString().isNotEmpty()) {
                    currentMinutesEditText.error = null

                    konuDk = currentMinutesEditText.text.toString().toInt()

                    if (currentTestsMinutesEditText.text.toString().isNotEmpty()) {
                        currentTestsMinutesEditText.error = null
                        soruDk = currentTestsMinutesEditText.text.toString().toInt()

                        if (currentTestsEditText.text.toString().isNotEmpty()) {
                            currentTestsEditText.error = null

                            soruSayi = currentTestsEditText.text.toString().toInt()


                            val study = buildStudyPayload(
                                documentID, cal2.time, dersAdi, subjectType, secilenKonu
                            )

                            val baslangicTarihi = cal.time


                            cal.add(Calendar.DAY_OF_YEAR, 1)
                            val bitisTarihi = cal.time
                            Toast.makeText(
                                this@EnterStudyActivity, "Lütfen Bekleyiniz...", Toast.LENGTH_SHORT
                            ).show()

                            studiesCollection()
                                .applyStudyDedup(dersAdi, subjectType, secilenKonu, baslangicTarihi, bitisTarihi)
                                .addSnapshotListener { value, error ->
                                    if (error != null) {
                                        println(error.localizedMessage)
                                    }
                                    if (!stopper) {
                                        if (value != null) {
                                            if (!value.isEmpty) {
                                                for (document in value) {
                                                    val studyUpdate = buildStudyUpdatePayload(
                                                        document.id,
                                                        cal2.time,
                                                        dersAdi,
                                                        subjectType,
                                                        secilenKonu,
                                                        document.get("konuAnlatımı").toString().toInt(),
                                                        document.get("konuTestiDK").toString().toInt(),
                                                        document.get("çözülenSoru").toString().toInt()
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
                                                                dutiesCollection()
                                                                    .applyDutyDedup(
                                                                        dersAdi, subjectType, secilenKonu, cal2.time
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
                                                                                            )
                                                                                                .toString()
                                                                                                .toInt() - (konuDk + soruDk),
                                                                                            "çözülenSoru" to document5.get(
                                                                                                "çözülenSoru"
                                                                                            )
                                                                                                .toString()
                                                                                                .toInt() - soruSayi
                                                                                        )

                                                                                    if (!stopper2) {
                                                                                        stopper2 =
                                                                                            true

                                                                                        db.collection(
                                                                                            "School"
                                                                                        ).document(
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
                                                                                                gorevUpdate as Map<String, Any>
                                                                                            )
                                                                                            .addOnSuccessListener {

                                                                                                if (document5.get(
                                                                                                        "toplamCalisma"
                                                                                                    )
                                                                                                        .toString()
                                                                                                        .toInt() - (konuDk + soruDk) <= 0 && document5.get(
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
                                                                                                                this@EnterStudyActivity,
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
                                                                                                        this@EnterStudyActivity,
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
                                                                this@EnterStudyActivity,
                                                                "İşlem Başarılı!",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            finish()


                                                        }


                                                }
                                            } else {
                                                stopper = true
                                                db.collection("School")
                                                    .document(kurumKodu.toString())
                                                    .collection("Student")
                                                    .document(auth.uid.toString())
                                                    .collection("Studies").document(documentID)
                                                    .set(study).addOnSuccessListener {

                                                        if (!stopper2) {
                                                            dutiesCollection()
                                                                .applyDutyDedup(
                                                                    dersAdi, subjectType, secilenKonu, cal2.time
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
                                                                                    )
                                                                                        .addOnSuccessListener {

                                                                                            if (document5.get(
                                                                                                    "toplamCalisma"
                                                                                                )
                                                                                                    .toString()
                                                                                                    .toInt() - (konuDk + soruDk) <= 0 && document5.get(
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
                                                                                                            this@EnterStudyActivity,
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
                                                                                                    this@EnterStudyActivity,
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
                                                            this@EnterStudyActivity,
                                                            "İşlem Başarılı!",
                                                            Toast.LENGTH_SHORT
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
                                                .collection("Studies").document(documentID)
                                                .set(study)
                                                .addOnSuccessListener {

                                                    dutiesCollection()
                                                        .applyDutyDedup(
                                                            dersAdi, subjectType, secilenKonu, cal2.time
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
                                                                                            Toast.makeText(
                                                                                                this@EnterStudyActivity,
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
                                        this@EnterStudyActivity,
                                        "İşlem Başarılı",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val cal3 = Calendar.getInstance()

                                    if (currentTime != null) {
                                        val date = currentTime.datetime.split("T")[0]
                                        val time = currentTime.datetime.split("T")[1].split(".")[0]
                                        cal3[Calendar.YEAR] = date.split("-")[0].toInt()
                                        cal3[Calendar.MONTH] = date.split("-")[1].toInt() - 1
                                        cal3[Calendar.DAY_OF_MONTH] = date.split("-")[2].toInt()
                                        cal3[Calendar.HOUR_OF_DAY] = time.split(":")[0].toInt()
                                        cal3[Calendar.MINUTE] = time.split(":")[1].toInt()
                                        cal3[Calendar.SECOND] = time.split(":")[2].toInt()

                                        db.collection("School").document(kurumKodu.toString())
                                            .collection("LastReports").document(auth.uid.toString())
                                            .set(
                                                hashMapOf(
                                                    "id" to auth.uid.toString(),
                                                    "timestamp" to cal3.time
                                                )
                                            )
                                    }


                                    finish()


                                }


                        } else {
                            currentTestsEditText.error = "Bu Alanı Boş Bırakamazsın!"
                            studySaveButton.isClickable = true

                        }


                    } else {
                        currentTestsMinutesEditText.error = "Bu Alanı Boş Bırakamazsın!"
                        studySaveButton.isClickable = true

                    }


                } else {
                    currentMinutesEditText.error = "Bu Alanı Boş Bırakamazsın!"
                    studySaveButton.isClickable = true

                }
            }

        }

    }

    private fun studiesCollection(): CollectionReference {
        return db.collection("School").document(kurumKodu.toString())
            .collection("Student")
            .document(auth.uid.toString())
            .collection("Studies")
    }

    private fun dutiesCollection(): CollectionReference {
        return db.collection("School").document(kurumKodu.toString())
            .collection("Student")
            .document(auth.uid.toString())
            .collection("Duties")
    }

    private fun CollectionReference.applyStudyDedup(
        dersAdi: String?,
        subjectType: String?,
        konuAdi: String,
        baslangic: java.util.Date,
        bitis: java.util.Date
    ): Query {
        var query = whereEqualTo("dersAdi", dersAdi)
            .whereGreaterThan("timestamp", baslangic)
            .whereLessThan("timestamp", bitis)
        query = if (isMaarif && temaId.isNotBlank()) {
            query.whereEqualTo("program", program).whereEqualTo("temaId", temaId)
        } else if (!isMaarif && !subjectType.isNullOrBlank()) {
            query.whereEqualTo("tür", subjectType).whereEqualTo("konuAdi", konuAdi)
        } else {
            query.whereEqualTo("konuAdi", konuAdi)
        }
        return query
    }

    private fun CollectionReference.applyDutyDedup(
        dersAdi: String?,
        subjectType: String?,
        konuAdi: String,
        afterTime: java.util.Date
    ): Query {
        var query = whereGreaterThan("bitisZamani", afterTime).whereEqualTo("dersAdi", dersAdi)
        query = if (isMaarif && temaId.isNotBlank()) {
            query.whereEqualTo("program", program).whereEqualTo("temaId", temaId)
        } else if (!isMaarif && !subjectType.isNullOrBlank()) {
            query.whereEqualTo("tür", subjectType).whereEqualTo("konuAdi", konuAdi)
        } else {
            query.whereEqualTo("konuAdi", konuAdi)
        }
        return query
    }

    private fun buildStudyPayload(
        documentID: String,
        timestamp: java.util.Date,
        dersAdi: String?,
        subjectType: String?,
        konuAdi: String
    ): HashMap<String, Any> {
        val konu = if (isMaarif) temaAdi.ifBlank { dersAdi.orEmpty() } else konuAdi
        val payload = hashMapOf<String, Any>(
            "id" to documentID,
            "timestamp" to timestamp,
            "konuAnlatımı" to konuDk,
            "konuTestiDK" to soruDk,
            "dersAdi" to dersAdi.orEmpty(),
            "konuAdi" to konu,
            "program" to program,
            "toplamCalisma" to konuDk + soruDk,
            "çözülenSoru" to soruSayi
        )
        if (sinif > 0) {
            payload["sinif"] = sinif
        }
        if (isMaarif) {
            if (temaId.isNotBlank()) payload["temaId"] = temaId
            if (temaAdi.isNotBlank()) payload["temaAdi"] = temaAdi
        } else if (!subjectType.isNullOrBlank()) {
            payload["tür"] = subjectType
        }
        return payload
    }

    private fun buildStudyUpdatePayload(
        documentId: String,
        timestamp: java.util.Date,
        dersAdi: String?,
        subjectType: String?,
        konuAdi: String,
        existingKonuAnlatim: Int,
        existingKonuTesti: Int,
        existingSoru: Int
    ): HashMap<String, Any> {
        val base = buildStudyPayload(documentId, timestamp, dersAdi, subjectType, konuAdi)
        base["konuAnlatımı"] = konuDk + existingKonuAnlatim
        base["konuTestiDK"] = soruDk + existingKonuTesti
        base["toplamCalisma"] = konuDk + soruDk + existingKonuAnlatim + existingKonuTesti
        base["çözülenSoru"] = soruSayi + existingSoru
        return base
    }

    private suspend fun resolveProfileGrade(): Int {
        return try {
            val uid = auth.uid ?: return 12
            val userSnap = db.collection("User").document(uid).get().await()
            userSnap.getLong("grade")?.toInt()
                ?: userSnap.getString("grade")?.toIntOrNull()
                ?: 12
        } catch (_: Exception) {
            12
        }
    }

}
