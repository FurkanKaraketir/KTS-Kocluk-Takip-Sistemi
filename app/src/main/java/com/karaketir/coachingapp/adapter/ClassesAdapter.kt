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

        if (classList.isNotEmpty() && position >= 0 && position < classList.size) {
            // code to access the element at the specified index
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
                binding.toplamCalismaTextView.text =
                    classList[position].toplamCalisma.toString() + "dk"

                binding.soruSayisiTextView.text =
                    classList[position].cozulenSoru.toString() + " Soru"
                if (classList[position].toplamCalisma != 0 && classList[position].cozulenSoru != 0) {
                    binding.classIcon.setImageResource(R.drawable.ic_baseline_check_circle_outline_24)
                } else {
                    binding.classIcon.setImageResource(R.drawable.ic_baseline_error_outline_24)
                }

            }

        } else {
            // handle the error
            println("Hata")
        }


    }

    override fun getItemCount(): Int {
        return classList.size
    }
}