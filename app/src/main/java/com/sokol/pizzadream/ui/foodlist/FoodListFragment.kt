package com.sokol.pizzadream.ui.foodlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sokol.pizzadream.Adapter.FoodAdapter
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Model.FoodModel
import com.sokol.pizzadream.R

class FoodListFragment : Fragment() {
    private lateinit var productsRecycler: RecyclerView
    private lateinit var layoutAnimatorController: LayoutAnimationController
    private var foodAdapter: FoodAdapter? = null
    private lateinit var sortSpinner: Spinner
    private var foodList: List<FoodModel> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val foodListViewModel = ViewModelProvider(this).get(FoodListViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_food_list, container, false)
        initView(root)
        if (Common.isConnectedToInternet(requireContext())) {
            foodListViewModel.getFoodListMutableLiveData().observe(viewLifecycleOwner, Observer {
                foodList = it
                updateFoodList(foodList)
            })
        } else {
            Toast.makeText(
                requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
            ).show()
        }
        return root
    }


    private fun initView(root: View) {
        layoutAnimatorController =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)
        productsRecycler = root.findViewById(R.id.products_recycler)
        productsRecycler.setHasFixedSize(true)
        productsRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        (activity as AppCompatActivity).supportActionBar!!.title = Common.categorySelected!!.name
        sortSpinner = root.findViewById(R.id.sort_spinner)
        val sortOptions = resources.getStringArray(R.array.sort_options)
        val adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, sortOptions
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = adapter
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val sortedFoodList = when (position) {
                    1 -> {
                        foodList.sortedBy { it.size[0].price }
                    }

                    2 -> {
                        foodList.sortedByDescending { it.size[0].price }
                    }

                    else -> {
                        foodList.sortedWith(compareByDescending {
                            if (it.ratingCount == 0L) {
                                0f
                            } else {
                                it.ratingSum.toFloat() / it.ratingCount
                            }
                        })
                    }
                }
                updateFoodList(sortedFoodList)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun updateFoodList(foodList: List<FoodModel>) {
        foodAdapter = FoodAdapter(foodList, requireContext())
        productsRecycler.adapter = foodAdapter
        productsRecycler.layoutAnimation = layoutAnimatorController
    }
}