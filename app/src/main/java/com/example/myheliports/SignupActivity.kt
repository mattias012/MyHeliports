package com.example.myheliports

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var userNameSignUpView: TextInputLayout
    private lateinit var emailSignUpView: TextInputLayout
    private lateinit var passwordSignUpView: TextInputLayout
    private lateinit var confirmPasswordSignUpView: TextInputLayout
    private lateinit var signupButton: Button
    private lateinit var backToLogin: TextView

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


        setupTextWatcher(emailSignUp)
        setupTextWatcher(passwordSignUp)
        setupTextWatcher(confirmPasswordSignUp)

        userNameSignUp.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val userName = userNameSignUp.text.toString()
                getUserName(userName) { userExists ->
                    if (userExists) {
                        userNameSignUpView.error = "Username already exists"
                        signupButton.isEnabled = false
                    }
                    else {
                        userNameSignUpView.error = null
                        signupButton.isEnabled = true
                    }
                }
            }
        }
        userNameSignUp.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                emailSignUp.requestFocus()
                return@setOnEditorActionListener true
            }
            false
        }
        emailSignUp.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                passwordSignUp.requestFocus()
                return@setOnEditorActionListener true
            }
            false
        }
        passwordSignUp.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                confirmPasswordSignUp.requestFocus()
                return@setOnEditorActionListener true
            }
            false
        }
        confirmPasswordSignUp.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                hideKeyboard()
                return@setOnKeyListener true
            }
            false
        }

        signupButton.setOnClickListener {
            signUp()
        }
        backToLogin.setOnClickListener {
            finish()
        }
    }
    private fun Activity.hideKeyboard() {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val currentFocusedView = currentFocus
        currentFocusedView?.let {
            inputMethodManager?.hideSoftInputFromWindow(
                it.windowToken,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
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

        val email = emailSignUp.text?.trim().toString()
        val password = passwordSignUp.text.toString()
        val confirmPassword = confirmPasswordSignUp.text.toString()
        val userName = userNameSignUp.text?.trim().toString()

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

        backToLogin = findViewById(R.id.backToLogin)


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