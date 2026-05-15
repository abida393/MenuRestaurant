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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.savoria.app.R
import com.savoria.app.SavoriaApplication
import com.savoria.app.data.local.SavoriaDatabase
import com.savoria.app.data.local.entity.User
import com.savoria.app.data.local.entity.UserRole
import com.savoria.app.util.SecurityUtils
import kotlinx.coroutines.launch

class GestionUsersFragment : Fragment() {

    private lateinit var container: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_gestion_users, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container = view.findViewById(R.id.ll_user_list)

        view.findViewById<FloatingActionButton>(R.id.fab_add_user).setOnClickListener {
            showAddUserDialog()
        }

        loadUsers()
    }

    private fun loadUsers() {
        viewLifecycleOwner.lifecycleScope.launch {
            val db = SavoriaDatabase.getDatabase(
                requireContext(),
                (requireActivity().application as SavoriaApplication).applicationScope
            )
            val users = db.userDao().getAllUsers()
            populateUsers(users)
        }
    }

    private fun populateUsers(users: List<User>) {
        container.removeAllViews()
        val inflater = LayoutInflater.from(context)

        for (user in users) {
            val itemView = inflater.inflate(android.R.layout.simple_list_item_2, container, false)
            val text1 = itemView.findViewById<TextView>(android.R.id.text1)
            val text2 = itemView.findViewById<TextView>(android.R.id.text2)

            text1.text = "${user.nom} (${user.role})"
            text2.text = user.email

            itemView.setOnLongClickListener {
                showDeleteConfirm(user)
                true
            }

            container.addView(itemView)
            
            // Add divider
            val div = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
                setBackgroundColor(0xFFEEEEEE.toInt())
            }
            container.addView(div)
        }
    }

    private fun showAddUserDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_user, null)
        val etNom = dialogView.findViewById<EditText>(R.id.et_user_name)
        val etEmail = dialogView.findViewById<EditText>(R.id.et_user_email)
        val etPassword = dialogView.findViewById<EditText>(R.id.et_user_password)
        val spinnerRole = dialogView.findViewById<Spinner>(R.id.spinner_user_role)

        val roles = UserRole.values().map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter

        AlertDialog.Builder(requireContext())
            .setTitle("Nouvel Utilisateur")
            .setView(dialogView)
            .setPositiveButton("Créer") { _, _ ->
                val user = User(
                    nom = etNom.text.toString(),
                    email = etEmail.text.toString(),
                    password = SecurityUtils.hashPassword(etPassword.text.toString()),
                    role = UserRole.valueOf(spinnerRole.selectedItem.toString()),
                    actif = true
                )
                saveUser(user)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun saveUser(user: User) {
        viewLifecycleOwner.lifecycleScope.launch {
            val db = SavoriaDatabase.getDatabase(
                requireContext(),
                (requireActivity().application as SavoriaApplication).applicationScope
            )
            db.userDao().insertUser(user)
            loadUsers()
            Toast.makeText(context, "Utilisateur créé", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirm(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Supprimer")
            .setMessage("Voulez-vous supprimer l'utilisateur ${user.nom} ?")
            .setPositiveButton("Supprimer") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val db = SavoriaDatabase.getDatabase(
                        requireContext(),
                        (requireActivity().application as SavoriaApplication).applicationScope
                    )
                    db.userDao().deleteUser(user)
                    loadUsers()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }
}
