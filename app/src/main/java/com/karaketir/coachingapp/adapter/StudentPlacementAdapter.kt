package com.karaketir.coachingapp.adapter

import android.annotation.SuppressLint
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.databinding.StudentPlacementRowBinding
import com.karaketir.coachingapp.models.StudentPlacment
import com.karaketir.coachingapp.services.setTextAnimation

class StudentPlacementAdapter(private val studentList: ArrayList<StudentPlacment>) :
    RecyclerView.Adapter<StudentPlacementAdapter.StudentHolder>() {
    class StudentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = StudentPlacementRowBinding.bind(itemView)
    }

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.student_placement_row, parent, false)
        return StudentHolder(view)
    }

    override fun getItemCount(): Int {
        return studentList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: StudentHolder, position: Int) {
        val myObject = studentList[position]
        val pos = position + 1
        auth = Firebase.auth
        db = Firebase.firestore



        with(holder) {
            val text = binding.studentPlacementNameText
            val hours = binding.studentPlacementHoursText
            val count = binding.countID


            count.text = (pos).toString()


            db.collection("User").document(myObject.studenID).get().addOnSuccessListener {

                text.setTextAnimation(it.get("nameAndSurname").toString())
                hours.setTextAnimation(myObject.toplamCalisma.toString())

                TransitionManager.beginDelayedTransition(binding.animationContainer)
                binding.cardMain.visibility = View.VISIBLE


            }


        }
    }
}
