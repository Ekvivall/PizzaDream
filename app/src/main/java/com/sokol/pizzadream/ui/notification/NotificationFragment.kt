package com.sokol.pizzadream.ui.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.google.firebase.database.FirebaseDatabase
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.EventBus.CategoryClick
import com.sokol.pizzadream.EventBus.MenuClick
import com.sokol.pizzadream.EventBus.ProfileClick
import com.sokol.pizzadream.R
import org.greenrobot.eventbus.EventBus

class NotificationFragment : Fragment() {
    private lateinit var switchOrderUpdates: SwitchCompat
    private lateinit var switchNewsUpdates: SwitchCompat
    private lateinit var btnSaveSettings: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_notification, container, false)
        initView(root)
        return root
    }

    private fun initView(view: View) {
        switchOrderUpdates = view.findViewById(R.id.switch_order_updates)
        switchNewsUpdates = view.findViewById(R.id.switch_news_updates)
        btnSaveSettings = view.findViewById(R.id.btn_save_settings)
        btnSaveSettings.setOnClickListener {
            saveNotificationSettings()
        }
        switchOrderUpdates.isChecked = Common.currentUser?.receiveOrderUpdates == true
        switchNewsUpdates.isChecked = Common.currentUser?.receiveNews == true
    }

    private fun saveNotificationSettings() {
        val updates = mapOf(
            "receiveOrderUpdates" to switchOrderUpdates.isChecked,
            "receiveNews" to switchNewsUpdates.isChecked
        )

        val userRef = FirebaseDatabase.getInstance().reference.child(Common.USER_REFERENCE)
            .child(Common.currentUser?.uid!!)
        userRef.updateChildren(updates).addOnSuccessListener {
            Common.currentUser?.receiveOrderUpdates = switchOrderUpdates.isChecked
            Common.currentUser?.receiveNews = switchNewsUpdates.isChecked
            EventBus.getDefault().postSticky(ProfileClick(true))
            Toast.makeText(requireContext(), "Налаштування збережено", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(
                requireContext(), "Помилка збереження: ${e.message}", Toast.LENGTH_SHORT
            ).show()
        }
    }
}