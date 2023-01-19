package com.karaketir.coachingapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.databinding.StatsClassGridRowBinding
import com.karaketir.coachingapp.models.Statistic

class StatisticsRecyclerAdapter(
    private val statisticList: ArrayList<Statistic>
) : RecyclerView.Adapter<StatisticsRecyclerAdapter.StatisticHolder>() {

    class StatisticHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = StatsClassGridRowBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.stats_class_grid_row, parent, false)
        return StatisticHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: StatisticHolder, position: Int) {
        if (statisticList.isNotEmpty() && position >= 0 && position < statisticList.size) {
            // code to access the element at the specified index
            with(holder) {

                if (statisticList[position].dersAdi == "Deneme") {
                    binding.statsDersAdi.text = "Deneme Tahlili"
                } else {
                    binding.statsDersAdi.text = statisticList[position].dersAdi
                }
                binding.statsToplamCalisma.text = "Ortalama Çalışılan Süre:  ${
                    statisticList[position].toplamCalisma.toFloat().format(2)
                } dk"
                binding.statsCozulenSoru.text = "Ortalama Çözülen Soru:  ${
                    statisticList[position].cozulenSoru.toFloat().format(2)
                } Soru"


            }

        } else {
            // handle the error
            println("Hata")
        }

    }

    override fun getItemCount(): Int {
        return statisticList.size
    }

    private fun Float.format(digits: Int) = "%.${digits}f".format(this)

}