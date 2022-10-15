package com.karaketir.coachingapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.adapter.TestResultsShortRecyclerAdapter
import com.karaketir.coachingapp.databinding.ActivityTestResultsShortBinding
import com.karaketir.coachingapp.models.DenemeResultShort

class TestResultsShortActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestResultsShortBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var resultList = ArrayList<DenemeResultShort>()
    private var studentList = ArrayList<String>()

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestResultsShortBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        db = Firebase.firestore
        val intent = intent
        val denemeAdi = intent.getStringExtra("denemeAdi").toString()
        binding.denemeName.text = denemeAdi

        val layoutManager = LinearLayoutManager(this)

        val recyclerView = binding.recyclerViewDenemeResults
        val recyclerViewAdapter = TestResultsShortRecyclerAdapter(resultList)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = recyclerViewAdapter

        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener { user ->
            val kurumKodu = user.get("kurumKodu")?.toString()?.toInt()

            db.collection("School").document(kurumKodu.toString()).collection("Student")
                .whereEqualTo("teacher", auth.uid.toString())
                .addSnapshotListener { students, error ->
                    if (error != null) {
                        println(error.localizedMessage)
                    }
                    studentList.clear()

                    if (students != null) {
                        for (i in students) {
                            studentList.add(i.get("id").toString())
                        }
                        var toplamNet = 0f
                        for (a in studentList) {
                            var name: String
                            db.collection("User").document(a).get()
                                .addOnSuccessListener { student ->
                                    name = student.get("nameAndSurname").toString()


                                    db.collection("School").document(kurumKodu.toString())
                                        .collection("Student").document(a).collection("Denemeler")
                                        .whereEqualTo("denemeAdi", denemeAdi)
                                        .addSnapshotListener { value, error2 ->
                                            if (error2 != null) {
                                                println(error2.localizedMessage)
                                            }
                                            if (value != null) {
                                                for (j in value) {
                                                    val denemeTur = j.get("denemeTür").toString()
                                                    val denemeNet =
                                                        j.get("toplamNet").toString().toFloat()
                                                    toplamNet += denemeNet
                                                    val currentDeneme = DenemeResultShort(
                                                        denemeAdi,
                                                        denemeNet,
                                                        name,
                                                        j.id,
                                                        denemeTur,
                                                        a
                                                    )
                                                    resultList.add(currentDeneme)
                                                    resultList.sortBy { -it.toplamNet }
                                                    binding.denemeOrtalamaNet.text =
                                                        "Ortalama Toplam Net: " + (toplamNet / resultList.size).format(
                                                            2
                                                        )
                                                    recyclerViewAdapter.notifyDataSetChanged()
                                                }
                                            }
                                        }

                                }
                        }


                    }
                }

        }


    }

    private fun Float.format(digits: Int) = "%.${digits}f".format(this)

}