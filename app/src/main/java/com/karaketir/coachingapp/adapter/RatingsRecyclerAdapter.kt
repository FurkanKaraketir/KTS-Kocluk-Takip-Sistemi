package com.karaketir.coachingapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.databinding.RatingsRowBinding
import com.karaketir.coachingapp.models.Rating
import java.text.SimpleDateFormat

class RatingsRecyclerAdapter(private val ratingsList: ArrayList<Rating>) :
    RecyclerView.Adapter<RatingsRecyclerAdapter.RatingHolder>() {
    class RatingHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = RatingsRowBinding.bind(itemView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.ratings_row, parent, false)
        return RatingHolder(view)
    }

    override fun getItemCount(): Int {
        return ratingsList.size
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onBindViewHolder(holder: RatingHolder, position: Int) {
        with(holder) {

            if (ratingsList.isNotEmpty() && position >= 0 && position < ratingsList.size) {

                val myItem = ratingsList[position]

                when (myItem.starCount) {
                    1 -> {
                        binding.starOne.visibility = View.VISIBLE
                        binding.starTwo.visibility = View.GONE
                        binding.starThree.visibility = View.GONE
                        binding.starFour.visibility = View.GONE
                        binding.starFive.visibility = View.GONE
                    }
                    2 -> {
                        binding.starOne.visibility = View.VISIBLE
                        binding.starTwo.visibility = View.VISIBLE
                        binding.starThree.visibility = View.GONE
                        binding.starFour.visibility = View.GONE
                        binding.starFive.visibility = View.GONE

                    }
                    3 -> {
                        binding.starOne.visibility = View.VISIBLE
                        binding.starTwo.visibility = View.VISIBLE
                        binding.starThree.visibility = View.VISIBLE
                        binding.starFour.visibility = View.GONE
                        binding.starFive.visibility = View.GONE
                    }
                    4 -> {
                        binding.starOne.visibility = View.VISIBLE
                        binding.starTwo.visibility = View.VISIBLE
                        binding.starThree.visibility = View.VISIBLE
                        binding.starFour.visibility = View.VISIBLE
                        binding.starFive.visibility = View.GONE
                    }
                    5 -> {
                        binding.starOne.visibility = View.VISIBLE
                        binding.starTwo.visibility = View.VISIBLE
                        binding.starThree.visibility = View.VISIBLE
                        binding.starFour.visibility = View.VISIBLE
                        binding.starFive.visibility = View.VISIBLE
                    }


                }

                val dateFormated = SimpleDateFormat("dd/MM/yyyy").format(myItem.ratingDate.toDate())
                binding.raingDateText.text = "Çalışma Tarihi: $dateFormated"
            }

        }

    }


}