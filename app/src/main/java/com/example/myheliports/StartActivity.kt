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

        showFragment(R.id.container, ListLocationFragment())
        Log.d("!!!", "ListLocationFragment is shown")

        addItemButton = findViewById(R.id.addItemButton)
        val bottomNavigation = findViewById<NavigationBarView>(R.id.bottom_navigation)

        addItemButton.setOnClickListener {
            val intent = Intent(this, AddLocationActivity::class.java)
            startActivity(intent)
        }

        NavigationBarView.OnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.item_1 -> {
                    showFragment(R.id.container, ListLocationFragment())
                    true
                }
                R.id.item_2 -> {
                    // Respond to navigation item 2 click
                    true
                }
                R.id.item_3 -> {
                    // Respond to navigation item 2 click
                    true
                }
                else -> false
            }
        }
        bottomNavigation.setOnItemReselectedListener { item ->
            when(item.itemId) {
                R.id.item_1 -> {
                    // Respond to navigation item 1 reselection
                }
                R.id.item_2 -> {
                    // Respond to navigation item 2 reselection
                }
                R.id.item_3 -> {
                    // Respond to navigation item 2 reselection
                }
            }
        }

    }

    private fun showFragment(containerId: Int, fragment: Fragment) {

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(containerId, fragment, "$containerId")
        transaction.commit()

    }
}