package com.karaketir.coachingapp.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.StudentClassUpdateActivity
import com.karaketir.coachingapp.StudiesActivity
import com.karaketir.coachingapp.databinding.StudentRowBinding
import com.karaketir.coachingapp.models.Student
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

open class StudentsRecyclerAdapter(
    private val studentList: ArrayList<Student>,
    private val kurumKodu: Int,
    private val baslangicTarihi: Date,
    private val bitisTarihi: Date,
    private val secilenZaman: String,
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

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: StudentHolder, position: Int) {
        with(holder) {

            if (studentList.isNotEmpty() && position >= 0 && position < studentList.size) {

                val myItem = studentList[position]

                val studentNameTextView = binding.studentNameTextView
                val studentGradeTextView = binding.studentGradeTextView
                val studentAddButton = binding.studentAddButton
                val studentDeleteButton = binding.studentDeleteButton
                val studentCard = binding.studentCard
                val todayStudyImageView = binding.todayStudyImageView
                val fiveStarButton = binding.fiveStarButton
                val degerlendirmeDate = binding.degerlendirmeDate
                val reportDate = binding.reportDate
                val starTwo = binding.starTwo
                val starThree = binding.starThree
                val starFour = binding.starFour
                val starFive = binding.starFive

                db = FirebaseFirestore.getInstance()
                auth = FirebaseAuth.getInstance()

                studentNameTextView.text = myItem.studentName
                studentGradeTextView.text = myItem.grade.toString()

                studentGradeTextView.setOnClickListener {

                    val newIntent =
                        Intent(holder.itemView.context, StudentClassUpdateActivity::class.java)
                    newIntent.putExtra("kurumKodu", kurumKodu.toString())
                    newIntent.putExtra("name", myItem.studentName)
                    newIntent.putExtra("grade", myItem.grade.toString())
                    newIntent.putExtra("id", myItem.id)
                    holder.itemView.context.startActivity(newIntent)

                }
                studentAddButton.visibility = View.GONE
                studentDeleteButton.visibility = View.VISIBLE

                studentDeleteButton.setOnClickListener {


                    studentNameTextView.text = myItem.studentName
                    val removeStudent = AlertDialog.Builder(holder.itemView.context)
                    removeStudent.setTitle("Öğrenci Çıkar")
                    removeStudent.setMessage("${myItem.studentName} Öğrencisini Koçluğunuzdan Çıkarmak İstediğinizden Emin misiniz?")
                    removeStudent.setPositiveButton("ÇIKAR") { _, _ ->

                        db.collection("School").document(kurumKodu.toString()).collection("Student")
                            .document(myItem.id).update("teacher", "")
                        db.collection("User").document(myItem.id).update("teacher", "")
                    }
                    removeStudent.setNegativeButton("İPTAL") { _, _ ->

                    }
                    removeStudent.show()


                }

                studentCard.setOnClickListener {
                    val intent = Intent(holder.itemView.context, StudiesActivity::class.java)
                    intent.putExtra("baslangicTarihi", baslangicTarihi)
                    intent.putExtra("bitisTarihi", bitisTarihi)
                    intent.putExtra("secilenZaman", secilenZaman)
                    intent.putExtra("studentID", myItem.id)
                    intent.putExtra("kurumKodu", kurumKodu.toString())
                    println(myItem.id)
                    holder.itemView.context.startActivity(intent)
                }


                db.collection("School").document(kurumKodu.toString()).collection("Student")
                    .document(myItem.id).collection("Studies")
                    .whereGreaterThan("timestamp", baslangicTarihi)
                    .whereLessThan("timestamp", bitisTarihi).addSnapshotListener { value, error ->
                        if (error != null) {
                            println(error.localizedMessage)
                        }

                        if (value != null) {

                            if (value.isEmpty) {
                                todayStudyImageView.setImageResource(R.drawable.ic_baseline_error_outline_24)
                            } else {
                                todayStudyImageView.setImageResource(R.drawable.ic_baseline_check_circle_outline_24)
                            }

                        } else {
                            todayStudyImageView.setImageResource(R.drawable.ic_baseline_error_outline_24)
                        }

                    }

                db.collection("School").document(kurumKodu.toString()).collection("Student")
                    .document(myItem.id).collection("Degerlendirme")
                    .orderBy("degerlendirmeDate", Query.Direction.DESCENDING).limit(1)
                    .addSnapshotListener { value, error ->

                        if (error != null) {
                            println(error.localizedMessage)
                        }

                        if (value != null) {
                            if (value.isEmpty) {
                                fiveStarButton.visibility = View.GONE
                            } else {
                                fiveStarButton.visibility = View.VISIBLE
                                for (i in value) {
                                    val tarih =
                                        i.get("degerlendirmeDate") as com.google.firebase.Timestamp
                                    val dateFormated =
                                        SimpleDateFormat("dd/MM/yyyy").format(tarih.toDate())
                                    degerlendirmeDate.text = dateFormated
                                    when (i.get("yildizSayisi").toString().toInt()) {
                                        5 -> {
                                            starTwo.visibility = View.VISIBLE
                                            starThree.visibility = View.VISIBLE
                                            starFour.visibility = View.VISIBLE
                                            starFive.visibility = View.VISIBLE
                                        }

                                        4 -> {
                                            starTwo.visibility = View.VISIBLE
                                            starThree.visibility = View.VISIBLE
                                            starFour.visibility = View.VISIBLE
                                            starFive.visibility = View.GONE
                                        }

                                        3 -> {
                                            starTwo.visibility = View.VISIBLE
                                            starThree.visibility = View.VISIBLE
                                            starFour.visibility = View.GONE
                                            starFive.visibility = View.GONE

                                        }

                                        2 -> {
                                            starTwo.visibility = View.VISIBLE
                                            starThree.visibility = View.GONE
                                            starFour.visibility = View.GONE
                                            starFive.visibility = View.GONE
                                        }

                                        1 -> {
                                            starTwo.visibility = View.GONE
                                            starThree.visibility = View.GONE
                                            starFour.visibility = View.GONE
                                            starFive.visibility = View.GONE
                                        }
                                    }
                                }
                            }

                        } else {
                            fiveStarButton.visibility = View.GONE
                        }
                    }

                // show the last report's date
                db.collection("School").document(kurumKodu.toString()).collection("Student")
                    .document(myItem.id).collection("Studies")
                    .orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
                    .addSnapshotListener { value, error ->

                        if (error != null) {
                            println(error.localizedMessage)
                        }

                        if (value != null) {
                            if (value.isEmpty) {
                                reportDate.text = "Rapor Yok"
                            } else {
                                for (i in value) {
                                    val tarih =
                                        i.get("timestamp") as com.google.firebase.Timestamp
                                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")
                                    dateFormat.timeZone =
                                        TimeZone.getTimeZone("GMT+3") // Set the timezone to GMT+3
                                    val dateFormatted = dateFormat.format(tarih.toDate())
                                    reportDate.text = dateFormatted
                                }
                            }

                        } else {
                            reportDate.text = "Rapor Yok"
                        }
                    }

            }

        }

    }

    override fun getItemCount(): Int {
        return studentList.size
    }
}