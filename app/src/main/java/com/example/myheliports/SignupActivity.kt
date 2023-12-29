package com.example.myheliports

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var userNameSignUpView: TextInputLayout
    private lateinit var emailSignUpView: TextInputLayout
    private lateinit var passwordSignUpView: TextInputLayout
    private lateinit var confirmPasswordSignUpView: TextInputLayout
    private lateinit var signupButton: Button

    private lateinit var userNameSignUp: TextInputEditText
    private lateinit var emailSignUp: TextInputEditText
    private lateinit var passwordSignUp: TextInputEditText
    private lateinit var confirmPasswordSignUp: TextInputEditText

    private lateinit var viewsToFade: List<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = Firebase.auth
        db = Firebase.firestore

        initilizeViews()

        userNameSignUp.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) { // Om fokus tas bort från fältet
                val userName = userNameSignUp.text.toString()
                getUserName(userName) { userExists ->
                    if (userExists) {
                        userNameSignUpView.error = "Username already exists"
                    }
                    else {
                        userNameSignUpView.error = null
                    }
                }
            }
        }

        setupTextWatcher(emailSignUp)
        setupTextWatcher(passwordSignUp)
        setupTextWatcher(confirmPasswordSignUp)

        signupButton.setOnClickListener {
            signUp()
        }
    }

    private fun setupTextWatcher(editText: TextInputEditText) {

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                //Check if the input is correct
                checkStuff()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //Clear potential earlier errors
                emailSignUpView.error = null
                passwordSignUpView.error = null
                confirmPasswordSignUpView.error = null

            }
        }
        editText.addTextChangedListener(textWatcher)
    }

    private fun checkStuff() {
        val email = emailSignUp.text.toString()
        val password = passwordSignUp.text.toString()
        val confirmPassword = confirmPasswordSignUp.text.toString()

        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

        if (email.isEmpty()) {
            emailSignUpView.error = "Enter a valid e-mail address"

        } else if (!email.matches(emailPattern.toRegex())) {
            emailSignUpView.error = "Enter a valid e-mail address"
        } else if (password.isEmpty() || password.length < 8) {
            passwordSignUpView.error = "Enter a valid password, at least 8 characters"
        } else if (password != confirmPassword) {
            passwordSignUpView.error = "Password doesn't match"
            confirmPasswordSignUpView.error = "Password doesn't match"
        } else {

        }
    }

    private fun getUserName(userName: String, callback: (Boolean) -> Unit) {
        db.collection("users").whereEqualTo("userName", userName).get()
            .addOnSuccessListener { userDocument ->
                if (!userDocument.isEmpty) {
                    callback(true)
                } else {
                    callback(false)
                }
            }
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

    private fun signUp() {

        val email = emailSignUp.text.toString()
        val password = passwordSignUp.text.toString()
        val confirmPassword = confirmPasswordSignUp.text.toString()
        val userName = userNameSignUp.text.toString()

        if (email.isEmpty()) {
            return
        }
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        if (!email.matches(emailPattern.toRegex())) {
            return
        }
        if (password.isEmpty() || password.length < 8) {
            return
        }
        if (password != confirmPassword) {
            return
        }


        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        progressBar.visibility = View.VISIBLE
        fadeViews(viewsToFade, true)
        signupButton.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null) {

                        val user = User(
                            userId = userId,
                            userName = userName
                        )

                        db.collection("users").add(user)
                            .addOnSuccessListener { documentReference ->
                                Log.d(
                                    "!!!",
                                    "DocumentSnapshot added with ID: ${documentReference.id}"
                                )
                            }
                            .addOnFailureListener { e ->
                                Log.w("!!!", "Error adding document", e)
                                fadeViews(viewsToFade, false)
                                signupButton.isEnabled = true
                            }
                    }

                    //Welcome the user
                    Toast.makeText(this, "Profile created, welcome $userName", Toast.LENGTH_LONG)
                        .show()

                    //Remove progressbar
                    progressBar.visibility = View.GONE

                    //Send to gridview start page
                    val intent = Intent(this, StartActivity::class.java)
                    startActivity(intent)

                } else {
                    Log.d("!!!", "user not created ${task.exception}")
                    finish()
                }
            }
    }

    private fun initilizeViews() {
        userNameSignUpView = findViewById(R.id.userNameSignUpView)
        emailSignUpView = findViewById(R.id.emailSignUpView)
        passwordSignUpView = findViewById(R.id.passwordSignUpView)
        confirmPasswordSignUpView = findViewById(R.id.confirmPasswordSignUpView)
        signupButton = findViewById(R.id.signUpButton)

        userNameSignUp = findViewById(R.id.userNameSignUp)
        emailSignUp = findViewById(R.id.emailSignUp)
        passwordSignUp = findViewById(R.id.passwordSignUp)
        confirmPasswordSignUp = findViewById(R.id.confirmPasswordSignUp)

        viewsToFade = listOf(
            userNameSignUpView,
            emailSignUpView, passwordSignUpView,
            confirmPasswordSignUpView,
            userNameSignUp,
            emailSignUp,
            passwordSignUp,
            confirmPasswordSignUp
        )
    }
}