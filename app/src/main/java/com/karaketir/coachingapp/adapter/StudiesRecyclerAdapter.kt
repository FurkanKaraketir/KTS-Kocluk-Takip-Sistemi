package com.karaketir.coachingapp.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.StudentGraphActivity
import com.karaketir.coachingapp.databinding.StudyGridRowBinding
import com.karaketir.coachingapp.models.Study
import java.text.SimpleDateFormat


open class StudiesRecyclerAdapter(
    private val studyList: ArrayList<Study>,
    private val zamanAraligi: String,
    private val kurumKodu: Int,
    private val personType: String
) : RecyclerView.Adapter<StudiesRecyclerAdapter.StudyHolder>() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth


    class StudyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = StudyGridRowBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.study_grid_row, parent, false)
        return StudyHolder(view)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: StudyHolder, position: Int) {
        with(holder) {

            if (studyList.isNotEmpty() && position >= 0 && position < studyList.size) {

                val myItem = studyList[position]

                db = FirebaseFirestore.getInstance()
                auth = FirebaseAuth.getInstance()
                val date = myItem.timestamp.toDate()
                val sdf = SimpleDateFormat("d MMMM EEEE")
                val dayOfTheWeek = sdf.format(date)

                binding.studyGridRowDayTextView.text = dayOfTheWeek



                if (personType == "Student") {

                    binding.studyGridRowStudyNameTextView.text = myItem.studyName
                    binding.studyGridRowStudyMinuteTextView.visibility = View.VISIBLE
                    binding.studyGridRowStudyCountTextView.visibility = View.VISIBLE
                    binding.studyTurText.text = myItem.dersTur
                    binding.studyDersText.text = myItem.studyDersAdi
                    binding.studyGridRowStudyMinuteTextView.text = myItem.studyCount + "dk"
                    binding.studyGridRowStudyCountTextView.text = myItem.soruSayisi + " Soru"
                } else {

                    binding.studyGridRowStudyMinuteTextView.visibility = View.VISIBLE
                    binding.studyGridRowStudyCountTextView.visibility = View.VISIBLE
                    binding.studyGridRowStudyNameTextView.text = myItem.studyName
                    binding.studyGridRowStudyMinuteTextView.text = myItem.studyCount + "dk"
                    binding.studyGridRowStudyCountTextView.text = myItem.soruSayisi + " Soru"
                    binding.studyTurText.text = myItem.dersTur
                    binding.studyDersText.text = myItem.studyDersAdi




                    binding.studyGridCard.setOnClickListener {

                        val soruOrSureAlertDialog = AlertDialog.Builder(holder.itemView.context)
                        soruOrSureAlertDialog.setTitle("Soru-Süre Grafikleri")
                        soruOrSureAlertDialog.setMessage(myItem.dersTur + " " + myItem.studyDersAdi + " " + myItem.studyName + " Konusundaki Görmek İstediğiniz Grafik Türünü Seçiniz")
                        soruOrSureAlertDialog.setPositiveButton("SÜRE GRAFİĞİ") { _, _ ->

                            val intent = Intent(
                                holder.itemView.context, StudentGraphActivity::class.java
                            )
                            intent.putExtra(
                                "studyOwnerID", myItem.studyOwnerID
                            )
                            intent.putExtra("zamanAraligi", zamanAraligi)
                            intent.putExtra("grafikTuru", "Süre")
                            intent.putExtra(
                                "studyDersAdi", myItem.studyDersAdi
                            )
                            intent.putExtra("studyKonuAdi", myItem.studyName)
                            intent.putExtra("studyTur", myItem.dersTur)
                            intent.putExtra("soruSayisi", myItem.soruSayisi)
                            intent.putExtra("kurumKodu", kurumKodu.toString())
                            holder.itemView.context.startActivity(intent)
                        }

                        soruOrSureAlertDialog.setNegativeButton("SORU GRAFİĞİ") { _, _ ->
                            val intent = Intent(
                                holder.itemView.context, StudentGraphActivity::class.java
                            )
                            intent.putExtra(
                                "studyOwnerID", myItem.studyOwnerID
                            )
                            intent.putExtra("zamanAraligi", zamanAraligi)
                            intent.putExtra("grafikTuru", "Soru")
                            intent.putExtra(
                                "studyDersAdi", myItem.studyDersAdi
                            )
                            intent.putExtra("studyKonuAdi", myItem.studyName)
                            intent.putExtra("studyTur", myItem.dersTur)
                            intent.putExtra("soruSayisi", myItem.soruSayisi)
                            intent.putExtra("kurumKodu", kurumKodu.toString())

                            holder.itemView.context.startActivity(intent)
                        }
                        soruOrSureAlertDialog.show()


                    }
                }



                binding.studyDeleteButton.setOnClickListener {

                    val deleteStudyDialog = AlertDialog.Builder(holder.itemView.context)
                    deleteStudyDialog.setTitle("Çalışma Sil")
                    deleteStudyDialog.setMessage("Bu Çalışmayı Silmek İstediğinizden Emin misiniz?")

                    deleteStudyDialog.setPositiveButton("Sil") { _, _ ->

                        db.collection("School").document(kurumKodu.toString()).collection("Student")
                            .document(myItem.studyOwnerID).collection("Studies")
                            .document(myItem.studyID).delete().addOnSuccessListener {
                                Toast.makeText(
                                    holder.itemView.context, "İşlem Başarılı!", Toast.LENGTH_SHORT
                                ).show()
                            }


                    }
                    deleteStudyDialog.setNegativeButton("İptal") { _, _ ->

                    }

                    deleteStudyDialog.show()


                }


            }

        }

    }

    override fun getItemCount(): Int {
        return studyList.size
    }
}
