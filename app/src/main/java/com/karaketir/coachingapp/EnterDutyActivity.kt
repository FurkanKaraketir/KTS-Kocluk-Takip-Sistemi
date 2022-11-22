package com.karaketir.coachingapp

//noinspection SuspiciousImport
import android.R
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.databinding.ActivityEnterDutyBinding
import com.karaketir.coachingapp.services.FcmNotificationsSenderService
import java.util.*

class EnterDutyActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityEnterDutyBinding
    private var dersAdlari = ArrayList<String>()
    private var konuAdlari = ArrayList<String>()
    private var secilenDers = ""
    private var secilenTur = ""
    private var secilenKonu = ""
    private var turler = arrayOf("TYT", "AYT")

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterDutyBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = Firebase.auth
        db = Firebase.firestore


        db.collection("Lessons").orderBy("dersAdi", Query.Direction.ASCENDING)
            .addSnapshotListener { dersAdlariDocument, _ ->
                if (dersAdlariDocument != null) {
                    dersAdlari.clear()
                    for (dersAdi in dersAdlariDocument) {
                        dersAdlari.add(dersAdi.id)
                    }
                    val dersAdiSpinner = binding.dutyDersAdiSpinner
                    val dersAdiAdapter = ArrayAdapter(
                        this@EnterDutyActivity, R.layout.simple_spinner_dropdown_item, dersAdlari
                    )
                    dersAdiAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                    dersAdiSpinner.adapter = dersAdiAdapter
                    dersAdiSpinner.onItemSelectedListener = object : OnItemSelectedListener {
                        override fun onItemSelected(
                            p0: AdapterView<*>?, p1: View?, position: Int, p3: Long
                        ) {
                            secilenDers = dersAdlari[position]

                            val dutyTurSpinner = binding.dutyTurSpinner

                            val dutyTurAdapter = ArrayAdapter(
                                this@EnterDutyActivity,
                                R.layout.simple_spinner_dropdown_item,
                                turler
                            )

                            dutyTurAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                            dutyTurSpinner.adapter = dutyTurAdapter
                            dutyTurSpinner.onItemSelectedListener =
                                object : OnItemSelectedListener {
                                    override fun onItemSelected(
                                        p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
                                    ) {
                                        secilenTur = turler[p2]

                                        db.collection("Lessons").document(secilenDers)
                                            .collection(secilenTur)
                                            .orderBy("konuAdi", Query.Direction.ASCENDING)
                                            .addSnapshotListener { konularDocument, _ ->

                                                if (konularDocument != null) {
                                                    konuAdlari.clear()
                                                    for (konuAdi in konularDocument) {

                                                        try {
                                                            val arrayType =
                                                                konuAdi.get("arrayType") as ArrayList<*>
                                                            if ("konu" in arrayType) {
                                                                konuAdlari.add(
                                                                    konuAdi.get("konuAdi")
                                                                        .toString()
                                                                )
                                                            }
                                                        } catch (e: Exception) {
                                                            Toast.makeText(
                                                                this@EnterDutyActivity,
                                                                e.localizedMessage,
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }


                                                    }


                                                    val dutyKonuSpinner = binding.dutyKonuAdiSpinner

                                                    val dutyKonuAdapter = ArrayAdapter(
                                                        this@EnterDutyActivity,
                                                        R.layout.simple_spinner_dropdown_item,
                                                        konuAdlari
                                                    )
                                                    dutyKonuAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                                                    dutyKonuSpinner.adapter = dutyKonuAdapter
                                                    dutyKonuSpinner.onItemSelectedListener =
                                                        object : OnItemSelectedListener {
                                                            override fun onItemSelected(
                                                                p0: AdapterView<*>?,
                                                                p1: View?,
                                                                p5: Int,
                                                                p3: Long
                                                            ) {
                                                                secilenKonu = konuAdlari[p5]
                                                            }

                                                            override fun onNothingSelected(p0: AdapterView<*>?) {
                                                            }
                                                        }

                                                }

                                            }

                                    }

                                    override fun onNothingSelected(p0: AdapterView<*>?) {
                                    }

                                }


                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {

                        }

                    }

                }
            }


        val documentID = UUID.randomUUID().toString()
        val intent = intent
        val studentID = intent.getStringExtra("studentID")
        val gorevCozulenSoru = binding.gorevCozulenSoru
        val gorevToplamCalisma = binding.gorevToplamCalisma

        val tarihSecButton = binding.tarihSecButton
        val dutySaveButton = binding.dutySaveButton

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        tarihSecButton.setOnClickListener {


            val dpd = DatePickerDialog(this, { _, year2, monthOfYear, dayOfMonth ->
                tarihSecButton.text = ("Bitiş Tarihi: $dayOfMonth/${monthOfYear + 1}/$year2")
                c.set(year2, monthOfYear, dayOfMonth, 0, 0, 0)
            }, year, month, day)

            dpd.show()
        }

        var stopper = false
        dutySaveButton.setOnClickListener {

            dutySaveButton.isClickable = false

            if (gorevToplamCalisma.text.toString().isNotEmpty()) {
                gorevToplamCalisma.error = null


                if (gorevCozulenSoru.text.toString().isNotEmpty()) {
                    gorevCozulenSoru.error = null

                    val duty = hashMapOf(
                        "id" to documentID,
                        "bitisZamani" to c.time,
                        "dersAdi" to secilenDers,
                        "konuAdi" to secilenKonu,
                        "tür" to secilenTur,
                        "toplamCalisma" to gorevToplamCalisma.text.toString().toInt(),
                        "çözülenSoru" to gorevCozulenSoru.text.toString().toInt(),
                        "eklenmeTarihi" to Timestamp.now(),
                        "tamamlandi" to false

                    )

                    var kurumKodu: Int
                    var sended = false
                    db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
                        kurumKodu = it.get("kurumKodu").toString().toInt()
                        db.collection("School").document(kurumKodu.toString()).collection("Student")
                            .document(studentID!!).collection("Duties")
                            .whereEqualTo("dersAdi", secilenDers).whereEqualTo("tür", secilenTur)
                            .whereEqualTo("konuAdi", secilenKonu).addSnapshotListener { value, _ ->
                                if (!stopper) {
                                    if (value != null) {

                                        if (!value.isEmpty) {

                                            if (!stopper) {
                                                for (document in value) {

                                                    val dutyUpdate = hashMapOf(

                                                        "eklenmeTarihi" to Timestamp.now(),
                                                        "toplamCalisma" to gorevToplamCalisma.text.toString()
                                                            .toInt(),
                                                        "çözülenSoru" to gorevCozulenSoru.text.toString()
                                                            .toInt(),
                                                        "bitisZamani" to c.time,
                                                        "tamamlandi" to false

                                                    )

                                                    if (!stopper) {
                                                        stopper = true

                                                        db.collection("School")
                                                            .document(kurumKodu.toString())
                                                            .collection("Student")
                                                            .document(studentID)
                                                            .collection("Duties")
                                                            .document(document.id)
                                                            .update(dutyUpdate as Map<String, Any>)
                                                            .addOnSuccessListener {

                                                                if (!sended) {
                                                                    val notificationsSender =
                                                                        FcmNotificationsSenderService(
                                                                            "/topics/$studentID",
                                                                            "Yeni Görev",
                                                                            "Yeni Göreviniz Var \n$secilenTur $secilenDers $secilenKonu",
                                                                            this
                                                                        )
                                                                    notificationsSender.sendNotifications()

                                                                    Toast.makeText(
                                                                        this,
                                                                        "İşlem Başarılı",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                    sended = true
                                                                }

                                                                finish()
                                                            }

                                                    }
                                                }
                                            }


                                        } else {
                                            if (!stopper) {
                                                db.collection("School")
                                                    .document(kurumKodu.toString())
                                                    .collection("Student").document(studentID)
                                                    .collection("Duties").document(documentID)
                                                    .set(duty).addOnSuccessListener {
                                                        if (!sended) {
                                                            val notificationsSender =
                                                                FcmNotificationsSenderService(
                                                                    "/topics/$studentID",
                                                                    "Yeni Görev",
                                                                    "Yeni Göreviniz Var \n$secilenTur $secilenDers $secilenKonu",
                                                                    this
                                                                )
                                                            stopper = true
                                                            notificationsSender.sendNotifications()

                                                            Toast.makeText(
                                                                this,
                                                                "İşlem Başarılı",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            sended = true
                                                        }

                                                        finish()
                                                    }
                                            }
                                        }

                                    }
                                }
                            }
                    }


                } else {
                    gorevCozulenSoru.error = "Bu Alan Boş Bırakılamaz"
                    dutySaveButton.isClickable = true


                }


            } else {
                gorevToplamCalisma.error = "Bu Alan Boş Bırakılamaz"
                dutySaveButton.isClickable = true
            }


        }


    }
}