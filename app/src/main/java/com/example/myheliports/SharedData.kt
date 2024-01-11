package com.example.myheliports

import androidx.fragment.app.Fragment

object SharedData {
    var position: Int = 0
    var fragment: Fragment? = null
    var prevFragment: Fragment? = null
    var previousSnapshotCount = 0
}