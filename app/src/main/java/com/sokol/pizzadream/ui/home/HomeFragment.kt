package com.sokol.pizzadream.ui.home

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sokol.pizzadream.Adapter.CategoryAdapter
import com.sokol.pizzadream.Adapter.FoodAdapter
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Model.FoodModel
import com.sokol.pizzadream.R

class HomeFragment : Fragment() {

    private lateinit var categoryRecycler: RecyclerView
    private lateinit var bestProductsRecycler: RecyclerView
    private lateinit var layoutAnimatorController: LayoutAnimationController
    private var foodAdapter: FoodAdapter? = null
    private var foodList: List<FoodModel> = ArrayList()

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
                foodList = it
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
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.food_list_menu, menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menuItem.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                startSearchFood(p0!!)
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }

        })
        val closeButton = searchView.findViewById<View>(androidx.appcompat.R.id.search_close_btn) as ImageView
        closeButton.setOnClickListener {
            val editText = searchView.findViewById<View>(androidx.appcompat.R.id.search_src_text) as EditText
            editText.setText("")
            searchView.setQuery("", false)
            searchView.onActionViewCollapsed()
            menuItem.collapseActionView()
            updateFoodList(foodList)
        }
    }

    private fun startSearchFood(s: String) {
        val resultFood: MutableList<FoodModel> = ArrayList()
        for (foodModel in foodList){
            if (foodModel.name?.lowercase()?.contains(s.lowercase()) == true){
                resultFood.add(foodModel)
            }
        }
        updateFoodList(resultFood)
    }
    private fun initView(root: View) {
        setHasOptionsMenu(true)
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
    private fun updateFoodList(foodList: List<FoodModel>) {
        foodAdapter = FoodAdapter(foodList, requireContext())
        bestProductsRecycler.adapter = foodAdapter
    }
}