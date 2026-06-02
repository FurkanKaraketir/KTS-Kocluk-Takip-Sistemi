package com.karaketir.coachingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase
import com.karaketir.coachingapp.databinding.ActivityMessageBinding
import com.karaketir.coachingapp.notifications.FcmNotificationSender
import com.karaketir.coachingapp.notifications.NotificationSendFeedback
import com.karaketir.coachingapp.notifications.NotificationSendResult
import com.karaketir.coachingapp.notifications.NotificationType

class MessageActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var binding: ActivityMessageBinding

    private var message = ""
    private var body = ""
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var kurumKodu = 0
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
        val titleInputLayout = binding.messageTitleInputLayout
        val bodyInputLayout = binding.messageBodyInputLayout
        val sendButton = binding.sendMessageButton

        val messageSpinner = binding.messageSpinner

        kurumKodu = intent.getStringExtra("kurumKodu").toString().toInt()

        val messageAdapter = ArrayAdapter(
            this@MessageActivity, android.R.layout.simple_spinner_item, targetList
        )
        messageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        messageSpinner.adapter = messageAdapter
        messageSpinner.onItemSelectedListener = this

        titleEdit.doAfterTextChanged { titleInputLayout.error = null }
        bodyEdit.doAfterTextChanged { bodyInputLayout.error = null }

        sendButton.setOnClickListener {
            if (!sendButton.isEnabled) return@setOnClickListener

            message = titleEdit.text.toString().trim()
            body = bodyEdit.text.toString().trim()

            if (message.isEmpty()) {
                titleInputLayout.error = getString(R.string.message_title_empty)
                titleEdit.requestFocus()
                return@setOnClickListener
            }
            titleInputLayout.error = null

            if (body.isEmpty()) {
                bodyInputLayout.error = getString(R.string.message_body_empty)
                bodyEdit.requestFocus()
                return@setOnClickListener
            }
            bodyInputLayout.error = null

            val target = when (setting) {
                0 -> "/topics/${auth.uid}"
                else -> "/topics/$kurumKodu"
            }

            setSendingState(true)
            FcmNotificationSender.send(
                this,
                target,
                NotificationType.MESSAGE,
                message,
                body,
            ) { result ->
                NotificationSendFeedback.showToast(this, result)
                if (result is NotificationSendResult.Success) {
                    finish()
                } else {
                    setSendingState(false)
                }
            }
        }
    }

    private fun setSendingState(sending: Boolean) {
        binding.sendMessageButton.isEnabled = !sending
        binding.messageTitleEdit.isEnabled = !sending
        binding.messageSubjectEdit.isEnabled = !sending
        binding.messageSpinner.isEnabled = !sending

        binding.sendMessageProgress.visibility = if (sending) View.VISIBLE else View.GONE
        binding.sendMessageButton.text = getString(
            if (sending) R.string.message_sending else R.string.g_nder
        )
        if (sending) {
            binding.sendMessageButton.icon = null
        } else {
            binding.sendMessageButton.setIconResource(R.drawable.baseline_message_24)
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        when (position) {
            0 -> setting = 0
            1 -> setting = 1
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}
