package com.karaketir.coachingapp.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
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

class StudentsRecyclerAdapter(
    private val studentList: ArrayList<Student>,
    private val kurumKodu: Int,
    private val baslangicTarihi: Date,
    private val bitisTarihi: Date,
    private val secilenZaman: String
) : RecyclerView.Adapter<StudentsRecyclerAdapter.StudentHolder>() {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    class StudentHolder(val binding: StudentRowBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun bind(student: Student, adapter: StudentsRecyclerAdapter) {
            binding.studentNameTextView.text = student.studentName
            binding.studentGradeTextView.text = student.grade.toString()

            setupClickListeners(student, adapter)
            setupFirestoreListeners(student, adapter)

            binding.studentAddButton.visibility = View.GONE
            binding.studentDeleteButton.visibility = View.VISIBLE

            binding.studentDeleteButton.setOnClickListener {


                binding.studentNameTextView.text = student.studentName
                val removeStudent = AlertDialog.Builder(itemView.context)
                removeStudent.setTitle("Öğrenci Çıkar")
                removeStudent.setMessage("${student.studentName} Öğrencisini Koçluğunuzdan Çıkarmak İstediğinizden Emin misiniz?")
                removeStudent.setPositiveButton("ÇIKAR") { _, _ ->

                    adapter.db.collection("School").document(adapter.kurumKodu.toString())
                        .collection("Student")
                        .document(student.id).update("teacher", "")
                    adapter.db.collection("User").document(student.id).update("teacher", "")
                }
                removeStudent.setNegativeButton("İPTAL") { _, _ ->

                }
                removeStudent.show()


            }
        }

        private fun setupClickListeners(student: Student, adapter: StudentsRecyclerAdapter) {
            binding.studentGradeTextView.setOnClickListener {
                val intent =
                    Intent(itemView.context, StudentClassUpdateActivity::class.java).apply {
                        putExtra("kurumKodu", adapter.kurumKodu.toString())
                        putExtra("name", student.studentName)
                        putExtra("grade", student.grade.toString())
                        putExtra("id", student.id)
                    }
                itemView.context.startActivity(intent)
            }

            binding.studentDeleteButton.setOnClickListener {
                showRemoveStudentDialog(student, adapter)
            }

            binding.studentCard.setOnClickListener {
                val intent = Intent(itemView.context, StudiesActivity::class.java).apply {
                    putExtra("baslangicTarihi", adapter.baslangicTarihi)
                    putExtra("bitisTarihi", adapter.bitisTarihi)
                    putExtra("secilenZaman", adapter.secilenZaman)
                    putExtra("studentID", student.id)
                    putExtra("kurumKodu", adapter.kurumKodu.toString())
                }
                itemView.context.startActivity(intent)
            }
        }

        private fun showRemoveStudentDialog(student: Student, adapter: StudentsRecyclerAdapter) {
            AlertDialog.Builder(itemView.context).apply {
                setTitle("Öğrenci Çıkar")
                setMessage("${student.studentName} Öğrencisini Koçluğunuzdan Çıkarmak İstediğinizden Emin misiniz?")
                setPositiveButton("ÇIKAR") { _, _ ->
                    adapter.db.collection("School").document(adapter.kurumKodu.toString())
                        .collection("Student").document(student.id).update("teacher", "")
                    adapter.db.collection("User").document(student.id).update("teacher", "")
                }
                setNegativeButton("İPTAL", null)
                show()
            }
        }

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        private fun setupFirestoreListeners(student: Student, adapter: StudentsRecyclerAdapter) {
            val schoolRef = adapter.db.collection("School").document(adapter.kurumKodu.toString())

            // Studies listener
            schoolRef.collection("Student").document(student.id).collection("Studies")
                .whereGreaterThan("timestamp", adapter.baslangicTarihi)
                .whereLessThan("timestamp", adapter.bitisTarihi)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        println(error.localizedMessage)
                        return@addSnapshotListener
                    }
                    binding.todayStudyImageView.setImageResource(
                        if (value?.isEmpty == false) R.drawable.ic_baseline_check_circle_outline_24
                        else R.drawable.ic_baseline_error_outline_24
                    )
                }

            // Degerlendirme listener
            schoolRef.collection("Student").document(student.id).collection("Degerlendirme")
                .orderBy("degerlendirmeDate", Query.Direction.DESCENDING).limit(1)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        println(error.localizedMessage)
                        return@addSnapshotListener
                    }
                    if (value?.isEmpty == true) {
                        binding.fiveStarButton.visibility = View.GONE
                        return@addSnapshotListener
                    }
                    binding.fiveStarButton.visibility = View.VISIBLE
                    value?.documents?.firstOrNull()?.let { document ->
                        updateDegerlendirmeUI(document)
                    }
                }

            // Last report date listener
            schoolRef.collection("LastReports").document(student.id)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        binding.reportIcon.visibility = View.GONE
                        binding.reportDate.visibility = View.GONE
                        return@addSnapshotListener
                    }
                    if (value?.exists() == true) {
                        updateReportDateUI(value)
                    } else {
                        binding.reportIcon.visibility = View.GONE
                        binding.reportDate.visibility = View.GONE
                    }
                }
        }

        @SuppressLint("SimpleDateFormat")
        private fun updateDegerlendirmeUI(document: com.google.firebase.firestore.DocumentSnapshot) {
            val tarih = document.get("degerlendirmeDate") as? Timestamp
            val dateFormatted = SimpleDateFormat("dd/MM/yyyy").format(tarih?.toDate() ?: Date())
            binding.degerlendirmeDate.text = dateFormatted

            val yildizSayisi = document.get("yildizSayisi").toString().toIntOrNull() ?: 0
            binding.starTwo.visibility = if (yildizSayisi >= 2) View.VISIBLE else View.GONE
            binding.starThree.visibility = if (yildizSayisi >= 3) View.VISIBLE else View.GONE
            binding.starFour.visibility = if (yildizSayisi >= 4) View.VISIBLE else View.GONE
            binding.starFive.visibility = if (yildizSayisi == 5) View.VISIBLE else View.GONE
        }

        @SuppressLint("SimpleDateFormat")
        private fun updateReportDateUI(document: com.google.firebase.firestore.DocumentSnapshot) {
            val tarih = document.get("timestamp") as? Timestamp
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm").apply {
                timeZone = TimeZone.getTimeZone("GMT+3")
            }
            val dateFormatted = dateFormat.format(tarih?.toDate() ?: Date())
            binding.reportDate.text = dateFormatted
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentHolder {
        val binding = StudentRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentHolder, position: Int) {
        val student = studentList.getOrNull(position) ?: return
        holder.bind(student, this)
    }

    override fun getItemCount() = studentList.size

    class StudentDiffCallback(
        private val oldList: List<Student>,
        private val newList: List<Student>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].id == newList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition] == newList[newItemPosition]
    }

    fun updateStudentList(newStudentList: List<Student>) {
        val diffResult = DiffUtil.calculateDiff(StudentDiffCallback(studentList, newStudentList))
        studentList.clear()
        studentList.addAll(newStudentList)
        diffResult.dispatchUpdatesTo(this)
    }
}