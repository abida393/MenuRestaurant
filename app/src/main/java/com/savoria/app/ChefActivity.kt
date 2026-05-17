package com.savoria.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.savoria.app.data.local.StaffSessionManager
import com.savoria.app.ui.admin.AdminActivity
import com.savoria.app.ui.admin.login.LoginActivity
import com.savoria.app.ui.serveur.ServeurActivity

class ChefActivity : AppCompatActivity() {

    private lateinit var toolbarTitle: TextView
    private lateinit var btnNavBack: ImageButton
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        when {
            !StaffSessionManager.isLoggedIn(this) -> {
                redirectToLogin()
                return
            }
            StaffSessionManager.isChef(this) -> Unit
            StaffSessionManager.isServeur(this) -> {
                startActivity(Intent(this, ServeurActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
                return
            }
            StaffSessionManager.isAdmin(this) -> {
                startActivity(Intent(this, AdminActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
                return
            }
            else -> {
                redirectToLogin()
                return
            }
        }

        setContentView(R.layout.activity_chef)

        toolbarTitle = findViewById(R.id.chef_toolbar_title)
        btnNavBack = findViewById(R.id.btn_chef_nav_back)
        bottomNav = findViewById(R.id.chef_bottom_nav)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chef_root_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(bottomNav) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        findViewById<View>(R.id.btn_chef_logout).setOnClickListener { logout() }
        btnNavBack.setOnClickListener {
            val navHost = supportFragmentManager
                .findFragmentById(R.id.chef_nav_host_fragment) as NavHostFragment
            navHost.navController.navigateUp()
        }

        val navHost = supportFragmentManager
            .findFragmentById(R.id.chef_nav_host_fragment) as NavHostFragment
        val navController = navHost.navController

        bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_chef_dashboard -> {
                    toolbarTitle.setText(R.string.chef_dashboard_title)
                    btnNavBack.visibility = View.GONE
                    bottomNav.visibility = View.VISIBLE
                }
                R.id.navigation_chef_orders -> {
                    toolbarTitle.setText(R.string.chef_orders_title)
                    btnNavBack.visibility = View.GONE
                    bottomNav.visibility = View.VISIBLE
                }
                R.id.navigation_chef_plats -> {
                    toolbarTitle.setText(R.string.chef_my_dishes)
                    btnNavBack.visibility = View.VISIBLE
                    bottomNav.visibility = View.GONE
                }
                else -> {
                    btnNavBack.visibility = View.GONE
                    bottomNav.visibility = View.VISIBLE
                }
            }
        }
    }

    fun logout() {
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
}
