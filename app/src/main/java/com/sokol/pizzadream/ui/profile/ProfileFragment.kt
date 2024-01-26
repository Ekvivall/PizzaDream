package com.sokol.pizzadream.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.EventBus.EditProfileClick
import com.sokol.pizzadream.EventBus.LogOutClick
import com.sokol.pizzadream.EventBus.NewsClick
import com.sokol.pizzadream.R
import org.greenrobot.eventbus.EventBus

class ProfileFragment : Fragment() {
    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var editProfileImg: ImageView
    private lateinit var logOut: TextView
    private lateinit var news: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        initView(root)
        return root
    }

    private fun initView(root: View) {
        profileImage = root.findViewById(R.id.profile_image)
        profileName = root.findViewById(R.id.profile_name)
        profileEmail = root.findViewById(R.id.profile_email)
        editProfileImg = root.findViewById(R.id.img_edit_profile)
        profileName.text = Common.currentUser?.firstName + " " + Common.currentUser?.lastName
        profileEmail.text = Common.currentUser?.email
        if (Common.currentUser!!.avatar.isNotEmpty()) {
            Glide.with(this).load(Common.currentUser!!.avatar).into(profileImage)
        }
        logOut = root.findViewById(R.id.logOutText)
        news = root.findViewById(R.id.news)
        logOut.setOnClickListener {
            EventBus.getDefault().postSticky(LogOutClick(true))
        }
        profileImage.setOnClickListener {
            goToEditProfile()
        }
        editProfileImg.setOnClickListener {
            goToEditProfile()
        }
        news.setOnClickListener{
            EventBus.getDefault().postSticky(NewsClick(true))
        }
    }

    private fun goToEditProfile() {
        if (Common.isConnectedToInternet(requireContext())) {
            EventBus.getDefault().postSticky(EditProfileClick(true))
        } else {
            Toast.makeText(
                requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
            ).show()
        }
    }
}