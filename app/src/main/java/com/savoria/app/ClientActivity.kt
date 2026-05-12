package com.savoria.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.savoria.app.ui.admin.login.LoginActivity

class ClientActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)

        drawerLayout = findViewById(R.id.drawer_layout)

        // Set up Navigation Component
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.client_nav_host_fragment) as NavHostFragment

        val navController: NavController = navHostFragment.navController
        val bottomNav: BottomNavigationView = findViewById(R.id.client_bottom_nav)
        bottomNav.setupWithNavController(navController)

        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_admin_login -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                else -> {
                    Toast.makeText(this, "${menuItem.title} cliqué", Toast.LENGTH_SHORT).show()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }
}
