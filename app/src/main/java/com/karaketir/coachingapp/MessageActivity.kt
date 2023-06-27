package com.karaketir.coachingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.databinding.ActivityMainBinding
import com.karaketir.coachingapp.databinding.ActivityMessageBinding
import com.karaketir.coachingapp.services.FcmNotificationsSenderService

class MessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageBinding

    private var message = ""
    private var body = ""
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore


        val titleEdit = binding.messageTitleEdit
        val bodyEdit = binding.messageSubjectEdit
        val sendButton = binding.sendMessageButton


        sendButton.setOnClickListener {

            message = titleEdit.text.toString()
            body = bodyEdit.text.toString()

            val notificationsSender = FcmNotificationsSenderService(
                "/topics/all", message, body, this
            )
            notificationsSender.sendNotifications()

            Toast.makeText(
                this, "İşlem Başarılı", Toast.LENGTH_SHORT
            ).show()
            finish()
        }


    }
}