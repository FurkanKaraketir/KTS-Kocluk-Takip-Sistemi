package com.kodgem.coachingapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kodgem.coachingapp.R
import com.kodgem.coachingapp.databinding.StudyGridRowBinding
import com.kodgem.coachingapp.models.Study


open class StudiesRecyclerAdapter(private val studyList: ArrayList<Study>) :
    RecyclerView.Adapter<StudiesRecyclerAdapter.StudyHolder>() {

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

    override fun onBindViewHolder(holder: StudyHolder, position: Int) {
        with(holder) {
            db = FirebaseFirestore.getInstance()
            auth = FirebaseAuth.getInstance()

            binding.studyGridRowStudyNameTextView.text = studyList[position].studyName
            binding.studyGridRowStudyMinuteTextView.text = studyList[position].studyCount

        }
    }

    override fun getItemCount(): Int {
        return studyList.size
    }
}
