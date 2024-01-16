package com.karaketir.coachingapp

import android.os.Bundle
import android.widget.Toast
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
import com.karaketir.coachingapp.databinding.ActivityDenemeAltKonuYanlisGraphBinding
import java.util.*

class DenemeAltKonuYanlisGraphActivity : AppCompatActivity() {


    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date
    private lateinit var binding: ActivityDenemeAltKonuYanlisGraphBinding
    private var zamanAraligi = ""
    private var denemeOwnerID = ""
    private var konuAdi = ""
    private var dersAdi = ""
    private var value = ""
    private var denemeTur = ""
    private var kurumKodu = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDenemeAltKonuYanlisGraphBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
        denemeTur = intent.getStringExtra("denemeTür").toString()
        zamanAraligi = intent.getStringExtra("zamanAraligi").toString()
        denemeOwnerID = intent.getStringExtra("denemeOwnerID").toString()
        dersAdi = intent.getStringExtra("dersAdi").toString()
        value = intent.getStringExtra("value").toString()
        konuAdi = intent.getStringExtra("konuAdi").toString()
        kurumKodu = intent.getStringExtra("kurumKodu").toString().toInt()


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

            "Son 30 Gün" -> {
                cal = Calendar.getInstance()

                bitisTarihi = cal.time

                cal.add(Calendar.DAY_OF_YEAR, -30)

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


            "Tüm Zamanlar" -> {
                cal.set(1970, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                baslangicTarihi = cal.time

                cal.set(2920, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                bitisTarihi = cal.time

            }
        }

        val denemeList = ArrayList<String>()

        val konuHashMap = hashMapOf<String, Int>()
        konuHashMap.clear()


        db.collection("School").document(kurumKodu.toString()).collection("Student")
            .document(denemeOwnerID).collection("Denemeler").whereEqualTo("denemeTür", denemeTur)
            .whereGreaterThan("denemeTarihi", baslangicTarihi)
            .whereLessThan("denemeTarihi", bitisTarihi)
            .orderBy("denemeTarihi", Query.Direction.DESCENDING).addSnapshotListener { value, _ ->
                denemeList.clear()
                if (value != null) {
                    for (deneme in value) {
                        denemeList.add(deneme.id)
                    }


                    for (id in denemeList) {
                        db.collection("School").document(kurumKodu.toString()).collection("Student")
                            .document(denemeOwnerID).collection("Denemeler").document(id)
                            .collection(dersAdi).document(konuAdi).collection("AltKonu")
                            .addSnapshotListener { konular, _ ->
                                if (konular != null) {
                                    for (konu in konular) {

                                        if (konu.get("konuAdi").toString() in konuHashMap.keys) {

                                            val currentValue =
                                                konuHashMap[konu.get("konuAdi").toString()]
                                            if (currentValue != null) {
                                                konuHashMap[konu.get("konuAdi").toString()] =
                                                    currentValue + konu.get("yanlisSayisi")
                                                        .toString().toInt()
                                            }


                                        } else {
                                            konuHashMap[konu.get("konuAdi").toString()] =
                                                konu.get("yanlisSayisi").toString().toInt()
                                        }


                                    }

                                }
                            }


                    }


                }


            }




        Toast.makeText(this, "Grafiği Görmek İçin Sağ Üsteki Butona Basın", Toast.LENGTH_LONG)
            .show()

        val showBtn = binding.showAltKonuBtn
        showBtn.setOnClickListener {
            drawGraph(konuHashMap)

        }


    }

    private fun drawGraph(konuHashMap: HashMap<String, Int>) {
        val data: MutableList<DataEntry> = ArrayList()
        val cartesian: Cartesian = AnyChart.column()
        val anyChartView = binding.anyChartDenemeAltKonuView

        val sortedMap = konuHashMap.toList().sortedBy { (key, _) -> key }.toMap()




        for (i in sortedMap.keys) {
            data.add(ValueDataEntry(i, sortedMap[i]))
        }


        val column: Column = cartesian.column(data)

        column.tooltip().titleFormat("{%X}").position(Position.CENTER_BOTTOM)
            .anchor(Anchor.CENTER_BOTTOM).offsetX(0.0).offsetY(5.0)
            .format("{%Value}{groupsSeparator:.}")

        cartesian.animation(true)
        val title = "$konuAdi - $value $zamanAraligi Deneme Yanlışları Konu Dağılımı"
        cartesian.title(title)

        cartesian.yScale().minimum(0.0)

        cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator:.}")

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
        cartesian.interactivity().hoverMode(HoverMode.BY_X)

        cartesian.xAxis(0).title("Konular")
        cartesian.yAxis(0).title("Yanlış Sayısı")

        anyChartView.setChart(cartesian)
    }
}