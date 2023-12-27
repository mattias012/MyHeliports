package com.example.myheliports

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationBarView

class StartActivity : AppCompatActivity() {

    lateinit var addItemButton: FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        showFragment(R.id.container, ListLocationFragment(), true)

        addItemButton = findViewById(R.id.addItemButton)
        val bottomNavigation = findViewById<NavigationBarView>(R.id.bottom_navigation)

        addItemButton.setOnClickListener {
            val intent = Intent(this, AddLocationActivity::class.java)
            startActivity(intent)
        }

        bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.item_1 -> {
                    showFragment(R.id.container, ListLocationFragment(), false)
                    true
                }
                R.id.item_2 -> {
                    showFragment(R.id.container, MapsFragment(), false)
                    true
                }
                R.id.item_3 -> {
                    // Respond to navigation item 2 click
                    true
                }
                else -> false
            }
        }
//        bottomNavigation.setOnItemReselectedListener { item ->
//            when(item.itemId) {
//                R.id.item_1 -> {
//                    showFragment(R.id.container, ListLocationFragment(), false)
//                }
//                R.id.item_2 -> {
//                    showFragment(R.id.container, MapsFragment(), false)
//                }
//                R.id.item_3 -> {
//                    // Respond to navigation item 2 reselection
//                }
//            }
//        }

    }

    private fun showFragment(containerId: Int, fragment: Fragment, isOnCreate: Boolean) {

        val transaction = supportFragmentManager.beginTransaction()

        if (isOnCreate) {
            transaction.add(containerId, fragment, "$containerId")
        }
        else {
            transaction.replace(containerId, fragment, "$containerId")
        }

        transaction.commit()

    }

    fun showLocationFragment(documentId: String) {
        val fragment = ShowLocationFragment()
        val args = Bundle()
        args.putString("documentId", documentId)
        fragment.arguments = args
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment, "ShowLocationFragment")
        transaction.addToBackStack(null) // Lägg till transactionen till back stack så att användaren kan navigera tillbaka
        transaction.commit()
    }
}