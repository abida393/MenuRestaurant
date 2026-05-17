package com.savoria.app.ui.admin.settings

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.savoria.app.R
import com.savoria.app.data.local.entity.User
import com.savoria.app.data.local.entity.UserRole
import java.util.Locale

class UsersAdapter(
    private val onUserClick: (User) -> Unit,
    private val onUserLongClick: (User) -> Unit
) : ListAdapter<User, UsersAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_user_name)
        private val tvEmail: TextView = itemView.findViewById(R.id.tv_user_email)
        private val tvAvatarText: TextView = itemView.findViewById(R.id.tv_avatar_text)
        private val flAvatarContainer: FrameLayout = itemView.findViewById(R.id.fl_avatar_container)
        private val tvRoleBadge: TextView = itemView.findViewById(R.id.tv_user_role_badge)

        fun bind(user: User) {
            tvName.text = user.nom
            tvEmail.text = user.email

            // Extract initial
            val initial = if (user.nom.isNotBlank()) {
                user.nom.trim().take(1).uppercase(Locale.getDefault())
            } else {
                "U"
            }
            tvAvatarText.text = initial

            // Set role colors dynamically
            val roleColor = when (user.role) {
                UserRole.ADMIN -> Color.parseColor("#A02020")
                UserRole.CHEF -> Color.parseColor("#E65100")
                UserRole.SERVEUR -> Color.parseColor("#1E88E5")
            }

            // Apply color tint to avatar circle and role badge
            flAvatarContainer.backgroundTintList = ColorStateList.valueOf(roleColor)
            tvRoleBadge.backgroundTintList = ColorStateList.valueOf(roleColor)
            tvRoleBadge.text = user.role.name

            itemView.setOnClickListener { onUserClick(user) }
            itemView.setOnLongClickListener {
                onUserLongClick(user)
                true
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}
