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
        if (position >= 0 && position < studentList.size) {
            // code to access the element at the specified index
            with(holder) {
                binding.nameText.text = studentList[position].name
                binding.netText.text = studentList[position].toplamNet.toString()
                binding.cardDenemeResult.setOnClickListener {
                    val intent =
                        Intent(holder.itemView.context, OneDenemeViewerActivity::class.java)
                    intent.putExtra("denemeID", studentList[position].denemeID)
                    intent.putExtra("denemeTür", studentList[position].denemeTur)
                    intent.putExtra("denemeStudentID", studentList[position].denemeOwnerID)
                    intent.putExtra("secilenZamanAraligi", "Tüm Zamanlar")
                    holder.itemView.context.startActivity(intent)
                }
            }

        } else {
            // handle the error
            println("Hata")
        }

    }
}