package com.kodgem.coachingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth

        val loginButton = findViewById<View>(R.id.LoginButton)
        val emailEditText = findViewById<EditText>(R.id.emailLoginEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordLoginEditText)
        val iWanSignUpText = findViewById<TextView>(R.id.i_want_sign_up_TextView)


        iWanSignUpText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            this.startActivity(intent)
        }

        loginButton.setOnClickListener {
            if (emailEditText.text.toString().isNotEmpty()){
                emailEditText.error = null

                if (passwordEditText.text.toString().isNotEmpty()){
                    passwordEditText.error = null
                    signIn(emailEditText.text.toString(),passwordEditText.text.toString())

                }else{
                    passwordEditText.error = "Bu Alan Boş Bırakılamaz"
                }
            }else{
                emailEditText.error = "Bu Alan Boş Bırakılamaz"
            }

        }


    }


    private fun signIn(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this,"Başarılı!",Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    this.startActivity(intent)
                    finish()

                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}