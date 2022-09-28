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


open class StudiesRecyclerAdapter(
    private val studyList: ArrayList<Study>, private val zamanAraligi: String
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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: StudyHolder, position: Int) {
        with(holder) {
            db = FirebaseFirestore.getInstance()
            auth = FirebaseAuth.getInstance()

            db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {


                if (it.get("personType").toString() == "Student") {

                    binding.studyGridRowStudyNameTextView.text = studyList[position].studyName
                    binding.studyGridRowStudyMinuteTextView.visibility = View.VISIBLE
                    binding.studyGridRowStudyCountTextView.visibility = View.VISIBLE
                    binding.studyTurText.text = studyList[position].dersTur
                    binding.studyDersText.text = studyList[position].studyDersAdi
                    binding.studyGridRowStudyMinuteTextView.text =
                        studyList[position].studyCount + "dk"
                    binding.studyGridRowStudyCountTextView.text =
                        studyList[position].soruSayisi + " Soru"
                } else {
                    if (position < studyList.size) {

                        binding.studyGridRowStudyMinuteTextView.visibility = View.VISIBLE
                        binding.studyGridRowStudyCountTextView.visibility = View.VISIBLE

                        binding.studyGridRowStudyNameTextView.text = studyList[position].studyName
                        binding.studyGridRowStudyMinuteTextView.text =
                            studyList[position].studyCount + "dk"
                        binding.studyGridRowStudyCountTextView.text =
                            studyList[position].soruSayisi + " Soru"
                        binding.studyTurText.text = studyList[position].dersTur
                        binding.studyDersText.text = studyList[position].studyDersAdi




                        binding.studyGridCard.setOnClickListener {

                            val soruOrSureAlertDialog = AlertDialog.Builder(holder.itemView.context)
                            soruOrSureAlertDialog.setTitle("Soru-Süre Grafikleri")
                            soruOrSureAlertDialog.setMessage(studyList[position].dersTur + " " + studyList[position].studyDersAdi + " " + studyList[position].studyName + " Konusundaki Görmek İstediğiniz Grafik Türünü Seçiniz")
                            soruOrSureAlertDialog.setPositiveButton("SÜRE GRAFİĞİ") { _, _ ->

                                val intent = Intent(
                                    holder.itemView.context, StudentGraphActivity::class.java
                                )
                                intent.putExtra("studyOwnerID", studyList[position].studyOwnerID)
                                intent.putExtra("zamanAraligi", zamanAraligi)
                                intent.putExtra("grafikTuru", "Süre")
                                intent.putExtra("studyDersAdi", studyList[position].studyDersAdi)
                                intent.putExtra("studyKonuAdi", studyList[position].studyName)
                                intent.putExtra("studyTur", studyList[position].dersTur)
                                intent.putExtra("soruSayisi", studyList[position].soruSayisi)
                                holder.itemView.context.startActivity(intent)
                            }

                            soruOrSureAlertDialog.setNegativeButton("SORU GRAFİĞİ") { _, _ ->
                                val intent = Intent(
                                    holder.itemView.context, StudentGraphActivity::class.java
                                )
                                intent.putExtra("studyOwnerID", studyList[position].studyOwnerID)
                                intent.putExtra("zamanAraligi", zamanAraligi)
                                intent.putExtra("grafikTuru", "Soru")
                                intent.putExtra("studyDersAdi", studyList[position].studyDersAdi)
                                intent.putExtra("studyKonuAdi", studyList[position].studyName)
                                intent.putExtra("studyTur", studyList[position].dersTur)
                                intent.putExtra("soruSayisi", studyList[position].soruSayisi)

                                holder.itemView.context.startActivity(intent)
                            }
                            soruOrSureAlertDialog.show()


                        }
                    }
                }

            }

            studyList[position].studyID

            binding.studyDeleteButton.setOnClickListener {

                val deleteStudyDialog = AlertDialog.Builder(holder.itemView.context)
                deleteStudyDialog.setTitle("Çalışma Sil")
                deleteStudyDialog.setMessage("Bu Çalışmayı Silmek İstediğinizden Emin misiniz?")

                deleteStudyDialog.setPositiveButton("Sil") { _, _ ->
                    db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {

                        val kurumKodu = it.get("kurumKodu").toString()
                        db.collection("School").document(kurumKodu).collection("Student")
                            .document(studyList[position].studyOwnerID).collection("Studies")
                            .document(studyList[position].studyID).delete().addOnSuccessListener {
                                Toast.makeText(
                                    holder.itemView.context, "İşlem Başarılı!", Toast.LENGTH_SHORT
                                ).show()
                            }

                    }
                }
                deleteStudyDialog.setNegativeButton("İptal") { _, _ ->

                }

                deleteStudyDialog.show()


            }


        }
    }

    override fun getItemCount(): Int {
        return studyList.size
    }
}
