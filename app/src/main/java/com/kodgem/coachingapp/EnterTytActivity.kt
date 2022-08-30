@file:Suppress("DEPRECATION")

package com.kodgem.coachingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kodgem.coachingapp.databinding.ActivityEnterTytBinding
import java.util.*

class EnterTytActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEnterTytBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterTytBinding.inflate(layoutInflater)

        setContentView(binding.root)

        auth = Firebase.auth

        db = Firebase.firestore
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

        val denemeAdiEditText = binding.denemeAdiEditText
        fizYanlis.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", "TYT")
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Fizik")
            this.startActivity(intent)
        }
        biyolojiYanlis.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", "TYT")
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Biyoloji")
            this.startActivity(intent)
        }

        kimyaYanlis.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", "TYT")
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Kimya")
            this.startActivity(intent)
        }

        geometriYanlis.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", "TYT")
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Geometri")
            this.startActivity(intent)
        }

        matYanlis.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", "TYT")
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Matematik")
            this.startActivity(intent)
        }

        dinYanlis.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", "TYT")
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Din")
            this.startActivity(intent)
        }

        felYanlis.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", "TYT")
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Felsefe")
            this.startActivity(intent)
        }

        tarihYanlis.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", "TYT")
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Tarih")
            this.startActivity(intent)
        }
        cogYanlis.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", "TYT")
            intent.putExtra("documentID", documentID)
            intent.putExtra("dersAdi", "Coğrafya")
            this.startActivity(intent)
        }
        turkYanlisBtn.setOnClickListener {
            val intent = Intent(this, DenemeYanlisKonuActivity::class.java)
            intent.putExtra("tür", "TYT")
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
                if (turDogru.text.isNotEmpty()) {
                    if (turkYanlisEditText.text.isNotEmpty()) {
                        if (cogDogru.text.isNotEmpty()) {
                            if (cogYanlisEditText.text.isNotEmpty()) {
                                if (tarihDogru.text.isNotEmpty()) {
                                    if (tarihYanlisEdit.text.isNotEmpty()) {
                                        if (felDogru.text.isNotEmpty()) {
                                            if (felYanlisEdit.text.isNotEmpty()) {
                                                if (dinDogru.text.isNotEmpty()) {
                                                    if (dinYanlisEdit.text.isNotEmpty()) {
                                                        if (matDogru.text.isNotEmpty()) {
                                                            if (matYanlisEdit.text.isNotEmpty()) {
                                                                if (geometriDogru.text.isNotEmpty()) {
                                                                    if (geoYanlisEdit.text.isNotEmpty()) {
                                                                        if (fizDogru.text.isNotEmpty()) {
                                                                            if (fizYanlisEdit.text.isNotEmpty()) {
                                                                                if (kimyaDogru.text.isNotEmpty()) {
                                                                                    if (kimyaYanlisEdit.text.isNotEmpty()) {
                                                                                        if (biyolojiDogru.text.isNotEmpty()) {
                                                                                            if (biyolojiYanlisEdit.text.isNotEmpty()) {
                                                                                                if (denemeAdiEditText.text.isNotEmpty()) {
                                                                                                    button.isClickable =
                                                                                                        false
                                                                                                    val turkceNet =
                                                                                                        turDogru.text.toString()
                                                                                                            .toInt() - (turkYanlisEditText.text.toString()
                                                                                                            .toInt() * 0.25f)
                                                                                                    val tarihNet =
                                                                                                        tarihDogru.text.toString()
                                                                                                            .toInt() - (tarihYanlisEdit.text.toString()
                                                                                                            .toInt() * 0.25f)
                                                                                                    val cogNet =
                                                                                                        cogDogru.text.toString()
                                                                                                            .toInt() - (cogYanlisEditText.text.toString()
                                                                                                            .toInt() * 0.25f)
                                                                                                    val felNet =
                                                                                                        felDogru.text.toString()
                                                                                                            .toInt() - (felYanlisEdit.text.toString()
                                                                                                            .toInt() * 0.25f)
                                                                                                    val dinNet =
                                                                                                        dinDogru.text.toString()
                                                                                                            .toInt() - (dinYanlisEdit.text.toString()
                                                                                                            .toInt() * 0.25f)
                                                                                                    val matNet =
                                                                                                        matDogru.text.toString()
                                                                                                            .toInt() - (matYanlisEdit.text.toString()
                                                                                                            .toInt() * 0.25f)
                                                                                                    val fizNet =
                                                                                                        fizDogru.text.toString()
                                                                                                            .toInt() - (fizYanlisEdit.text.toString()
                                                                                                            .toInt() * 0.25f)
                                                                                                    val kimyaNet =
                                                                                                        kimyaDogru.text.toString()
                                                                                                            .toInt() - (kimyaYanlisEdit.text.toString()
                                                                                                            .toInt() * 0.25f)
                                                                                                    val biyoNet =
                                                                                                        biyolojiDogru.text.toString()
                                                                                                            .toInt() - (biyolojiYanlisEdit.text.toString()
                                                                                                            .toInt() * 0.25f)
                                                                                                    val geoNet =
                                                                                                        geometriDogru.text.toString()
                                                                                                            .toInt() - (geoYanlisEdit.text.toString()
                                                                                                            .toInt() * 0.25f)


                                                                                                    val toplamNet =
                                                                                                        turkceNet + tarihNet + cogNet + felNet + dinNet + matNet + fizNet + kimyaNet + biyoNet + geoNet
                                                                                                    val deneme =
                                                                                                        hashMapOf(
                                                                                                            "denemeTürü" to "TYT",
                                                                                                            "denemeAdi" to denemeAdiEditText.text.toString(),
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
                                                                                                            "Denemeler"
                                                                                                        )
                                                                                                        .document(
                                                                                                            documentID
                                                                                                        )
                                                                                                        .set(
                                                                                                            deneme
                                                                                                        )
                                                                                                        .addOnSuccessListener {
                                                                                                            Toast.makeText(
                                                                                                                this,
                                                                                                                "İşlem Başarılı",
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
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}





