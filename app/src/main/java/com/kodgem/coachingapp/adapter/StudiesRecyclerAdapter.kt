package com.kodgem.coachingapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kodgem.coachingapp.R
import com.kodgem.coachingapp.models.Study
import com.kodgem.coachingapp.databinding.StudyRowBinding


open class StudiesRecyclerAdapter(private val studyList: ArrayList<Study>) :
    RecyclerView.Adapter<StudiesRecyclerAdapter.StudyHolder>() {

    private lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth

    class StudyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = StudyRowBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.study_row, parent, false)
        return StudyHolder(view)    }

    override fun onBindViewHolder(holder: StudyHolder, position: Int) {
        with(holder){
            db = FirebaseFirestore.getInstance()
            auth = FirebaseAuth.getInstance()

            binding.studyNameTextView.text = studyList[position].studyName
            binding.studyCountTextView.text = studyList[position].studyCount

        }
    }

    override fun getItemCount(): Int {
        return studyList.size
    }
}
