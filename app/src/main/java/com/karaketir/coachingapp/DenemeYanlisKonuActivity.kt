package com.karaketir.coachingapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.adapter.DenemeKonulariRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityDenemeYanlisKonuBinding


class DenemeYanlisKonuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDenemeYanlisKonuBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerViewDenemeKonulariRecyclerAdapter: DenemeKonulariRecyclerAdapter
    private lateinit var recyclerViewDenemeYanlisKonu: RecyclerView
    private var paragrafKonuList = arrayOf(
        "Paragraf Tamamlama",
        "Cümle Yerleştirme",
        "Cümlelerin Yerini Değiştirme",
        "Cümleleri Sıralama",
        "Anlatım Teknikleri (Öyküleme, Betimleme...)",
        "Düşünceyi Geliştirme Yolları (Tanık Gösterme, Örnekleme...)",
        "Bakış Açıları (Gözlemci, Kahraman...)",
        "Yapı (Giriş, Gelişme, Sonuç)",
        "Ana Düşünce (Ne Anlatılmak İsteniyor?)",
        "Yardımcı Düşünce (Olumsuz Soru Kökleri)",
        "Düşüncenin Akışını Bozan Cümle",
        "Paragraf Hangi Soruya Karşılık Yazılmıştır?",
        "Başlık",
        "Paragrafı İkiye Bölme"
    )

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDenemeYanlisKonuBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore
        val saveButton = binding.yanlisKonuSave
        recyclerViewDenemeYanlisKonu = binding.recyclerViewDenemeYanlisKonu
        val konuList = ArrayList<String>()

        val layoutManager = LinearLayoutManager(applicationContext)
        recyclerViewDenemeYanlisKonu.layoutManager = layoutManager
        recyclerViewDenemeKonulariRecyclerAdapter = DenemeKonulariRecyclerAdapter(konuList)
        recyclerViewDenemeYanlisKonu.adapter = recyclerViewDenemeKonulariRecyclerAdapter

        val intent = intent

        val tur = intent.getStringExtra("tür")
        val dersAdi = intent.getStringExtra("dersAdi")
        val denemeID = intent.getStringExtra("documentID")
        saveButton.setOnClickListener {
            val konuHash = recyclerViewDenemeKonulariRecyclerAdapter.konuHash
            var kurumKodu: Int
            db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
                kurumKodu = it.get("kurumKodu").toString().toInt()

                for (j in konuHash.keys) {
                    val konuHashMap = hashMapOf(
                        "konuAdi" to j, "yanlisSayisi" to konuHash[j]
                    )


                    if (denemeID != null) {
                        db.collection("School").document(kurumKodu.toString()).collection("Student")
                            .document(auth.uid.toString()).collection("Denemeler")
                            .document(denemeID).collection(dersAdi!!).add(
                                konuHashMap
                            ).addOnSuccessListener {
                                Toast.makeText(this, "İşlem Başarılı", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                    }


                }


            }


        }




        if (dersAdi != null) {
            db.collection("Lessons").document(dersAdi).collection(tur!!)
                .orderBy("konuAdi", Query.Direction.ASCENDING).addSnapshotListener { value2, _ ->
                    if (value2 != null) {
                        konuList.clear()

                        for (document in value2) {

                            val konuIndex = document.get("arrayType") as ArrayList<*>
                            if ("deneme" in konuIndex) {
                                konuList.add(document.get("konuAdi").toString())
                            }

                        }
                        if (dersAdi == "Türkçe-Edebiyat") {
                            for (i in paragrafKonuList) {
                                konuList.add(i)
                            }
                        }


                        konuList.sortBy { it }

                        recyclerViewDenemeYanlisKonu.setItemViewCacheSize(konuList.size)

                        recyclerViewDenemeKonulariRecyclerAdapter.notifyDataSetChanged()
                    }
                }
        }


    }

}


