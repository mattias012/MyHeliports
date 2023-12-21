package com.example.myheliports

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    lateinit var auth : FirebaseAuth
    lateinit var emailView : EditText
    lateinit var passwordView : EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        emailView = findViewById(R.id.emailText)
        passwordView = findViewById(R.id.passwordText)




//        val intent = Intent(this, MapsActivity::class.java)
//        startActivity(intent)
    }
}   