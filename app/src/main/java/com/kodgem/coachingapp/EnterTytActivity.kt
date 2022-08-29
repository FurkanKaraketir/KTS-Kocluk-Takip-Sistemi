package com.kodgem.coachingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
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
        val turYanlis = binding.turkYanlis

        val cogDogru = binding.cogDogru
        val cogYanlis = binding.cogYanlis

        val tarihDogru = binding.tarihDogru
        val tarihYanlis = binding.tarihYanlis

        val felDogru = binding.felDogru
        val felYanlis = binding.felYanlis

        val dinDogru = binding.dinDogru
        val dinYanlis = binding.dinYanlis

        val matDogru = binding.matDogru
        val matYanlis = binding.matYanlis

        val fizDogru = binding.fizDogru
        val fizYanlis = binding.fizYanlis

        val kimyaDogru = binding.kimyaDogru
        val kimyaYanlis = binding.kimyaYanlis

        val biyolojiDogru = binding.biyolojiDogru
        val biyolojiYanlis = binding.biyolojiYanlis

        val denemeAdiEditText = binding.denemeAdiEditText

        button.setOnClickListener {
            button.isClickable = false
            if (turDogru.text.isNotEmpty()) {
                if (turYanlis.text.isNotEmpty()) {
                    if (cogDogru.text.isNotEmpty()) {
                        if (cogYanlis.text.isNotEmpty()) {
                            if (tarihDogru.text.isNotEmpty()) {
                                if (tarihYanlis.text.isNotEmpty()) {
                                    if (felDogru.text.isNotEmpty()) {
                                        if (felYanlis.text.isNotEmpty()) {
                                            if (dinDogru.text.isNotEmpty()) {
                                                if (dinYanlis.text.isNotEmpty()) {
                                                    if (matDogru.text.isNotEmpty()) {
                                                        if (matYanlis.text.isNotEmpty()) {
                                                            if (fizDogru.text.isNotEmpty()) {
                                                                if (fizYanlis.text.isNotEmpty()) {
                                                                    if (kimyaDogru.text.isNotEmpty()) {
                                                                        if (kimyaYanlis.text.isNotEmpty()) {
                                                                            if (biyolojiDogru.text.isNotEmpty()) {
                                                                                if (biyolojiYanlis.text.isNotEmpty()) {
                                                                                    if (denemeAdiEditText.text.isNotEmpty()) {

                                                                                        val turkceNet =
                                                                                            turDogru.text.toString()
                                                                                                .toInt() - (turYanlis.text.toString()
                                                                                                .toInt() * 0.25f)
                                                                                        val tarihNet =
                                                                                            tarihDogru.text.toString()
                                                                                                .toInt() - (tarihYanlis.text.toString()
                                                                                                .toInt() * 0.25f)
                                                                                        val cogNet =
                                                                                            cogDogru.text.toString()
                                                                                                .toInt() - (cogYanlis.text.toString()
                                                                                                .toInt() * 0.25f)
                                                                                        val felNet =
                                                                                            felDogru.text.toString()
                                                                                                .toInt() - (felYanlis.text.toString()
                                                                                                .toInt() * 0.25f)
                                                                                        val dinNet =
                                                                                            dinDogru.text.toString()
                                                                                                .toInt() - (dinYanlis.text.toString()
                                                                                                .toInt() * 0.25f)
                                                                                        val matNet =
                                                                                            matDogru.text.toString()
                                                                                                .toInt() - (matYanlis.text.toString()
                                                                                                .toInt() * 0.25f)
                                                                                        val fizNet =
                                                                                            fizDogru.text.toString()
                                                                                                .toInt() - (fizYanlis.text.toString()
                                                                                                .toInt() * 0.25f)
                                                                                        val kimyaNet =
                                                                                            kimyaDogru.text.toString()
                                                                                                .toInt() - (kimyaYanlis.text.toString()
                                                                                                .toInt() * 0.25f)
                                                                                        val biyoNet =
                                                                                            biyolojiDogru.text.toString()
                                                                                                .toInt() - (biyolojiYanlis.text.toString()
                                                                                                .toInt() * 0.25f)

                                                                                        db.collection(
                                                                                            "User"
                                                                                        ).document(
                                                                                            auth.uid.toString()
                                                                                        ).get()
                                                                                            .addOnSuccessListener {
                                                                                                val kurumKodu =
                                                                                                    it.get(
                                                                                                        "kurumKodu"
                                                                                                    )
                                                                                                        .toString()
                                                                                                val toplamNet =
                                                                                                    turkceNet + tarihNet + cogNet + felNet + dinNet + matNet + fizNet + kimyaNet + biyoNet
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
                                                                                                        "denemeTarihi" to Timestamp.now(),
                                                                                                        "toplamNet" to toplamNet
                                                                                                    )

                                                                                                db.collection(
                                                                                                    "School"
                                                                                                )
                                                                                                    .document(
                                                                                                        kurumKodu
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