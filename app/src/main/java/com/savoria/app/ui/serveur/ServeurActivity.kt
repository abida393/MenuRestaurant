package com.savoria.app.ui.serveur

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.savoria.app.R
import com.savoria.app.data.local.StaffSessionManager
import com.savoria.app.ui.admin.AdminActivity
import com.savoria.app.ui.admin.login.LoginActivity
import com.savoria.app.ChefActivity

class ServeurActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        when {
            !StaffSessionManager.isLoggedIn(this) -> {
                redirectToLogin()
                return
            }
            StaffSessionManager.isServeur(this) -> Unit
            StaffSessionManager.isChef(this) -> {
                startActivity(Intent(this, ChefActivity::class.java).apply {
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

        setContentView(R.layout.activity_serveur)
        setSupportActionBar(findViewById(R.id.serveur_toolbar))

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.serveur_root_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_serveur, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            StaffSessionManager.clearSession(this)
            redirectToLogin()
            return true
        }
        return super.onOptionsItemSelected(item)
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
