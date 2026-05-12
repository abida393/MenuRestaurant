package com.savoria.app.ui.client.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.savoria.app.R
import com.savoria.app.ui.admin.login.LoginActivity

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Admin login button — top-right dark circle icon
        view.findViewById<View>(R.id.btn_admin_login).setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        // Hero card action
        view.findViewById<View>(R.id.btn_view_special).setOnClickListener {
            Toast.makeText(context, "Today's Special — coming soon", Toast.LENGTH_SHORT).show()
        }

        // Sidebar action
        view.findViewById<View>(R.id.btn_sidebar).setOnClickListener {
            (activity as? com.savoria.app.ClientActivity)?.openDrawer()
        }
    }
}
