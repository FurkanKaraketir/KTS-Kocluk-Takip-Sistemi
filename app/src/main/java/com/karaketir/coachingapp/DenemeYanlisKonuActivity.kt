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
import com.karaketir.coachingapp.adapter.DenemeKonulariShowMoreRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityDenemeYanlisKonuBinding
import com.karaketir.coachingapp.models.Item
import com.karaketir.coachingapp.models.SubItem


class DenemeYanlisKonuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDenemeYanlisKonuBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerViewDenemeKonulariRecyclerAdapter: DenemeKonulariShowMoreRecyclerAdapter
    private lateinit var recyclerViewDenemeYanlisKonu: RecyclerView

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDenemeYanlisKonuBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore
        val itemList: ArrayList<Item> = ArrayList()
        binding.backButton.setOnClickListener {
            finish()
        }

        recyclerViewDenemeYanlisKonu = binding.recyclerViewDenemeYanlisKonu
        val konuList = ArrayList<String>()

        val layoutManager = LinearLayoutManager(applicationContext)

        val intent = intent

        val tur = intent.getStringExtra("tür").toString()
        val dersAdi = intent.getStringExtra("dersAdi").toString()
        val denemeID = intent.getStringExtra("documentID").toString()



        db.collection("Lessons").document(dersAdi).collection(tur)
            .orderBy("konuAdi", Query.Direction.ASCENDING).addSnapshotListener { value2, _ ->
                if (value2 != null) {
                    konuList.clear()

                    for (document in value2) {

                        try {
                            val konuIndex = document.get("arrayType") as ArrayList<*>
                            if ("deneme" in konuIndex) {
                                konuList.add(document.id)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
                        }


                    }



                    konuList.sortBy { it }
                    recyclerViewDenemeYanlisKonu.layoutManager = layoutManager

                    for (i in konuList) {
                        val subItemList: ArrayList<SubItem> = ArrayList()

                        db.collection("Lessons").document(dersAdi).collection(tur).document(i)
                            .collection("AltKonu").addSnapshotListener { value, _ ->
                                if (value != null) {
                                    for (a in value) {
                                        val newSubItem = SubItem(a.get("konuAdi").toString())
                                        subItemList.add(newSubItem)
                                    }
                                    val item = Item(i, subItemList, dersAdi, tur)
                                    itemList.add(item)
                                }
                                recyclerViewDenemeKonulariRecyclerAdapter =
                                    DenemeKonulariShowMoreRecyclerAdapter(
                                        itemList, denemeID, dersAdi
                                    )

                                recyclerViewDenemeYanlisKonu.adapter =
                                    recyclerViewDenemeKonulariRecyclerAdapter

                                recyclerViewDenemeYanlisKonu.setItemViewCacheSize(konuList.size)

                                recyclerViewDenemeKonulariRecyclerAdapter.notifyDataSetChanged()
                            }

                    }


                }
            }


    }

}


