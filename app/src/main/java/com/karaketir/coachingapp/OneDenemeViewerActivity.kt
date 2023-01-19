package com.karaketir.coachingapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.databinding.ActivityOneDenemeViewerBinding

class OneDenemeViewerActivity : AppCompatActivity() {

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


    private lateinit var binding: ActivityOneDenemeViewerBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var denemeStudentID = ""
    private var secilenZamanAraligi = ""
    private var denemeTur = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityOneDenemeViewerBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
        val denemeID = intent.getStringExtra("denemeID")
        denemeTur = intent.getStringExtra("denemeTür").toString()
        denemeStudentID = intent.getStringExtra("denemeStudentID").toString()
        secilenZamanAraligi = intent.getStringExtra("secilenZamanAraligi").toString()
        val denemeTitle = binding.oneDenemeTitle
        val turkNetTextView = binding.turkceNetTextView
        val tarihNetTextView = binding.tarihNetTextView
        val cografyaNetTextView = binding.cografyaNetTextView
        val felsefeNetTextView = binding.felsefeNetTextView
        val dinNetTextView = binding.dinNetTextView
        val matematikNetTextView = binding.matematikNetTextView
        val geometriNetTextView = binding.geometriNetTextView
        val fizikNetTextView = binding.fizikNetTextView
        val kimyaNetTextView = binding.kimyaNetTextView
        val biyolojiNetTextView = binding.biyolojiNetTextView
        val toplamNetTextView = binding.toplamNetTextView


        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            val kurumKodu = it.get("kurumKodu").toString().toInt()

            db.collection("School").document(kurumKodu.toString()).collection("Student")
                .document(denemeStudentID).collection("Denemeler").document(denemeID!!).get()
                .addOnSuccessListener { deneme ->


                    denemeTitle.text = "Deneme Adı: " + deneme.get("denemeAdi").toString()
                    turkNetTextView.text = "Türkçe Net: " + deneme.get("turkceNet").toString()
                    tarihNetTextView.text = "Tarih Net: " + deneme.get("tarihNet").toString()
                    cografyaNetTextView.text = "Coğrafya Net: " + deneme.get("cogNet").toString()
                    felsefeNetTextView.text = "Felsefe Net: " + deneme.get("felNet").toString()
                    dinNetTextView.text = "Din Net: " + deneme.get("dinNet").toString()
                    matematikNetTextView.text = "Matematik Net: " + deneme.get("matNet").toString()
                    geometriNetTextView.text = "Geometri Net: " + deneme.get("geoNet").toString()
                    fizikNetTextView.text = "Fizik Net: " + deneme.get("fizNet").toString()
                    kimyaNetTextView.text = "Kimya Net: " + deneme.get("kimyaNet").toString()
                    biyolojiNetTextView.text = "Biyoloji Net: " + deneme.get("biyoNet").toString()
                    toplamNetTextView.text = "Toplam Net: " + deneme.get("toplamNet").toString()

                }


        }


        cografyaNetTextView.setOnClickListener {

            alertGoster("Coğrafya")

        }
        tarihNetTextView.setOnClickListener {
            alertGoster("Tarih")

        }
        felsefeNetTextView.setOnClickListener {
            alertGoster("Felsefe")
        }
        dinNetTextView.setOnClickListener {
            alertGoster("Din")

        }
        matematikNetTextView.setOnClickListener {
            alertGoster("Matematik")

        }

        turkNetTextView.setOnClickListener {
            alertGoster("Türkçe-Edebiyat")

        }
        geometriNetTextView.setOnClickListener {
            alertGoster("Geometri")

        }
        fizikNetTextView.setOnClickListener {
            alertGoster("Fizik")

        }
        kimyaNetTextView.setOnClickListener {
            alertGoster("Kimya")

        }
        biyolojiNetTextView.setOnClickListener {
            alertGoster("Biyoloji")

        }
        toplamNetTextView.setOnClickListener {

            val intent = Intent(this, DenemeNetGraphByTimeActivity::class.java)
            intent.putExtra("dersAdi", "ToplamNet")
            intent.putExtra("denemeOwnerID", denemeStudentID)
            intent.putExtra("denemeTür", denemeTur)
            intent.putExtra("zamanAraligi", secilenZamanAraligi)
            this.startActivity(intent)
        }


    }

    private fun alertGoster(dersAdi: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Grafik Türü")
        alertDialog.setMessage("Görmek İstedğiniz Grafik Türünü Seçiniz")
        alertDialog.setPositiveButton("$dersAdi Deneme-Net Grafiği") { _, _ ->

            val intent = Intent(this, DenemeNetGraphByTimeActivity::class.java)
            intent.putExtra("dersAdi", dersAdi)
            intent.putExtra("denemeTür", denemeTur)
            intent.putExtra("denemeOwnerID", denemeStudentID)
            intent.putExtra("zamanAraligi", secilenZamanAraligi)
            this.startActivity(intent)

        }
        alertDialog.setNegativeButton("Yanlış Soru-Konu Dağılımı Grafiği") { _, _ ->

            val intent = Intent(this, DenemeGraphActivity::class.java)
            intent.putExtra("dersAdi", dersAdi)
            intent.putExtra("denemeTür", denemeTur)
            intent.putExtra("denemeOwnerID", denemeStudentID)
            intent.putExtra("zamanAraligi", secilenZamanAraligi)
            this.startActivity(intent)

        }
        alertDialog.show()
    }
}