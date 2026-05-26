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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.karaketir.coachingapp.curriculum.CurriculumProgram
import com.karaketir.coachingapp.curriculum.GradeCurriculumConfig
import com.karaketir.coachingapp.curriculum.GradeCurriculumRepository
import com.karaketir.coachingapp.curriculum.Subjects
import com.karaketir.coachingapp.curriculum.TemaOption
import com.karaketir.coachingapp.databinding.ActivityEnterDutyBinding
import com.karaketir.coachingapp.services.FcmNotificationsSenderService
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.UUID

@Suppress("UNCHECKED_CAST")
class EnterDutyActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityEnterDutyBinding

    private val dersAdlari = ArrayList<String>()
    private val konuAdlari = ArrayList<String>()
    private var temalar = emptyList<TemaOption>()

    private var secilenDers = ""
    private var kurumKodu = 0
    private var secilenTur = ""
    private var secilenKonu = ""
    private var temaId = ""
    private var temaAdi = ""
    private var studentID = ""
    private var studentGrade = 12
    private var studentProgram = CurriculumProgram.LEGACY
    private val turler = arrayOf("TYT", "AYT")

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterDutyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
        kurumKodu = intent.getStringExtra("kurumKodu").orEmpty().toIntOrNull() ?: 763455
        studentID = intent.getStringExtra("studentID").orEmpty()

        val documentID = UUID.randomUUID().toString()
        val gorevCozulenSoru = binding.gorevCozulenSoru
        val gorevToplamCalisma = binding.gorevToplamCalisma
        val tarihSecButton = binding.tarihSecButton
        val dutySaveButton = binding.dutySaveButton

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        tarihSecButton.setOnClickListener {
            DatePickerDialog(this, { _, year2, monthOfYear, dayOfMonth ->
                tarihSecButton.text = "Bitiş Tarihi: $dayOfMonth/${monthOfYear + 1}/$year2"
                c.set(year2, monthOfYear, dayOfMonth, 0, 0, 0)
            }, year, month, day).show()
        }

        lifecycleScope.launch {
            loadStudentProgram()
            setupLessonSpinner()
        }

        var stopper = false
        dutySaveButton.setOnClickListener {
            dutySaveButton.isClickable = false

            if (gorevToplamCalisma.text.isNullOrEmpty()) {
                gorevToplamCalisma.error = "Bu Alan Boş Bırakılamaz"
                dutySaveButton.isClickable = true
                return@setOnClickListener
            }
            gorevToplamCalisma.error = null

            if (gorevCozulenSoru.text.isNullOrEmpty()) {
                gorevCozulenSoru.error = "Bu Alan Boş Bırakılamaz"
                dutySaveButton.isClickable = true
                return@setOnClickListener
            }
            gorevCozulenSoru.error = null

            val isMaarif = studentProgram == CurriculumProgram.TYMM
            if (isMaarif) {
                if (temaId.isBlank() || temaAdi.isBlank()) {
                    Toast.makeText(this, "Lütfen tema seçiniz", Toast.LENGTH_SHORT).show()
                    dutySaveButton.isClickable = true
                    return@setOnClickListener
                }
                secilenKonu = temaAdi
            } else if (secilenTur.isBlank() || secilenKonu.isBlank()) {
                Toast.makeText(this, "Lütfen tür ve konu seçiniz", Toast.LENGTH_SHORT).show()
                dutySaveButton.isClickable = true
                return@setOnClickListener
            }

            val toplam = gorevToplamCalisma.text.toString().toInt()
            val cozulen = gorevCozulenSoru.text.toString().toInt()
            stopper = false

            dutiesCollection()
                .applyDutyDedup(isMaarif)
                .addSnapshotListener { value, _ ->
                    if (stopper) return@addSnapshotListener
                    if (value == null) return@addSnapshotListener

                    if (!value.isEmpty) {
                        stopper = true
                        val existing = value.documents.first()
                        val dutyUpdate = hashMapOf(
                            "eklenmeTarihi" to Timestamp.now(),
                            "toplamCalisma" to toplam,
                            "çözülenSoru" to cozulen,
                            "bitisZamani" to c.time,
                            "tamamlandi" to false,
                        )
                        dutiesCollection().document(existing.id)
                            .update(dutyUpdate as Map<String, Any>)
                            .addOnSuccessListener {
                                sendDutyNotification(isMaarif)
                                Toast.makeText(this, "İşlem Başarılı", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener {
                                stopper = false
                                dutySaveButton.isClickable = true
                            }
                    } else if (!stopper) {
                        stopper = true
                        val duty = buildDutyPayload(documentID, c.time, toplam, cozulen, isMaarif)
                        dutiesCollection().document(documentID)
                            .set(duty)
                            .addOnSuccessListener {
                                sendDutyNotification(isMaarif)
                                Toast.makeText(this, "İşlem Başarılı", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener {
                                stopper = false
                                dutySaveButton.isClickable = true
                            }
                    }
                }
        }
    }

    private suspend fun loadStudentProgram() {
        try {
            val studentSnap = db.collection("User").document(studentID).get().await()
            studentGrade = studentSnap.getLong("grade")?.toInt()
                ?: studentSnap.getString("grade")?.toIntOrNull()
                ?: 12
            val config = GradeCurriculumRepository.load(db)
            studentProgram = GradeCurriculumRepository.programForGrade(config, studentGrade)
        } catch (_: Exception) {
            studentProgram = GradeCurriculumRepository.programForGrade(
                GradeCurriculumConfig(GradeCurriculumRepository.defaultGradePrograms),
                studentGrade,
            )
        }

        val isMaarif = studentProgram == CurriculumProgram.TYMM
        binding.dutyTurSpinnerLayout.visibility = if (isMaarif) View.GONE else View.VISIBLE
        binding.dutyKonuLabelText.text = getString(
            if (isMaarif) R.string.tema_sec else R.string.konu_se_iniz
        )
    }

    private fun setupLessonSpinner() {
        dersAdlari.clear()
        if (studentProgram == CurriculumProgram.TYMM) {
            dersAdlari.addAll(Subjects.tymmSubjectsForGrade(studentGrade))
        } else {
            dersAdlari.addAll(Subjects.LEGACY_SUBJECT_NAMES)
        }

        val adapter = ArrayAdapter(
            this@EnterDutyActivity,
            android.R.layout.simple_spinner_dropdown_item,
            dersAdlari,
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.dutyDersAdiSpinner.adapter = adapter
        binding.dutyDersAdiSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    secilenDers = dersAdlari[position]
                    if (studentProgram == CurriculumProgram.TYMM) {
                        loadTemaSpinner()
                    } else {
                        setupLegacyTurSpinner()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        if (dersAdlari.isNotEmpty()) {
            secilenDers = dersAdlari[0]
            if (studentProgram == CurriculumProgram.TYMM) {
                loadTemaSpinner()
            } else {
                setupLegacyTurSpinner()
            }
        }
    }

    private fun setupLegacyTurSpinner() {
        val dutyTurAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            turler,
        )
        dutyTurAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.dutyTurSpinner.adapter = dutyTurAdapter
        binding.dutyTurSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                secilenTur = turler[position]
                loadLegacyKonuSpinner()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadLegacyKonuSpinner() {
        db.collection("Lessons").document(secilenDers).collection(secilenTur)
            .orderBy("konuAdi", Query.Direction.ASCENDING)
            .addSnapshotListener { konularDocument, _ ->
                if (konularDocument == null) return@addSnapshotListener
                konuAdlari.clear()
                for (konuAdi in konularDocument) {
                    try {
                        val arrayType = konuAdi.get("arrayType") as ArrayList<*>
                        if ("konu" in arrayType) {
                            konuAdlari.add(konuAdi.get("konuAdi").toString())
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@EnterDutyActivity, e.localizedMessage, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                bindKonuSpinner()
            }
    }

    private fun loadTemaSpinner() {
        lifecycleScope.launch {
            try {
                temalar = GradeCurriculumRepository.loadTymmThemes(db, secilenDers, studentGrade)
                val names = temalar.map { it.name }
                bindKonuSpinner(names)
            } catch (e: Exception) {
                Toast.makeText(
                    this@EnterDutyActivity,
                    e.localizedMessage ?: "Tema yüklenemedi",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun bindKonuSpinner(items: List<String> = konuAdlari) {
        val adapter = ArrayAdapter(
            this@EnterDutyActivity,
            android.R.layout.simple_spinner_dropdown_item,
            items,
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.dutyKonuAdiSpinner.adapter = adapter
        binding.dutyKonuAdiSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (studentProgram == CurriculumProgram.TYMM) {
                    val tema = temalar.getOrNull(position) ?: return
                    temaId = tema.id
                    temaAdi = tema.name
                    secilenKonu = temaAdi
                } else {
                    secilenKonu = konuAdlari[position]
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun dutiesCollection(): CollectionReference {
        return db.collection("School").document(kurumKodu.toString())
            .collection("Student")
            .document(studentID)
            .collection("Duties")
    }

    private fun CollectionReference.applyDutyDedup(isMaarif: Boolean): Query {
        var query = whereEqualTo("dersAdi", secilenDers)
        query = if (isMaarif) {
            query.whereEqualTo("program", CurriculumProgram.TYMM.firestoreValue)
                .whereEqualTo("temaId", temaId)
        } else {
            query.whereEqualTo("tür", secilenTur).whereEqualTo("konuAdi", secilenKonu)
        }
        return query
    }

    private fun buildDutyPayload(
        documentID: String,
        bitis: java.util.Date,
        toplam: Int,
        cozulen: Int,
        isMaarif: Boolean,
    ): HashMap<String, Any> {
        val konu = if (isMaarif) temaAdi else secilenKonu
        val payload = hashMapOf<String, Any>(
            "id" to documentID,
            "bitisZamani" to bitis,
            "dersAdi" to secilenDers,
            "konuAdi" to konu,
            "program" to studentProgram.firestoreValue,
            "toplamCalisma" to toplam,
            "çözülenSoru" to cozulen,
            "eklenmeTarihi" to Timestamp.now(),
            "tamamlandi" to false,
        )
        if (isMaarif) {
            payload["sinif"] = studentGrade
            payload["temaId"] = temaId
            payload["temaAdi"] = temaAdi
        } else {
            payload["tür"] = secilenTur
        }
        return payload
    }

    private fun sendDutyNotification(isMaarif: Boolean) {
        val body = if (isMaarif) {
            "Yeni Göreviniz Var\nMaarif · $temaAdi $secilenDers"
        } else {
            "Yeni Göreviniz Var\n$secilenTur $secilenDers $secilenKonu"
        }
        FcmNotificationsSenderService(
            "/topics/$studentID",
            "Yeni Görev",
            body,
            this,
        ).sendNotifications()
    }
}
