package com.karaketir.coachingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.databinding.ActivityMessageBinding
import com.karaketir.coachingapp.services.FcmNotificationsSenderService

class MessageActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var binding: ActivityMessageBinding

    private var message = ""
    private var body = ""
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val targetList = arrayListOf("Koçluğum", "Okulum")
    private var setting = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore


        val titleEdit = binding.messageTitleEdit
        val bodyEdit = binding.messageSubjectEdit
        val sendButton = binding.sendMessageButton

        var kurumKodu = 0
        val messageSpinner = binding.messageSpinner


        db.collection("User").document(auth.uid.toString()).get().addOnSuccessListener {
            kurumKodu = it.get("kurumKodu").toString().toInt()
        }


        val messageAdapter = ArrayAdapter(
            this@MessageActivity, android.R.layout.simple_spinner_item, targetList
        )
        messageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        messageSpinner.adapter = messageAdapter
        messageSpinner.onItemSelectedListener = this



        sendButton.setOnClickListener {

            message = titleEdit.text.toString()
            body = bodyEdit.text.toString()


            when (setting) {
                0 -> {
                    val notificationsSender = FcmNotificationsSenderService(
                        "/topics/${auth.uid.toString()}", message, body, this
                    )
                    notificationsSender.sendNotifications()

                }

                1 -> {
                    val notificationsSender = FcmNotificationsSenderService(
                        "/topics/${kurumKodu}", message, body, this
                    )
                    notificationsSender.sendNotifications()
                }
            }
            Toast.makeText(
                this, "İşlem Başarılı", Toast.LENGTH_SHORT
            ).show()
            finish()


        }


    }


    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {

        when (position) {
            0 -> {
                setting = 0
            }

            1 -> {
                setting = 1
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

}