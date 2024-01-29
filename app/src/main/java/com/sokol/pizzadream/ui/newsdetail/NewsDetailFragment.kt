package com.sokol.pizzadream.ui.newsdetail

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Model.NewsModel
import com.sokol.pizzadream.R

class NewsDetailFragment : Fragment() {
    private lateinit var newsImage: ImageView
    private lateinit var newsTitle: TextView
    private lateinit var newsText: TextView
    private lateinit var newsDate: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val newsDetailViewModel = ViewModelProvider(this).get(NewsDetailViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_news_detail, container, false)
        initView(root)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        if (Common.isConnectedToInternet(requireContext())) {
            newsDetailViewModel.getNewsDetailMutableLiveData().observe(viewLifecycleOwner) {
                // Оновлення інтерфейсу з детальною інформацією про новину
                displayInfo(it)
                actionBar?.title = it.title ?: getString(R.string.menu_food_detail)
            }
        } else {
            Toast.makeText(
                requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
            ).show()
        }
        return root
    }

    private fun displayInfo(it: NewsModel) {
        Glide.with(requireContext()).load(it.image).into(newsImage)
        newsTitle.text = it.title
        newsText.text = Html.fromHtml(it.content, Html.FROM_HTML_MODE_LEGACY)
        newsDate.text = it.date
    }

    private fun initView(root: View) {
        newsImage = root.findViewById(R.id.news_image)
        newsTitle = root.findViewById(R.id.news_title)
        newsText = root.findViewById(R.id.news_text)
        newsDate = root.findViewById(R.id.news_date)
    }
}

