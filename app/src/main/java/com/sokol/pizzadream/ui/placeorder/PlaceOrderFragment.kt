package com.sokol.pizzadream.ui.placeorder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.dropin.DropInResult
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Database.Entities.CartItemDB
import com.sokol.pizzadream.Database.PizzaDatabase
import com.sokol.pizzadream.Database.Repositories.CartInterface
import com.sokol.pizzadream.Database.Repositories.CartRepository
import com.sokol.pizzadream.EventBus.MenuClick
import com.sokol.pizzadream.Model.AddonModel
import com.sokol.pizzadream.Model.CartItem
import com.sokol.pizzadream.Model.FCMSendData
import com.sokol.pizzadream.Model.FoodModel
import com.sokol.pizzadream.Model.OrderModel
import com.sokol.pizzadream.R
import com.sokol.pizzadream.Remote.ICloudFunctions
import com.sokol.pizzadream.Remote.IFCMService
import com.sokol.pizzadream.Remote.RetrofitCloudClient
import com.sokol.pizzadream.Remote.RetrofitFCMClient
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class PlaceOrderFragment : Fragment() {
    private val REQUEST_BRAINTREE_CODE: Int = 8888
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
    lateinit var cloudFunctions: ICloudFunctions
    private var name = ""
    private var phone = ""
    private var address = ""
    private var email = ""
    private lateinit var rdiAsSoon: RadioButton
    private lateinit var rdiForTime: RadioButton
    private lateinit var titleAsSoon: TextView
    private lateinit var orderSpinners: LinearLayout
    private lateinit var dateSpinner: Spinner
    private lateinit var timeSpinner: Spinner
    private val timeFormat = SimpleDateFormat("HH:mm")
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy")
    private var calendar = Calendar.getInstance()
    private lateinit var ifcmService: IFCMService
    private lateinit var cbUsePoints: CheckBox
    private lateinit var availablePoints: TextView
    private var startPrice: Double = 0.0
    private var finalPrice: Double = 0.0
    private var pointsToUse: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        cart = CartRepository(PizzaDatabase.getInstance(requireContext()).getCartDAO())
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
        cloudFunctions = RetrofitCloudClient.getInstance().create(ICloudFunctions::class.java)
        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService::class.java)
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
        rdiAsSoon = root.findViewById(R.id.rdi_as_soon)
        rdiForTime = root.findViewById(R.id.rdi_for_time)
        titleAsSoon = root.findViewById(R.id.title_as_soon)
        orderSpinners = root.findViewById(R.id.order_spinners)
        dateSpinner = root.findViewById(R.id.date_spinner)
        timeSpinner = root.findViewById(R.id.time_spinner)
        calendar = addTime(rdiHome.isChecked)
        val formattedTime = checkTime(calendar)
        titleAsSoon.text = StringBuilder("Зверніть увагу! Доставка до ").append(formattedTime)
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
        val regex = Regex("\\d+(?=,\\d{2})")
        val price = Common.totalPrice.replace("\u00A0", "")
        startPrice = regex.find(price)?.value!!.toDouble()
        cbUsePoints = root.findViewById(R.id.cb_use_points)
        availablePoints = root.findViewById(R.id.tv_available_points)
        if (Common.currentUser!!.points == 0) {
            cbUsePoints.visibility = View.GONE
            availablePoints.visibility = View.GONE
        } else {
            cbUsePoints.visibility = View.VISIBLE
            availablePoints.visibility = View.VISIBLE
        }
        updateTotalPrice(isDelivery = true)
        rdiHome.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                edtAddress.visibility = View.VISIBLE
                radioGroupAddresses.visibility = View.GONE
            } else {
                edtAddress.visibility = View.GONE
                radioGroupAddresses.visibility = View.VISIBLE
            }
            updateTotalPrice(isChecked, cbUsePoints.isChecked)
            updateTime(rdiAsSoon.isChecked)
        }
        rdiAsSoon.setOnCheckedChangeListener { _, isChecked ->
            updateTime(isChecked)
        }
        cbUsePoints.setOnCheckedChangeListener { _, isChecked ->
            updateTotalPrice(rdiHome.isChecked, isChecked)
        }
        btnOrder.setOnClickListener {
            if (Common.isConnectedToInternet(requireContext())) {
                // Обробка кнопки замовлення
                name = edtName.text.toString().trim()
                phone = "+380 " + edtPhone.text.toString().trim()
                email = edtEmail.text.toString().trim()
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
                if (rdiForTime.isChecked && timeSpinner.selectedItemPosition == -1) {
                    Toast.makeText(requireContext(), "Оберіть час доставки", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
                if (name.isEmpty()) {
                    tilName.error = "Будь ласка, введіть своє ім'я"
                    return@setOnClickListener
                }
                if (phone.length == 5) {
                    tilPhone.error = "Будь ласка, введіть свій номер телефону"
                    return@setOnClickListener
                } else if (phone.length < 17) {
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
                } else {
                    if (Common.currentToken.isNotEmpty()) {
                        val dropInRequest = DropInRequest().clientToken(Common.currentToken)
                        startActivityForResult(
                            dropInRequest.getIntent(context), REQUEST_BRAINTREE_CODE
                        )
                    }
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

    private fun updateTotalPrice(isDelivery: Boolean = false, usePoints: Boolean = false) {
        val priceWithDelivery = if (isDelivery) startPrice + 60 else startPrice
        pointsToUse = minOf(Common.currentUser!!.points, (priceWithDelivery - 1).toInt())
        finalPrice = if (usePoints) {
            // Використання балів
            priceWithDelivery - pointsToUse
        } else {
            priceWithDelivery
        }
        availablePoints.text = StringBuilder(pointsToUse.toString()).append(" Балів")
        totalPrice.text =
            StringBuilder("Всього: ").append(Common.formatPrice(finalPrice)).toString()
    }

    private fun checkTime(calendar: Calendar): String {

        val currentTime = calendar.get(Calendar.HOUR_OF_DAY)
        if (currentTime < 10) {
            calendar.set(Calendar.HOUR_OF_DAY, 10)
            calendar.set(Calendar.MINUTE, 30)
        } else if (currentTime > 19) {
            calendar.add(Calendar.DATE, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 10)
            calendar.set(Calendar.MINUTE, 30)
        }
        return timeFormat.format(calendar.time)
    }

    private fun updateTime(isChecked: Boolean) {
        if (isChecked) {
            calendar = addTime(rdiHome.isChecked)
            orderSpinners.visibility = View.GONE
            titleAsSoon.visibility = View.VISIBLE
            val formattedTime = checkTime(calendar)
            titleAsSoon.text =
                StringBuilder(if (rdiHome.isChecked) "Зверніть увагу! Доставка до " else "Зверніть увагу! Можна отримати о ").append(
                    formattedTime
                )
        } else {
            calendar = addTime(rdiHome.isChecked)
            titleAsSoon.visibility = View.GONE
            orderSpinners.visibility = View.VISIBLE
            val dates = mutableListOf<String>()
            for (i in 0..4) {
                dates.add(dateFormat.format(calendar.time))
                calendar.add(Calendar.DATE, 1)
            }
            val adapterDate = ArrayAdapter(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, dates
            )
            adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            dateSpinner.adapter = adapterDate
            dateSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    calendar = addTime(rdiHome.isChecked)
                    val times = mutableListOf<String>()
                    val currentTime = calendar.get(Calendar.HOUR_OF_DAY)
                    var startTime = if (position == 0 && currentTime > 10) currentTime else 10
                    var currentMinute = 20
                    if (position == 0) {
                        currentMinute = calendar.get(Calendar.MINUTE)
                        // Округлення хвилин до найближчого кратного числа 10
                        currentMinute = (Math.ceil(currentMinute / 10.0) * 10).toInt()
                        // Якщо поточні хвилини більше 50, перехід до наступної години
                        if (currentMinute >= 60) {
                            startTime++
                            currentMinute = 0
                        }
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, startTime)
                    calendar.set(Calendar.MINUTE, currentMinute)
                    while (calendar.get(Calendar.HOUR_OF_DAY) < 20) {
                        times.add(timeFormat.format(calendar.time))
                        calendar.add(Calendar.MINUTE, 10)
                    }

                    val adapterTime = ArrayAdapter(
                        requireContext(), android.R.layout.simple_spinner_dropdown_item, times
                    )
                    adapterTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    timeSpinner.adapter = adapterTime
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }

        }
    }

    private fun addTime(checked: Boolean): Calendar {
        val calendar = Calendar.getInstance()
        if (checked) {
            calendar.add(Calendar.MINUTE, 30)
        } else {
            calendar.add(Calendar.MINUTE, 10)
        }
        return calendar
    }

    private fun paymentCOD(name: String, phone: String, email: String, address: String) {
        compositeDisposable.add(
            cart.getAllCart(Common.currentUser!!.uid!!).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ cartItems ->
                    val tempList = ArrayList<CartItem>()
                    var counter = 0
                    for (item in cartItems) {
                        val foodRef =
                            FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF)
                                .child(item.categoryId).child("foods").child(item.foodId)
                        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val model = snapshot.getValue(FoodModel::class.java)
                                if (model != null) {
                                    val cartItem = createCartItem(model, item)
                                    tempList.add(cartItem)
                                    counter++
                                    if (counter == cartItems.size) {
                                        val order = OrderModel()
                                        order.userId = Common.currentUser!!.uid
                                        order.customerName = name
                                        order.customerPhone = phone
                                        order.customerAddress = address
                                        order.customerEmail = email
                                        order.cartItems = tempList
                                        order.isDeliveryAddress = rdiHome.isChecked
                                        order.status = Common.STATUSES[0]
                                        order.totalPrice = finalPrice
                                        order.transactionId = "Оплата при отриманні"
                                        order.orderedTime = Calendar.getInstance().timeInMillis
                                        if (rdiForTime.isChecked) {
                                            order.forTime =
                                                StringBuilder().append(dateSpinner.selectedItem)
                                                    .append(" ").append(timeSpinner.selectedItem)
                                                    .toString()
                                        }
                                        writeOrderToFirebase(order)
                                    }
                                } else {
                                    cart.deleteCart(item).subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread()).subscribe()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }

                        })
                    }
                }, { throwable ->
                    Toast.makeText(
                        requireContext(), "" + throwable.message, Toast.LENGTH_SHORT
                    ).show()
                })
        )
    }

    private fun writeOrderToFirebase(order: OrderModel) {
        //Надсилання до бази даних
        val orderId = Common.createOrderId()
        order.orderId = orderId
        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF).child(orderId).setValue(order)
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(), "" + e.message, Toast.LENGTH_SHORT
                ).show()
            }.addOnCompleteListener { task ->
                //Очищення кошика
                if (task.isSuccessful) {
                    // Списання балів, якщо виділений чекбокс
                    if (cbUsePoints.isChecked) {
                        Common.currentUser!!.points = Common.currentUser!!.points - pointsToUse
                        updateUserPoints(Common.currentUser!!.uid!!, Common.currentUser!!.points)
                    }
                    cart.cleanCart(Common.currentUser!!.uid!!).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : SingleObserver<Int> {
                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun onError(e: Throwable) {
                                Toast.makeText(
                                    requireContext(), "" + e.message, Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onSuccess(t: Int) {
                                compositeDisposable.clear()
                                val dataSend = HashMap<String, String>()
                                dataSend[Common.NOTIFICATION_TITLE] = "Нове замовлення"
                                dataSend[Common.NOTIFICATION_CONTENT] =
                                    "У Вас нове замовлення $orderId."
                                val sendData = FCMSendData(Common.getNewOrderTopic(), dataSend)
                                compositeDisposable.add(ifcmService.sendNotification(sendData)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread()).subscribe {
                                        Toast.makeText(
                                            requireContext(),
                                            "Замовлення розміщено успішно",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        EventBus.getDefault().postSticky(
                                            MenuClick(true)
                                        )
                                    })
                            }

                        })
                }
            }

    }

    private fun updateUserPoints(userId: String, points: Int) {
        FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCE).child(userId)
            .child("points").setValue(points)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_BRAINTREE_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val result =
                    data.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT)
                val nonce = result!!.paymentMethodNonce
                compositeDisposable.add(
                    cart.getAllCart(Common.currentUser!!.uid!!).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe({ cartItems ->
                            val tempList = ArrayList<CartItem>()
                            var counter = 0
                            for (item in cartItems) {
                                val foodRef =
                                    FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF)
                                        .child(item.categoryId).child("foods").child(item.foodId)
                                foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val model = snapshot.getValue(FoodModel::class.java)
                                        if (model != null) {
                                            val cartItem = createCartItem(model, item)
                                            tempList.add(cartItem)
                                            counter++
                                            if (counter == cartItems.size) {
                                                val headers = HashMap<String, String>()
                                                headers.put(
                                                    "Authorization",
                                                    Common.buildToken(Common.authorizeToken!!)
                                                )
                                                // Після того, як виберемо всі товари в кошику, надішлемо платіж
                                                compositeDisposable.add(
                                                    cloudFunctions.submitPayment(
                                                        headers, finalPrice, nonce!!.nonce
                                                    ).subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe({ braintreeTransaction ->
                                                            if (braintreeTransaction.success) {
                                                                // Створення замовлення
                                                                val order = OrderModel()
                                                                order.userId =
                                                                    Common.currentUser!!.uid
                                                                order.customerName = name
                                                                order.customerPhone = phone
                                                                order.customerAddress = address
                                                                order.customerEmail = email
                                                                order.cartItems = tempList
                                                                order.isDeliveryAddress =
                                                                    rdiHome.isChecked
                                                                order.status = Common.STATUSES[0]
                                                                order.totalPrice = finalPrice
                                                                order.transactionId =
                                                                    braintreeTransaction.transaction!!.id
                                                                order.orderedTime =
                                                                    Calendar.getInstance().timeInMillis
                                                                if (rdiForTime.isChecked) {
                                                                    order.forTime =
                                                                        StringBuilder().append(
                                                                            dateSpinner.selectedItem
                                                                        ).append(" ")
                                                                            .append(timeSpinner.selectedItem)
                                                                            .toString()
                                                                }
                                                                writeOrderToFirebase(order)
                                                            }
                                                        }, { err: Throwable ->
                                                            Log.e("Err", err.message.toString())
                                                            Toast.makeText(
                                                                requireContext(),
                                                                err.message,
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        })
                                                )
                                            }
                                        } else {
                                            cart.deleteCart(item).subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe()
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                    }

                                })
                            }
                        }, { terr: Throwable ->
                            Toast.makeText(requireContext(), terr.message, Toast.LENGTH_SHORT)
                                .show()

                        })
                )
            }
        }
    }

    private fun createCartItem(model: FoodModel, item: CartItemDB): CartItem {
        val cartItem = CartItem()
        cartItem.foodId = model.id.toString()
        cartItem.foodName = model.name
        cartItem.foodImage = model.image
        var totalPrice = model.size.find { it.name == item.foodSize }!!.price.toDouble()
        if (item.foodAddon.isNotEmpty()) {
            val addonModels: List<AddonModel> = Gson().fromJson(
                item.foodAddon, object : TypeToken<List<AddonModel>>() {}.type
            )
            totalPrice += addonModels.sumOf { x -> x.price.toDouble() * x.userCount }
        }
        cartItem.foodPrice = totalPrice
        cartItem.foodQuantity = item.foodQuantity
        cartItem.foodAddon = item.foodAddon
        cartItem.foodSize = item.foodSize
        cartItem.categoryId = model.categoryId.toString()
        cartItem.userEmail = item.userEmail
        cartItem.uid = item.uid
        cartItem.id = item.id
        cartItem.createdUserId = model.createdUserId
        cartItem.createdUserName = model.createdUserName
        return cartItem
    }
}