package com.karaketir.coachingapp.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.databinding.DutyGridRowBinding
import com.karaketir.coachingapp.models.Duty
import java.text.SimpleDateFormat
import java.util.*

open class DutiesRecyclerAdapter(
    private val dutyList: List<Duty>, private val kurumKodu: Int, private val personType: String
) : RecyclerView.Adapter<DutiesRecyclerAdapter.DutyHolder>() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth


    class DutyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val binding = DutyGridRowBinding.bind(itemView)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DutyHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.duty_grid_row, parent, false)
        return DutyHolder(view)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: DutyHolder, position: Int) {

        with(holder) {

            if (dutyList.isNotEmpty() && position >= 0 && position < dutyList.size) {

                val myItem = dutyList[position]

                auth = Firebase.auth
                db = Firebase.firestore

                binding.dutyKonuAdiTextView.text = myItem.konuAdi
                binding.dutyTurText.text = myItem.tur
                binding.dutyDersText.text = myItem.dersAdi

                if (myItem.toplamCalisma.toInt() < 0) {
                    binding.dutyToplamCalisma.text = "0 dk"
                } else {
                    binding.dutyToplamCalisma.text = myItem.toplamCalisma + "dk"
                }

                if (myItem.cozulenSoru.toInt() < 0) {
                    binding.dutyCozulenSoru.text = "0 Soru"
                } else {
                    binding.dutyCozulenSoru.text = myItem.cozulenSoru + " Soru"
                }

                val date = myItem.bitisZamani.toDate()
                val dateFormated = SimpleDateFormat("dd/MM/yyyy").format(date)

                binding.bitisZamaniText.text = "Görev Bitiş Tarihi \n$dateFormated"


                if (myItem.dutyTamamlandi) {
                    binding.completeIcon.setImageResource(R.drawable.ic_baseline_check_circle_outline_24)
                } else {
                    if (Calendar.getInstance().time.after(date)) {
                        binding.completeIcon.setImageResource(R.drawable.ic_baseline_error_outline_24)
                    } else {
                        binding.completeIcon.setImageResource(R.drawable.ic_baseline_timelapse_24)
                    }
                }



                if (personType == "Student") {
                    binding.deleteDutyButton.visibility = View.GONE
                } else {
                    binding.deleteDutyButton.visibility = View.VISIBLE
                }

                binding.deleteDutyButton.setOnClickListener {
                    val deleteDutyDialog = AlertDialog.Builder(holder.itemView.context)
                    deleteDutyDialog.setTitle("Görev Sil")
                    deleteDutyDialog.setMessage("Bu Görevi Silmek İstediğinizden Emin misiniz?")

                    deleteDutyDialog.setPositiveButton("Sil") { _, _ ->

                        db.collection("School").document(kurumKodu.toString()).collection("Student")
                            .document(myItem.studyOwnerID).collection("Duties")
                            .document(myItem.dutyID).delete().addOnSuccessListener {
                                Toast.makeText(
                                    holder.itemView.context, "İşlem Başarılı!", Toast.LENGTH_SHORT
                                ).show()
                            }


                    }
                    deleteDutyDialog.setNegativeButton("İptal") { _, _ ->

                    }

                    deleteDutyDialog.show()
                }


            }

        }

    }

    override fun getItemCount(): Int {
        return dutyList.size
    }

}
