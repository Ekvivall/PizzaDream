package com.sokol.pizzadream.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.EventBus.LogOutClick
import com.sokol.pizzadream.R
import org.greenrobot.eventbus.EventBus

class ProfileFragment : Fragment() {
    private lateinit var profileName:TextView
    private lateinit var profileEmail:TextView
    private lateinit var logOut:TextView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        initView(root)
        return root
    }
    private fun initView(root: View){
        profileName = root.findViewById(R.id.profile_name)
        profileEmail = root.findViewById(R.id.profile_email)
        profileName.text = Common.currentUser?.firstName
        profileEmail.text = Common.currentUser?.email
        logOut = root.findViewById(R.id.logOutText)
        logOut.setOnClickListener {
            EventBus.getDefault().postSticky(LogOutClick(true))
        }
    }
}