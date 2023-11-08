package com.sokol.pizzadream.ui.placeorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sokol.pizzadream.Adapter.FoodAdapter
import com.sokol.pizzadream.R
import com.sokol.pizzadream.ui.foodlist.FoodListViewModel

class PlaceOrderFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val placeOrderViewModel =
            ViewModelProvider(this).get(PlaceOrderViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_place_order, container, false)
        return root
    }
}