package com.sokol.pizzadream.ui.home

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
import com.sokol.pizzadream.databinding.FragmentHomeBinding
import dmax.dialog.SpotsDialog

class HomeFragment : Fragment() {

    private lateinit var  categoryRecycler:RecyclerView
    private lateinit var  bestProductsRecycler:RecyclerView
    private lateinit var layoutAnimatorController:LayoutAnimationController
    private lateinit var dialog: AlertDialog
    private var foodAdapter:FoodAdapter ?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        initView(root)
        homeViewModel.categoryList.observe(viewLifecycleOwner, Observer {
            dialog.dismiss()
            val listData = it
            val adapter = CategoryAdapter(listData, requireContext())
            categoryRecycler.adapter = adapter
            categoryRecycler.layoutAnimation = layoutAnimatorController
        })
        homeViewModel.getFoodListMutableLiveData().observe(viewLifecycleOwner, Observer {
            foodAdapter = FoodAdapter(it, requireContext())
            bestProductsRecycler.adapter = foodAdapter
            //bestProductsRecycler.layoutAnimation = layoutAnimatorController
        })
        return root
    }

    override fun onStop() {
        if(foodAdapter != null)
            foodAdapter!!.onStop()
        super.onStop()
    }
    private fun initView(root: View) {
        dialog = SpotsDialog.Builder().setContext(context).setCancelable(false).build()
        dialog.show()
        layoutAnimatorController = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)
        categoryRecycler = root.findViewById(R.id.category_recycler)
        categoryRecycler.setHasFixedSize(true)
        categoryRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        bestProductsRecycler = root.findViewById(R.id.best_products_recycler)
        bestProductsRecycler.setHasFixedSize(true)
        bestProductsRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }

}