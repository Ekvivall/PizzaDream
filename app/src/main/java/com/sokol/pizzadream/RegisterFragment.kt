package com.sokol.pizzadream

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Model.UserModel
import com.sokol.pizzadream.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userInfoRef: DatabaseReference
    private lateinit var database: FirebaseDatabase
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userInfoRef = database.getReference(Common.USER_REFERENCE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnRegister.setOnClickListener {
            var check = true
            val til_first_name = binding.tilFirstName
            til_first_name.error = null
            val edt_first_name = binding.edtFirstName
            val til_last_name = binding.tilLastName
            til_last_name.error = null
            val edt_last_name = binding.edtLastName
            val til_email = binding.tilEmailReg
            til_email.error = null
            val edt_email = binding.edtEmail
            val til_password = binding.tilPasswordReg
            til_password.error = null
            val edt_password = binding.edtPassword
            if (TextUtils.isDigitsOnly(edt_first_name.text.toString())) {
                til_first_name.error = "Введіть Ім\'я."
                check = false
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(edt_email.text.toString()).matches()) {
                til_email.error = "Введіть коректну електронну адресу."
                check = false
            }
            val passwordPattern = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=\\S+$).{6,}$".toRegex()
            if (!passwordPattern.matches(edt_password.text.toString())) {
                til_password.error =
                    "Пароль недостатньо надійний. Введіть принаймні 6 символів, включаючи букви, цифри."
                check = false
            }
            if (check) {
                val model = UserModel()
                model.firstName = edt_first_name.text.toString()
                model.lastName = edt_last_name.text.toString()
                model.email = edt_email.text.toString()
                firebaseAuth.createUserWithEmailAndPassword(
                    edt_email.text.toString(),
                    edt_password.text.toString()
                ).addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        userInfoRef.child(user!!.uid)
                            .setValue(model)
                    } else {
                        til_email.error = "Користувач з такою електронною адресою вже існує."
                    }
                }

            }
        }
    }
}
