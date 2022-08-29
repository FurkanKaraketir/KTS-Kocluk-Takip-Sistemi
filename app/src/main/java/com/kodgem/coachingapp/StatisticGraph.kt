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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kodgem.coachingapp.databinding.ActivityStatisticGraphBinding
import com.kodgem.coachingapp.models.Statistic
import java.util.*

class StatisticGraph : AppCompatActivity() {
    private lateinit var binding: ActivityStatisticGraphBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date
    private var statsList = ArrayList<Statistic>()

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityStatisticGraphBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val anyChartView = binding.anyChartStatisticView
        auth = Firebase.auth
        db = Firebase.firestore

        val intent = intent

        val cartesian: Cartesian = AnyChart.column()
        var cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)

        val tur = intent.getStringExtra("grafikTuru")
        val zamanAraligi = intent.getStringExtra("zamanAraligi")

        when (zamanAraligi) {
            "Bu Hafta" -> {
                cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek
                baslangicTarihi = cal.time


                cal.add(Calendar.WEEK_OF_YEAR, 1)
                bitisTarihi = cal.time
            }
            "Geçen Hafta" -> {
                cal[Calendar.DAY_OF_WEEK] = cal.firstDayOfWeek
                bitisTarihi = cal.time

                cal.add(Calendar.DAY_OF_YEAR, -7)
                baslangicTarihi = cal.time
            }
            "Bu Ay" -> {
                cal = Calendar.getInstance()
                cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                cal.clear(Calendar.MINUTE)
                cal.clear(Calendar.SECOND)
                cal.clear(Calendar.MILLISECOND)

                cal.set(Calendar.DAY_OF_MONTH, 1)
                baslangicTarihi = cal.time


                cal.add(Calendar.MONTH, 1)
                bitisTarihi = cal.time
            }
            "Tüm Zamanlar" -> {
                cal.set(1970, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                baslangicTarihi = cal.time
                println(baslangicTarihi)

                cal.set(2920, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                bitisTarihi = cal.time
                println(bitisTarihi)

            }
        }

        var kurumKodu: Int
        val dersListesi = kotlin.collections.ArrayList<String>()
        val data: MutableList<DataEntry> = ArrayList()

        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            kurumKodu = it.get("kurumKodu").toString().toInt()

            db.collection("Lessons").addSnapshotListener { dersler, _ ->
                if (dersler != null) {
                    for (i in dersler) {
                        dersListesi.add(i.id)
                    }
                }
                for (dersIndex in dersListesi) {
                    db.collection("School").document(kurumKodu.toString()).collection("Student")
                        .whereEqualTo("teacher", auth.uid.toString())
                        .addSnapshotListener { ogrencliler, _ ->
                            if (ogrencliler != null) {
                                statsList.clear()

                                var valueIndex = 0

                                for (ogrenci in ogrencliler) {
                                    db.collection("School").document(kurumKodu.toString())
                                        .collection("Student").document(ogrenci.id)
                                        .collection("Studies").whereEqualTo("dersAdi", dersIndex)
                                        .whereGreaterThan("timestamp", baslangicTarihi)
                                        .whereLessThan("timestamp", bitisTarihi)
                                        .addSnapshotListener { value, _ ->
                                            var toplamCalisma = 0
                                            var cozulenSoru = 0

                                            if (value != null && ogrencliler.size() != 0) {

                                                for (study in value) {
                                                    toplamCalisma += study.get("toplamCalisma")
                                                        .toString().toInt()
                                                    cozulenSoru += study.get("çözülenSoru")
                                                        .toString().toInt()
                                                }


                                                if (tur == "Soru") {

                                                    data.add(
                                                        ValueDataEntry(
                                                            dersIndex,
                                                            (cozulenSoru / ogrencliler.size()).toString()
                                                                .toInt()
                                                        )
                                                    )


                                                } else if (tur == "Süre") {
                                                    data.add(
                                                        ValueDataEntry(
                                                            dersIndex,
                                                            (toplamCalisma / ogrencliler.size()).toString()
                                                                .toInt()
                                                        )
                                                    )
                                                }


                                            }
                                            if (valueIndex == ogrencliler.size()) {
                                                val column: Column = cartesian.column(data)


                                                column.tooltip().titleFormat("{%X}")
                                                    .position(Position.CENTER_BOTTOM)
                                                    .anchor(Anchor.CENTER_BOTTOM).offsetX(0.0)
                                                    .offsetY(5.0)
                                                    .format("{%Value}{groupsSeparator:.}")

                                                cartesian.animation(true)
                                                val title = "Koçluk $zamanAraligi"
                                                cartesian.title(title)

                                                cartesian.yScale().minimum(0.0)



                                                cartesian.tooltip()
                                                    .positionMode(TooltipPositionMode.POINT)
                                                cartesian.interactivity().hoverMode(HoverMode.BY_X)

                                                cartesian.xAxis(0).title("Dersler")
                                                if (tur == "Soru") {
                                                    cartesian.yAxis(0)
                                                        .title("Ortalama Çözülen Soru")
                                                    cartesian.yAxis(0).labels()
                                                        .format("{%Value}{groupsSeparator:.}")

                                                    column.tooltip().titleFormat("{%X}")
                                                        .position(Position.CENTER_BOTTOM)
                                                        .anchor(Anchor.CENTER_BOTTOM).offsetX(0.0)
                                                        .offsetY(5.0)
                                                        .format("{%Value}{groupsSeparator:.}")

                                                } else {
                                                    cartesian.yAxis(0)
                                                        .title("Ortalama Çalışılan Süre")
                                                    cartesian.yAxis(0).labels()
                                                        .format("{%Value}{groupsSeparator:.}dk")

                                                    column.tooltip().titleFormat("{%X}")
                                                        .position(Position.CENTER_BOTTOM)
                                                        .anchor(Anchor.CENTER_BOTTOM).offsetX(0.0)
                                                        .offsetY(5.0)
                                                        .format("{%Value}{groupsSeparator:.}dk")
                                                }

                                                anyChartView.setChart(cartesian)

                                            }
                                            valueIndex += 1


                                        }


                                }
                            }


                        }
                }
            }


        }


    }
}