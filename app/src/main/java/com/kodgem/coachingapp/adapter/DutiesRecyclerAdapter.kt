@file:Suppress("DEPRECATION")

package com.kodgem.coachingapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kodgem.coachingapp.R
import com.kodgem.coachingapp.databinding.DutyGridRowBinding
import com.kodgem.coachingapp.models.Duty
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Level.parse

open class DutiesRecyclerAdapter(private val dutyList: kotlin.collections.List<Duty>) :
    RecyclerView.Adapter<DutiesRecyclerAdapter.DutyHolder>() {

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
            auth = Firebase.auth
            db = Firebase.firestore
            var kurumKodu: Int
            db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
                kurumKodu = it.get("kurumKodu").toString().toInt()
                binding.dutyKonuAdiTextView.text = dutyList[position].konuAdi
                binding.dutyTurText.text = dutyList[position].tur
                binding.dutyDersText.text = dutyList[position].dersAdi

                if (dutyList[position].toplamCalisma.toInt() < 0) {
                    binding.dutyToplamCalisma.text = "0 dk"
                } else {
                    binding.dutyToplamCalisma.text = dutyList[position].toplamCalisma + "dk"
                }

                if (dutyList[position].cozulenSoru.toInt() < 0) {
                    binding.dutyCozulenSoru.text = "0 Soru"
                } else {
                    binding.dutyCozulenSoru.text = dutyList[position].cozulenSoru + " Soru"
                }

                val date = dutyList[position].bitisZamani.toDate()
                val dateFormated = SimpleDateFormat("dd/MM/yyyy").format(date)

                binding.bitisZamaniText.text = "Görev Bitiş Tarihi \n$dateFormated"

                db.collection("School").document(kurumKodu.toString()).collection("Student")
                    .document(dutyList[position].studyOwnerID).collection("Duties")
                    .document(dutyList[position].dutyID).get().addOnSuccessListener {it2->
                        val bitisZamani = it2.get("bitisZamani") as Timestamp
                        bitisZamani.toDate()


                        if (Calendar.getInstance().time.after(date)){
                            binding.completeIcon.setImageResource(R.drawable.ic_baseline_error_24)
                        }else{
                            binding.completeIcon.setImageResource(R.drawable.ic_baseline_timelapse_24)
                        }
                    }
            }




        }

    }

    override fun getItemCount(): Int {
        return dutyList.size
    }

}
