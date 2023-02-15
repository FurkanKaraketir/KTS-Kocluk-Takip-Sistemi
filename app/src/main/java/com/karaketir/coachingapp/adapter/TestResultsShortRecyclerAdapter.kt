package com.karaketir.coachingapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.karaketir.coachingapp.OneDenemeViewerActivity
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.databinding.TestResultsRowBinding
import com.karaketir.coachingapp.models.DenemeResultShort

class TestResultsShortRecyclerAdapter(
    private var studentList: ArrayList<DenemeResultShort>
) : RecyclerView.Adapter<TestResultsShortRecyclerAdapter.ResultHolder>() {

    class ResultHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = TestResultsRowBinding.bind(itemView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.test_results_row, parent, false)
        return ResultHolder(view)
    }

    override fun getItemCount(): Int {
        return studentList.size
    }

    override fun onBindViewHolder(holder: ResultHolder, position: Int) {
        with(holder) {
            if (studentList.isNotEmpty() && position >= 0 && position < studentList.size) {

                val myItem = studentList[position]

                binding.nameText.text = myItem.name
                binding.netText.text = myItem.toplamNet.toString()
                binding.cardDenemeResult.setOnClickListener {
                    val intent =
                        Intent(holder.itemView.context, OneDenemeViewerActivity::class.java)
                    intent.putExtra("denemeID", myItem.denemeID)
                    intent.putExtra("denemeTür", myItem.denemeTur)
                    intent.putExtra("denemeStudentID", myItem.denemeOwnerID)
                    intent.putExtra("secilenZamanAraligi", "Tüm Zamanlar")
                    holder.itemView.context.startActivity(intent)
                }
            }

        }

    }
}