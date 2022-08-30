package com.kodgem.coachingapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
import com.kodgem.coachingapp.databinding.ActivityDenemeNetGraphByTimeBinding
import java.text.SimpleDateFormat
import java.util.*

class DenemeNetGraphByTimeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDenemeNetGraphByTimeBinding

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date

    private var zamanAraligi = ""
    private var dersAdi = ""

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDenemeNetGraphByTimeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
        zamanAraligi = intent.getStringExtra("zamanAraligi").toString()

        val denemeOwnerID = intent.getStringExtra("denemeOwnerID")

        dersAdi = intent.getStringExtra("dersAdi").toString()

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

        var kurumKodu: Int
        val netHash = hashMapOf<String, Float>()

        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            kurumKodu = it.get("kurumKodu").toString().toInt()

            db.collection("School").document(kurumKodu.toString()).collection("Student")
                .document(denemeOwnerID!!).collection("Denemeler")
                .whereGreaterThan("denemeTarihi", baslangicTarihi)
                .whereLessThan("denemeTarihi", bitisTarihi)
                .orderBy("denemeTarihi", Query.Direction.DESCENDING)
                .addSnapshotListener { value, _ ->

                    if (value != null) {
                        for (deneme in value) {
                            val denemeAdi = deneme.get("denemeAdi").toString()

                            when (dersAdi) {
                                "ToplamNet" -> {
                                    netHash[denemeAdi] =
                                        deneme.get("toplamNet").toString().toFloat()
                                }
                                "Türkçe-Edebiyat" -> {
                                    netHash[denemeAdi] =
                                        deneme.get("turkceNet").toString().toFloat()
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

                        println(netHash)

                        for (i in netHash.keys) {
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
                        cartesian.yAxis(0).title("Yanlış Sayısı")

                        anyChartView.setChart(cartesian)

                    }

                }
        }
    }
}