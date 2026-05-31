package com.karaketir.coachingapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.curriculum.CurriculumProgram
import com.karaketir.coachingapp.curriculum.Subjects

class ProgramOptionAdapter(
    private val highlightProgram: CurriculumProgram,
    private val profilePreferredProgram: CurriculumProgram?,
    private val onProgramClick: (CurriculumProgram) -> Unit,
) : RecyclerView.Adapter<ProgramOptionAdapter.ProgramHolder>() {

    private val programs = listOf(CurriculumProgram.LEGACY, CurriculumProgram.TYMM)

    class ProgramHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.gradeTileCard)
        val icon: ImageView = itemView.findViewById(R.id.tileIconImage)
        val label: TextView = itemView.findViewById(R.id.gradeLabelText)
        val profileBadge: TextView = itemView.findViewById(R.id.profileBadgeText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.grade_option_tile_row, parent, false)
        return ProgramHolder(view)
    }

    override fun onBindViewHolder(holder: ProgramHolder, position: Int) {
        val program = programs[position]
        holder.icon.setImageResource(Subjects.programIconRes(program))
        holder.icon.imageTintList = ContextCompat.getColorStateList(
            holder.itemView.context,
            R.color.icon_tint,
        )
        holder.label.text = Subjects.programHeaderLabel(program)
        holder.label.isSelected = true
        holder.card.setCardBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, Subjects.programTileColorRes(program)),
        )
        val isHighlighted = program == highlightProgram
        val isProfilePreference = program == profilePreferredProgram
        holder.profileBadge.visibility = if (isProfilePreference) View.VISIBLE else View.GONE
        holder.profileBadge.text = holder.itemView.context.getString(R.string.profil_tercihiniz)
        val strokeColor = if (isHighlighted) {
            ContextCompat.getColor(holder.itemView.context, R.color.design_default_color_primary)
        } else {
            Color.TRANSPARENT
        }
        holder.card.strokeWidth = if (isHighlighted) 4 else 0
        holder.card.strokeColor = strokeColor
        holder.card.setOnClickListener { onProgramClick(program) }
    }

    override fun getItemCount(): Int = programs.size
}
