package com.karaketir.coachingapp.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karaketir.coachingapp.ActivityStudiesByClasses
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.databinding.ClassGridRowBinding

class ClassesAdapter(private val classList: ArrayList<com.karaketir.coachingapp.models.Class>) :
    RecyclerView.Adapter<ClassesAdapter.ClassHolder>() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    class ClassHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ClassGridRowBinding.bind(itemView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.class_grid_row, parent, false)
        return ClassHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ClassHolder, position: Int) {

        with(holder) {
            if (classList.isNotEmpty() && position >= 0 && position < classList.size) {

                val myItem = classList[position]

                db = FirebaseFirestore.getInstance()
                auth = FirebaseAuth.getInstance()

                binding.dersAdiTextView.text = myItem.dersAdi
                binding.dersAdiCard.setOnClickListener {
                    val intent = Intent(
                        holder.itemView.context, ActivityStudiesByClasses::class.java
                    )
                    intent.putExtra("studentID", myItem.studentID)
                    intent.putExtra("dersAdi", myItem.dersAdi)
                    intent.putExtra("baslangicTarihi", myItem.baslangicTarihi)
                    intent.putExtra("bitisTarihi", myItem.bitisTarihi)
                    intent.putExtra("secilenZamanAraligi", myItem.secilenZamanAraligi)
                    holder.itemView.context.startActivity(intent)
                }
                binding.toplamCalismaTextView.text = myItem.toplamCalisma.toString() + "dk"

                binding.soruSayisiTextView.text = myItem.cozulenSoru.toString() + " Soru"

                if (myItem.toplamCalisma != 0 && myItem.cozulenSoru != 0) {
                    binding.classIcon.setImageResource(R.drawable.ic_baseline_check_circle_outline_24)
                } else {
                    binding.classIcon.setImageResource(R.drawable.ic_baseline_error_outline_24)
                }

            }

        }


    }

    override fun getItemCount(): Int {
        return classList.size
    }
}