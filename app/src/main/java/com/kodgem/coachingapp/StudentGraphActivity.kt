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
import java.util.*
import kotlin.collections.ArrayList


class StudentGraphActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentGraphBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var konular = ArrayList<Study>()
    private var konuSureAdlari = ArrayList<String>()
    private var konuSoruAdlari = ArrayList<String>()

    private var konuSureHash = hashMapOf<String, Int>()
    private var konuSoruHash = hashMapOf<String, Int>()
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        val studyOwnerID = intent.getStringExtra("studyOwnerID")
        val studyDersAdi = intent.getStringExtra("studyDersAdi")
        val studyTur = intent.getStringExtra("studyTur")
        val zamanAraligi = intent.getStringExtra("zamanAraligi")
        val grafikTuru = intent.getStringExtra("grafikTuru")
        val soruSayisi = intent.getStringExtra("soruSayisi")
        val anyChartView = binding.anyChartView

        val cartesian: Cartesian = AnyChart.column()
        var cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)


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


        if (studyOwnerID != null) {
            db.collection("School").document("SchoolIDDDD").collection("Student")
                .document(studyOwnerID).collection("Studies").whereEqualTo("dersAdi", studyDersAdi)
                .whereEqualTo("tür", studyTur).whereGreaterThan("timestamp", baslangicTarihi)
                .whereLessThan("timestamp", bitisTarihi)
                .orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { value, _ ->

                    if (value != null) {
                        konular.clear()
                        if (grafikTuru == "Süre") {
                            for (document in value) {
                                val documentKonuAdi = document.get("konuAdi").toString()
                                var studyCount: Int


                                if (documentKonuAdi in konuSureAdlari) {

                                    val currentValue = konuSureHash[documentKonuAdi]

                                    konuSureHash[documentKonuAdi] =
                                        document.get("toplamCalisma").toString()
                                            .toInt() + currentValue!!
                                    println(konuSureHash)

                                    studyCount = konuSureHash[documentKonuAdi]!!

                                } else {
                                    konuSureAdlari.add(documentKonuAdi)

                                    konuSureHash[documentKonuAdi] =
                                        document.get("toplamCalisma").toString().toInt()

                                    studyCount = konuSureHash[documentKonuAdi]!!
                                }

                                val currentDocument = Study(
                                    documentKonuAdi,
                                    studyCount.toString(),
                                    studyOwnerID,
                                    studyDersAdi!!,
                                    studyTur!!,
                                    soruSayisi!!
                                )


                                konular.add(currentDocument)

                            }
                            val data: MutableList<DataEntry> = ArrayList()



                            for (i in konuSureHash) {
                                println(i.key + " " + i.value)
                                data.add(ValueDataEntry(i.key, i.value))
                            }

                            val column: Column = cartesian.column(data)

                            column.tooltip().titleFormat("{%X}").position(Position.CENTER_BOTTOM)
                                .anchor(Anchor.CENTER_BOTTOM).offsetX(0.0).offsetY(5.0)
                                .format("{%Value}{groupsSeparator:.}dk")

                            cartesian.animation(true)
                            val title = "$studyTur $studyDersAdi $zamanAraligi"
                            cartesian.title(title)

                            cartesian.yScale().minimum(0.0)

                            cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator:.}dk")

                            cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
                            cartesian.interactivity().hoverMode(HoverMode.BY_X)

                            cartesian.xAxis(0).title("Konu Adları")
                            cartesian.yAxis(0).title("Süre")

                            anyChartView.setChart(cartesian)
                        } else if (grafikTuru == "Soru") {
                            for (document in value) {
                                val documentKonuAdi = document.get("konuAdi").toString()
                                var studyCount: Int


                                if (documentKonuAdi in konuSoruAdlari) {

                                    val currentValue = konuSoruHash[documentKonuAdi]
                                    konuSoruHash[documentKonuAdi] =
                                        document.get("çözülenSoru").toString()
                                            .toInt() + currentValue!!
                                    println("Selam")
                                    println(konuSoruHash)

                                    studyCount = konuSoruHash[documentKonuAdi]!!

                                } else {
                                    konuSoruAdlari.add(documentKonuAdi)

                                    konuSoruHash[documentKonuAdi] =
                                        document.get("çözülenSoru").toString().toInt()

                                    studyCount = konuSoruHash[documentKonuAdi]!!
                                }

                                val currentDocument = Study(
                                    documentKonuAdi,
                                    studyCount.toString(),
                                    studyOwnerID,
                                    studyDersAdi!!,
                                    studyTur!!,
                                    soruSayisi!!
                                )

                                konular.add(currentDocument)
                            }
                            val data: MutableList<DataEntry> = ArrayList()



                            for (i in konuSoruHash) {
                                println(i.key + " " + i.value)
                                data.add(ValueDataEntry(i.key, i.value))
                            }

                            val column: Column = cartesian.column(data)

                            column.tooltip().titleFormat("{%X}").position(Position.CENTER_BOTTOM)
                                .anchor(Anchor.CENTER_BOTTOM).offsetX(0.0).offsetY(5.0)
                                .format("{%Value}{groupsSeparator:.}")

                            cartesian.animation(true)
                            val title = "$studyTur $studyDersAdi $zamanAraligi"
                            cartesian.title(title)

                            cartesian.yScale().minimum(0.0)

                            cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator:.}")

                            cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
                            cartesian.interactivity().hoverMode(HoverMode.BY_X)

                            cartesian.xAxis(0).title("Konu Adları")
                            cartesian.yAxis(0).title("Çözülen Soru Sayısı")

                            anyChartView.setChart(cartesian)
                        }

                    }

                }
        }


    }


}