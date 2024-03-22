package com.sokol.pizzadream

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Database.PizzaDatabase
import com.sokol.pizzadream.Database.Repositories.CartInterface
import com.sokol.pizzadream.Database.Repositories.CartRepository
import com.sokol.pizzadream.EventBus.CategoryClick
import com.sokol.pizzadream.EventBus.CommentsClick
import com.sokol.pizzadream.EventBus.EditProfileClick
import com.sokol.pizzadream.EventBus.FavoritesClick
import com.sokol.pizzadream.EventBus.FoodItemClick
import com.sokol.pizzadream.EventBus.LogOutClick
import com.sokol.pizzadream.EventBus.MenuClick
import com.sokol.pizzadream.EventBus.NewsClick
import com.sokol.pizzadream.EventBus.NewsItemClick
import com.sokol.pizzadream.EventBus.OrderDetailClick
import com.sokol.pizzadream.EventBus.PlaceOrderClick
import com.sokol.pizzadream.EventBus.ProfileClick
import com.sokol.pizzadream.EventBus.ReviewPizzeriaClick
import com.sokol.pizzadream.EventBus.VacanciesClick
import com.sokol.pizzadream.EventBus.VacancyItemClick
import com.sokol.pizzadream.EventBus.ViewAddCommentClick
import com.sokol.pizzadream.EventBus.ViewOrdersClick
import com.sokol.pizzadream.databinding.ActivityHomeBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding
    private lateinit var cart: CartInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Common.categorySelected = null
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cart = CartRepository(PizzaDatabase.getInstance(this).getCartDAO())
        val navView: BottomNavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_pizzerias, R.id.nav_cart, R.id.nav_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        checkOpenOrderFragment()
    }
    private fun checkOpenOrderFragment() {
        val  isOpenNewOrder = intent.extras!!.getBoolean(Common.IS_OPEN_ACTIVITY_ORDER, false)
        if(isOpenNewOrder){
            navController.popBackStack();
            navController.navigate(R.id.nav_view_orders)
        }
    }
     private fun signOut() {
         val builder = androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomAlertDialog)
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
         val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
         positiveButton.setTextColor(ContextCompat.getColor(this, R.color.red))
         val negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
         negativeButton.setTextColor(ContextCompat.getColor(this, R.color.black))
     }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
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
            findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.nav_foodList)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onFoodSelected(event: FoodItemClick) {
        if (event.isSuccess) {
            findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.nav_foodDetail)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMenu(event: MenuClick) {
        if (event.isSuccess) {
            findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.nav_home)
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onPlaceOrder(event: PlaceOrderClick) {
        if (event.isSuccess) {
            findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.nav_place_order)
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEditProfile(event: EditProfileClick) {
        if (event.isSuccess) {
            findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.nav_edit_profile)
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onProfile(event: ProfileClick) {
        if (event.isSuccess) {
            findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.nav_profile)
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onLogOut(event: LogOutClick) {
        if (event.isSuccess) {
            signOut()
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onNews(event: NewsClick) {
        if (event.isSuccess) {
            findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.nav_news)
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onNewsSelected(event: NewsItemClick) {
        if (event.isSuccess) {
            findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.nav_news_detail)
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onFavorites(event: FavoritesClick) {
        if (event.isSuccess) {
            findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.nav_favorites)
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onVacancies(event: VacanciesClick) {
        if (event.isSuccess) {
            findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.nav_vacancies)
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onVacancySelected(event: VacancyItemClick) {
        if (event.isSuccess) {
            findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.nav_vacancy_detail)
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onReviewPizzeria(event: ReviewPizzeriaClick) {
        if (event.isSuccess) {
            findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.nav_add_review_pizzeria)
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onViewOrders(event: ViewOrdersClick) {
        if (event.isSuccess) {
            findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.nav_view_orders)
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onAddComment(event: ViewAddCommentClick) {
        if (event.isSuccess) {
            findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.nav_add_comment_food)
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onOrderSelected(event: OrderDetailClick) {
        if (event.isSuccess) {
            findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.nav_order_detail)
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onComments(event: CommentsClick) {
        if (event.isSuccess) {
            findNavController(R.id.nav_host_fragment_activity_main).navigate(R.id.nav_comments)
        }
    }
}