package com.kodgem.coachingapp.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kodgem.coachingapp.OneDenemeViewerActivity
import com.kodgem.coachingapp.R
import com.kodgem.coachingapp.databinding.DenemeGridRowBinding
import com.kodgem.coachingapp.models.Deneme
import java.text.SimpleDateFormat

class DenemelerRecyclerAdapter(private val denemeList: List<Deneme>, private val secilenZamanAraligi: String) :
    RecyclerView.Adapter<DenemelerRecyclerAdapter.DenemeHolder>() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    class DenemeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = DenemeGridRowBinding.bind(itemView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DenemeHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.deneme_grid_row, parent, false)
        return DenemeHolder(view)
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onBindViewHolder(holder: DenemeHolder, position: Int) {

        with(holder) {
            auth = Firebase.auth
            db = Firebase.firestore
            binding.denemeAdiTextView.text = denemeList[position].denemeAdi
            binding.denemeToplamNetTextView.text =
                "Toplam Net: " + denemeList[position].denemeToplamNet.toString()
            val date = denemeList[position].denemeTarihi.toDate()
            val dateFormated = SimpleDateFormat("dd/MM/yyyy").format(date)

            binding.denemeTarihTextView.text = dateFormated

            binding.denemeCard.setOnClickListener {
                val intent = Intent(holder.itemView.context, OneDenemeViewerActivity::class.java)
                intent.putExtra("denemeID", denemeList[position].denemeID)
                intent.putExtra("secilenZamanAraligi",secilenZamanAraligi)
                intent.putExtra("denemeStudentID",denemeList[position].denemeStudentID)
                holder.itemView.context.startActivity(intent)
            }
        }

    }

    override fun getItemCount(): Int {
        return denemeList.size
    }

}