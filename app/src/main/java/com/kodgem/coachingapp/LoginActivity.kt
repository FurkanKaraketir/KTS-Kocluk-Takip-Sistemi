package com.kodgem.coachingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kodgem.coachingapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        val loginButton = binding.LoginButton
        val emailEditText = binding.emailLoginEditText
        val passwordEditText = binding.passwordLoginEditText
        val iWanSignUpText = binding.iWantSignUpTextView


        iWanSignUpText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            this.startActivity(intent)
        }

        loginButton.setOnClickListener {
            if (emailEditText.text.toString().isNotEmpty()) {
                emailEditText.error = null

                if (passwordEditText.text.toString().isNotEmpty()) {
                    passwordEditText.error = null
                    signIn(emailEditText.text.toString(), passwordEditText.text.toString())

                } else {
                    passwordEditText.error = "Bu Alan Boş Bırakılamaz"
                }
            } else {
                emailEditText.error = "Bu Alan Boş Bırakılamaz"
            }

        }


    }


    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Toast.makeText(this, "Başarılı!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                this.startActivity(intent)
                finish()

            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(
                    baseContext, "Giriş Başarısız!", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}