package com.savoria.app.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.savoria.app.R
import com.savoria.app.data.local.StaffSessionManager
import com.savoria.app.ui.admin.login.LoginActivity

class AdminActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var fabAddDish: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (!StaffSessionManager.isAdmin(this)) {
            redirectToLogin()
            return
        }

        setContentView(R.layout.activity_admin)
        setSupportActionBar(findViewById(R.id.admin_toolbar))

        applyWindowInsets()
        setupNavigation()
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin_root_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        val bottomNav: BottomNavigationView = findViewById(R.id.admin_bottom_nav)
        ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, 0)
            val params = v.layoutParams as android.view.ViewGroup.MarginLayoutParams
            params.bottomMargin = systemBars.bottom
            v.layoutParams = params
            insets
        }

        fabAddDish = findViewById(R.id.fab_add_dish)
        ViewCompat.setOnApplyWindowInsetsListener(fabAddDish) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val marginParams = v.layoutParams as android.view.ViewGroup.MarginLayoutParams
            marginParams.bottomMargin =
                (72 * resources.displayMetrics.density).toInt() + systemBars.bottom
            v.layoutParams = marginParams
            insets
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.admin_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val topLevelDestinations = setOf(
            R.id.navigation_dashboard,
            R.id.navigation_plats,
            R.id.navigation_categories,
            R.id.navigation_users
        )
        appBarConfiguration = AppBarConfiguration(topLevelDestinations)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val bottomNav: BottomNavigationView = findViewById(R.id.admin_bottom_nav)
        bottomNav.setupWithNavController(navController)

        fabAddDish.setOnClickListener {
            when (navController.currentDestination?.id) {
                R.id.navigation_dashboard -> {
                    navController.navigate(R.id.action_dashboard_to_add_dish)
                }
                R.id.navigation_plats -> {
                    navController.navigate(R.id.action_plats_to_add_dish)
                }
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_dashboard, R.id.navigation_plats -> {
                    fabAddDish.visibility = View.VISIBLE
                }
                R.id.navigation_categories, R.id.navigation_users, R.id.navigation_add_dish -> {
                    fabAddDish.visibility = View.GONE
                }
                else -> {
                    fabAddDish.visibility = View.GONE
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_admin, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            logout()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun logout() {
        StaffSessionManager.clearSession(this)
        redirectToLogin()
    }

    private fun redirectToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }

    companion object {
        const val EXTRA_USER_ID = "USER_ID"
        const val EXTRA_USER_ROLE = "ADMIN"
    }
}
