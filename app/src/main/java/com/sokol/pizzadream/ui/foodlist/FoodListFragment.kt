package com.sokol.pizzadream.ui.foodlist

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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
    private var resultSearch: MutableList<FoodModel> = ArrayList()
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.food_list_menu, menu)
        val menuItem = menu.findItem(R.id.action_search)
        val searchManager =
            requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menuItem.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                startSearchFood(p0!!)
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }

        })
        val closeButton =
            searchView.findViewById<View>(androidx.appcompat.R.id.search_close_btn) as ImageView
        closeButton.setOnClickListener {
            val editText =
                searchView.findViewById<View>(androidx.appcompat.R.id.search_src_text) as EditText
            editText.setText("")
            searchView.setQuery("", false)
            searchView.onActionViewCollapsed()
            menuItem.collapseActionView()
            resultSearch = ArrayList()
            updateFoodList(foodList)
        }
    }

    private fun startSearchFood(s: String) {
        resultSearch = ArrayList()
        for (foodModel in Common.categorySelected?.foods?.values!!) {
            if (foodModel.name?.lowercase()?.contains(s.lowercase()) == true) {
                resultSearch.add(foodModel)
            }
        }
        updateFoodList(resultSearch)
    }

    private fun initView(root: View) {
        setHasOptionsMenu(true)
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
                val list = if (resultSearch.isEmpty()) foodList else resultSearch
                val sortedFoodList = when (position) {
                    1 -> {
                        list.sortedBy { it.size[0].price }
                    }

                    2 -> {
                        list.sortedByDescending { it.size[0].price }
                    }

                    else -> {
                        list.sortedWith(compareByDescending {
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