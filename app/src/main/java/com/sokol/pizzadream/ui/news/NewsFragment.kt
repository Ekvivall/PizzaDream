package com.sokol.pizzadream.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sokol.pizzadream.Adapter.NewsAdapter
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.R

class NewsFragment : Fragment() {
    private lateinit var newsRecycler: RecyclerView
    private lateinit var layoutAnimatorController: LayoutAnimationController
    private var newsAdapter: NewsAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val newsViewModel = ViewModelProvider(this).get(NewsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_news, container, false)
        initView(root)
        if (Common.isConnectedToInternet(requireContext())) {
            newsViewModel.newsList.observe(viewLifecycleOwner, Observer {
                val listData = it
                newsAdapter = NewsAdapter(listData, requireContext())
                newsRecycler.adapter = newsAdapter
                newsRecycler.layoutAnimation = layoutAnimatorController
            })
        } else {
            Toast.makeText(
                requireContext(),
                "Будь ласка, перевірте своє з'єднання!",
                Toast.LENGTH_SHORT
            ).show()
        }
        return root
    }

    private fun initView(root: View) {
        layoutAnimatorController =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)
        newsRecycler = root.findViewById(R.id.news_recycler)
        newsRecycler.setHasFixedSize(true)
        newsRecycler.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }
}