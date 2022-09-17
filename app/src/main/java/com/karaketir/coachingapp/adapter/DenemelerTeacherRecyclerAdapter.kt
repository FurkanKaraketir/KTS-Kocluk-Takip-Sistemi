package com.karaketir.coachingapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.databinding.DenemelerTeacherRowBinding

class DenemelerTeacherRecyclerAdapter(private var denemeList: ArrayList<String>) :
    RecyclerView.Adapter<DenemelerTeacherRecyclerAdapter.DenemeHolder>() {
    class DenemeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = DenemelerTeacherRowBinding.bind(itemView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DenemeHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.denemeler_teacher_row, parent, false)
        return DenemeHolder(view)
    }

    override fun onBindViewHolder(holder: DenemeHolder, position: Int) {
        with(holder) {
            binding.denemeAdiTeacherTextView.text = denemeList[position]
        }

    }

    override fun getItemCount(): Int {
        return denemeList.size
    }
}