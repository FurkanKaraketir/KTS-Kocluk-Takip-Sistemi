package com.karaketir.coachingapp

import android.annotation.SuppressLint
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
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.databinding.ActivityStudentGraphBinding
import com.karaketir.coachingapp.models.Study
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class StudentGraphActivity : AppCompatActivity() {

    init {
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        )
    }


    private lateinit var binding: ActivityStudentGraphBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var kurumKodu = 0

    private var konular = ArrayList<Study>()
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        val studyOwnerID = intent.getStringExtra("studyOwnerID")
        val studyDersAdi = intent.getStringExtra("studyDersAdi")
        val studyKonuAdi = intent.getStringExtra("studyKonuAdi")
        val studyTur = intent.getStringExtra("studyTur")
        val zamanAraligi = intent.getStringExtra("zamanAraligi")
        val grafikTuru = intent.getStringExtra("grafikTuru")
        val soruSayisi = intent.getStringExtra("soruSayisi")
        val anyChartView = binding.anyChartView
        kurumKodu = intent.getStringExtra("kurumKodu").toString().toInt()

        val cartesian: Cartesian = AnyChart.column()
        var cal = Calendar.getInstance()
        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

        cal.clear(Calendar.MINUTE)
        cal.clear(Calendar.SECOND)
        cal.clear(Calendar.MILLISECOND)


        when (zamanAraligi) {
            "Bugün" -> {
                baslangicTarihi = cal.time
                cal.add(Calendar.DAY_OF_YEAR, 1)
                bitisTarihi = cal.time
            }

            "Dün" -> {
                bitisTarihi = cal.time

                cal.add(Calendar.DAY_OF_YEAR, -1)
                baslangicTarihi = cal.time
            }

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

            "Geçen Ay" -> {
                cal = Calendar.getInstance()
                cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                cal.clear(Calendar.MINUTE)
                cal.clear(Calendar.SECOND)
                cal.clear(Calendar.MILLISECOND)

                cal.set(Calendar.DAY_OF_MONTH, 1)
                bitisTarihi = cal.time


                cal.add(Calendar.MONTH, -1)
                baslangicTarihi = cal.time

            }

            "Tüm Zamanlar" -> {
                cal.set(1970, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                baslangicTarihi = cal.time

                cal.set(2920, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                bitisTarihi = cal.time

            }
        }


        if (studyOwnerID != null) {
            db.collection("School").document(kurumKodu.toString()).collection("Student")
                .document(studyOwnerID).collection("Studies").whereEqualTo("dersAdi", studyDersAdi)
                .whereEqualTo("tür", studyTur).whereEqualTo("konuAdi", studyKonuAdi)
                .whereGreaterThan("timestamp", baslangicTarihi)
                .whereLessThan("timestamp", bitisTarihi)
                .orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { value, _ ->

                    if (value != null) {
                        konular.clear()
                        if (grafikTuru == "Süre") {
                            for (document in value) {
                                val documentKonuAdi = document.get("konuAdi").toString()
                                val studyCount = document.get("toplamCalisma").toString()
                                val timestamp = document.get("timestamp") as Timestamp

                                val currentDocument = Study(
                                    documentKonuAdi,
                                    studyCount,
                                    studyOwnerID,
                                    studyDersAdi!!,
                                    studyTur!!,
                                    soruSayisi!!,
                                    timestamp,
                                    document.id
                                )


                                konular.add(currentDocument)

                            }
                            val data: MutableList<DataEntry> = ArrayList()



                            for (i in konular) {
                                val date = i.timestamp.toDate()
                                val dateFormated = SimpleDateFormat("dd/MM/yyyy").format(date)
                                data.add(ValueDataEntry(dateFormated, i.studyCount.toInt()))
                            }

                            val column: Column = cartesian.column(data)

                            column.tooltip().titleFormat("{%X}").position(Position.CENTER_BOTTOM)
                                .anchor(Anchor.CENTER_BOTTOM).offsetX(0.0).offsetY(5.0)
                                .format("{%Value}{groupsSeparator:.}dk")

                            cartesian.animation(true)
                            val title = "$studyTur $studyDersAdi $studyKonuAdi $zamanAraligi"
                            cartesian.title(title)

                            cartesian.yScale().minimum(0.0)

                            cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator:.}dk")

                            cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
                            cartesian.interactivity().hoverMode(HoverMode.BY_X)

                            cartesian.xAxis(0).title("Tarihler")
                            cartesian.yAxis(0).title("Çalışılan Süre")

                            anyChartView.setChart(cartesian)
                        } else if (grafikTuru == "Soru") {
                            for (document in value) {
                                val documentKonuAdi = document.get("konuAdi").toString()
                                val studyCount = document.get("çözülenSoru").toString()
                                val timestamp = document.get("timestamp") as Timestamp


                                val currentDocument = Study(
                                    documentKonuAdi,
                                    studyCount,
                                    studyOwnerID,
                                    studyDersAdi!!,
                                    studyTur!!,
                                    soruSayisi!!,
                                    timestamp,
                                    document.id
                                )

                                konular.add(currentDocument)
                            }
                            val data: MutableList<DataEntry> = ArrayList()



                            for (i in konular) {
                                val date = i.timestamp.toDate()
                                val dateFormated = SimpleDateFormat("dd/MM/yyyy").format(date)
                                data.add(ValueDataEntry(dateFormated, i.soruSayisi.toInt()))
                            }

                            val column: Column = cartesian.column(data)

                            column.tooltip().titleFormat("{%X}").position(Position.CENTER_BOTTOM)
                                .anchor(Anchor.CENTER_BOTTOM).offsetX(0.0).offsetY(5.0)
                                .format("{%Value}{groupsSeparator:.}")

                            cartesian.animation(true)
                            val title = "$studyTur $studyDersAdi $studyKonuAdi $zamanAraligi"
                            cartesian.title(title)

                            cartesian.yScale().minimum(0.0)

                            cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator:.}")

                            cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
                            cartesian.interactivity().hoverMode(HoverMode.BY_X)

                            cartesian.xAxis(0).title("Tarihler")
                            cartesian.yAxis(0).title("Çözülen Soru Sayısı")

                            anyChartView.setChart(cartesian)
                        }

                    }

                }
        }


    }


}