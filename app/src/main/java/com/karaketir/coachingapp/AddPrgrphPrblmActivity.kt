package com.karaketir.coachingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.databinding.ActivityAddPrgrphPrblmBinding
import java.util.*

class AddPrgrphPrblmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPrgrphPrblmBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAddPrgrphPrblmBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        val paragrafTitle = binding.paragrafTitle
        val paragrafSaveButton = binding.paragrafSaveButton
        val currentTestsMinutesEditText = binding.paragrafDkEditText
        val currentTestsEditText = binding.paragrafSoruEditText
        val intent = intent
        val documentID = UUID.randomUUID().toString()
        val subjectType = "TYT"

        val dersAdi = intent.getStringExtra("dersAdi").toString()

        paragrafTitle.text = dersAdi

        paragrafSaveButton.setOnClickListener {
            paragrafSaveButton.isClickable = false

            var stopper = false
            var stopper2 = false
            val cal = Calendar.getInstance()
            val studyTime = Calendar.getInstance()
            studyTime.add(Calendar.HOUR_OF_DAY, -3)

            cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

            cal.clear(Calendar.MINUTE)
            cal.clear(Calendar.SECOND)
            cal.clear(Calendar.MILLISECOND)



            if (currentTestsMinutesEditText.text.toString().isNotEmpty()) {
                currentTestsMinutesEditText.error = null

                if (currentTestsEditText.text.toString().isNotEmpty()) {
                    currentTestsEditText.error = null
                    val study = hashMapOf(
                        "id" to documentID,
                        "timestamp" to studyTime.time,
                        "konuAnlatımı" to 0,
                        "konuTestiDK" to currentTestsMinutesEditText.text.toString().toInt(),
                        "dersAdi" to dersAdi,
                        "tür" to subjectType,
                        "konuAdi" to dersAdi,
                        "toplamCalisma" to currentTestsMinutesEditText.text.toString().toInt(),
                        "çözülenSoru" to currentTestsEditText.text.toString().toInt()
                    )

                    val baslangicTarihi = cal.time


                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    val bitisTarihi = cal.time
                    Toast.makeText(
                        this, "Lütfen Bekleyiniz...", Toast.LENGTH_SHORT
                    ).show()
                    var kurumKodu: Int
                    db.collection("User").document(auth.uid.toString()).get()
                        .addOnSuccessListener { it2 ->
                            kurumKodu = it2.get("kurumKodu").toString().toInt()
                            db.collection("School").document(kurumKodu.toString())
                                .collection("Student").document(auth.uid.toString())
                                .collection("Studies").whereEqualTo("konuAdi", dersAdi)
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
                                                        "konuTestiDK" to currentTestsMinutesEditText.text.toString()
                                                            .toInt() + document.get("konuTestiDK")
                                                            .toString().toInt(),
                                                        "tür" to subjectType,
                                                        "dersAdi" to dersAdi,
                                                        "konuAdi" to dersAdi,
                                                        "toplamCalisma" to currentTestsMinutesEditText.text.toString()
                                                            .toInt() + document.get("konuAnlatımı")
                                                            .toString()
                                                            .toInt() + document.get("konuTestiDK")
                                                            .toString().toInt(),
                                                        "çözülenSoru" to currentTestsEditText.text.toString()
                                                            .toInt() + document.get("çözülenSoru")
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
                                                                        "bitisZamani",
                                                                        studyTime.time
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
                                                                                            )
                                                                                                .toString()
                                                                                                .toInt() - (currentTestsMinutesEditText.text.toString()
                                                                                                .toInt()),
                                                                                            "çözülenSoru" to document5.get(
                                                                                                "çözülenSoru"
                                                                                            )
                                                                                                .toString()
                                                                                                .toInt() - currentTestsEditText.text.toString()
                                                                                                .toInt()
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
                                                                                                        .toInt() - (currentTestsMinutesEditText.text.toString()
                                                                                                        .toInt()) <= 0 && document5.get(
                                                                                                        "çözülenSoru"
                                                                                                    )
                                                                                                        .toString()
                                                                                                        .toInt() - currentTestsEditText.text.toString()
                                                                                                        .toInt() <= 0
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
                                                                                                                this,
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
                                                                                                        this,
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
                                                                this,
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
                                                                                            .toInt() - (currentTestsMinutesEditText.text.toString()
                                                                                            .toInt()),
                                                                                        "çözülenSoru" to document5.get(
                                                                                            "çözülenSoru"
                                                                                        ).toString()
                                                                                            .toInt() - currentTestsEditText.text.toString()
                                                                                            .toInt()
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
                                                                                                    .toInt() - (currentTestsMinutesEditText.text.toString()
                                                                                                    .toInt()) <= 0 && document5.get(
                                                                                                    "çözülenSoru"
                                                                                                )
                                                                                                    .toString()
                                                                                                    .toInt() - currentTestsEditText.text.toString()
                                                                                                    .toInt() <= 0
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
                                                                                                            this,
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
                                                                                                    this,
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
                                                            this,
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
                                                .set(study).addOnSuccessListener {

                                                    db.collection("School")
                                                        .document(kurumKodu.toString())
                                                        .collection("Student")
                                                        .document(auth.uid.toString())
                                                        .collection("Duties").whereGreaterThan(
                                                            "bitisZamani", studyTime.time
                                                        ).whereEqualTo("dersAdi", dersAdi)
                                                        .whereEqualTo("tür", subjectType)
                                                        .whereEqualTo(
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
                                                                                .toInt() - (currentTestsMinutesEditText.text.toString()
                                                                                .toInt()),
                                                                            "çözülenSoru" to document5.get(
                                                                                "çözülenSoru"
                                                                            ).toString()
                                                                                .toInt() - currentTestsEditText.text.toString()
                                                                                .toInt()
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
                                                                                        .toInt() - (currentTestsMinutesEditText.text.toString()
                                                                                        .toInt()) <= 0 && document5.get(
                                                                                        "çözülenSoru"
                                                                                    ).toString()
                                                                                        .toInt() - currentTestsEditText.text.toString()
                                                                                        .toInt() <= 0
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