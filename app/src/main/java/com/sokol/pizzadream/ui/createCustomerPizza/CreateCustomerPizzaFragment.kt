package com.sokol.pizzadream.ui.createCustomerPizza

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Model.FoodModel
import com.sokol.pizzadream.R

class CreateCustomerPizzaFragment : Fragment() {
    private lateinit var tilPizzaName: TextInputLayout
    private lateinit var edtPizzaName: EditText
    private lateinit var pizzaDescription: TextView
    private lateinit var listView: ListView
    private lateinit var btnPlaceOrder: Button
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val createCustomerPizzaViewModel =
            ViewModelProvider(this)[CreateCustomerPizzaViewModel::class.java]
        val root = inflater.inflate(R.layout.fragment_create_customer_pizza, container, false)
        initView(root)
        if (Common.isConnectedToInternet(requireContext())) {
            createCustomerPizzaViewModel.getFoodDetailMutableLiveData()
                .observe(viewLifecycleOwner) {
                    displayInfo(it)
                }
        } else {
            Toast.makeText(
                requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
            ).show()
        }
        return root
    }

    private fun displayInfo(it: FoodModel) {
        val foodAddons = Common.userSelectedAddon
        var res = ""
        var addonSum = 0
        if (foodAddons != null) {
            for (foodAddon in foodAddons) {
                if (foodAddon != foodAddons[0]) res += ", "
                res += foodAddon.name + " x" + foodAddon.userCount
                addonSum += foodAddon.price * foodAddon.userCount
            }
        }
        pizzaDescription.text = res
        // Створення списку цін на піцу
        val pizzaPrices = ArrayList<String>()
        val sizes = Common.foodSelected?.size!!
        for (size in sizes){
            pizzaPrices.add("Ціна за ${size.name}: ${size.price + addonSum} грн.")
        }
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_pizza_price, pizzaPrices)
        listView.adapter = adapter
    }

    private fun initView(root: View) {
        tilPizzaName = root.findViewById(R.id.til_pizza_name)
        edtPizzaName = root.findViewById(R.id.edt_pizza_name)
        pizzaDescription = root.findViewById(R.id.tvPizzaDescription)
        listView = root.findViewById(R.id.lvPizzaPrices)
        btnPlaceOrder = root.findViewById(R.id.btnPlaceOrder)
    }
}