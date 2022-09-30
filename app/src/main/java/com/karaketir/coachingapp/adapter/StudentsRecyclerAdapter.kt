package com.karaketir.coachingapp.adapter

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.StudiesActivity
import com.karaketir.coachingapp.databinding.StudentRowBinding
import com.karaketir.coachingapp.models.Student
import java.util.*
import kotlin.collections.ArrayList

open class StudentsRecyclerAdapter(
    private val studentList: ArrayList<Student>, private val secilenZaman: String
) : RecyclerView.Adapter<StudentsRecyclerAdapter.StudentHolder>() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    class StudentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = StudentRowBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.student_row, parent, false)
        return StudentHolder(view)
    }

    override fun onBindViewHolder(holder: StudentHolder, position: Int) {
        with(holder) {
            db = FirebaseFirestore.getInstance()
            auth = FirebaseAuth.getInstance()

            binding.studentNameTextView.text = studentList[position].studentName
            binding.studentGradeTextView.text = studentList[position].grade.toString()

            binding.studentAddButton.visibility = View.GONE
            binding.studentDeleteButton.visibility = View.VISIBLE
            binding.studentDeleteButton.setOnClickListener {

                db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
                    val kurumKodu = it.get("kurumKodu").toString().toInt()
                    binding.studentNameTextView.text = studentList[position].studentName
                    val removeStudent = AlertDialog.Builder(holder.itemView.context)
                    removeStudent.setTitle("Öğrenci Çıkar")
                    removeStudent.setMessage("${studentList[position].studentName} Öğrencisini Koçluğunuzdan Çıkarmak İstediğinizden Emin misiniz?")
                    removeStudent.setPositiveButton("ÇIKAR") { _, _ ->

                        db.collection("School").document(kurumKodu.toString()).collection("Student")
                            .document(studentList[position].id).update("teacher", "")
                    }
                    removeStudent.setNegativeButton("İPTAL") { _, _ ->

                    }
                    removeStudent.show()
                }


            }

            binding.studentCard.setOnClickListener {
                val intent = Intent(holder.itemView.context, StudiesActivity::class.java)
                intent.putExtra("secilenZaman", secilenZaman)
                intent.putExtra("studentID", studentList[position].id)
                holder.itemView.context.startActivity(intent)
            }

            val cal = Calendar.getInstance()
            cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

            cal.clear(Calendar.MINUTE)
            cal.clear(Calendar.SECOND)
            cal.clear(Calendar.MILLISECOND)

            val baslangicTarihi = cal.time


            cal.add(Calendar.DAY_OF_YEAR, 1)
            val bitisTarihi = cal.time

            db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
                val kurumKodu = it.get("kurumKodu").toString().toInt()

                db.collection("School").document(kurumKodu.toString()).collection("Student")
                    .document(studentList[position].id).collection("Studies")
                    .whereGreaterThan("timestamp", baslangicTarihi)
                    .whereLessThan("timestamp", bitisTarihi).addSnapshotListener { value, error ->
                        if (error != null) {
                            println(error.localizedMessage)
                        }

                        if (value != null) {

                            if (value.isEmpty) {
                                binding.todayStudyImageView.setImageResource(R.drawable.ic_baseline_error_outline_24)
                            } else {
                                binding.todayStudyImageView.setImageResource(R.drawable.ic_baseline_check_circle_outline_24)
                            }

                        } else {
                            binding.todayStudyImageView.setImageResource(R.drawable.ic_baseline_error_outline_24)
                        }

                    }

            }

        }
    }

    override fun getItemCount(): Int {
        return studentList.size
    }
}