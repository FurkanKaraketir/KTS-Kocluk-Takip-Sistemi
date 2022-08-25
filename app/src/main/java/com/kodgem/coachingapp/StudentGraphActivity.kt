package com.kodgem.coachingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.core.cartesian.series.Column
import com.anychart.enums.Anchor
import com.anychart.enums.HoverMode
import com.anychart.enums.Position
import com.anychart.enums.TooltipPositionMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kodgem.coachingapp.databinding.ActivityStudentGraphBinding
import com.kodgem.coachingapp.models.Study


class StudentGraphActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentGraphBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var konular = ArrayList<Study>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        val studyOwnerID = intent.getStringExtra("studyOwnerID")
        val studyDersAdi = intent.getStringExtra("studyDersAdi")
        val studyTur = intent.getStringExtra("studyTur")
        val anyChartView = binding.anyChartView

        val cartesian: Cartesian = AnyChart.column()



        if (studyOwnerID != null) {
            db.collection("School").document("SchoolIDDDD").collection("Student")
                .document(studyOwnerID).collection("Studies").whereEqualTo("dersAdi", studyDersAdi)
                .whereEqualTo("tür", studyTur).orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { value, _ ->

                    if (value != null) {
                        konular.clear()
                        for (document in value) {
                            val documentKonuAdi = document.get("konuAdi").toString()
                            val studyCount = document.get("toplamCalisma").toString()
                            val currentDocument = Study(
                                documentKonuAdi,
                                studyCount,
                                studyOwnerID,
                                studyDersAdi!!,
                                studyTur!!
                            )

                            konular.add(currentDocument)
                        }
                        val data: MutableList<DataEntry> = ArrayList()

                        for (i in konular) {
                            data.add(ValueDataEntry(i.studyName, i.studyCount.toInt()))
                        }

                        val column: Column = cartesian.column(data)

                        column.tooltip()
                            .titleFormat("{%X}")
                            .position(Position.CENTER_BOTTOM)
                            .anchor(Anchor.CENTER_BOTTOM)
                            .offsetX(0.0)
                            .offsetY(5.0)
                            .format("{%Value}{groupsSeparator:.}dk")

                        cartesian.animation(true)
                        val title = "$studyTur $studyDersAdi"
                        cartesian.title(title)

                        cartesian.yScale().minimum(0.0)

                        cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator:.}dk")

                        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
                        cartesian.interactivity().hoverMode(HoverMode.BY_X)

                        cartesian.xAxis(0).title("Konu Adları")
                        cartesian.yAxis(0).title("Süre")

                        anyChartView.setChart(cartesian)


                    }

                }
        }


    }


}