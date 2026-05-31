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
import com.karaketir.coachingapp.curriculum.Subjects

class GradeOptionAdapter(
    private val profileGrade: Int,
    private val onGradeClick: (Int) -> Unit,
) : RecyclerView.Adapter<GradeOptionAdapter.GradeHolder>() {

    private val grades = Subjects.GRADE_OPTIONS

    class GradeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView.findViewById(R.id.gradeTileCard)
        val icon: ImageView = itemView.findViewById(R.id.tileIconImage)
        val label: TextView = itemView.findViewById(R.id.gradeLabelText)
        val profileBadge: TextView = itemView.findViewById(R.id.profileBadgeText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradeHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.grade_option_tile_row, parent, false)
        return GradeHolder(view)
    }

    override fun onBindViewHolder(holder: GradeHolder, position: Int) {
        val grade = grades[position]
        holder.icon.setImageResource(Subjects.gradeIconRes(grade))
        holder.icon.imageTintList = ContextCompat.getColorStateList(
            holder.itemView.context,
            R.color.icon_tint,
        )
        holder.label.text = Subjects.gradeOptionLabel(grade)
        holder.label.isSelected = true
        holder.card.setCardBackgroundColor(
            ContextCompat.getColor(holder.itemView.context, Subjects.gradeTileColorRes(grade)),
        )
        val isProfile = grade == profileGrade
        holder.profileBadge.visibility = if (isProfile) View.VISIBLE else View.GONE
        holder.profileBadge.text = holder.itemView.context.getString(R.string.profiliniz)
        val strokeColor = if (isProfile) {
            ContextCompat.getColor(holder.itemView.context, R.color.design_default_color_primary)
        } else {
            Color.TRANSPARENT
        }
        holder.card.strokeWidth = if (isProfile) 4 else 0
        holder.card.strokeColor = strokeColor
        holder.card.setOnClickListener { onGradeClick(grade) }
    }

    override fun getItemCount(): Int = grades.size
}
