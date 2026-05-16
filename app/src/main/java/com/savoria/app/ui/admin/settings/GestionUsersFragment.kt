package com.savoria.app.ui.admin.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.savoria.app.R
import com.savoria.app.SavoriaApplication
import com.savoria.app.data.local.entity.User
import com.savoria.app.util.SecurityUtils
import com.savoria.app.data.local.entity.UserRole
import com.savoria.app.ui.viewmodel.AdminViewModel
import com.savoria.app.ui.viewmodel.AdminViewModelFactory
import kotlinx.coroutines.launch

class GestionUsersFragment : Fragment() {

    private val viewModel: AdminViewModel by activityViewModels {
        AdminViewModelFactory(requireActivity().application as SavoriaApplication)
    }

    private lateinit var container: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_gestion_users, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.container = view.findViewById(R.id.ll_user_list)

        view.findViewById<FloatingActionButton>(R.id.fab_add_user).setOnClickListener {
            showAddUserDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.users.collect { populateUsers(it) }
        }
        viewModel.refreshUsers()
    }

    private fun populateUsers(users: List<User>) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        for (user in users) {
            val itemView = inflater.inflate(android.R.layout.simple_list_item_2, container, false)
            itemView.findViewById<TextView>(android.R.id.text1).text = "${user.nom} (${user.role})"
            itemView.findViewById<TextView>(android.R.id.text2).text = user.email

            itemView.setOnLongClickListener {
                showDeleteConfirm(user)
                true
            }

            container.addView(itemView)

            val div = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                )
                setBackgroundColor(0xFFEEEEEE.toInt())
            }
            container.addView(div)
        }
    }

    private fun showAddUserDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_user, null)
        val etNom = dialogView.findViewById<EditText>(R.id.et_user_name)
        val etEmail = dialogView.findViewById<EditText>(R.id.et_user_email)
        val etPassword = dialogView.findViewById<EditText>(R.id.et_user_password)
        val spinnerRole = dialogView.findViewById<Spinner>(R.id.spinner_user_role)

        val roles = UserRole.values().map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.user_add_title)
            .setView(dialogView)
            .setPositiveButton(R.string.user_add_confirm) { _, _ ->
                val email = etEmail.text.toString().trim().lowercase()
                val password = etPassword.text.toString()
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(requireContext(), R.string.login_error_invalid, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val user = User(
                    nom = etNom.text.toString().trim(),
                    email = email,
                    password = SecurityUtils.hashPassword(password),
                    role = UserRole.valueOf(spinnerRole.selectedItem.toString()),
                    actif = true
                )
                viewModel.addUser(user)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showDeleteConfirm(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.user_delete_title)
            .setMessage(getString(R.string.user_delete_message, user.nom))
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.deleteUser(user)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
