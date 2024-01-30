package com.sokol.pizzadream

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Model.UserModel
import com.sokol.pizzadream.Remote.ICloudFunctions
import com.sokol.pizzadream.Remote.RetrofitCloudClient
import dmax.dialog.SpotsDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.Arrays

class MainActivity : AppCompatActivity() {
    companion object {
        private var LOGIN_REQUEST_CODE = 7171
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private lateinit var dialog: android.app.AlertDialog
    private var compositeDisposable = CompositeDisposable()
    private lateinit var database: FirebaseDatabase
    private lateinit var userInfoRef: DatabaseReference
    private lateinit var providers: List<AuthUI.IdpConfig>
    private lateinit var cloudFunctions: ICloudFunctions
    override fun onStart() {
        super.onStart()
        val fragment: Fragment = SignInFragment()
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.replace(R.id.fragment_place, fragment)
        ft.commit()
        firebaseAuth.addAuthStateListener(listener)
    }

    override fun onResume() {
        super.onResume()
        firebaseAuth.addAuthStateListener(listener)
    }

    override fun onStop() {
        if (firebaseAuth != null && listener != null) {
            firebaseAuth.removeAuthStateListener(listener)
        }
        compositeDisposable.clear()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initFirebase()
    }

    private fun initFirebase() {
        database = FirebaseDatabase.getInstance()
        userInfoRef = database.getReference(Common.USER_REFERENCE)
        firebaseAuth = FirebaseAuth.getInstance()
        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()
        cloudFunctions = RetrofitCloudClient.getInstance().create(ICloudFunctions::class.java)
        providers = Arrays.asList(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        dialog.show()
        listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {/*val model = UserModel()
                if(user.displayName.toString() != ""){
                    model.firstName = user.displayName.toString()
                    model.email = user.email.toString()
                    userInfoRef.child(user.uid).setValue(model)
                }*/
                userInfoRef.child(user.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            Toast.makeText(this@MainActivity, "" + p0.message, Toast.LENGTH_SHORT)
                                .show()
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if (!p0.exists()) {
                                val model = UserModel()
                                model.uid = user.uid
                                model.firstName = user.displayName.toString()
                                model.email = user.email.toString()
                                model.role = "user"
                                userInfoRef.child(user.uid).setValue(model)
                                    /*.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            *//*compositeDisposable.add(
                                                cloudFunctions.getToken()
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe({ braintreeToken ->
                                                        Common.currentToken = braintreeToken.token
                                                    }, { throwable ->
                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            throwable.message,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    })
                                            )*//*
                                        }
                                    }*/
                                Common.currentUser = model
                            } else {
                                val model = p0.getValue(UserModel::class.java)
                                Common.currentUser = model
                            }
                        }
                    })
                compositeDisposable.add(
                    cloudFunctions.getToken().subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe({ braintreeToken ->
                            Common.currentToken = braintreeToken.token
                        }, { throwable ->
                            Toast.makeText(
                                this, throwable.message, Toast.LENGTH_SHORT
                            ).show()
                        })
                )
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                dialog.dismiss()
            }
        }
    }


    fun showLoginLayout(view: View) {
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers)
                .setIsSmartLockEnabled(false).build(), LOGIN_REQUEST_CODE
        )
    }

    fun changeInfo(view: View) {
        var fragment: Fragment? = null
        if (view.id == R.id.register) {
            fragment = RegisterFragment()
        } else if (view.id == R.id.sign_in) {
            fragment = SignInFragment()
        }
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.replace(R.id.fragment_place, fragment!!)
        ft.commit()
    }

}