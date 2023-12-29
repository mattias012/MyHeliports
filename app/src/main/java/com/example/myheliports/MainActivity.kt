package com.example.myheliports

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth

    private lateinit var emailView : TextInputLayout
    private lateinit var passwordView : TextInputLayout
    private lateinit var email: TextInputEditText
    private lateinit var password: TextInputEditText

    private lateinit var viewsToFade: List<View>

    lateinit var signupView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        initilizeViews()

        signupView.setOnClickListener {
            goToSignUp()
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

    private fun login() {
        val emailLogin = email.text.toString()
        val passwordLogin = password.text.toString()

        if (emailLogin.isEmpty()) {
            return
        }
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        if (!emailLogin.matches(emailPattern.toRegex())) {
            return
        }
        if (passwordLogin.isEmpty() || passwordLogin.length < 8) {
            return
        }

        emailView.error = null
        passwordView.error = null

        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        progressBar.visibility = View.VISIBLE
        fadeViews(viewsToFade, true)

            auth.signInWithEmailAndPassword(emailLogin, passwordLogin)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    progressBar.visibility = View.GONE
                    val intent = Intent(this, StartActivity::class.java)
                    startActivity(intent)

                } else {
                    Log.d("!!!", "user not logged in ${task.exception}")
                    progressBar.visibility = View.GONE
                    emailView.error = "Wrong e-mail or password"
                    passwordView.error = "Wrong e-mail or password"
                    fadeViews(viewsToFade, false)
                }
            }
    }
    private fun goToSignUp(){
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
    }
    private fun fadeViews(views: List<View>, fadeOut: Boolean) {
        val alphaValue = if (fadeOut) 0.5f else 1f

        views.forEach { view ->
            view.animate()
                .alpha(alphaValue)
                .setDuration(2000)
                .setListener(null)
        }
    }
    private fun initilizeViews(){
        emailView = findViewById(R.id.emailView)
        passwordView = findViewById(R.id.passwordView)

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)

        viewsToFade = listOf(
            emailView, passwordView, email, password)

        signupView = findViewById(R.id.sigupView)
    }
}