package com.karaketir.coachingapp

//noinspection SuspiciousImport
import android.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.databinding.ActivityGoalEnterBinding
import java.util.UUID

class GoalEnterActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var binding: ActivityGoalEnterBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var dersler = ArrayList<String>()
    private var secilenDers = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityGoalEnterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore
        val toplamCalisma = binding.hedefToplamCalismaEditText
        val cozulenSoru = binding.hedefToplamSoruEditText
        val goalSave = binding.goalSave
        val studentID = intent.getStringExtra("studentID").toString()
        val dersAdiSpinner = binding.hedefDersSpinner


        db.collection("Lessons").orderBy("dersAdi", Query.Direction.ASCENDING)
            .addSnapshotListener { value, _ ->
                if (value != null) {
                    for (document in value) {
                        dersler.add(document.get("dersAdi").toString())
                    }


                    val studentAdapter = ArrayAdapter(
                        this@GoalEnterActivity, R.layout.simple_spinner_item, dersler
                    )

                    studentAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                    dersAdiSpinner.adapter = studentAdapter
                    dersAdiSpinner.onItemSelectedListener = this


                }
            }


        val documentID = UUID.randomUUID()


        goalSave.setOnClickListener {
            if (toplamCalisma.text.toString().isNotEmpty()) {
                toplamCalisma.error = null

                if (cozulenSoru.text.toString().isNotEmpty()) {
                    cozulenSoru.error = null

                    db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
                        val kurumKodu = it.get("kurumKodu").toString()

                        val data = hashMapOf(
                            "dersAdi" to secilenDers,
                            "toplamCalisma" to toplamCalisma.text.toString().toInt(),
                            "çözülenSoru" to cozulenSoru.text.toString().toInt()
                        )

                        db.collection("School").document(kurumKodu).collection("Student")
                            .document(studentID).collection("HaftalikHedefler")
                            .whereEqualTo("dersAdi", secilenDers).addSnapshotListener { value, _ ->

                                if (value != null) {
                                    if (!value.isEmpty) {
                                        for (document in value) {
                                            db.collection("School").document(kurumKodu)
                                                .collection("Student").document(studentID)
                                                .collection("HaftalikHedefler")
                                                .document(document.id).set(data)
                                                .addOnSuccessListener {
                                                    finish()
                                                }
                                        }
                                    } else {
                                        db.collection("School").document(kurumKodu)
                                            .collection("Student").document(studentID)
                                            .collection("HaftalikHedefler")
                                            .document(documentID.toString()).set(data)
                                            .addOnSuccessListener {
                                                finish()
                                            }
                                    }
                                } else {
                                    db.collection("School").document(kurumKodu)
                                        .collection("Student").document(studentID)
                                        .collection("HaftalikHedefler")
                                        .document(documentID.toString()).set(data)
                                        .addOnSuccessListener {
                                            finish()
                                        }
                                }

                            }

                    }

                } else {
                    cozulenSoru.error = "Bu Alan Boş Bırakılamaz"
                }

            } else {
                toplamCalisma.error = "Bu Alan Boş Bırakılamaz"
            }
        }

    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        secilenDers = dersler[p2]
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }
}