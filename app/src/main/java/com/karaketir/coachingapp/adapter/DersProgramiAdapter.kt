package com.karaketir.coachingapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.databinding.ProgramRowBinding
import com.karaketir.coachingapp.models.Ders

class DersProgramiAdapter(private val dersList: ArrayList<Ders>) :
    RecyclerView.Adapter<DersProgramiAdapter.DersHolder>() {

    class DersHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ProgramRowBinding.bind(itemView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DersHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.program_row, parent, false)
        return DersHolder(view)
    }

    override fun getItemCount(): Int {
        return dersList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DersHolder, position: Int) {
        with(holder) {

            val myItem = dersList[position]
            val dersAdiText = binding.dersAdiProgram
            val dersTuruText = binding.dersTuruProgram
            val dersSureText = binding.derSureProgram
            val dersNumaraText = binding.dersNumaraProgram

            val dersNumber = (myItem.dersNumara.toInt()+1).toString()

            dersAdiText.text = myItem.dersAdi
            dersTuruText.text = myItem.dersTuru
            dersSureText.text = myItem.dersSure.toString() + "dk"
            dersNumaraText.text = "$dersNumber. Ders"


        }
    }
}