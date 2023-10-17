package com.karaketir.coachingapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.databinding.KonuYanlisShowMoreRowBinding
import com.karaketir.coachingapp.models.Item


class DenemeKonulariShowMoreRecyclerAdapter(
    private val konuListesi: ArrayList<Item>,
    val denemeID: String,
    val dersAdi: String,
    private val kurumKodu: Int
) : RecyclerView.Adapter<DenemeKonulariShowMoreRecyclerAdapter.KonuHolder>() {
    private val viewPool = RecycledViewPool()
    private val itemList = konuListesi
    private lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth

    class KonuHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val binding = KonuYanlisShowMoreRowBinding.bind(itemView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KonuHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.konu_yanlis_show_more_row, parent, false)
        return KonuHolder(view)
    }

    override fun getItemCount(): Int {
        return konuListesi.size
    }

    override fun onBindViewHolder(holder: KonuHolder, position: Int) {
        with(holder) {
            if (konuListesi.isNotEmpty() && position >= 0 && position < konuListesi.size) {

                val myItem = itemList[position]

                auth = Firebase.auth
                db = Firebase.firestore

                db.collection("Lessons").document(myItem.dersAdi).collection(myItem.tur)
                    .document(myItem.itemTitle).get().addOnSuccessListener {
                        binding.denemeKonuAdi.text = it.get("konuAdi").toString()
                    }

                binding.showButton.setOnClickListener {
                    if (binding.showMoreSubjects.visibility == View.VISIBLE) {
                        binding.showMoreSubjects.visibility = View.GONE
                    } else {
                        binding.showMoreSubjects.visibility = View.VISIBLE
                    }
                }


                // Create layout manager with initial prefetch item count

                // Create layout manager with initial prefetch item count


                val layoutManager = LinearLayoutManager(
                    binding.showMoreSubjects.context, LinearLayoutManager.VERTICAL, false
                )
                layoutManager.initialPrefetchItemCount = myItem.getSubItemList().size

                // Create sub item view adapter


                // Create sub item view adapter
                val subItemAdapter =
                    DenemeKonulariRecyclerAdapter(myItem.getSubItemList(), myItem.dersAdi)
                binding.showMoreSubjects.layoutManager = layoutManager
                binding.showMoreSubjects.adapter = subItemAdapter
                binding.showMoreSubjects.setRecycledViewPool(viewPool)

                binding.saveSubjectButton.setOnClickListener {
                    val konuHash = subItemAdapter.konuHash



                    if (konuHash.isEmpty()) {
                        println("Error Here")
                    }

                    var konuToplamYanlis = 0
                    for (j in konuHash.keys) {
                        val konuHashMap = hashMapOf(
                            "konuAdi" to j, "yanlisSayisi" to konuHash[j]
                        )


                        db.collection("School").document(kurumKodu.toString()).collection("Student")
                            .document(auth.uid.toString()).collection("Denemeler")
                            .document(denemeID).collection(dersAdi)
                            .document(binding.denemeKonuAdi.text.toString()).collection("AltKonu")
                            .document(j).set(
                                konuHashMap
                            ).addOnSuccessListener {

                                konuToplamYanlis += konuHash[j]!!


                                val ustKonuHashMap = hashMapOf(
                                    "konuAdi" to binding.denemeKonuAdi.text.toString(),
                                    "yanlisSayisi" to konuToplamYanlis
                                )
                                db.collection("School").document(kurumKodu.toString())
                                    .collection("Student").document(auth.uid.toString())
                                    .collection("Denemeler").document(denemeID).collection(dersAdi)
                                    .document(binding.denemeKonuAdi.text.toString())
                                    .set(ustKonuHashMap).addOnSuccessListener {
                                        Toast.makeText(
                                            holder.itemView.context,
                                            "İşlem Başarılı",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                            }


                    }


                }


            }

        }

    }
}