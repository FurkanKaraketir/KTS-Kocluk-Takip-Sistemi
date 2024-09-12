package com.karaketir.coachingapp.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.AddDenemeTeacherActivity
import com.karaketir.coachingapp.MainActivity
import com.karaketir.coachingapp.adapter.DenemelerTeacherRecyclerAdapter
import com.karaketir.coachingapp.databinding.FragmentDenemelerTeacherBinding
import com.karaketir.coachingapp.models.DenemeTeacher


class DenemelerTeacherFragment : Fragment() {

    private var mainActivity: MainActivity? = null

    fun setMainActivity(activity: MainActivity) {
        this.mainActivity = activity
    }

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


    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var denemelerList = ArrayList<DenemeTeacher>()
    private var gradeList = arrayOf("Bütün Sınıflar", "12", "11", "10", "9", "0", "13")
    private val turler = arrayOf("Tüm Denemeler", "TYT", "AYT")
    private var secilenGrade = ""
    private var secilenTur = ""
    private var kurumKodu = 0

    private var _binding: FragmentDenemelerTeacherBinding? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding ?: throw IllegalStateException("Binding is null")
    private var isViewCreated = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDenemelerTeacherBinding.inflate(inflater, container, false)
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

        if (isBindingAvailable()) {
            val mBinding = binding
            val layoutManager = LinearLayoutManager(mainActivity)

            val recyclerView = mBinding.recyclerViewDenemeler
            val addDenemeButton = mBinding.addDeneme
            val recyclerViewAdapter = DenemelerTeacherRecyclerAdapter(denemelerList, kurumKodu)
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = recyclerViewAdapter
            val gradeSpinner = mBinding.denemeTeacherGradeSpinner
            val turSpinner = mBinding.denemeTeacherTurSpinner
            addDenemeButton.setOnClickListener {
                val intent = Intent(mainActivity, AddDenemeTeacherActivity::class.java)
                intent.putExtra("kurumKodu", kurumKodu.toString())
                this.startActivity(intent)
            }

            val gradeAdapter = mainActivity?.let {
                ArrayAdapter(
                    it, android.R.layout.simple_spinner_item, gradeList
                )
            }
            val turAdapter = mainActivity?.let {
                ArrayAdapter(
                    it, android.R.layout.simple_spinner_item, turler
                )
            }

            gradeAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            gradeSpinner.adapter = gradeAdapter
            gradeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {

                    secilenGrade = gradeList[position]

                    turAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    turSpinner.adapter = turAdapter
                    turSpinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?, view: View?, position: Int, id: Long
                            ) {

                                secilenTur = turler[position]

                                if (secilenGrade == "Bütün Sınıflar" && secilenTur == "Tüm Denemeler") {
                                    denemelerList.clear()

                                    auth.currentUser?.let { it1 ->
                                        db.collection("School").document(kurumKodu.toString())
                                            .collection("Teacher").document(
                                                it1.uid
                                            ).collection("Denemeler")
                                            .orderBy("bitisTarihi", Query.Direction.DESCENDING)
                                            .addSnapshotListener { value, _ ->
                                                if (value != null) {
                                                    denemelerList.clear()
                                                    for (deneme in value) {
                                                        val currentDeneme = DenemeTeacher(
                                                            deneme.get("denemeAdi").toString(),
                                                            deneme.id
                                                        )
                                                        denemelerList.add(currentDeneme)
                                                    }
                                                    recyclerViewAdapter.notifyDataSetChanged()
                                                }
                                            }
                                    }


                                }
                                if (secilenGrade == "Bütün Sınıflar" && secilenTur != "Tüm Denemeler") {
                                    denemelerList.clear()

                                    auth.currentUser?.let { it1 ->
                                        db.collection("School").document(kurumKodu.toString())
                                            .collection("Teacher").document(
                                                it1.uid
                                            ).collection("Denemeler")
                                            .whereEqualTo("tür", secilenTur)
                                            .orderBy("bitisTarihi", Query.Direction.DESCENDING)
                                            .addSnapshotListener { value, _ ->
                                                if (value != null) {
                                                    denemelerList.clear()
                                                    for (deneme in value) {
                                                        val currentDeneme = DenemeTeacher(
                                                            deneme.get("denemeAdi").toString(),
                                                            deneme.id
                                                        )
                                                        denemelerList.add(currentDeneme)
                                                    }
                                                    recyclerViewAdapter.notifyDataSetChanged()
                                                }
                                            }
                                    }


                                }
                                if (secilenGrade != "Bütün Sınıflar" && secilenTur == "Tüm Denemeler") {
                                    denemelerList.clear()

                                    auth.currentUser?.let { it1 ->
                                        db.collection("School").document(kurumKodu.toString())
                                            .collection("Teacher").document(
                                                it1.uid
                                            ).collection("Denemeler")
                                            .whereEqualTo("grade", secilenGrade.toInt())
                                            .orderBy("bitisTarihi", Query.Direction.DESCENDING)
                                            .addSnapshotListener { value, _ ->
                                                if (value != null) {
                                                    denemelerList.clear()
                                                    for (deneme in value) {
                                                        val currentDeneme = DenemeTeacher(
                                                            deneme.get("denemeAdi").toString(),
                                                            deneme.id
                                                        )
                                                        denemelerList.add(currentDeneme)
                                                    }
                                                    recyclerViewAdapter.notifyDataSetChanged()
                                                }
                                            }
                                    }


                                }
                                if (secilenGrade != "Bütün Sınıflar" && secilenTur != "Tüm Denemeler") {
                                    denemelerList.clear()

                                    auth.currentUser?.let { it1 ->
                                        db.collection("School").document(kurumKodu.toString())
                                            .collection("Teacher").document(
                                                it1.uid
                                            ).collection("Denemeler")
                                            .whereEqualTo("grade", secilenGrade.toInt())
                                            .whereEqualTo("tür", secilenTur)
                                            .orderBy("bitisTarihi", Query.Direction.DESCENDING)
                                            .addSnapshotListener { value, _ ->
                                                if (value != null) {
                                                    denemelerList.clear()
                                                    for (deneme in value) {
                                                        val currentDeneme = DenemeTeacher(
                                                            deneme.get("denemeAdi").toString(),
                                                            deneme.id
                                                        )
                                                        denemelerList.add(currentDeneme)
                                                    }
                                                    recyclerViewAdapter.notifyDataSetChanged()
                                                }
                                            }
                                    }


                                }

                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {

                            }

                        }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }
        }


    }
}