package com.sokol.pizzadream.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.EventBus.EditProfileClick
import com.sokol.pizzadream.EventBus.FavoritesClick
import com.sokol.pizzadream.EventBus.LogOutClick
import com.sokol.pizzadream.EventBus.NewsClick
import com.sokol.pizzadream.EventBus.ReviewPizzeriaClick
import com.sokol.pizzadream.EventBus.VacanciesClick
import com.sokol.pizzadream.EventBus.ViewOrdersClick
import com.sokol.pizzadream.Model.UserModel
import com.sokol.pizzadream.R
import org.greenrobot.eventbus.EventBus

class ProfileFragment : Fragment() {
    private lateinit var profileImage: ImageView
    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private lateinit var profilePoints: TextView
    private lateinit var editProfileImg: ImageView
    private lateinit var logOut: TextView
    private lateinit var news: TextView
    private lateinit var favorites: TextView
    private lateinit var vacancies: TextView
    private lateinit var leaveReview: TextView
    private lateinit var viewOrders: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val userViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        initView(root)
        if (Common.isConnectedToInternet(requireContext())) {
            userViewModel.getUserMutableLiveData().observe(viewLifecycleOwner) {
                displayInfo(it)
            }
        } else {
            Toast.makeText(
                requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
            ).show()
        }
        return root
    }

    private fun displayInfo(it: UserModel) {
        profileName.text = StringBuilder(it.firstName).append(" ")
            .append(it.lastName)
        profileEmail.text = it.email
        profilePoints.text = StringBuilder(it.points.toString()).append(" Балів")
        if (it.avatar.isNotEmpty()) {
            Glide.with(this).load(it.avatar).into(profileImage)
        }
    }

    private fun initView(root: View) {
        profileImage = root.findViewById(R.id.profile_image)
        profileName = root.findViewById(R.id.profile_name)
        profileEmail = root.findViewById(R.id.profile_email)
        profilePoints = root.findViewById(R.id.profile_points)
        editProfileImg = root.findViewById(R.id.img_edit_profile)
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
        news.setOnClickListener {
            if (Common.isConnectedToInternet(requireContext())) {
                EventBus.getDefault().postSticky(NewsClick(true))
            } else {
                Toast.makeText(
                    requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
                ).show()
            }
        }
        favorites = root.findViewById(R.id.favorites)
        favorites.setOnClickListener {
            if (Common.isConnectedToInternet(requireContext())) {
                EventBus.getDefault().postSticky(FavoritesClick(true))
            } else {
                Toast.makeText(
                    requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
                ).show()
            }
        }
        vacancies = root.findViewById(R.id.vacancies)
        vacancies.setOnClickListener {
            if (Common.isConnectedToInternet(requireContext())) {
                EventBus.getDefault().postSticky(VacanciesClick(true))
            } else {
                Toast.makeText(
                    requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
                ).show()
            }
        }
        leaveReview = root.findViewById(R.id.leave_review)
        leaveReview.setOnClickListener {
            if (Common.isConnectedToInternet(requireContext())) {
                EventBus.getDefault().postSticky(ReviewPizzeriaClick(true))
            } else {
                Toast.makeText(
                    requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
                ).show()
            }
        }
        viewOrders = root.findViewById(R.id.view_orders)
        viewOrders.setOnClickListener {
            if (Common.isConnectedToInternet(requireContext())) {
                EventBus.getDefault().postSticky(ViewOrdersClick(true))
            } else {
                Toast.makeText(
                    requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
                ).show()
            }
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