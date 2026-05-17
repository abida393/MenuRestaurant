package com.savoria.app.ui.admin.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.savoria.app.ChefActivity
import com.savoria.app.R
import com.savoria.app.SavoriaApplication
import com.savoria.app.data.local.database.SavoriaDatabase
import com.savoria.app.data.local.StaffSessionManager
import com.savoria.app.data.local.UserSeeder
import com.savoria.app.data.local.entity.UserRole
import com.savoria.app.ui.admin.AdminActivity
import com.savoria.app.ui.serveur.ServeurActivity
import com.savoria.app.util.SecurityUtils
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvError: TextView
    private lateinit var tvRecover: TextView
    private lateinit var tvContactAdmin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when {
            StaffSessionManager.isAdmin(this) -> {
                startStaffHome(Intent(this, AdminActivity::class.java))
                return
            }
            StaffSessionManager.isChef(this) -> {
                startStaffHome(Intent(this, ChefActivity::class.java))
                return
            }
            StaffSessionManager.isServeur(this) -> {
                startStaffHome(Intent(this, ServeurActivity::class.java))
                return
            }
        }

        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        tvError = findViewById(R.id.tv_error)
        tvRecover = findViewById(R.id.tv_recover)
        tvContactAdmin = findViewById(R.id.tv_contact_admin)

        lifecycleScope.launch {
            val database = SavoriaDatabase.getDatabase(
                this@LoginActivity,
                (application as SavoriaApplication).applicationScope
            )
            UserSeeder.ensureStaffAccounts(this@LoginActivity, database.userDao())
        }

        btnLogin.setOnClickListener { attemptLogin() }

        tvRecover.setOnClickListener {
            Toast.makeText(this, getString(R.string.login_recover_hint), Toast.LENGTH_LONG).show()
        }

        tvContactAdmin.setOnClickListener {
            Toast.makeText(this, "system@savoria.com", Toast.LENGTH_LONG).show()
        }
    }

    private fun attemptLogin() {
        val email = etEmail.text.toString().trim().lowercase()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = getString(R.string.login_email_required)
            etEmail.requestFocus()
            return
        }
        if (password.isEmpty()) {
            etPassword.error = getString(R.string.login_password_required)
            etPassword.requestFocus()
            return
        }

        lifecycleScope.launch {
            val database = SavoriaDatabase.getDatabase(
                this@LoginActivity,
                (application as SavoriaApplication).applicationScope
            )
            UserSeeder.ensureStaffAccounts(this@LoginActivity, database.userDao())

            val user = database.userDao().getUserByEmail(email)

            when {
                user == null || !SecurityUtils.verifyPassword(password, user.password) -> {
                    tvError.text = getString(R.string.login_error_invalid)
                    tvError.visibility = View.VISIBLE
                    etPassword.text.clear()
                }
                !user.actif -> {
                    tvError.text = getString(R.string.login_error_inactive)
                    tvError.visibility = View.VISIBLE
                }
                else -> {
                    if (SecurityUtils.needsRehash(user.password)) {
                        database.userDao().updateUser(
                            user.copy(password = SecurityUtils.hashPassword(password))
                        )
                    }
                    tvError.visibility = View.GONE
                    StaffSessionManager.saveSession(
                        this@LoginActivity,
                        user.id,
                        user.role.name
                    )
                    val intent = when (user.role) {
                        UserRole.ADMIN -> Intent(this@LoginActivity, AdminActivity::class.java)
                        UserRole.CHEF -> Intent(this@LoginActivity, ChefActivity::class.java)
                        UserRole.SERVEUR -> Intent(this@LoginActivity, ServeurActivity::class.java)
                    }.apply {
                        putExtra("USER_ID", user.id)
                        putExtra("USER_ROLE", user.role.name)
                    }
                    startStaffHome(intent)
                }
            }
        }
    }

    private fun startStaffHome(intent: Intent) {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
