package com.karaketir.coachingapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.curriculum.SubjectTile
import com.karaketir.coachingapp.curriculum.Subjects

class SubjectTileAdapter(
    private var tiles: List<SubjectTile>,
    private val onSubjectClick: (SubjectTile) -> Unit,
) : RecyclerView.Adapter<SubjectTileAdapter.SubjectHolder>() {

    class SubjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.subjectTileCard)
        val icon: ImageView = itemView.findViewById(R.id.subjectIconImage)
        val name: TextView = itemView.findViewById(R.id.subjectNameText)
    }

    fun submitList(newTiles: List<SubjectTile>) {
        tiles = newTiles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.study_subject_tile_row, parent, false)
        return SubjectHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectHolder, position: Int) {
        val tile = tiles[position]
        holder.icon.setImageResource(tile.iconRes)
        holder.icon.imageTintList = ContextCompat.getColorStateList(
            holder.itemView.context,
            R.color.icon_tint,
        )
        holder.name.text = tile.name
        holder.name.isSelected = true
        holder.card.setCardBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, Subjects.subjectTileColorRes(position)),
        )
        holder.itemView.setOnClickListener { onSubjectClick(tile) }
    }

    override fun getItemCount(): Int = tiles.size
}
