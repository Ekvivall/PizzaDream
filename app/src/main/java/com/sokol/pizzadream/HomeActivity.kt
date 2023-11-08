package com.sokol.pizzadream

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Database.CartDatabase
import com.sokol.pizzadream.Database.Repositories.CartInterface
import com.sokol.pizzadream.Database.Repositories.CartRepository
import com.sokol.pizzadream.EventBus.CategoryClick
import com.sokol.pizzadream.EventBus.CountCartEvent
import com.sokol.pizzadream.EventBus.FoodItemClick
import com.sokol.pizzadream.EventBus.HideFABCart
import com.sokol.pizzadream.EventBus.MenuClick
import com.sokol.pizzadream.databinding.ActivityHomeBinding
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    private lateinit var cart: CartInterface
    private lateinit var drawerLayout: DrawerLayout

    override fun onResume() {
        super.onResume()
        countCartItem()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Common.categorySelected = null
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cart = CartRepository(CartDatabase.getInstance(this).getCartDAO())
        setSupportActionBar(binding.appBarHome.toolbar)
        binding.appBarHome.fab.setOnClickListener { view ->
            navController.navigate(R.id.nav_cart)
        }
        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_cart
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        val headerView = navView.getHeaderView(0)
        val txtUser = headerView.findViewById<TextView>(R.id.txt_user)
        val txtEmail = headerView.findViewById<TextView>(R.id.txt_email)
        Common.setWelcomeString("Привіт, ", Common.currentUser?.firstName, txtUser)
        Common.setWelcomeString("Електронна адреса: ", Common.currentUser?.email, txtEmail)
        navView.setNavigationItemSelectedListener { item ->
            item.isChecked = true
            drawerLayout.closeDrawers()
            if (item.itemId == R.id.nav_sign_out) {
                signOut()
            } else if (item.itemId == R.id.nav_home) {
                navController.navigate(R.id.nav_home)
            } else if (item.itemId == R.id.nav_cart) {
                navController.navigate(R.id.nav_cart)
            }
            true
        }
        countCartItem()
    }

    private fun signOut() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Вихід")
            .setMessage("Ви дійсно хочете вийти?")
            .setNegativeButton("Відміна") { dialogInterface, _ -> dialogInterface.dismiss() }
            .setPositiveButton("Так") { dialogInterface, _ ->
                Common.foodSelected = null
                Common.categorySelected = null
                Common.addonCategorySelected = null
                Common.currentUser = null
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@HomeActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        val dialog = builder.create()
        dialog.show()
    }
    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }*/

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onCategorySelected(event: CategoryClick) {
        if (event.isSuccess) {
            findNavController(R.id.nav_host_fragment_content_home).navigate(R.id.nav_foodList)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onFoodSelected(event: FoodItemClick) {
        if (event.isSuccess) {
            findNavController(R.id.nav_host_fragment_content_home).navigate(R.id.nav_foodDetail)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMenu(event: MenuClick) {
        if (event.isSuccess) {
            findNavController(R.id.nav_host_fragment_content_home).navigate(R.id.nav_home)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onCountCartEvent(event: CountCartEvent) {
        if (event.isSuccess) {
            countCartItem()
        }
    }

    private fun countCartItem() {
        cart.countItemInCart(Common.currentUser?.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Int> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onError(e: Throwable) {
                    if (!e.message!!.contains("Query returned empty")) {
                        Toast.makeText(
                            this@HomeActivity,
                            "Помилка при отриманні кількості товарів в кошику " + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        binding.appBarHome.fab.count = 0
                    }
                }

                override fun onSuccess(t: Int) {
                    binding.appBarHome.fab.count = t
                }

            })
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onHideFABCart(event: HideFABCart) {
        if (event.isHide) {
            binding.appBarHome.fab.hide()
        } else {
            binding.appBarHome.fab.show()
        }
    }
}