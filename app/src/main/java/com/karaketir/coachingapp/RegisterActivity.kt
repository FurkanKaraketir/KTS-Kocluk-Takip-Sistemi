@file:Suppress("DEPRECATION")

package com.karaketir.coachingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

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

    private val googleCode = 9001 // You can choose any unique code


    private lateinit var documentID: String
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.firestore

        val signUpButton = binding.signUpButton
        val emailEditText = binding.emailRegisterEditText
        val passwordEditText = binding.passwordRegisterEditText
        val nameAndSurnameEditText = binding.nameAndSurnameEditText
        val checkBox = binding.acceptCheckBox
        val googleButton = binding.signInGoogleButton
        val logInButton = binding.logInButton


        logInButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("383545978416-3hq2l4h64j1blghcj2e4brjtq5sblh7p.apps.googleusercontent.com")
            .requestEmail().requestProfile().build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleButton.setOnClickListener {
            signUpWithGoogle()

        }
        signUpButton.setOnClickListener {
            if (emailEditText.text.toString().isNotEmpty()) {
                emailEditText.error = null

                if (passwordEditText.text.toString().isNotEmpty()) {

                    if (passwordEditText.text.toString().length >= 6) {
                        passwordEditText.error = null

                        if (nameAndSurnameEditText.text.toString().isNotEmpty()) {
                            nameAndSurnameEditText.error = null

                            if (checkBox.isChecked) {
                                checkBox.error = null


                                val nameAndSurname = nameAndSurnameEditText.text.toString()


                                signUp(
                                    emailEditText.text.toString(),
                                    passwordEditText.text.toString(),
                                    nameAndSurname
                                )


                            } else {
                                checkBox.error = "Lütfen Kullanıcı Sözleşmesini Kabul Ediniz"
                            }


                        } else {
                            nameAndSurnameEditText.error = "Bu Alan Boş Bırakılamaz"

                        }


                    }
                } else {
                    passwordEditText.error = "Şifre En Az 6 Karakter Uzunluğunda Olmalı"
                }


            } else {
                emailEditText.error = "Bu Alan Boş Bırakılamaz"
            }
        }


    }

    private fun signUpWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, googleCode)
    }

    private fun signUp(email: String, password: String, nameAndSurname: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                documentID = auth.uid!!

                val newIntent = Intent(this, AddUserDataActivity::class.java)
                newIntent.putExtra("documentID", documentID)
                newIntent.putExtra("nameAndSurname", nameAndSurname)
                newIntent.putExtra("email", email)
                this.startActivity(newIntent)
                finish()


            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(
                    baseContext, "Kayıt Başarısız!", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == googleCode) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, handle failure
                Toast.makeText(
                    baseContext, "Bir hata oluştu.", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, navigate to the next activity or perform additional actions
                val user = auth.currentUser
                // You can get user information from 'user' variable if needed
                // For example, user.displayName, user.email, etc.

                // Proceed to the next activity or perform any other actions
                val newIntent = Intent(this, AddUserDataActivity::class.java)
                newIntent.putExtra("email", user?.email)
                newIntent.putExtra("nameAndSurname", user?.displayName)
                newIntent.putExtra("documentID", user?.uid)
                // Add other data if needed
                this.startActivity(newIntent)
                finish()
            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(
                    baseContext, "Bir hata oluştu.", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}