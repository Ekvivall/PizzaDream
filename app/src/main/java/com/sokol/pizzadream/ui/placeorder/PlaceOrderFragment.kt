package com.sokol.pizzadream.ui.placeorder

import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Database.CartDatabase
import com.sokol.pizzadream.Database.Repositories.CartInterface
import com.sokol.pizzadream.Database.Repositories.CartRepository
import com.sokol.pizzadream.EventBus.MenuClick
import com.sokol.pizzadream.Model.OrderModel
import com.sokol.pizzadream.R
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import java.util.Locale


class PlaceOrderFragment : Fragment() {
    private lateinit var edtName: EditText
    private lateinit var tilName: TextInputLayout
    private lateinit var edtPhone: EditText
    private lateinit var tilPhone: TextInputLayout
    private lateinit var edtEmail: EditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var edtAddress: EditText
    private lateinit var tilAddress: TextInputLayout
    private lateinit var rdiHome: RadioButton
    private lateinit var rdiSelfService: RadioButton
    private lateinit var radioGroupAddresses: RadioGroup
    private lateinit var rdiCod: RadioButton
    private lateinit var rdiOnlinePayment: RadioButton
    private lateinit var btnOrder: Button
    private lateinit var totalPrice: TextView
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var cart: CartInterface
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var totalPriceWithDelivery = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        cart = CartRepository(CartDatabase.getInstance(requireContext()).getCartDAO())
        //EventBus.getDefault().postSticky(HideFABCart(true))
        val placeOrderViewModel = ViewModelProvider(this).get(PlaceOrderViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_place_order, container, false)
        initView(root)
        if (Common.isConnectedToInternet(requireContext())) {
            //Ініціалізація списку адрес
            placeOrderViewModel.getAddressListMutableLiveData()
                .observe(viewLifecycleOwner, Observer {
                    for (address in it) {
                        val radioButton = RadioButton(context)
                        radioButton.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                Common.userSelectedAddress = address
                            }
                        }
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        radioButton.layoutParams = params
                        radioButton.text = address
                        radioButton.textSize = 16f
                        radioButton.tag = address
                        radioGroupAddresses.addView(radioButton)
                    }
                    // Встановлення першої адреси як вибрану, якщо список не порожній
                    if (radioGroupAddresses.childCount > 0) {
                        val radioButton = radioGroupAddresses.getChildAt(0) as RadioButton
                        radioButton.isChecked = true
                    }
                })
        } else {
            Toast.makeText(
                requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
            ).show()
        }
        return root
    }

    private fun initView(root: View) {
        edtName = root.findViewById(R.id.edt_name)
        tilName = root.findViewById(R.id.til_name)
        edtPhone = root.findViewById(R.id.edt_phone)
        tilPhone = root.findViewById(R.id.til_phone)
        edtEmail = root.findViewById(R.id.edt_email)
        tilEmail = root.findViewById(R.id.til_email)
        edtAddress = root.findViewById(R.id.edt_address)
        tilAddress = root.findViewById(R.id.til_address)
        rdiHome = root.findViewById(R.id.rdi_home_address)
        rdiSelfService = root.findViewById(R.id.rdi_self_service)
        radioGroupAddresses = root.findViewById(R.id.radio_group_addresses)
        rdiCod = root.findViewById(R.id.rdi_cod)
        rdiOnlinePayment = root.findViewById(R.id.rdi_online_payment)
        btnOrder = root.findViewById(R.id.btn_order)
        totalPrice = root.findViewById(R.id.txt_total_price)
        val regex = Regex("\\d+(?=,\\d{2})")
        val price = Common.totalPrice.replace("\u00A0", "")
        val finalPrice = regex.find(price)?.value
        totalPriceWithDelivery =
            StringBuilder("Всього: ").append(Common.formatPrice(finalPrice!!.toDouble() + 60))
                .toString()
        totalPrice.text = totalPriceWithDelivery
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (!p0.isNullOrEmpty()) {
                    val unmaskedText = StringBuilder()
                    val chars: CharArray = p0.toString().toCharArray()
                    for (x in chars.indices) {
                        if (Character.isDigit(chars[x])) {
                            unmaskedText.append(chars[x])
                        }
                    }
                    if (unmaskedText.length <= 9) {
                        val formattedText = StringBuilder()
                        for (i in unmaskedText.indices) {
                            if (i == 2 || i == 5 || i == 7) {
                                formattedText.append(" ")
                            }
                            formattedText.append(unmaskedText[i])
                        }
                        edtPhone.removeTextChangedListener(this)
                        edtPhone.setText(formattedText.toString())
                        edtPhone.setSelection(formattedText.length)
                        edtPhone.addTextChangedListener(this)
                    }
                }
            }
        }
        edtPhone.addTextChangedListener(textWatcher)
        edtName.setText(Common.currentUser!!.firstName)
        edtEmail.setText(Common.currentUser!!.email)
        edtPhone.setText(Common.currentUser?.phone!!.replace(" ", ""))
        rdiHome.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                edtAddress.visibility = View.VISIBLE
                radioGroupAddresses.visibility = View.GONE
                totalPrice.text = totalPriceWithDelivery
            } else {
                edtAddress.visibility = View.GONE
                radioGroupAddresses.visibility = View.VISIBLE
                totalPrice.text = Common.totalPrice
            }
        }
        btnOrder.setOnClickListener {
            if (Common.isConnectedToInternet(requireContext())) {
                // Обробка кнопки замовлення
                val name = edtName.text.toString().trim()
                val phone = "+380 " + edtPhone.text.toString().trim()
                val email = edtEmail.text.toString().trim()
                var address = ""
                if (rdiHome.isChecked) {
                    address = edtAddress.text.toString().trim()
                    Common.totalPrice = totalPrice.text.toString()
                } else {
                    val selectedRadioButton =
                        radioGroupAddresses.findViewById<RadioButton>(radioGroupAddresses.checkedRadioButtonId)
                    address = selectedRadioButton?.text?.toString()?.trim() ?: ""
                }
                tilName.error = null
                tilPhone.error = null
                tilAddress.error = null
                tilEmail.error = null
                if (name.isEmpty()) {
                    tilName.error = "Будь ласка, введіть своє ім'я"
                    return@setOnClickListener
                }
                if (phone.length == 5) {
                    tilPhone.error = "Будь ласка, введіть свій номер телефону"
                    return@setOnClickListener
                } else if (phone.length < 12) {
                    tilPhone.error = "Будь ласка, введіть свій повний номер телефону"
                    return@setOnClickListener
                }
                if (email.isEmpty()) {
                    tilEmail.error = "Будь ласка, введіть свою електронну адресу"
                    return@setOnClickListener
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(edtEmail.text.toString())
                        .matches()
                ) {
                    tilEmail.error = "Введіть коректну електронну адресу."
                    return@setOnClickListener
                }
                if (address.isEmpty()) {
                    tilAddress.error = "Будь ласка, введіть свою адресу"
                    return@setOnClickListener
                }
                if (rdiCod.isChecked) {
                    paymentCOD(name, phone, email, address)
                }
            } else {
                Toast.makeText(
                    requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
                ).show()
            }
        }
        // Ініціалізація LocationManager
        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Ініціалізація LocationListener
        locationListener = LocationListener { location -> // Отримання розташування користувача
            val latitude = location.latitude
            val longitude = location.longitude
            // Автоматичне заповнення поля адреси з розташуванням користувача
            val geocoder = Geocoder(root.context, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address: Address = addresses[0]
                val addressText = address.getAddressLine(0)
                edtAddress.setText(addressText)
            } else {
                edtAddress.setText("Address not found")
            }
        }
        if (ActivityCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Якщо дозвіл на доступ наданий, отримання останнього відомого розташування і починаємо прослуховування змін розташування
            val lastKnownLocationGPS =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastKnownLocationNetwork =
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val lastKnownLocation = lastKnownLocationGPS ?: lastKnownLocationNetwork
            if (lastKnownLocation != null) {
                val latitude = lastKnownLocation.latitude
                val longitude = lastKnownLocation.longitude
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address: Address = addresses[0]
                    val addressText = address.getAddressLine(0)
                    edtAddress.setText(addressText)
                } else {
                    edtAddress.setText("Address not found")
                }
            }
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                Common.MIN_TIME_BETWEEN_UPDATES,
                Common.MIN_DISTANCE_CHANGE_FOR_UPDATES,
                locationListener
            )
        }
    }

    private fun paymentCOD(name: String, phone: String, email: String, address: String) {
        compositeDisposable.add(
            cart.getAllCart(Common.currentUser!!.uid!!).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ cartItem ->
                    //Коли є всі cartItems, отримання загальної вартості
                    cart.sumPrice(Common.currentUser!!.uid!!).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : SingleObserver<Double> {
                            override fun onSubscribe(d: Disposable) {
                            }

                            override fun onError(e: Throwable) {
                                if (!e.message!!.contains("Query returned empty")) {
                                    Toast.makeText(
                                        requireContext(), "" + e.message, Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onSuccess(t: Double) {
                                val order = OrderModel()
                                order.userId = Common.currentUser!!.uid
                                order.customerName = name
                                order.customerPhone = phone
                                order.customerAddress = address
                                order.customerEmail = email
                                order.cartItems = cartItem
                                order.isDeliveryAddress = rdiHome.isChecked
                                order.isComment = false
                                order.status = Common.STATUSES[0]
                                val regex = Regex("\\d+(?=,\\d{2})")
                                val price = totalPrice.text.toString().replace("\u00A0", "")
                                val finalPrice = regex.find(price)?.value
                                order.totalPrice = finalPrice!!.toDouble()
                                order.isCod = true
                                //Надсилання до бази даних
                                FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                                    .child(Common.createOrderId()).setValue(order)
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            requireContext(), "" + e.message, Toast.LENGTH_SHORT
                                        ).show()
                                    }.addOnCompleteListener { task ->
                                        //Очищення кошика
                                        if (task.isSuccessful) {
                                            cart.cleanCart(Common.currentUser!!.uid!!)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(object : SingleObserver<Int> {
                                                    override fun onSubscribe(d: Disposable) {

                                                    }

                                                    override fun onError(e: Throwable) {
                                                        Toast.makeText(
                                                            requireContext(),
                                                            "" + e.message,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }

                                                    override fun onSuccess(t: Int) {
                                                        Toast.makeText(
                                                            requireContext(),
                                                            "Замовлення розміщено успішно",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        EventBus.getDefault().postSticky(
                                                            MenuClick(true)
                                                        )
                                                    }

                                                })
                                        }
                                    }
                            }

                        })
                }, { throwable ->
                    Toast.makeText(
                        requireContext(), "" + throwable.message, Toast.LENGTH_SHORT
                    ).show()
                })
        )
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == Common.PERMISSIONS_REQUEST_LOCATION) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Дозвіл на доступ наданий
//                if (ActivityCompat.checkSelfPermission(
//                        requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
//                    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                        requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION
//                    ) == PackageManager.PERMISSION_GRANTED
//                ) {
//                    // Отримання останнього відомого розташування і починаємо прослуховування змін розташування
//                    val lastKnownLocation =
//                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
//                    if (lastKnownLocation != null) {
//                        val latitude = lastKnownLocation.latitude
//                        val longitude = lastKnownLocation.longitude
//                        edtAddress.setText("Latitude: $latitude\\nLongitude: $longitude")
//                    }
//                    locationManager.requestLocationUpdates(
//                        LocationManager.GPS_PROVIDER,
//                        Common.MIN_TIME_BETWEEN_UPDATES,
//                        Common.MIN_DISTANCE_CHANGE_FOR_UPDATES,
//                        locationListener
//                    )
//                }
//            } else {
//                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        }
//    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
        //EventBus.getDefault().postSticky(HideFABCart(false))
    }
}