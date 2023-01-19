package com.karaketir.coachingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
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
import com.karaketir.coachingapp.databinding.ActivityClassAllStudiesGraphBinding
import java.util.*
import kotlin.collections.ArrayList

class ClassAllStudiesGraphActivity : AppCompatActivity() {

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



    private lateinit var binding: ActivityClassAllStudiesGraphBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date
    private var zamanAraligi = ""

    private var dersAdi = ""
    private var secilenTur = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityClassAllStudiesGraphBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
        zamanAraligi = intent.getStringExtra("secilenZamanAraligi").toString()
        val studentID = intent.getStringExtra("studentID").toString()
        dersAdi = intent.getStringExtra("dersAdi").toString()
        secilenTur = intent.getStringExtra("tür").toString()
        val konuHash = hashMapOf<String, Int>()

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

        db.collection("Lessons").document(dersAdi).collection(secilenTur)
            .addSnapshotListener { konular, _ ->
                if (konular != null) {
                    for (konu in konular) {
                        try {
                            val arrayType = konu.get("arrayType") as ArrayList<*>
                            if ("konu" in arrayType) {
                                konuHash[konu.get("konuAdi").toString()] = 0
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
                        }


                    }
                    db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
                        val kurumKodu = it.get("kurumKodu").toString().toInt()

                        db.collection("School").document(kurumKodu.toString()).collection("Student")
                            .document(studentID).collection("Studies")
                            .whereEqualTo("dersAdi", dersAdi).whereEqualTo("tür", secilenTur)
                            .whereGreaterThan("timestamp", baslangicTarihi)
                            .whereLessThan("timestamp", bitisTarihi)
                            .addSnapshotListener { value, error ->

                                if (error != null) {
                                    println(error.localizedMessage)
                                }

                                if (value != null) {

                                    for (i in value) {
                                        val konuAdi = i.get("konuAdi").toString()
                                        val currentValue = konuHash[konuAdi]

                                        if (i.get("toplamCalisma") != null && currentValue != null) {
                                            konuHash[konuAdi] = i.get("toplamCalisma").toString()
                                                .toInt() + currentValue
                                        }

                                    }


                                }
                                drawGraph(konuHash)


                            }

                    }


                }
            }


    }

    private fun drawGraph(konuHashMap: HashMap<String, Int>) {
        val data: MutableList<DataEntry> = ArrayList()
        val cartesian: Cartesian = AnyChart.column()
        val anyChartView = binding.anyChartClassAllStudies


        for (i in konuHashMap.keys) {
            data.add(ValueDataEntry(i, konuHashMap[i]))
        }

        val column: Column = cartesian.column(data)

        column.tooltip().titleFormat("{%X}").position(Position.CENTER_BOTTOM)
            .anchor(Anchor.CENTER_BOTTOM).offsetX(0.0).offsetY(5.0)
            .format("{%Value}{groupsSeparator:.}dk")

        cartesian.animation(true)
        val title = "$dersAdi $secilenTur $zamanAraligi Süre-Konu Dağılımı"
        cartesian.title(title)

        cartesian.yScale().minimum(0.0)

        cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator:.}dk")

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
        cartesian.interactivity().hoverMode(HoverMode.BY_X)

        cartesian.xAxis(0).title("Konular")
        cartesian.yAxis(0).title("Çalışma Süresi")

        anyChartView.setChart(cartesian)

    }

}