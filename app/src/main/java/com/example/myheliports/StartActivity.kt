package com.example.myheliports

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationBarView

class StartActivity : AppCompatActivity() {

    lateinit var addItemButton: FloatingActionButton
    lateinit var topAppBar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        topAppBar = findViewById(R.id.topAppBar)

        showFragment(R.id.container, ListLocationFragment(), true)

        addItemButton = findViewById(R.id.addItemButton)
        val bottomNavigation = findViewById<NavigationBarView>(R.id.bottom_navigation)

        addItemButton.setOnClickListener {
            val intent = Intent(this, AddLocationActivity::class.java)
            startActivity(intent)
        }


        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_1 -> {
                    showFragment(R.id.container, ListLocationFragment(), false)
                    setupTopBar()
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
        bottomNavigation.setOnItemReselectedListener { item ->
            when(item.itemId) {
                R.id.item_1 -> {
                    // Respond to navigation item 1 reselection
                }
                R.id.item_2 -> {
                    // Respond to navigation item 2 reselection
                }
                R.id.item_3 -> {
                    // Respond to navigation item 2 click
                }
            }
        }
    }

    fun showFragment(containerId: Int, fragment: Fragment, isOnCreate: Boolean) {

        val transaction = supportFragmentManager.beginTransaction()

        if (isOnCreate) {
            transaction.add(containerId, fragment, "$containerId")
        } else {
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

    fun goBack() {
        supportFragmentManager.popBackStack()
        setupTopBar()
    }
    private fun setupTopBar(){
        topAppBar.menu.clear(); // Rensa den gamla menyn
        topAppBar.inflateMenu(R.menu.top_app_bar); // Lägg till den nya menyn
        topAppBar.navigationIcon = null
        topAppBar.title = "MyHeliports"
        addItemButton.show()
    }
}