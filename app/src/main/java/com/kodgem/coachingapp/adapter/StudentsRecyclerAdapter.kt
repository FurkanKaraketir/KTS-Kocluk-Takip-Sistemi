package com.kodgem.coachingapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kodgem.coachingapp.R
import com.kodgem.coachingapp.StudiesActivity
import com.kodgem.coachingapp.databinding.StudentRowBinding
import com.kodgem.coachingapp.models.Student

open class StudentsRecyclerAdapter(private val studentList: ArrayList<Student>) :
    RecyclerView.Adapter<StudentsRecyclerAdapter.StudentHolder>() {
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

            binding.studentCard.setOnClickListener {
                val intent = Intent(holder.itemView.context, StudiesActivity::class.java)
                intent.putExtra("studentID", studentList[position].id)
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return studentList.size
    }
}