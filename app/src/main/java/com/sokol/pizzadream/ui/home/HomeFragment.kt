package com.sokol.pizzadream.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sokol.pizzadream.Adapter.CategoryAdapter
import com.sokol.pizzadream.Adapter.FoodAdapter
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.R

class HomeFragment : Fragment() {

    private lateinit var categoryRecycler: RecyclerView
    private lateinit var bestProductsRecycler: RecyclerView
    private lateinit var layoutAnimatorController: LayoutAnimationController
    private var foodAdapter: FoodAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        initView(root)
        if (Common.isConnectedToInternet(requireContext())) {
            homeViewModel.categoryList.observe(viewLifecycleOwner) {
                val listData = it
                val adapter = CategoryAdapter(listData, requireContext())
                categoryRecycler.adapter = adapter
                categoryRecycler.layoutAnimation = layoutAnimatorController
            }
            homeViewModel.getFoodListMutableLiveData().observe(viewLifecycleOwner) {
                foodAdapter = FoodAdapter(it, requireContext())
                bestProductsRecycler.adapter = foodAdapter
            }
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
        categoryRecycler = root.findViewById(R.id.category_recycler)
        categoryRecycler.setHasFixedSize(true)
        categoryRecycler.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        bestProductsRecycler = root.findViewById(R.id.best_products_recycler)
        bestProductsRecycler.setHasFixedSize(true)
        bestProductsRecycler.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }

}