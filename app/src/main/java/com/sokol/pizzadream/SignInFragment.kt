package com.sokol.pizzadream

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.sokol.pizzadream.databinding.FragmentSignInBinding

class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater)
        firebaseAuth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSignIn.setOnClickListener {
            var check = true
            val tilEmail = binding.tilEmail
            tilEmail.error = null
            val edtEmail = binding.edtEmail
            val tilPassword = binding.tilPassword
            tilPassword.error = null
            val edtPassword = binding.edtPassword
            if (TextUtils.isDigitsOnly(edtEmail.text.toString())) {
                tilEmail.error = "Введіть Електронну адресу."
                check = false
            }
            if (TextUtils.isDigitsOnly(edtPassword.text.toString())) {
                tilPassword.error = "Введіть Пароль."
                check = false
            }
            if (check) {
                firebaseAuth.signInWithEmailAndPassword(
                    edtEmail.text.toString(),
                    edtPassword.text.toString()
                )
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                        } else {
                            tilEmail.error = "Введено неправильну Електронну адресу та/або пароль."
                        }
                    }
            }
        }
        binding.tvForgotPassword.setOnClickListener {
            val builder = AlertDialog.Builder(view.context)
            builder.setTitle("СКИНУТИ ПАРОЛЬ")
            builder.setMessage("Ми надішлемо Вам посилання для відновлення паролю на Вашу електронну пошту")
            val itemView = LayoutInflater.from(view.context)
                .inflate(R.layout.layout_forgot_password, null)
            val til_email = itemView.findViewById<TextInputLayout>(R.id.til_email)
            til_email.error = null
            val edt_email = itemView.findViewById<TextInputEditText>(R.id.edt_email)
            builder.setView(itemView)
            val dialog = builder.create()
            itemView.findViewById<Button>(R.id.sendEmailBtn).setOnClickListener {
                if (TextUtils.isDigitsOnly(edt_email.text.toString())) {
                    til_email.error = "Введіть Електронну адресу."
                } else {
                    firebaseAuth
                        .sendPasswordResetEmail(edt_email.text.toString())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                dialog.dismiss()
                                Toast.makeText(
                                    view.context,
                                    "Посилання для скидання паролю було надіслано на Вашу електронну пошту.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                til_email.error =
                                    "Користувача з такою електронною адресою не існує."
                            }
                        }
                }
            }
            itemView.findViewById<Button>(R.id.dismissBtn).setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }
    }
}