package com.example.myheliports

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    lateinit var auth : FirebaseAuth
    lateinit var emailView : EditText
    lateinit var passwordView : EditText
    lateinit var signupView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        emailView = findViewById(R.id.emailText)
        passwordView = findViewById(R.id.passwordText)
        signupView = findViewById(R.id.sigupView)

        signupView.setOnClickListener {
            signUp()
        }

        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            login()
        }

        if (auth.currentUser != null){
            val intent = Intent(this, StartActivity::class.java)
            startActivity(intent)
        }

    }

    fun login(){
        val email = emailView.text.toString()
        val password = passwordView.text.toString()

        if (email.isEmpty() || password.isEmpty()){
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){

                    val intent = Intent(this, StartActivity::class.java)
                    startActivity(intent)

                } else {
                    Log.d("!!!", "user not logged in ${task.exception}")
                }
            }
    }

    fun signUp(){
        val email = emailView.text.toString()
        val password = passwordView.text.toString()

        if (email.isEmpty() || password.isEmpty()){
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
            if (task.isSuccessful){
                Toast.makeText(this, "User created", LENGTH_SHORT).show()

                val intent = Intent(this, StartActivity::class.java)
                startActivity(intent)

            } else {
                Log.d("!!!", "user not created ${task.exception}")
            }
        }

    }
}