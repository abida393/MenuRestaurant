package com.savoria.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.savoria.app.ui.admin.login.LoginActivity

class ClientActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NAVIGATE_TO_MENU = "navigate_to_menu"
    }

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* optional: user may deny */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client)
        requestNotificationPermissionIfNeeded()

        drawerLayout = findViewById(R.id.drawer_layout)

        // Adjust for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.client_root_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Adjust BottomNav for bottom insets
        val bottomNav: BottomNavigationView = findViewById(R.id.client_bottom_nav)
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // Set up Navigation Component
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.client_nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController
        val bottomNav: BottomNavigationView = findViewById(R.id.client_bottom_nav)
        bottomNav.setupWithNavController(navController)

        if (intent.getBooleanExtra(EXTRA_NAVIGATE_TO_MENU, false)) {
            navController.navigate(R.id.navigation_menu_client)
        }

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_NAVIGATE_TO_MENU, false)) {
            navController.navigate(R.id.navigation_menu_client)
        }
    }

    fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
