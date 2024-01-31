package com.karaketir.coachingapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.karaketir.coachingapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

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
    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("383545978416-3hq2l4h64j1blghcj2e4brjtq5sblh7p.apps.googleusercontent.com")
            .requestEmail().requestProfile().build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val loginButton = binding.LoginButton
        val emailEditText = binding.emailLoginEditText
        val passwordEditText = binding.passwordLoginEditText
        val iWanSignUpText = binding.iWantSignUpTextView
        val signUpButton = binding.signUpButton
        val forgotPassword = binding.forgotPasswordText
        val googleButton = binding.signInGoogleButton

        googleButton.setOnClickListener {
            signUpWithGoogle()

        }

        forgotPassword.setOnClickListener {
            showDialog()
        }

        signUpButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            this.startActivity(intent)
        }
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

    private fun signUpWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResult(task)
        } else {
            Toast.makeText(this, result.resultCode.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleResult(task: Task<GoogleSignInAccount>) {

        if (task.isSuccessful) {

            val account: GoogleSignInAccount? = task.result
            if (account != null) {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Google ile giriş başarılı", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        this.startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Google ile giriş başarısız", Toast.LENGTH_SHORT)
                            .show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Google ile giriş başarısız", Toast.LENGTH_SHORT).show()
                }
            }

        } else {
            Toast.makeText(this, "Google ile giriş başarısız", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Email Adresinizi Girin")

        val input = EditText(this)
        input.hint = "Email"
        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        builder.setView(input)

        builder.setPositiveButton("Tamam") { _, _ ->
            // Here you get get input text from the Edittext
            if (input.text.toString().isNotEmpty()) {
                input.error = null
                val email = input.text.toString()
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Şifrenizi Sıfırlamak İçin Mail Başarıyla Gönderilmiştir",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } else {
                input.error = "Bu Alan Boş Bırakılamaz"
            }
        }
        builder.setNegativeButton("İptal") { dialog, _ -> dialog.cancel() }

        builder.show()
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
        }.addOnFailureListener {
            println(it.localizedMessage)

        }
    }
}