package com.karaketir.coachingapp.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.EnterDutyActivity
import com.karaketir.coachingapp.adapter.DutiesRecyclerAdapter
import com.karaketir.coachingapp.databinding.FragmentDutiesBinding
import com.karaketir.coachingapp.models.Duty

class DutiesFragment : Fragment(), AdapterView.OnItemSelectedListener {


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
    private lateinit var dutiesRecyclerAdapter: DutiesRecyclerAdapter
    private var gorevTurleri =
        arrayOf("Tamamlanmayan Görevler", "Tamamlanan Görevler", "Tüm Görevler")
    private var dutyList = ArrayList<Duty>()
    private lateinit var dutyAddButton: FloatingActionButton

    private var kurumKodu = 0
    private var studentID = ""
    private var personType = ""


    private var _binding: FragmentDutiesBinding? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding ?: throw IllegalStateException("Binding is null")
    private var isViewCreated = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDutiesBinding.inflate(inflater, container, false)
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
            studentID = auth.uid.toString()
            kurumKodu = user.get("kurumKodu").toString().toInt()
            personType = user.get("personType").toString()

            if (isBindingAvailable()) {
                val mBinding = binding


                val dutiesRecyclerView = mBinding.dutiesRecyclerView
                val gorevTuruSpinner = mBinding.gorevSpinner
                dutyAddButton = mBinding.addDutyButton
                val dutyAdapter = ArrayAdapter(
                    requireActivity(), android.R.layout.simple_spinner_item, gorevTurleri
                )
                dutyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                gorevTuruSpinner.adapter = dutyAdapter
                gorevTuruSpinner.onItemSelectedListener = this

                if (personType == "Teacher") {
                    dutyAddButton.visibility = View.VISIBLE
                } else {
                    dutyAddButton.visibility = View.GONE
                }


                val layoutManager = GridLayoutManager(requireActivity(), 2)

                dutiesRecyclerView.layoutManager = layoutManager

                dutiesRecyclerAdapter =
                    DutiesRecyclerAdapter(dutyList, kurumKodu, personType = "fdf")

                dutiesRecyclerView.adapter = dutiesRecyclerAdapter

            }


        }


    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {


        when (p2) {
            0 -> {
                db.collection("School").document(kurumKodu.toString()).collection("Student")
                    .document(studentID).collection("Duties").whereEqualTo("tamamlandi", false)
                    .orderBy("eklenmeTarihi", Query.Direction.DESCENDING)
                    .addSnapshotListener { value, e ->
                        if (e != null) {
                            println(e.localizedMessage)
                        }
                        if (value != null) {
                            dutyList.clear()
                            for (document in value) {
                                val konuAdi = document.get("konuAdi").toString()
                                val tur = document.get("tür").toString()
                                val dersAdi = document.get("dersAdi").toString()
                                val toplamCalisma = document.get("toplamCalisma").toString()
                                val cozulenSoru = document.get("çözülenSoru").toString()
                                val bitisZamani = document.get("bitisZamani") as Timestamp
                                val dutyTamamlandi = document.get("tamamlandi") as Boolean

                                val currentDuty = Duty(
                                    konuAdi,
                                    toplamCalisma,
                                    studentID,
                                    dersAdi,
                                    tur,
                                    cozulenSoru,
                                    bitisZamani,
                                    document.id,
                                    dutyTamamlandi
                                )
                                dutyList.add(currentDuty)
                            }
                            dutiesRecyclerAdapter.notifyDataSetChanged()


                        }
                    }
            }

            1 -> {
                db.collection("School").document(kurumKodu.toString()).collection("Student")
                    .document(studentID).collection("Duties").whereEqualTo("tamamlandi", true)
                    .orderBy("eklenmeTarihi", Query.Direction.DESCENDING)
                    .addSnapshotListener { value, e ->
                        if (e != null) {
                            println(e.localizedMessage)
                        }
                        if (value != null) {
                            dutyList.clear()
                            for (document in value) {
                                val konuAdi = document.get("konuAdi").toString()
                                val tur = document.get("tür").toString()
                                val dersAdi = document.get("dersAdi").toString()
                                val toplamCalisma = document.get("toplamCalisma").toString()
                                val cozulenSoru = document.get("çözülenSoru").toString()
                                val bitisZamani = document.get("bitisZamani") as Timestamp
                                val dutyTamamlandi = document.get("tamamlandi") as Boolean

                                val currentDuty = Duty(
                                    konuAdi,
                                    toplamCalisma,
                                    studentID,
                                    dersAdi,
                                    tur,
                                    cozulenSoru,
                                    bitisZamani,
                                    document.id,
                                    dutyTamamlandi
                                )
                                dutyList.add(currentDuty)
                            }
                            dutiesRecyclerAdapter.notifyDataSetChanged()


                        }
                    }
            }

            2 -> {
                db.collection("School").document(kurumKodu.toString()).collection("Student")
                    .document(studentID).collection("Duties")
                    .orderBy("eklenmeTarihi", Query.Direction.DESCENDING)
                    .addSnapshotListener { value, e ->
                        if (e != null) {
                            println(e.localizedMessage)
                        }
                        if (value != null) {
                            dutyList.clear()
                            for (document in value) {
                                val konuAdi = document.get("konuAdi").toString()
                                val tur = document.get("tür").toString()
                                val dersAdi = document.get("dersAdi").toString()
                                val toplamCalisma = document.get("toplamCalisma").toString()
                                val cozulenSoru = document.get("çözülenSoru").toString()
                                val bitisZamani = document.get("bitisZamani") as Timestamp
                                val dutyTamamlandi = document.get("tamamlandi") as Boolean

                                val currentDuty = Duty(
                                    konuAdi,
                                    toplamCalisma,
                                    studentID,
                                    dersAdi,
                                    tur,
                                    cozulenSoru,
                                    bitisZamani,
                                    document.id,
                                    dutyTamamlandi
                                )
                                dutyList.add(currentDuty)
                            }
                            dutiesRecyclerAdapter.notifyDataSetChanged()


                        }
                    }
            }

        }


        dutyAddButton = binding.addDutyButton

        dutyAddButton.setOnClickListener {
            val sendIntent = Intent(requireActivity(), EnterDutyActivity::class.java)
            sendIntent.putExtra("studentID", studentID)
            sendIntent.putExtra("kurumKodu", kurumKodu.toString())
            this.startActivity(sendIntent)
        }


    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }
}