package com.sokol.pizzadream.ui.createCustomerPizza

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.EventBus.MenuClick
import com.sokol.pizzadream.Model.FoodModel
import com.sokol.pizzadream.Model.SizeModel
import com.sokol.pizzadream.R
import com.sokol.pizzadream.Remote.ICloudFunctions
import com.sokol.pizzadream.Remote.RetrofitCloudClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus

class CreateCustomerPizzaFragment : Fragment() {
    private val REQUEST_BRAINTREE_CODE: Int = 8888
    private lateinit var tilPizzaName: TextInputLayout
    private lateinit var edtPizzaName: EditText
    private lateinit var pizzaDescription: TextView
    private lateinit var listView: ListView
    private lateinit var btnPlaceOrder: Button
    private lateinit var pizzaSizes: MutableList<SizeModel>
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    lateinit var cloudFunctions: ICloudFunctions
    private var pizzaName = ""
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
        pizzaSizes = ArrayList()
        for (size in sizes) {
            val sum = size.price + addonSum
            pizzaPrices.add("Ціна за ${size.name}: $sum грн.")
            val pizzaSize = SizeModel()
            pizzaSize.name = size.name
            pizzaSize.price = sum
            pizzaSizes.add(pizzaSize)
        }
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_pizza_price, pizzaPrices)
        listView.adapter = adapter
    }

    private fun initView(root: View) {
        cloudFunctions = RetrofitCloudClient.getInstance().create(ICloudFunctions::class.java)
        tilPizzaName = root.findViewById(R.id.til_pizza_name)
        edtPizzaName = root.findViewById(R.id.edt_pizza_name)
        pizzaDescription = root.findViewById(R.id.tvPizzaDescription)
        listView = root.findViewById(R.id.lvPizzaPrices)
        btnPlaceOrder = root.findViewById(R.id.btnPlaceOrder)
        btnPlaceOrder.setOnClickListener {
            if (Common.isConnectedToInternet(requireContext())) {
                pizzaName = edtPizzaName.text.toString().trim()
                tilPizzaName.error = null
                if (pizzaName.isEmpty()) {
                    tilPizzaName.error = "Будь ласка, введіть назву піци"
                    return@setOnClickListener
                }
                if (Common.currentToken.isNotEmpty()) {
                    val dropInRequest = DropInRequest().clientToken(Common.currentToken)
                    startActivityForResult(
                        dropInRequest.getIntent(context), REQUEST_BRAINTREE_CODE
                    )
                }
            } else {
                Toast.makeText(
                    requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_BRAINTREE_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val result =
                    data.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                val nonce = result!!.paymentMethodNonce
                val headers = HashMap<String, String>()
                headers["Authorization"] = Common.buildToken(Common.authorizeToken!!)
                compositeDisposable.add(
                    cloudFunctions.submitPayment(
                        headers, 20.0, nonce!!.nonce
                    ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ braintreeTransaction ->
                            if (braintreeTransaction.success) {
                                val foodModel = FoodModel()
                                val foodRef = FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF)
                                    .child(Common.customerPizzas).child("foods")
                                foodModel.id = foodRef.push().key.toString()
                                foodModel.categoryId = Common.customerPizzas
                                foodModel.name = pizzaName
                                foodModel.image = ""
                                foodModel.description = pizzaDescription.text.toString()
                                foodModel.addon = ""
                                foodModel.size = pizzaSizes
                                foodModel.ratingSum = 0L
                                foodModel.ratingCount = 0L
                                val user = Common.currentUser!!
                                foodModel.createdUserId = user.uid
                                foodModel.createdUserName = user.lastName + " " + user.firstName
                                foodModel.transactionId = braintreeTransaction.transaction!!.id
                                foodRef.child(foodModel.id!!).setValue(foodModel).addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                                }.addOnCompleteListener {
                                    Toast.makeText(
                                        requireContext(),
                                        "Ваше замовлення оплачено! Наш адміністратор перевірить і додасть фото вашої піци.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    EventBus.getDefault().postSticky(MenuClick(true))
                                }
                            }
                        }, { err: Throwable ->
                            Log.e("Err", err.message.toString())
                            Toast.makeText(
                                requireContext(), err.message, Toast.LENGTH_SHORT
                            ).show()
                        })
                )
            }
        }
    }
}