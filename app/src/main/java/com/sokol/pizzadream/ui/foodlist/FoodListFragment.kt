package com.sokol.pizzadream.ui.foodlist

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sokol.pizzadream.Adapter.CategoryAdapter
import com.sokol.pizzadream.Adapter.FoodAdapter
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.R
import com.sokol.pizzadream.ui.home.HomeViewModel
import dmax.dialog.SpotsDialog

class FoodListFragment: Fragment() {
    private lateinit var  productsRecycler: RecyclerView
    private lateinit var layoutAnimatorController:LayoutAnimationController
    private var foodAdapter:FoodAdapter ?=null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val foodListViewModel =
            ViewModelProvider(this).get(FoodListViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_food_list, container, false)
        initView(root)
        foodListViewModel.getFoodListMutableLiveData().observe(viewLifecycleOwner, Observer {
            foodAdapter = FoodAdapter(it, requireContext())
            productsRecycler.adapter = foodAdapter
            productsRecycler.layoutAnimation = layoutAnimatorController
        })
        return root
    }
    override fun onStop() {
        if(foodAdapter != null)
            foodAdapter!!.onStop()
        super.onStop()
    }
    private fun initView(root: View) {
        layoutAnimatorController = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)
        productsRecycler = root.findViewById(R.id.products_recycler)
        productsRecycler.setHasFixedSize(true)
        productsRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        (activity as AppCompatActivity).supportActionBar!!.title = Common.categorySelected!!.name
    }
}