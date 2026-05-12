package com.savoria.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.savoria.app.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up Navigation Component
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment?

        if (navHostFragment != null) {
            val navController: NavController = navHostFragment.navController
            val bottomNav: BottomNavigationView = findViewById(R.id.bottom_nav)
            bottomNav.setupWithNavController(navController)
        }

        // FAB
        val fab: FloatingActionButton = findViewById(R.id.fab_add)
        fab.setOnClickListener {
            findNavController(R.id.nav_host_fragment).navigate(R.id.navigation_add_dish)
        }
    }
}
