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
import com.karaketir.coachingapp.databinding.ActivityDenemeNetGraphByTimeBinding
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class DenemeNetGraphByTimeActivity : AppCompatActivity() {


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


    private lateinit var binding: ActivityDenemeNetGraphByTimeBinding

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var baslangicTarihi: Date
    private var kurumKodu = 0
    private lateinit var bitisTarihi: Date

    private var zamanAraligi = ""
    private var dersAdi = ""

    private var denemeTur = ""

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDenemeNetGraphByTimeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
        kurumKodu = intent.getStringExtra("kurumKodu").toString().toInt()
        zamanAraligi = intent.getStringExtra("zamanAraligi").toString()
        denemeTur = intent.getStringExtra("denemeTür").toString()

        val denemeOwnerID = intent.getStringExtra("denemeOwnerID")

        dersAdi = intent.getStringExtra("dersAdi").toString()

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

            "Son 2 Ay" -> {
                cal = Calendar.getInstance()
                cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                cal.clear(Calendar.MINUTE)
                cal.clear(Calendar.SECOND)
                cal.clear(Calendar.MILLISECOND)

                bitisTarihi = cal.time

                cal.add(Calendar.MONTH, -2)
                baslangicTarihi = cal.time
            }

            "Son 3 Ay" -> {
                cal = Calendar.getInstance()
                cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                cal.clear(Calendar.MINUTE)
                cal.clear(Calendar.SECOND)
                cal.clear(Calendar.MILLISECOND)

                bitisTarihi = cal.time

                cal.add(Calendar.MONTH, -3)
                baslangicTarihi = cal.time
            }

            "Son 4 Ay" -> {
                cal = Calendar.getInstance()
                cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                cal.clear(Calendar.MINUTE)
                cal.clear(Calendar.SECOND)
                cal.clear(Calendar.MILLISECOND)

                bitisTarihi = cal.time

                cal.add(Calendar.MONTH, -4)
                baslangicTarihi = cal.time
            }

            "Son 5 Ay" -> {
                cal = Calendar.getInstance()
                cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                cal.clear(Calendar.MINUTE)
                cal.clear(Calendar.SECOND)
                cal.clear(Calendar.MILLISECOND)

                bitisTarihi = cal.time

                cal.add(Calendar.MONTH, -5)
                baslangicTarihi = cal.time
            }

            "Son 6 Ay" -> {
                cal = Calendar.getInstance()
                cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                cal.clear(Calendar.MINUTE)
                cal.clear(Calendar.SECOND)
                cal.clear(Calendar.MILLISECOND)

                bitisTarihi = cal.time

                cal.add(Calendar.MONTH, -6)
                baslangicTarihi = cal.time
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

        val netHash = hashMapOf<String, Float>()


        db.collection("School").document(kurumKodu.toString()).collection("Student")
            .document(denemeOwnerID!!).collection("Denemeler").whereEqualTo("denemeTür", denemeTur)
            .whereGreaterThan("denemeTarihi", baslangicTarihi)
            .whereLessThan("denemeTarihi", bitisTarihi)
            .orderBy("denemeTarihi", Query.Direction.DESCENDING).addSnapshotListener { value, _ ->

                if (value != null) {
                    val denemeTarihList = HashMap<String, Timestamp>()
                    for (deneme in value) {
                        val denemeAdi = deneme.get("denemeAdi").toString()
                        denemeTarihList[denemeAdi] = deneme.get("denemeTarihi") as Timestamp
                        when (dersAdi) {
                            "ToplamNet" -> {
                                netHash[denemeAdi] = deneme.get("toplamNet").toString().toFloat()
                            }

                            "Türkçe-Edebiyat" -> {
                                netHash[denemeAdi] = deneme.get("turkceNet").toString().toFloat()
                            }

                            "Tarih" -> {
                                netHash[denemeAdi] = deneme.get("tarihNet").toString().toFloat()
                            }

                            "Coğrafya" -> {
                                netHash[denemeAdi] = deneme.get("cogNet").toString().toFloat()
                            }

                            "Felsefe" -> {
                                netHash[denemeAdi] = deneme.get("felNet").toString().toFloat()
                            }

                            "Din" -> {
                                netHash[denemeAdi] = deneme.get("dinNet").toString().toFloat()
                            }

                            "Matematik" -> {
                                netHash[denemeAdi] = deneme.get("matNet").toString().toFloat()
                            }

                            "Geometri" -> {
                                netHash[denemeAdi] = deneme.get("geoNet").toString().toFloat()
                            }

                            "Fizik" -> {
                                netHash[denemeAdi] = deneme.get("fizNet").toString().toFloat()
                            }

                            "Kimya" -> {
                                netHash[denemeAdi] = deneme.get("kimyaNet").toString().toFloat()
                            }

                            "Biyoloji" -> {
                                netHash[denemeAdi] = deneme.get("biyoNet").toString().toFloat()
                            }
                        }
                    }
                    val data: MutableList<DataEntry> = ArrayList()
                    val cartesian: Cartesian = AnyChart.column()
                    val anyChartView = binding.anyChartDenemeByTimeView
                    val sortedDateMap =
                        denemeTarihList.toList().sortedBy { (_, date) -> date }.toMap()

                    for (i in sortedDateMap.keys) {
                        data.add(ValueDataEntry(i, netHash[i]))
                    }


                    val column: Column = cartesian.column(data)

                    column.tooltip().titleFormat("{%X}").position(Position.CENTER_BOTTOM)
                        .anchor(Anchor.CENTER_BOTTOM).offsetX(0.0).offsetY(5.0)
                        .format("{%Value}{groupsSeparator:.}")

                    cartesian.animation(true)
                    val title: String = if (dersAdi == "ToplamNet") {
                        "$zamanAraligi Toplam Deneme Netleri"
                    } else {
                        "$dersAdi $zamanAraligi Deneme Netleri"
                    }
                    cartesian.title(title)

                    cartesian.yScale().minimum(0.0)

                    cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator:.}")

                    cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
                    cartesian.interactivity().hoverMode(HoverMode.BY_X)

                    cartesian.xAxis(0).title("Deneme Adları")
                    cartesian.yAxis(0).title("Net Sayısı")

                    anyChartView.setChart(cartesian)

                }

            }

    }
}