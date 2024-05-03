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
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Model.UserModel
import com.sokol.pizzadream.Remote.ICloudFunctions
import com.sokol.pizzadream.Remote.RetrofitCloudClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {
    companion object {
        private var LOGIN_REQUEST_CODE = 7171
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener
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

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(listener)
        compositeDisposable.clear()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initFirebase()
    }

    override fun onDestroy() {
        super.onDestroy()
        firebaseAuth.removeAuthStateListener(listener)
        compositeDisposable.clear()
    }

    private fun initFirebase() {
        database = FirebaseDatabase.getInstance()
        userInfoRef = database.getReference(Common.USER_REFERENCE)
        firebaseAuth = FirebaseAuth.getInstance()
        cloudFunctions = RetrofitCloudClient.getInstance().create(ICloudFunctions::class.java)
        providers = listOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                var model = UserModel()
                userInfoRef.child(user.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            Toast.makeText(this@MainActivity, "" + p0.message, Toast.LENGTH_SHORT)
                                .show()
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if (!p0.exists()) {
                                model.uid = user.uid
                                model.firstName = user.displayName.toString()
                                model.email = user.email.toString()
                                model.role = "user"
                                userInfoRef.child(user.uid).setValue(model)
                                Common.currentUser = model
                            } else {
                                model = p0.getValue(UserModel::class.java)!!
                                Common.currentUser = model
                                if (model.role == "user") {
                                    user.getIdToken(true).addOnFailureListener { t ->
                                        Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                        .addOnCompleteListener {
                                            Common.authorizeToken = it.result!!.token
                                            val headers = HashMap<String, String>()
                                            headers["Authorization"] = Common.buildToken(Common.authorizeToken!!)
                                            compositeDisposable.add(
                                                cloudFunctions.getToken(headers)
                                                    .subscribeOn(Schedulers.io())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe({ braintreeToken ->
                                                        FirebaseMessaging.getInstance().token.addOnFailureListener { e ->
                                                            Toast.makeText(
                                                                this@MainActivity,
                                                                e.message,
                                                                Toast.LENGTH_SHORT
                                                            )
                                                                .show()
                                                            Common.currentToken = braintreeToken.token
                                                        }.addOnCompleteListener { task ->
                                                            if (task.isSuccessful) {
                                                                Common.currentToken =
                                                                    braintreeToken.token
                                                                Common.updateToken(
                                                                    this@MainActivity,
                                                                    task.result
                                                                )
                                                                val myIntent = Intent(
                                                                    this@MainActivity, HomeActivity::class.java
                                                                )
                                                                var isOpenNewOrder = false
                                                                var isOpenNews = false
                                                                if (intent.extras != null) {
                                                                    isOpenNewOrder =
                                                                        intent.extras!!.getBoolean(Common.IS_OPEN_ACTIVITY_ORDER, false)
                                                                    isOpenNews =
                                                                        intent.extras!!.getBoolean(Common.IS_OPEN_ACTIVITY_NEWS, false)
                                                                }
                                                                myIntent.putExtra(Common.IS_OPEN_ACTIVITY_ORDER, isOpenNewOrder)
                                                                myIntent.putExtra(Common.IS_OPEN_ACTIVITY_NEWS, isOpenNews)
                                                                startActivity(myIntent)
                                                                finish()
                                                            }
                                                        }
                                                    }, { throwable ->
                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            throwable.message,
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    })
                                            )
                                        }
                                } else {
                                    firebaseAuth.signOut()
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Ви ввели неправильні облікові дані клієнта",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        }
                    })
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