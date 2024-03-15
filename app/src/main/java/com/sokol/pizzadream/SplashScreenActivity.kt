package com.sokol.pizzadream

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
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

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener

    //private lateinit var dialog: android.app.AlertDialog
    private lateinit var database: FirebaseDatabase
    private lateinit var userInfoRef: DatabaseReference
    private var compositeDisposable = CompositeDisposable()
    private lateinit var cloudFunctions: ICloudFunctions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFirebase()
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(listener)
    }


    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(listener)
        compositeDisposable.clear()
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
        listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            // Користувач вже увійшов, перенаправити на HomeActivity
            if (user != null) {
                userInfoRef.child(user.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                            Toast.makeText(
                                this@SplashScreenActivity, "" + p0.message, Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            val model = p0.getValue(UserModel::class.java)
                            if (model!!.role == "user") {
                                goToHome(model)
                            }
                            else{
                                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                                finish()
                            }
                        }
                    })
            } else {
                // Користувач ще не увійшов, перенаправляємо на MainActivity
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun goToHome(model: UserModel?) {
        FirebaseAuth.getInstance().currentUser!!.getIdToken(true).addOnFailureListener { t ->
            Toast.makeText(this, t.message, Toast.LENGTH_SHORT).show()
        }.addOnCompleteListener {
            Common.authorizeToken = it.result!!.token
            val headers = HashMap<String, String>()
            headers.put("Authorization", Common.buildToken(Common.authorizeToken!!))
            compositeDisposable.add(
                cloudFunctions.getToken(headers).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe({ braintreeToken ->
                        FirebaseMessaging.getInstance().token.addOnFailureListener { e ->
                            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                            Common.currentToken = braintreeToken.token
                        }.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Common.currentUser = model
                                Common.currentToken = braintreeToken.token
                                Common.updateToken(this@SplashScreenActivity, task.result)
                                startActivity(
                                    Intent(
                                        this@SplashScreenActivity,
                                        HomeActivity::class.java
                                    )
                                )
                                finish()
                            }
                        }
                    }, { throwable ->
                        Toast.makeText(
                            this, throwable.message, Toast.LENGTH_SHORT
                        ).show()
                    })
            )
        }
    }
}