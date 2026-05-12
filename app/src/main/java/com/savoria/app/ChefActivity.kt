package com.savoria.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class ChefActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chef)
        val navHost = supportFragmentManager
            .findFragmentById(R.id.chef_nav_host_fragment) as NavHostFragment
        val navController = navHost.navController
        val bottomNav: BottomNavigationView = findViewById(R.id.chef_bottom_nav)
        bottomNav.setupWithNavController(navController)
    }
}
