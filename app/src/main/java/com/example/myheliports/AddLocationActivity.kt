package com.example.myheliports

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.AppCompatImageButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddLocationActivity : AppCompatActivity() {

    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth

    lateinit var backButton: AppCompatImageButton
    lateinit var nameOfLocationView: EditText
    lateinit var descriptionOfLocationView: EditText
    lateinit var latOfLocationView: EditText
    lateinit var longOfLocationView: EditText
    lateinit var saveLocationButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_location)

        db = Firebase.firestore
        auth = Firebase.auth

        backButton = findViewById(R.id.backButton)
        nameOfLocationView = findViewById(R.id.nameOfLocationView)
        descriptionOfLocationView = findViewById(R.id.descriptionOfLocationView)
        latOfLocationView = findViewById(R.id.latOfLocationView)
        longOfLocationView = findViewById(R.id.longOfLocationView)
        saveLocationButton = findViewById(R.id.saveLocationButton)


        backButton.setOnClickListener {
            finish()
        }

        saveLocationButton.setOnClickListener {
            saveLocation()
        }

    }

    fun saveLocation() {

        val lat = latOfLocationView.text.toString()
        val long = longOfLocationView.text.toString()

        val checkCoordinates = checkLatLong(lat, long)

        val colorStateListRed = ColorStateList.valueOf(Color.RED)
        val colorStateListWhite = ColorStateList.valueOf(Color.WHITE)

        if (!checkCoordinates) {
            latOfLocationView.backgroundTintList = colorStateListRed
            longOfLocationView.backgroundTintList = colorStateListRed
            return
        }
        else {
            latOfLocationView.backgroundTintList = colorStateListWhite
            longOfLocationView.backgroundTintList = colorStateListWhite
        }

        val latDouble = lat.toDouble()
        val longDouble = long.toDouble()

        //Create Location Object
        val location = Location("??",
            nameOfLocationView.text.toString(),
            descriptionOfLocationView.text.toString(),
            latDouble,
            longDouble,
            "greenland"
        )

        val user = auth.currentUser ?: return

        //db.collection("users").document(user.uid).collection("locations").add(location)
    }

    fun checkLatLong(latStr: String?, longStr: String?): Boolean {

        try {
            val lat = latStr?.toDouble()
            val long = longStr?.toDouble()

            if (lat == null || long == null) {
                return false
            }

            if (lat !in -90.0..90.0 || long !in -180.0..180.0) {
                return false
            }
            return true
        } catch (e: NumberFormatException) {
            //Catch errors..
        }
        return false
    }
}