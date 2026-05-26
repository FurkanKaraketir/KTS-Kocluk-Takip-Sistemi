package com.karaketir.coachingapp.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.databinding.DayCounterRowBinding
import com.karaketir.coachingapp.models.Student

class NoReportDayCounterAdapter(
    private val studentList: ArrayList<Student>,
    private val kurumKodu: Int,
) : RecyclerView.Adapter<NoReportDayCounterAdapter.ViewHolder>() {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var dayCountByStudentId: Map<String, Int> = emptyMap()

    fun updateDayCounts(counts: Map<String, Int>) {
        dayCountByStudentId = counts
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: DayCounterRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DayCounterRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = studentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = studentList[position]
        holder.binding.name.text = student.studentName
        holder.binding.counter.text = (dayCountByStudentId[student.id] ?: 0).toString()

        holder.binding.removeStudent.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Öğrenci Çıkar")
                .setMessage(
                    "${student.studentName} Öğrencisini Koçluğunuzdan Çıkarmak İstediğinizden Emin misiniz?"
                )
                .setPositiveButton("ÇIKAR") { _, _ ->
                    db.collection("School").document(kurumKodu.toString())
                        .collection("Student")
                        .document(student.id)
                        .update("teacher", "")
                    db.collection("User").document(student.id).update("teacher", "")
                    Toast.makeText(holder.itemView.context, "İşlem Başarılı", Toast.LENGTH_SHORT)
                        .show()
                }
                .setNegativeButton("İPTAL", null)
                .show()
        }
    }
}
