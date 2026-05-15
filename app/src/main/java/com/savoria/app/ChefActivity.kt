package com.savoria.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.savoria.app.data.local.StaffSessionManager
import com.savoria.app.ui.admin.login.LoginActivity

class ChefActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!StaffSessionManager.isLoggedIn(this)) {
            redirectToLogin()
            return
        }

        setContentView(R.layout.activity_chef)
        val navHost = supportFragmentManager
            .findFragmentById(R.id.chef_nav_host_fragment) as NavHostFragment
        val navController = navHost.navController
        val bottomNav: BottomNavigationView = findViewById(R.id.chef_bottom_nav)
        bottomNav.setupWithNavController(navController)
    }

    private fun redirectToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}
