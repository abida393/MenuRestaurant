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
import com.savoria.app.MainActivity
import com.savoria.app.R
import com.savoria.app.SavoriaApplication
import com.savoria.app.data.local.SavoriaDatabase
import com.savoria.app.data.local.UserSeeder
import com.savoria.app.data.local.entity.UserRole
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvError: TextView
    private lateinit var tvRecover: TextView
    private lateinit var tvContactAdmin: TextView
    private lateinit var tvDemoAccounts: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        tvError = findViewById(R.id.tv_error)
        tvRecover = findViewById(R.id.tv_recover)
        tvContactAdmin = findViewById(R.id.tv_contact_admin)
        tvDemoAccounts = findViewById(R.id.tv_demo_accounts)

        lifecycleScope.launch {
            val database = SavoriaDatabase.getDatabase(
                this@LoginActivity,
                (application as SavoriaApplication).applicationScope
            )
            UserSeeder.ensureDefaultUsers(database.userDao())
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
            UserSeeder.ensureDefaultUsers(database.userDao())

            val user = database.userDao().getUserByEmail(email)

            when {
                user == null || user.password != UserSeeder.hashPassword(password) -> {
                    tvError.text = getString(R.string.login_error_invalid)
                    tvError.visibility = View.VISIBLE
                    etPassword.text.clear()
                }
                !user.actif -> {
                    tvError.text = getString(R.string.login_error_inactive)
                    tvError.visibility = View.VISIBLE
                }
                else -> {
                    tvError.visibility = View.GONE
                    val intent = when (user.role) {
                        UserRole.ADMIN -> Intent(this@LoginActivity, MainActivity::class.java)
                        UserRole.CHEF, UserRole.SERVEUR ->
                            Intent(this@LoginActivity, ChefActivity::class.java)
                    }.apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("USER_ID", user.id)
                        putExtra("USER_ROLE", user.role.name)
                    }
                    startActivity(intent)
                }
            }
        }
    }
}
