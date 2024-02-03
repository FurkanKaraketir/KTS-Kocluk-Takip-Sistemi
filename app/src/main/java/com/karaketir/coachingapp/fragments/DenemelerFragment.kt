package com.karaketir.coachingapp.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.EnterTytActivity
import com.karaketir.coachingapp.MainActivity
import com.karaketir.coachingapp.R
import com.karaketir.coachingapp.adapter.DenemelerRecyclerAdapter
import com.karaketir.coachingapp.databinding.FragmentDenemelerBinding
import com.karaketir.coachingapp.models.Deneme
import java.util.Calendar
import java.util.Date


class DenemelerFragment(private var mainActivity: MainActivity) : Fragment() {
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


    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerAdapter: DenemelerRecyclerAdapter
    private lateinit var spinner: Spinner
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var denemeList = ArrayList<Deneme>()
    private lateinit var baslangicTarihi: Date
    private lateinit var bitisTarihi: Date
    private var kurumKodu = 763455
    private var secilenZamanAraligi = ""
    private lateinit var studentID: String
    private val zamanAraliklari = arrayOf(
        "Tüm Zamanlar",
        "Bu Ay",
        "Son 30 Gün",
        "Geçen Ay",
        "Son 2 Ay",
        "Son 3 Ay",
        "Son 4 Ay",
        "Son 5 Ay",
        "Son 6 Ay"
    )
    private val turler = arrayOf("Tüm Denemeler", "TYT", "AYT")
    private lateinit var layoutManager: GridLayoutManager
    private var grade = 0
    private var teacher = ""
    private var personType = ""

    private var _binding: FragmentDenemelerBinding? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding ?: throw IllegalStateException("Binding is null")
    private var isViewCreated = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDenemelerBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        isViewCreated = false
    }

    private fun isBindingAvailable(): Boolean {
        return isViewCreated && _binding != null
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewCreated = true


        auth = Firebase.auth
        db = Firebase.firestore
        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener { user ->
            kurumKodu = user.get("kurumKodu").toString().toInt()
            grade = user.get("grade").toString().toInt()
            teacher = user.get("teacher").toString()
            personType = user.get("personType").toString()
            studentID = auth.uid.toString()

            if (isBindingAvailable()) {
                val mBinding = binding

                recyclerView = mBinding.denemelerRecyclerView
                spinner = mBinding.denemeSpinner
                val turSpinner = mBinding.denemeTurSpinner
                val denemeAddButton = mBinding.denemeAddButton

                layoutManager = GridLayoutManager(mainActivity, 2)

                if (personType == "Student") {
                    denemeAddButton.visibility = View.VISIBLE
                } else {
                    denemeAddButton.visibility = View.GONE
                }


                val denemeAdapter = ArrayAdapter(
                    mainActivity, android.R.layout.simple_spinner_item, zamanAraliklari
                )
                val turAdapter = ArrayAdapter(
                    mainActivity, android.R.layout.simple_spinner_item, turler
                )


                denemeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = denemeAdapter
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onItemSelected(
                        p0: AdapterView<*>?, p1: View?, position: Int, p3: Long
                    ) {
                        secilenZamanAraligi = zamanAraliklari[position]

                        recyclerView = binding.denemelerRecyclerView
                        recyclerView.layoutManager = layoutManager
                        recyclerAdapter =
                            DenemelerRecyclerAdapter(denemeList, secilenZamanAraligi, kurumKodu)
                        recyclerView.adapter = recyclerAdapter
                        recyclerAdapter.notifyDataSetChanged()

                        var cal = Calendar.getInstance()
                        cal[Calendar.HOUR_OF_DAY] = 0 // ! clear would not reset the hour of day !

                        cal.clear(Calendar.MINUTE)
                        cal.clear(Calendar.SECOND)
                        cal.clear(Calendar.MILLISECOND)

                        when (secilenZamanAraligi) {
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
                                cal[Calendar.HOUR_OF_DAY] =
                                    0 // ! clear would not reset the hour of day !

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
                                cal[Calendar.HOUR_OF_DAY] =
                                    0 // ! clear would not reset the hour of day !

                                cal.clear(Calendar.MINUTE)
                                cal.clear(Calendar.SECOND)
                                cal.clear(Calendar.MILLISECOND)

                                cal.set(Calendar.DAY_OF_MONTH, 1)
                                bitisTarihi = cal.time


                                cal.add(Calendar.MONTH, -1)
                                baslangicTarihi = cal.time

                            }

                            "Son 30 Gün" -> {
                                cal = Calendar.getInstance()

                                bitisTarihi = cal.time

                                cal.add(Calendar.DAY_OF_YEAR, -30)

                                baslangicTarihi = cal.time

                            }

                            "Son 2 Ay" -> {
                                cal = Calendar.getInstance()
                                cal[Calendar.HOUR_OF_DAY] =
                                    0 // ! clear would not reset the hour of day !

                                cal.clear(Calendar.MINUTE)
                                cal.clear(Calendar.SECOND)
                                cal.clear(Calendar.MILLISECOND)

                                bitisTarihi = cal.time

                                cal.add(Calendar.MONTH, -2)
                                baslangicTarihi = cal.time
                            }

                            "Son 3 Ay" -> {
                                cal = Calendar.getInstance()
                                cal[Calendar.HOUR_OF_DAY] =
                                    0 // ! clear would not reset the hour of day !

                                cal.clear(Calendar.MINUTE)
                                cal.clear(Calendar.SECOND)
                                cal.clear(Calendar.MILLISECOND)

                                bitisTarihi = cal.time

                                cal.add(Calendar.MONTH, -3)
                                baslangicTarihi = cal.time
                            }

                            "Son 4 Ay" -> {
                                cal = Calendar.getInstance()
                                cal[Calendar.HOUR_OF_DAY] =
                                    0 // ! clear would not reset the hour of day !

                                cal.clear(Calendar.MINUTE)
                                cal.clear(Calendar.SECOND)
                                cal.clear(Calendar.MILLISECOND)

                                bitisTarihi = cal.time

                                cal.add(Calendar.MONTH, -4)
                                baslangicTarihi = cal.time
                            }

                            "Son 5 Ay" -> {
                                cal = Calendar.getInstance()
                                cal[Calendar.HOUR_OF_DAY] =
                                    0 // ! clear would not reset the hour of day !

                                cal.clear(Calendar.MINUTE)
                                cal.clear(Calendar.SECOND)
                                cal.clear(Calendar.MILLISECOND)

                                bitisTarihi = cal.time

                                cal.add(Calendar.MONTH, -5)
                                baslangicTarihi = cal.time
                            }

                            "Son 6 Ay" -> {
                                cal = Calendar.getInstance()
                                cal[Calendar.HOUR_OF_DAY] =
                                    0 // ! clear would not reset the hour of day !

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


                                cal.set(2077, Calendar.JANUARY, Calendar.DAY_OF_WEEK)
                                bitisTarihi = cal.time

                            }
                        }

                        turAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        turSpinner.adapter = turAdapter
                        turSpinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {

                                override fun onItemSelected(
                                    p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
                                ) {

                                    val secilenTur = turler[p2]
                                    if (secilenTur != "Tüm Denemeler") {


                                        db.collection("School").document(kurumKodu.toString())
                                            .collection("Student").document(studentID)
                                            .collection("Denemeler")
                                            .whereEqualTo("denemeTür", secilenTur)
                                            .whereGreaterThan("denemeTarihi", baslangicTarihi)
                                            .whereLessThan("denemeTarihi", bitisTarihi)
                                            .orderBy("denemeTarihi", Query.Direction.DESCENDING)
                                            .addSnapshotListener { value, error ->

                                                if (error != null) {
                                                    println(error.localizedMessage)
                                                }
                                                if (value != null) {
                                                    denemeList.clear()
                                                    if (!value.isEmpty) {
                                                        for (document in value) {
                                                            val denemeID = document.id
                                                            val denemeAdi =
                                                                document.get("denemeAdi").toString()
                                                            val denemeToplamNet =
                                                                document.get("toplamNet").toString()
                                                                    .toFloat()
                                                            val denemeTarihi =
                                                                document.get("denemeTarihi") as Timestamp
                                                            val denemeTur =
                                                                document.get("denemeTür").toString()

                                                            val currentDeneme = Deneme(
                                                                denemeID,
                                                                denemeAdi,
                                                                denemeToplamNet,
                                                                denemeTarihi,
                                                                studentID,
                                                                denemeTur
                                                            )
                                                            denemeList.add(currentDeneme)
                                                        }

                                                        recyclerAdapter.notifyDataSetChanged()

                                                    } else {
                                                        denemeList.clear()
                                                        recyclerAdapter.notifyDataSetChanged()

                                                    }


                                                } else {
                                                    denemeList.clear()
                                                    recyclerAdapter.notifyDataSetChanged()

                                                }

                                            }


                                    } else {
                                        db.collection("School").document(kurumKodu.toString())
                                            .collection("Student").document(studentID)
                                            .collection("Denemeler")
                                            .whereGreaterThan("denemeTarihi", baslangicTarihi)
                                            .whereLessThan("denemeTarihi", bitisTarihi)
                                            .orderBy("denemeTarihi", Query.Direction.DESCENDING)
                                            .addSnapshotListener { value, error ->

                                                if (error != null) {
                                                    println(error.localizedMessage)
                                                }
                                                if (value != null) {
                                                    denemeList.clear()
                                                    if (!value.isEmpty) {
                                                        for (document in value) {
                                                            val denemeID = document.id
                                                            val denemeAdi =
                                                                document.get("denemeAdi").toString()
                                                            val denemeToplamNet =
                                                                document.get("toplamNet").toString()
                                                                    .toFloat()
                                                            val denemeTarihi =
                                                                document.get("denemeTarihi") as Timestamp
                                                            val denemeTur =
                                                                document.get("denemeTür").toString()

                                                            val currentDeneme = Deneme(
                                                                denemeID,
                                                                denemeAdi,
                                                                denemeToplamNet,
                                                                denemeTarihi,
                                                                studentID,
                                                                denemeTur
                                                            )
                                                            denemeList.add(currentDeneme)
                                                        }

                                                        recyclerAdapter.notifyDataSetChanged()

                                                    } else {
                                                        denemeList.clear()
                                                        recyclerAdapter.notifyDataSetChanged()

                                                    }


                                                } else {
                                                    denemeList.clear()
                                                    recyclerAdapter.notifyDataSetChanged()

                                                }

                                            }


                                    }


                                }

                                override fun onNothingSelected(p0: AdapterView<*>?) {
                                }

                            }




                        db.collection("School").document(kurumKodu.toString()).collection("Student")
                            .document(studentID).collection("Denemeler")
                            .whereGreaterThan("denemeTarihi", baslangicTarihi)
                            .whereLessThan("denemeTarihi", bitisTarihi)
                            .orderBy("denemeTarihi", Query.Direction.DESCENDING)
                            .addSnapshotListener { value, error ->

                                if (error != null) {
                                    println(error.localizedMessage)
                                }
                                if (value != null) {
                                    denemeList.clear()
                                    if (!value.isEmpty) {
                                        for (document in value) {
                                            val denemeID = document.id
                                            val denemeAdi = document.get("denemeAdi").toString()
                                            val denemeToplamNet =
                                                document.get("toplamNet").toString().toFloat()
                                            val denemeTarihi =
                                                document.get("denemeTarihi") as Timestamp
                                            val denemeTur = document.get("denemeTür").toString()

                                            val currentDeneme = Deneme(
                                                denemeID,
                                                denemeAdi,
                                                denemeToplamNet,
                                                denemeTarihi,
                                                studentID,
                                                denemeTur
                                            )
                                            denemeList.add(currentDeneme)
                                        }

                                        recyclerAdapter.notifyDataSetChanged()

                                    } else {
                                        denemeList.clear()
                                        recyclerAdapter.notifyDataSetChanged()

                                    }


                                } else {
                                    denemeList.clear()
                                    recyclerAdapter.notifyDataSetChanged()

                                }

                            }


                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }

                }
                denemeAddButton.setOnClickListener {


                    if (teacher.isNotEmpty()) {
                        val popup = PopupMenu(mainActivity, it)
                        //inflate menu with layout mainmenu
                        popup.inflate(R.menu.subject_context)
                        popup.show()

                        popup.setOnMenuItemClickListener { item ->
                            if (item.itemId == R.id.TYT) {
                                val intent = Intent(mainActivity, EnterTytActivity::class.java)
                                intent.putExtra("studyType", "TYT")
                                intent.putExtra("grade", grade.toString())
                                intent.putExtra("teacher", teacher)
                                intent.putExtra("kurumKodu", kurumKodu.toString())
                                this.startActivity(intent)
                            }

                            if (item.itemId == R.id.AYT) {
                                val intent = Intent(mainActivity, EnterTytActivity::class.java)
                                intent.putExtra("studyType", "AYT")
                                intent.putExtra("grade", grade.toString())
                                intent.putExtra("teacher", teacher)
                                intent.putExtra("kurumKodu", kurumKodu.toString())
                                this.startActivity(intent)
                            }
                            false
                        }
                    } else {
                        Toast.makeText(
                            mainActivity, "Koç Öğretmeniniz Bulunmamaktadır.", Toast.LENGTH_SHORT
                        ).show()
                    }


                }


            }

        }

    }
}