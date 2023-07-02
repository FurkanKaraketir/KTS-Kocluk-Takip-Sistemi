package com.karaketir.coachingapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.databinding.ActivityEnterTytBinding
import java.util.*
import kotlin.collections.ArrayList

class EnterTytActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
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


    private lateinit var binding: ActivityEnterTytBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var turkceDogruDegeri = 0
    private var turkceYanlisDegeri = 0
    private var cogDogruDegeri = 0
    private var cogYanlisDegeri = 0
    private var tarihDogruDegeri = 0
    private var tarihYanlisDegeri = 0
    private var felsefeDogruDegeri = 0
    private var felsefeYanlisDegeri = 0
    private var dinDogruDegeri = 0
    private var dinYanlisDegeri = 0
    private var matDogruDegeri = 0
    private var matYanlisDegeri = 0
    private var geoDogruDegeri = 0
    private var geoYanlisDegeri = 0
    private var fizikDogruDegeri = 0
    private var fizikYanlisDegeri = 0
    private var kimyaDogruDegeri = 0
    private var kimyaYanlisDegeri = 0
    private var biyolojiDogruDegeri = 0
    private var biyolojiYanlisDegeri = 0
    private var denemeAdi = ""
    private var denemeList = ArrayList<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterTytBinding.inflate(layoutInflater)

        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore


        val denemeAdiSpinner = binding.denemeAdiSpinner

        setupNumberPickerForStringValues()

        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0

        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)


        val studyType = intent.getStringExtra("studyType")

        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            val kurumKodu = it.get("kurumKodu").toString().toInt()
            val grade = it.get("grade").toString().toInt()

            db.collection("School").document(kurumKodu.toString()).collection("Student")
                .document(auth.uid.toString()).get().addOnSuccessListener { ogrenci ->
                    val ogretmenID = ogrenci.get("teacher").toString()

                    db.collection("School").document(kurumKodu.toString()).collection("Teacher")
                        .document(ogretmenID).collection("Denemeler")
                        .whereGreaterThan("bitisTarihi", cal.time).whereEqualTo("tür", studyType)
                        .whereEqualTo("grade", grade).addSnapshotListener { value, error ->
                            if (error != null) {
                                println(error.localizedMessage)
                            }
                            denemeList.clear()
                            if (value != null && !value.isEmpty) {

                                for (deneme in value) {
                                    denemeList.add(deneme.get("denemeAdi").toString())
                                }
                                val denemeAdapter = ArrayAdapter(
                                    this@EnterTytActivity,
                                    android.R.layout.simple_spinner_item,
                                    denemeList
                                )
                                denemeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                denemeAdiSpinner.adapter = denemeAdapter
                                denemeAdiSpinner.onItemSelectedListener = this
                            }
                            if (denemeList.isEmpty()) {
                                binding.denemeNameEditText.visibility = View.VISIBLE
                                binding.denemeSpinnerLayout.visibility = View.GONE

                            } else {
                                binding.denemeNameEditText.visibility = View.GONE
                                binding.denemeSpinnerLayout.visibility = View.VISIBLE
                            }

                        }

                }

        }

        val documentID = UUID.randomUUID().toString()

        val button = binding.denemeKaydetButton
        val turkYanlisBtn = binding.turkYanlisButton

        val cogYanlis = binding.cogYanlis
        val tarihYanlis = binding.tarihYanlisButton

        val felYanlis = binding.felYanlis

        val dinYanlisButton = binding.dinYanlisButton

        val matYanlisButton = binding.matYanlisButton

        val geoYanlisButton = binding.geoYanlisButton

        val fizYanlisButton = binding.fizYanlisButton

        val kimYanlis = binding.kimYanlisButton

        val biYanlisButton = binding.biYanlisButton




        fizYanlisButton.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", studyType)
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Fizik")
            this.startActivity(intent)
        }
        biYanlisButton.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", studyType)
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Biyoloji")
            this.startActivity(intent)
        }

        kimYanlis.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", studyType)
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Kimya")
            this.startActivity(intent)
        }

        geoYanlisButton.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", studyType)
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Geometri")
            this.startActivity(intent)
        }

        matYanlisButton.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", studyType)
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Matematik")
            this.startActivity(intent)
        }

        dinYanlisButton.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", studyType)
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Din")
            this.startActivity(intent)
        }

        felYanlis.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", studyType)
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Felsefe")
            this.startActivity(intent)
        }

        tarihYanlis.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", studyType)
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Tarih")
            this.startActivity(intent)
        }
        cogYanlis.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", studyType)
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Coğrafya")
            this.startActivity(intent)
        }
        turkYanlisBtn.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", studyType)
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Türkçe-Edebiyat")
            this.startActivity(intent)
        }

        button.setOnClickListener {
            db.collection(
                "User"
            ).document(
                auth.uid.toString()
            ).get().addOnSuccessListener {
                val kurumKodu = it.get("kurumKodu").toString().toInt()

                button.isClickable = false
                val turkceNet = turkceDogruDegeri - (turkceYanlisDegeri * 0.25f)
                val tarihNet = tarihDogruDegeri - (tarihYanlisDegeri * 0.25f)
                val cogNet = cogDogruDegeri - (cogYanlisDegeri * 0.25f)
                val felNet = felsefeDogruDegeri - (felsefeYanlisDegeri * 0.25f)
                val dinNet = dinDogruDegeri - (dinYanlisDegeri * 0.25f)
                val matNet = matDogruDegeri - (matYanlisDegeri * 0.25f)
                val fizNet = fizikDogruDegeri - (fizikYanlisDegeri * 0.25f)
                val kimyaNet = kimyaDogruDegeri - (kimyaYanlisDegeri * 0.25f)
                val biyoNet = biyolojiDogruDegeri - (biyolojiYanlisDegeri * 0.25f)
                val geoNet = geoDogruDegeri - (geoYanlisDegeri * 0.25f)


                val toplamNet =
                    turkceNet + tarihNet + cogNet + felNet + dinNet + matNet + fizNet + kimyaNet + biyoNet + geoNet

                if (denemeAdi == "") {
                    if (binding.denemeNameEditText.text.isNotEmpty()) {
                        binding.denemeNameEditText.error = null
                        denemeAdi = binding.denemeNameEditText.text.toString()


                        val deneme = hashMapOf(
                            "id" to documentID,
                            "denemeTür" to studyType,
                            "denemeAdi" to denemeAdi,
                            "turkceNet" to turkceNet,
                            "tarihNet" to tarihNet,
                            "cogNet" to cogNet,
                            "felNet" to felNet,
                            "dinNet" to dinNet,
                            "matNet" to matNet,
                            "fizNet" to fizNet,
                            "kimyaNet" to kimyaNet,
                            "biyoNet" to biyoNet,
                            "geoNet" to geoNet,
                            "denemeTarihi" to Timestamp.now(),
                            "toplamNet" to toplamNet
                        )

                        db.collection(
                            "School"
                        ).document(
                            kurumKodu.toString()
                        ).collection(
                            "Student"
                        ).document(
                            auth.uid.toString()
                        ).collection(
                            "Denemeler"
                        ).document(
                            documentID
                        ).set(
                            deneme
                        ).addOnSuccessListener {
                            Toast.makeText(
                                this, "İşlem Başarılı", Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }

                    } else {
                        binding.denemeNameEditText.error = "Bu Alan Boş Bırakılamaz"
                        button.isClickable = true
                    }
                } else {

                    val deneme = hashMapOf(
                        "id" to documentID,
                        "denemeTür" to studyType,
                        "denemeAdi" to denemeAdi,
                        "turkceNet" to turkceNet,
                        "tarihNet" to tarihNet,
                        "cogNet" to cogNet,
                        "felNet" to felNet,
                        "dinNet" to dinNet,
                        "matNet" to matNet,
                        "fizNet" to fizNet,
                        "kimyaNet" to kimyaNet,
                        "biyoNet" to biyoNet,
                        "geoNet" to geoNet,
                        "denemeTarihi" to Timestamp.now(),
                        "toplamNet" to toplamNet
                    )

                    db.collection(
                        "School"
                    ).document(
                        kurumKodu.toString()
                    ).collection(
                        "Student"
                    ).document(
                        auth.uid.toString()
                    ).collection(
                        "Denemeler"
                    ).document(
                        documentID
                    ).set(
                        deneme
                    ).addOnSuccessListener {
                        Toast.makeText(
                            this, "İşlem Başarılı", Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }


            }
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        denemeAdi = denemeList[p2]
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    private fun setupNumberPickerForStringValues() {

        val turkceDogru = binding.turkceDogru
        turkceDogru.minValue = 0
        turkceDogru.maxValue = 40
        turkceDogru.wrapSelectorWheel = true
        turkceDogru.setOnValueChangedListener { _, _, newVal ->
            turkceDogruDegeri = newVal
        }

        val turkceYanlis = binding.turkceYanlis

        turkceYanlis.minValue = 0
        turkceYanlis.maxValue = 40
        turkceYanlis.wrapSelectorWheel = true
        turkceYanlis.setOnValueChangedListener { _, _, newVal ->
            turkceYanlisDegeri = newVal
        }


        val tarihDogru = binding.tarihDogru
        tarihDogru.minValue = 0
        tarihDogru.maxValue = 40
        tarihDogru.wrapSelectorWheel = true
        tarihDogru.setOnValueChangedListener { _, _, newVal ->
            tarihDogruDegeri = newVal
        }

        val tarihYanlis = binding.tarihYanlis

        tarihYanlis.minValue = 0
        tarihYanlis.maxValue = 40
        tarihYanlis.wrapSelectorWheel = true
        tarihYanlis.setOnValueChangedListener { _, _, newVal ->
            tarihYanlisDegeri = newVal
        }

        val cografyaDogru = binding.cografyaDogru
        cografyaDogru.minValue = 0
        cografyaDogru.maxValue = 40
        cografyaDogru.wrapSelectorWheel = true
        cografyaDogru.setOnValueChangedListener { _, _, newVal ->
            cogDogruDegeri = newVal
        }

        val cografyaYanlis = binding.cografyaYanlis
        cografyaYanlis.minValue = 0
        cografyaYanlis.maxValue = 40
        cografyaYanlis.wrapSelectorWheel = true
        cografyaYanlis.setOnValueChangedListener { _, _, newVal ->
            cogYanlisDegeri = newVal
        }

        val felsefeDogru = binding.felsefeDogru
        felsefeDogru.minValue = 0
        felsefeDogru.maxValue = 40
        felsefeDogru.wrapSelectorWheel = true
        felsefeDogru.setOnValueChangedListener { _, _, newVal ->
            felsefeDogruDegeri = newVal
        }

        val felsefeYanlis = binding.felsefeYanlis
        felsefeYanlis.minValue = 0
        felsefeYanlis.maxValue = 40
        felsefeYanlis.wrapSelectorWheel = true
        felsefeYanlis.setOnValueChangedListener { _, _, newVal ->
            felsefeYanlisDegeri = newVal
        }


        val dinDogru = binding.dinDogru
        dinDogru.minValue = 0
        dinDogru.maxValue = 40
        dinDogru.wrapSelectorWheel = true
        dinDogru.setOnValueChangedListener { _, _, newVal ->
            dinDogruDegeri = newVal
        }

        val dinYanlis = binding.dinYanlis
        dinYanlis.minValue = 0
        dinYanlis.maxValue = 40
        dinYanlis.wrapSelectorWheel = true
        dinYanlis.setOnValueChangedListener { _, _, newVal ->
            dinYanlisDegeri = newVal
        }

        val matDogru = binding.matDogru
        matDogru.minValue = 0
        matDogru.maxValue = 40
        matDogru.wrapSelectorWheel = true
        matDogru.setOnValueChangedListener { _, _, newVal ->
            matDogruDegeri = newVal
        }

        val matYanlis = binding.matYanlis
        matYanlis.minValue = 0
        matYanlis.maxValue = 40
        matYanlis.wrapSelectorWheel = true
        matYanlis.setOnValueChangedListener { _, _, newVal ->
            matYanlisDegeri = newVal
        }

        val geoDogru = binding.geoDogru
        geoDogru.minValue = 0
        geoDogru.maxValue = 40
        geoDogru.wrapSelectorWheel = true
        geoDogru.setOnValueChangedListener { _, _, newVal ->
            geoDogruDegeri = newVal
        }

        val geoYanlis = binding.geoYanlis
        geoYanlis.minValue = 0
        geoYanlis.maxValue = 40
        geoYanlis.wrapSelectorWheel = true
        geoYanlis.setOnValueChangedListener { _, _, newVal ->
            geoYanlisDegeri = newVal
        }

        val fizDogru = binding.fizDogru
        fizDogru.minValue = 0
        fizDogru.maxValue = 40
        fizDogru.wrapSelectorWheel = true
        fizDogru.setOnValueChangedListener { _, _, newVal ->
            fizikDogruDegeri = newVal
        }

        val fizYanlis = binding.fizYanlis
        fizYanlis.minValue = 0
        fizYanlis.maxValue = 40
        fizYanlis.wrapSelectorWheel = true
        fizYanlis.setOnValueChangedListener { _, _, newVal ->
            fizikYanlisDegeri = newVal
        }

        val kimDogru = binding.kimDogru
        kimDogru.minValue = 0
        kimDogru.maxValue = 40
        kimDogru.wrapSelectorWheel = true
        kimDogru.setOnValueChangedListener { _, _, newVal ->
            kimyaDogruDegeri = newVal
        }

        val kimYanlis = binding.kimYanlis
        kimYanlis.minValue = 0
        kimYanlis.maxValue = 40
        kimYanlis.wrapSelectorWheel = true
        kimYanlis.setOnValueChangedListener { _, _, newVal ->
            kimyaYanlisDegeri = newVal
        }

        val biDogru = binding.biDogru
        biDogru.minValue = 0
        biDogru.maxValue = 40
        biDogru.wrapSelectorWheel = true
        biDogru.setOnValueChangedListener { _, _, newVal ->
            biyolojiDogruDegeri = newVal
        }

        val biYanlis = binding.biYanlis
        biYanlis.minValue = 0
        biYanlis.maxValue = 40
        biYanlis.wrapSelectorWheel = true
        biYanlis.setOnValueChangedListener { _, _, newVal ->
            biyolojiYanlisDegeri = newVal
        }


    }
}




