package com.karaketir.coachingapp.adapter

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

    override fun onBindViewHolder(holder: ClassHolder, position: Int) {
        with(holder) {
            db = FirebaseFirestore.getInstance()
            auth = FirebaseAuth.getInstance()

            binding.dersAdiTextView.text = classList[position].dersAdi
            binding.dersAdiCard.setOnClickListener {
                val intent = Intent(
                    holder.itemView.context, ActivityStudiesByClasses::class.java
                )
                intent.putExtra("studentID", classList[position].studentID)
                intent.putExtra("dersAdi", classList[position].dersAdi)
                intent.putExtra("baslangicTarihi", classList[position].baslangicTarihi)
                intent.putExtra("bitisTarihi", classList[position].bitisTarihi)
                intent.putExtra("secilenZamanAraligi", classList[position].secilenZamanAraligi)
                holder.itemView.context.startActivity(intent)
            }


        }
    }

    override fun getItemCount(): Int {
        return classList.size
    }
}