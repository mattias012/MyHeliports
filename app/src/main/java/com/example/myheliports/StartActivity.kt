package com.example.myheliports

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationBarView
interface BadgeUpdater {
    fun updateBadge(difference: Int)
    fun removeBadge()
}
class StartActivity : AppCompatActivity(), BadgeUpdater {

    lateinit var addItemButton: FloatingActionButton
    lateinit var topAppBar: MaterialToolbar

    private val listLocationFragment = ListLocationFragment()
    private val mapsFragment = MapsFragment()
    private val showLocationFragment = ShowLocationFragment()

    override fun updateBadge(difference: Int) {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        var badge = bottomNavigation.getOrCreateBadge(R.id.item_1)
        badge.isVisible = true
        badge.number = difference
    }
    override fun removeBadge() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.removeBadge(R.id.item_1)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        topAppBar = findViewById(R.id.topAppBar)


        showFragment(R.id.container, ListLocationFragment(), true)

        SharedData.fragment = listLocationFragment

        addItemButton = findViewById(R.id.addItemButton)
        val bottomNavigation = findViewById<NavigationBarView>(R.id.bottom_navigation)

        addItemButton.setOnClickListener {
            val intent = Intent(this, AddLocationActivity::class.java)
            startActivity(intent)
        }

        bottomMenu()

    }
     fun showFragment(containerId: Int, fragment: Fragment, isOnCreate: Boolean) {

        val transaction = supportFragmentManager.beginTransaction()

        if (isOnCreate) {
            transaction.add(containerId, fragment, "$containerId")
        } else {
            transaction.replace(containerId, fragment, "$containerId")
        }
        transaction.addToBackStack("listFragment")
        transaction.commit()

//        SharedData.fragment = fragment
    }

    fun showLocationFragment(documentId: String) {
        val fragment = ShowLocationFragment()
        val args = Bundle()
        args.putString("documentId", documentId)
        fragment.arguments = args
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment, "ShowLocationFragment")
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun showMapsFragment(documentId: String) {
        val fragment = MapsFragment()
        val args = Bundle()
        args.putString("documentId", documentId)
        fragment.arguments = args
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment, "MapsFragment")
        transaction.addToBackStack(null)
        transaction.commit()

    }

    fun getActiveFragment(): Fragment? {
        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            if (fragment.isVisible) {
                return fragment
            }
        }
        return null
    }

    fun goBack(item: Int?, comingFromThisFragment: Fragment?) {
        val bottomNavigation = findViewById<NavigationBarView>(R.id.bottom_navigation)

        if (comingFromThisFragment != null){
            if (item != null) {
                if (comingFromThisFragment is ListLocationFragment) {
                    bottomNavigation.selectedItemId = item
                    showFragment(R.id.container, ListLocationFragment(), false)
                    setupTopBar()
                }
                else if (comingFromThisFragment is MapsFragment){
                    Log.d("!!!", "this code is run")
                    bottomNavigation.selectedItemId = item
                    showFragment(R.id.container, MapsFragment(), false)
                }
            }
        }
        else {
            supportFragmentManager.popBackStack()
        }
    }
    private fun bottomMenu(){
        val bottomNavigation = findViewById<NavigationBarView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_1 -> {
                    showFragment(R.id.container, listLocationFragment, false)
                    setupTopBar()
                    true
                }

                R.id.item_2 -> {

                    showFragment(R.id.container, mapsFragment, false)

                    true
                }

                R.id.item_3 -> {
                    showFragment(R.id.container, ListUserFragment(), false)
                    true
                }

                else -> false
            }
        }

        bottomNavigation.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.item_1 -> {
                    showFragment(R.id.container, listLocationFragment, false)
                    setupTopBar()
                }

                R.id.item_2 -> {
                    showFragment(R.id.container, mapsFragment, false)
                }

                R.id.item_3 -> {
                    showFragment(R.id.container, ListUserFragment(), false)
                }
            }
        }
    }

    fun setupTopBar() {
        topAppBar.menu.clear(); //Clear menu
        topAppBar.inflateMenu(R.menu.top_app_bar); //add new menu
        topAppBar.navigationIcon = null
        topAppBar.title = "MyHeliports"
        addItemButton.show()
    }
}