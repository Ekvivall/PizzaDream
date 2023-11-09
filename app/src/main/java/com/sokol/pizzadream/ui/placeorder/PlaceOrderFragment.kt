package com.sokol.pizzadream.ui.placeorder

import android.R.attr.digits
import android.R.attr.phoneNumber
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
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.R


class PlaceOrderFragment : Fragment() {
    private lateinit var edtName: EditText
    private lateinit var tilName: TextInputLayout
    private lateinit var edtPhone: EditText
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
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val placeOrderViewModel = ViewModelProvider(this).get(PlaceOrderViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_place_order, container, false)
        initView(root)
        placeOrderViewModel.getAddressListMutableLiveData().observe(viewLifecycleOwner, Observer {
            for (address in it) {
                val radioButton = RadioButton(context)
                radioButton.setOnCheckedChangeListener { compoundButton, b ->
                    if (b) {
                        Common.userSelectedAddress = address
                    }
                }
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                radioButton.layoutParams = params
                radioButton.text = address
                radioButton.tag = address
                radioGroupAddresses.addView(radioButton)
            }
            if (radioGroupAddresses.childCount > 0) {
                val radioButton = radioGroupAddresses.getChildAt(0) as RadioButton
                radioButton.isChecked = true
            }
        })
        return root
    }

    private fun initView(root: View) {
        edtName = root.findViewById(R.id.edt_name)
        tilName = root.findViewById(R.id.til_name)
        edtPhone = root.findViewById(R.id.edt_phone)
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
        rdiHome.setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                edtAddress.visibility = View.VISIBLE
                radioGroupAddresses.visibility = View.GONE
            } else {
                edtAddress.visibility = View.GONE
                radioGroupAddresses.visibility = View.VISIBLE
            }
        }
        btnOrder.setOnClickListener {
            Toast.makeText(requireContext(), "Implement late!", Toast.LENGTH_SHORT).show()
        }
    }
}