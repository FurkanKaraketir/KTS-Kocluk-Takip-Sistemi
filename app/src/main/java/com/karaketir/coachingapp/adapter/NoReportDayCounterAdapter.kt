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
import com.karaketir.coachingapp.databinding.DayCounterRowBinding
import java.util.Calendar
import java.util.Date

class NoReportDayCounterAdapter(
    private val studentList: ArrayList<String>,
    private var secilenZaman: String,
    private val kurumKodu: Int
) : RecyclerView.Adapter<NoReportDayCounterAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = DayCounterRowBinding.bind(itemView)
    }


    private lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.day_counter_row, parent, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {
        return studentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        var cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0
        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)
        when (secilenZaman) {

            "Bugün" -> {
                baslangicTarihi = cal.time

                cal.add(Calendar.DAY_OF_YEAR, 1)
                bitisTarihi = cal.time
            }

            "Dün" -> {
                bitisTarihi = cal.time

                cal.add(Calendar.DAY_OF_YEAR, -1)
                baslangicTarihi = cal.time

            }

            "Bu Hafta" -> {
                cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek
                baslangicTarihi = cal.time


                cal.add(Calendar.WEEK_OF_YEAR, 1)
                bitisTarihi = cal.time

            }

            "Geçen Hafta" -> {
                cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek
                bitisTarihi = cal.time


                cal.add(Calendar.DAY_OF_YEAR, -7)
                baslangicTarihi = cal.time


            }

            "Bu Ay" -> {

                cal = Calendar.getInstance()
                cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                cal.clear(Calendar.MINUTE)
                cal.clear(Calendar.SECOND)
                cal.clear(Calendar.MILLISECOND)

                cal.set(Calendar.DAY_OF_MONTH, 1)
                baslangicTarihi = cal.time


                cal.add(Calendar.MONTH, 1)
                bitisTarihi = cal.time


            }

            "Geçen Ay" -> {
                cal = Calendar.getInstance()
                cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                cal.clear(Calendar.MINUTE)
                cal.clear(Calendar.SECOND)
                cal.clear(Calendar.MILLISECOND)

                cal.set(Calendar.DAY_OF_MONTH, 1)
                bitisTarihi = cal.time


                cal.add(Calendar.MONTH, -1)
                baslangicTarihi = cal.time

            }

            "Tüm Zamanlar" -> {
                cal.set(1970, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                baslangicTarihi = cal.time


                cal.set(2077, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                bitisTarihi = cal.time

            }


        }

        with(holder) {

            val myItem = studentList[position]
            var studentName = ""

            db.collection("User").document(myItem).get().addOnSuccessListener {
                studentName = it.get("nameAndSurname").toString()
                binding.name.text = studentName
            }


            db.collection("School").document(kurumKodu.toString()).collection("Student")
                .document(myItem).collection("NoDayReport")
                .whereGreaterThan("timestamp", baslangicTarihi)
                .whereLessThan("timestamp", bitisTarihi).addSnapshotListener { value, error ->
                    if (error != null) {
                        println(error.localizedMessage)
                    }
                    var size = 0
                    if (value != null) {
                        for (i in value) {
                            size += 1
                        }
                    }
                    binding.counter.text = size.toString()
                }

            binding.removeStudent.setOnClickListener {

                val removeStudent = AlertDialog.Builder(holder.itemView.context)
                removeStudent.setTitle("Öğrenci Çıkar")
                removeStudent.setMessage("$studentName Öğrencisini Koçluğunuzdan Çıkarmak İstediğinizden Emin misiniz?")
                removeStudent.setPositiveButton("ÇIKAR") { _, _ ->

                    db.collection("School").document(kurumKodu.toString()).collection("Student")
                        .document(myItem).update("teacher", "")
                    db.collection("User").document(myItem).update("teacher", "")
                    Toast.makeText(holder.itemView.context, "İşlem Başarılı", Toast.LENGTH_SHORT)
                        .show()

                }
                removeStudent.setNegativeButton("İPTAL") { _, _ ->

                }
                removeStudent.show()


            }
        }


    }


}