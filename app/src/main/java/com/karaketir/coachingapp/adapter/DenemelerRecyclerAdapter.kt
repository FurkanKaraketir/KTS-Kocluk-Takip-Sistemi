package com.karaketir.coachingapp.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.OneDenemeViewerActivity
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.databinding.DenemeGridRowBinding
import com.karaketir.coachingapp.models.Deneme
import java.text.SimpleDateFormat

class DenemelerRecyclerAdapter(
    private val denemeList: List<Deneme>, private val secilenZamanAraligi: String
) : RecyclerView.Adapter<DenemelerRecyclerAdapter.DenemeHolder>() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    class DenemeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = DenemeGridRowBinding.bind(itemView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DenemeHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.deneme_grid_row, parent, false)
        return DenemeHolder(view)
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onBindViewHolder(holder: DenemeHolder, position: Int) {

        with(holder) {
            if (denemeList.isNotEmpty() && position >= 0 && position < denemeList.size) {

                val myItem = denemeList[position]

                auth = Firebase.auth
                db = Firebase.firestore
                binding.denemeAdiTextView.text = myItem.denemeAdi
                binding.denemeToplamNetTextView.text =
                    "Toplam Net: " + myItem.denemeToplamNet.toString() + " " + myItem.denemeTur
                val date = myItem.denemeTarihi.toDate()
                val dateFormated = SimpleDateFormat("dd/MM/yyyy").format(date)

                binding.denemeTarihTextView.text = dateFormated

                binding.denemeCard.setOnClickListener {
                    val intent =
                        Intent(holder.itemView.context, OneDenemeViewerActivity::class.java)
                    intent.putExtra("denemeID", myItem.denemeID)
                    intent.putExtra("secilenZamanAraligi", secilenZamanAraligi)
                    intent.putExtra("denemeStudentID", myItem.denemeStudentID)
                    intent.putExtra("denemeTür", myItem.denemeTur)
                    holder.itemView.context.startActivity(intent)
                }
                binding.denemeDeleteStudentButton.setOnClickListener {
                    db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
                        val kurumKodu = it.get("kurumKodu")?.toString()?.toInt()

                        val deleteAlertDialog = AlertDialog.Builder(holder.itemView.context)
                        deleteAlertDialog.setTitle("Deneme Sil")
                        deleteAlertDialog.setMessage("Bu Denemeyi Silmek İstediğinize Emin misiniz?")
                        deleteAlertDialog.setPositiveButton("Sil") { _, _ ->

                            db.collection("School").document(kurumKodu.toString())
                                .collection("Student").document(myItem.denemeStudentID)
                                .collection("Denemeler").document(myItem.denemeID).delete()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        holder.itemView.context,
                                        "İşlem Başarılı!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }

                        deleteAlertDialog.setNegativeButton("İptal") { _, _ ->

                        }
                        deleteAlertDialog.show()


                    }
                }
            }

        }


    }

    override fun getItemCount(): Int {
        return denemeList.size
    }

}