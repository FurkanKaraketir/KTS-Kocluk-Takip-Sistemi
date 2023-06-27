package com.karaketir.coachingapp.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
            if (studentList.isNotEmpty() && position >= 0 && position < studentList.size) {

                val myItem = studentList[position]
                val studentDeleteButton = binding.studentDeleteButton
                val studentAddButton = binding.studentAddButton
                val studentHardDeleteButton = binding.studentHardDeleteButton
                val studentGradeTextView = binding.studentGradeTextView
                val studentNameTextView = binding.studentNameTextView

                db = FirebaseFirestore.getInstance()
                auth = FirebaseAuth.getInstance()
                var kurumKodu: Int
                studentDeleteButton.visibility = View.GONE
                studentAddButton.visibility = View.VISIBLE



                db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
                    if (it.get("subjectType").toString() == "İdare") {
                        studentHardDeleteButton.visibility = View.VISIBLE
                    } else {
                        studentHardDeleteButton.visibility = View.GONE
                    }
                }


                studentHardDeleteButton.setOnClickListener {
                    val alertDialog = AlertDialog.Builder(holder.itemView.context)
                    alertDialog.setTitle("Hesabı Sil")
                    alertDialog.setMessage("Hesabı Silmek İstediğinize Emin misiniz?\nBu İşlem Geri Alınamaz!!")
                    alertDialog.setPositiveButton("Sil") { _, _ ->
                        db.collection("User").document(myItem.id).get().addOnSuccessListener {
                            kurumKodu = it.get("kurumKodu").toString().toInt()
                            val personType = it.get("personType").toString()

                            db.collection("School").document(kurumKodu.toString())
                                .collection(personType).document(myItem.id).delete()
                                .addOnSuccessListener {
                                    db.collection("User").document(myItem.id).delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                holder.itemView.context,
                                                "İşlem Başarılı!",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                        }
                                }


                        }


                    }
                    alertDialog.setNegativeButton("İptal") { _, _ ->

                    }
                    alertDialog.show()
                }

                db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
                    kurumKodu = it.get("kurumKodu").toString().toInt()
                    studentNameTextView.text = myItem.studentName

                    studentAddButton.setOnClickListener {

                        val addStudent = AlertDialog.Builder(holder.itemView.context)
                        addStudent.setTitle("Öğrenci Ekle")
                        addStudent.setMessage("${myItem.studentName} Öğrencisini Koçluğunuza Eklemek İstediğinizden Emin misiniz?")
                        addStudent.setPositiveButton("EKLE") { _, _ ->
                            db.collection("School").document(kurumKodu.toString())
                                .collection("Student").document(myItem.id)
                                .update("teacher", auth.uid.toString())
                            db.collection("User").document(myItem.id)
                                .update("teacher", auth.uid.toString())
                        }
                        addStudent.setNegativeButton("İPTAL") { _, _ ->

                        }
                        addStudent.show()


                    }
                }
                studentGradeTextView.text = myItem.grade.toString()


            }

        }


    }

    override fun getItemCount(): Int {
        return studentList.size
    }
}