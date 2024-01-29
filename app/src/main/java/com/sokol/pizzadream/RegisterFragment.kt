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
import com.sokol.pizzadream.Remote.ICloudFunctions
import com.sokol.pizzadream.databinding.FragmentRegisterBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userInfoRef: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private var compositeDisposable = CompositeDisposable()
    private lateinit var cloudFunctions: ICloudFunctions
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
            if (Common.isConnectedToInternet(requireContext())) {
                var check = true
                val tilFirstName = binding.tilFirstName
                tilFirstName.error = null
                val edtFirstName = binding.edtFirstName
                val tilLastName = binding.tilLastName
                tilLastName.error = null
                val edtLastName = binding.edtLastName
                val tilEmail = binding.tilEmailReg
                tilEmail.error = null
                val edtEmail = binding.edtEmail
                val tilPassword = binding.tilPasswordReg
                tilPassword.error = null
                val edtPassword = binding.edtPassword
                if (TextUtils.isDigitsOnly(edtFirstName.text.toString())) {
                    tilFirstName.error = "Введіть Ім\'я."
                    check = false
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(edtEmail.text.toString())
                        .matches()
                ) {
                    tilEmail.error = "Введіть коректну електронну адресу."
                    check = false
                }
                val passwordPattern = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=\\S+$).{6,}$".toRegex()
                if (!passwordPattern.matches(edtPassword.text.toString())) {
                    tilPassword.error =
                        "Пароль недостатньо надійний. Введіть принаймні 6 символів, включаючи букви, цифри."
                    check = false
                }
                if (check) {
                    val model = UserModel()
                    model.firstName = edtFirstName.text.toString()
                    model.lastName = edtLastName.text.toString()
                    model.email = edtEmail.text.toString()
                    model.role = "user"
                    firebaseAuth.createUserWithEmailAndPassword(
                        edtEmail.text.toString(), edtPassword.text.toString()
                    ).addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser
                            model.uid = user!!.uid
                            userInfoRef.child(user.uid).setValue(model)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        compositeDisposable.add(
                                            cloudFunctions.getToken().subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe({ braintreeToken ->
                                                    Common.currentToken = braintreeToken.token
                                                }, { throwable ->
                                                    Toast.makeText(
                                                        requireContext(),
                                                        throwable.message,
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                })
                                        )
                                    }
                                }
                        } else {
                            tilEmail.error = "Користувач з такою електронною адресою вже існує."
                        }
                    }

                }
            } else {
                Toast.makeText(
                    requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
        }
    }

    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }
}
