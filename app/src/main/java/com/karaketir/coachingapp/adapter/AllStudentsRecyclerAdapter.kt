package com.karaketir.coachingapp.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.databinding.StudentRowBinding
import com.karaketir.coachingapp.models.Student

open class AllStudentsRecyclerAdapter(private val studentList: ArrayList<Student>) :
    RecyclerView.Adapter<AllStudentsRecyclerAdapter.StudentHolder>() {
    private lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth

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
            var kurumKodu: Int
            binding.studentDeleteButton.visibility = View.GONE
            binding.studentAddButton.visibility = View.VISIBLE
            db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
                kurumKodu = it.get("kurumKodu").toString().toInt()
                binding.studentNameTextView.text = studentList[position].studentName

                binding.studentAddButton.setOnClickListener {

                    val addStudent = AlertDialog.Builder(holder.itemView.context)
                    addStudent.setTitle("Öğrenci Ekle")
                    addStudent.setMessage("${studentList[position].studentName} Öğrencisini Koçluğunuza Eklemek İstediğinizden Emin misiniz?")
                    addStudent.setPositiveButton("EKLE") { _, _ ->
                        db.collection("School").document(kurumKodu.toString()).collection("Student")
                            .document(studentList[position].id)
                            .update("teacher", auth.uid.toString())
                        db.collection("User").document(studentList[position].id)
                            .update("teacher", auth.uid.toString())
                    }
                    addStudent.setNegativeButton("İPTAL") { _, _ ->

                    }
                    addStudent.show()


                }
            }


        }

    }

    override fun getItemCount(): Int {
        return studentList.size
    }
}