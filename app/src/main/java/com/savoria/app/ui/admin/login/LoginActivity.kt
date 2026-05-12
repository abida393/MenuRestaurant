package com.savoria.app.ui.admin.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.savoria.app.MainActivity
import com.savoria.app.ChefActivity
import com.savoria.app.R
import com.savoria.app.SavoriaApplication
import com.savoria.app.data.local.SavoriaDatabase
import com.savoria.app.data.local.entity.UserRole
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
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        btnLogin = findViewById(R.id.btn_login)
        tvError = findViewById(R.id.tv_error)
        tvRecover = findViewById(R.id.tv_recover)
        tvContactAdmin = findViewById(R.id.tv_contact_admin)

        btnLogin.setOnClickListener { attemptLogin() }

        tvRecover.setOnClickListener {
            Toast.makeText(this, "Contact your system administrator to reset access.", Toast.LENGTH_LONG).show()
        }

        tvContactAdmin.setOnClickListener {
            Toast.makeText(this, "system@savoria.com", Toast.LENGTH_LONG).show()
        }
    }

    private fun hashPassword(raw: String): String =
        java.security.MessageDigest.getInstance("SHA-256")
            .digest(raw.toByteArray())
            .joinToString("") { "%02x".format(it) }

    private fun attemptLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Email requis"
            etEmail.requestFocus()
            return
        }
        if (password.isEmpty()) {
            etPassword.error = "Mot de passe requis"
            etPassword.requestFocus()
            return
        }

        lifecycleScope.launch {
            val database = SavoriaDatabase.getDatabase(this@LoginActivity, (application as SavoriaApplication).applicationScope)
            val user = database.userDao().getUserByEmail(email)

            if (user != null && user.password == hashPassword(password)) {
                tvError.visibility = View.GONE
                val intent = when (user.role) {
                    UserRole.ADMIN -> Intent(this@LoginActivity, MainActivity::class.java)
                    UserRole.CHEF, UserRole.SERVEUR -> Intent(this@LoginActivity, ChefActivity::class.java)
                }.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("USER_ID", user.id)
                    putExtra("USER_ROLE", user.role.name)
                }
                startActivity(intent)
            } else {
                tvError.visibility = View.VISIBLE
                etPassword.text.clear()
            }
        }
    }
}
