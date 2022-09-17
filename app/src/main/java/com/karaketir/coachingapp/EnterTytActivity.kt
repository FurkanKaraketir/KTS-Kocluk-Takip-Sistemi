@file:Suppress("DEPRECATION")

package com.karaketir.coachingapp

//noinspection SuspiciousImport
import android.R
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

        val cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)


        val studyType = intent.getStringExtra("studyType")

        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            val kurumKodu = it.get("kurumKodu").toString().toInt()

            db.collection("School").document(kurumKodu.toString()).collection("Student")
                .document(auth.uid.toString()).get().addOnSuccessListener { ogrenci ->
                    val ogretmenID = ogrenci.get("teacher").toString()

                    db.collection("School").document(kurumKodu.toString()).collection("Teacher")
                        .document(ogretmenID).collection("Denemeler")
                        .whereGreaterThan("bitisTarihi", cal.time).whereEqualTo("tür", studyType)
                        .addSnapshotListener { value, error ->
                            if (error != null) {
                                println(error.localizedMessage)
                            }
                            denemeList.clear()
                            if (value != null) {

                                for (deneme in value) {
                                    denemeList.add(deneme.get("denemeAdi").toString())
                                }
                                val denemeAdapter = ArrayAdapter(
                                    this@EnterTytActivity, R.layout.simple_spinner_item, denemeList
                                )
                                denemeAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                                denemeAdiSpinner.adapter = denemeAdapter
                                denemeAdiSpinner.onItemSelectedListener = this
                            }

                        }

                }

        }

        val documentID = UUID.randomUUID().toString()

        val button = binding.denemeKaydetButton

        val turDogru = binding.turkDogru
        val turkYanlisEditText = binding.turkYanlisEditText
        val turkYanlisBtn = binding.turkYanlisButton

        val cogDogru = binding.cogDogru
        val cogYanlisEditText = binding.cogYanlisEditText

        val cogYanlis = binding.cogYanlis

        val tarihDogru = binding.tarihDogru

        val tarihYanlis = binding.tarihYanlis
        val tarihYanlisEdit = binding.tarihYanlisEditText

        val felDogru = binding.felDogru
        val felYanlis = binding.felYanlis
        val felYanlisEdit = binding.felYanlisEditText

        val dinDogru = binding.dinDogru
        val dinYanlis = binding.dinYanlis
        val dinYanlisEdit = binding.dinYanlisEditText


        val matDogru = binding.matDogru
        val matYanlis = binding.matYanlis
        val matYanlisEdit = binding.matYanlisEditText

        val geometriDogru = binding.geometriDogru
        val geometriYanlis = binding.geometriYanlis
        val geoYanlisEdit = binding.geometriYanlisEditText

        val fizDogru = binding.fizDogru
        val fizYanlis = binding.fizYanlis
        val fizYanlisEdit = binding.fizYanlisEditText

        val kimyaDogru = binding.kimyaDogru
        val kimyaYanlis = binding.kimyaYanlis
        val kimyaYanlisEdit = binding.kimyaYanlisEditText

        val biyolojiDogru = binding.biyolojiDogru
        val biyolojiYanlis = binding.biyolojiYanlis
        val biyolojiYanlisEdit = binding.biyolojiYanlisEditText




        fizYanlis.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", studyType)
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Fizik")
            this.startActivity(intent)
        }
        biyolojiYanlis.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", studyType)
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Biyoloji")
            this.startActivity(intent)
        }

        kimyaYanlis.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", studyType)
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Kimya")
            this.startActivity(intent)
        }

        geometriYanlis.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", studyType)
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Geometri")
            this.startActivity(intent)
        }

        matYanlis.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", studyType)
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Matematik")
            this.startActivity(intent)
        }

        dinYanlis.setOnClickListener {
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
                turkceDogruDegeri = if (turDogru.text.isNotEmpty()) {
                    turDogru.text.toString().toInt()

                } else {
                    0
                }
                turkceYanlisDegeri = if (turkYanlisEditText.text.isNotEmpty()) {
                    turkYanlisEditText.text.toString().toInt()

                } else {
                    0
                }
                cogDogruDegeri = if (cogDogru.text.isNotEmpty()) {
                    cogDogru.text.toString().toInt()


                } else {
                    0
                }
                cogYanlisDegeri = if (cogYanlisEditText.text.isNotEmpty()) {
                    cogYanlisEditText.text.toString().toInt()

                } else {
                    0
                }
                tarihDogruDegeri = if (tarihDogru.text.isNotEmpty()) {
                    tarihDogru.text.toString().toInt()


                } else {
                    0
                }
                tarihYanlisDegeri = if (tarihYanlisEdit.text.isNotEmpty()) {
                    tarihYanlisEdit.text.toString().toInt()

                } else {
                    0
                }
                felsefeDogruDegeri = if (felDogru.text.isNotEmpty()) {
                    felDogru.text.toString().toInt()

                } else {
                    0
                }
                felsefeYanlisDegeri = if (felYanlisEdit.text.isNotEmpty()) {
                    felYanlisEdit.text.toString().toInt()

                } else {
                    0
                }
                dinDogruDegeri = if (dinDogru.text.isNotEmpty()) {
                    dinDogru.text.toString().toInt()

                } else {
                    0
                }
                dinYanlisDegeri = if (dinYanlisEdit.text.isNotEmpty()) {
                    dinYanlisEdit.text.toString().toInt()

                } else {
                    0
                }
                matDogruDegeri = if (matDogru.text.isNotEmpty()) {
                    matDogru.text.toString().toInt()

                } else {
                    0
                }
                matYanlisDegeri = if (matYanlisEdit.text.isNotEmpty()) {
                    matYanlisEdit.text.toString().toInt()

                } else {
                    0
                }
                geoDogruDegeri = if (geometriDogru.text.isNotEmpty()) {
                    geometriDogru.text.toString().toInt()

                } else {
                    0
                }
                geoYanlisDegeri = if (geoYanlisEdit.text.isNotEmpty()) {
                    geoYanlisEdit.text.toString().toInt()
                } else {
                    0
                }
                fizikDogruDegeri = if (fizDogru.text.isNotEmpty()) {
                    fizDogru.text.toString().toInt()
                } else {
                    0
                }
                fizikYanlisDegeri = if (fizYanlisEdit.text.isNotEmpty()) {
                    fizYanlisEdit.text.toString().toInt()
                } else {
                    0
                }
                kimyaDogruDegeri = if (kimyaDogru.text.isNotEmpty()) {
                    kimyaDogru.text.toString().toInt()
                } else {
                    0
                }
                kimyaYanlisDegeri = if (kimyaYanlisEdit.text.isNotEmpty()) {
                    kimyaYanlisEdit.text.toString().toInt()
                } else {
                    0
                }
                biyolojiDogruDegeri = if (biyolojiDogru.text.isNotEmpty()) {
                    biyolojiDogru.text.toString().toInt()

                } else {
                    0
                }
                biyolojiYanlisDegeri = if (biyolojiYanlisEdit.text.isNotEmpty()) {
                    biyolojiYanlisEdit.text.toString().toInt()


                } else {
                    0
                }

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

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        denemeAdi = denemeList[p2]
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }
}





