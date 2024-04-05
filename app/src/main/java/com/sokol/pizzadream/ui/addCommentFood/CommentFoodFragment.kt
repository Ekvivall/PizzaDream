package com.sokol.pizzadream.ui.addCommentFood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Database.Entities.CartItemDB
import com.sokol.pizzadream.EventBus.MenuClick
import com.sokol.pizzadream.Model.AddonModel
import com.sokol.pizzadream.Model.CartItem
import com.sokol.pizzadream.Model.CommentModel
import com.sokol.pizzadream.Model.FoodModel
import com.sokol.pizzadream.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.util.Calendar

class CommentFoodFragment : Fragment() {
    //private lateinit var addCommentsRecycler: RecyclerView
    private lateinit var foodImgLayout: ConstraintLayout
    private lateinit var foodName: TextView
    private lateinit var foodSize: TextView
    private lateinit var foodAddon: TextView
    private lateinit var foodPrice: TextView
    private lateinit var foodAddonTitle: TextView
    private lateinit var foodQuantity: TextView
    private lateinit var tilComment: TextInputLayout
    private lateinit var edtComment: EditText
    private lateinit var ratingBar: RatingBar
    private lateinit var tilRatingBar: TextInputLayout
    private lateinit var btnSend: Button
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val commentFoodViewModel = ViewModelProvider(this).get(CommentFoodViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_add_comment, container, false)
        initView(root)
        if (Common.isConnectedToInternet(requireContext())) {
            commentFoodViewModel.getFoodMutableLiveData().observe(viewLifecycleOwner, Observer {
                displayInfo(it)
            })
        } else {
            Toast.makeText(
                requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
            ).show()
        }
        return root
    }

    private fun displayInfo(it: CartItem) {
        foodImgLayout.removeAllViews()
        val foodImg = ImageView(context)
        foodImg.id = View.generateViewId()
        foodImg.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        Glide.with(requireContext()).load(it.foodImage).into(foodImg)
        foodImgLayout.addView(foodImg)
        foodName.text = it.foodName.toString()
        foodSize.text = it.foodSize
        val typeToken = object : TypeToken<List<AddonModel>>() {}.type
        val foodAddons = Gson().fromJson<List<AddonModel>>(it.foodAddon, typeToken)
        var res = ""
        if (foodAddons != null) {
            for (foodAddon in foodAddons) {
                if (foodAddon != foodAddons[0]) res += ", "
                res += foodAddon.name + " x" + foodAddon.userCount
                if (it.foodName.toString().contains("Конструктор")) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val foodImg = ImageView(context)
                        foodImg.id = View.generateViewId()
                        foodImg.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        val imageBitmap = withContext(Dispatchers.IO) {
                            Glide.with(requireContext()).asBitmap().load(foodAddon.imageFill)
                                .submit().get()
                        }
                        foodImg.setImageBitmap(imageBitmap)
                        foodImgLayout.addView(foodImg)
                    }
                }
            }
        }
        foodAddon.text = res
        if (it.foodAddon == "") {
            foodAddonTitle.visibility = View.GONE
        } else {
            foodAddonTitle.visibility = View.VISIBLE
        }
        foodQuantity.text = StringBuilder(it.foodQuantity.toString()).append(" шт.")
        foodPrice.text =
            StringBuilder("").append(Common.formatPrice(it.foodPrice * it.foodQuantity)).toString()
    }


    private fun initView(root: View) {
        foodImgLayout = root.findViewById(R.id.food_img_layout)
        foodName = root.findViewById(R.id.txt_food_name)
        foodSize = root.findViewById(R.id.txt_food_size)
        foodAddon = root.findViewById(R.id.txt_food_addon)
        foodPrice = root.findViewById(R.id.txt_food_price)
        foodAddonTitle = root.findViewById(R.id.txt_food_addon_title)
        foodQuantity = root.findViewById(R.id.txt_food_quantity)
        tilComment = root.findViewById(R.id.til_comment)
        edtComment = root.findViewById(R.id.edt_comment)
        ratingBar = root.findViewById(R.id.rating_bar)
        tilRatingBar = root.findViewById(R.id.til_rating_bar)
        btnSend = root.findViewById(R.id.send)
        btnSend.setOnClickListener {
            if (Common.isConnectedToInternet(requireContext())) {
                if (ratingBar.rating == 0f) {
                    tilRatingBar.error = "Будь ласка, поставте оцінку"
                    return@setOnClickListener
                }
                val commentModel = CommentModel()
                commentModel.comment = edtComment.text.toString().trim()
                commentModel.ratingValue = ratingBar.rating.toInt()
                commentModel.name = Common.currentUser!!.firstName
                commentModel.uid = Common.currentUser!!.uid!!
                commentModel.avatar = Common.currentUser!!.avatar
                commentModel.foodId = Common.cartItemSelected!!.foodId
                commentModel.categoryId = Common.cartItemSelected!!.categoryId
                commentModel.commentTimeStamp = Calendar.getInstance().timeInMillis
                writeReviewToFirebase(commentModel)
            } else {
                Toast.makeText(
                    requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun writeReviewToFirebase(comment: CommentModel) {
        val commentRef =
            FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF).child(comment.foodId)
        comment.id = commentRef.push().key.toString()
        commentRef.child(comment.id).setValue(comment).addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(), "" + e.message, Toast.LENGTH_SHORT
                ).show()
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    addRatingToFood(comment)
                }
            }
    }

    private fun addRatingToFood(comment: CommentModel) {
        val updateData = HashMap<String, Any>()
        val cartItems = Common.orderSelected?.cartItems!!
        cartItems.forEach { cartItem ->
            if (cartItem.id == Common.cartItemSelected?.id) {
                cartItem.isComment = true
            }
        }
        updateData["cartItems"] = cartItems
        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
            .child(Common.orderSelected?.orderId!!).updateChildren(updateData)
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }.addOnSuccessListener {
                FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF)
                    .child(comment.categoryId).child("foods").child(comment.foodId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val foodModel = snapshot.getValue(FoodModel::class.java)
                            val ratingSum = foodModel?.ratingSum!! + comment.ratingValue
                            val ratingCount = foodModel.ratingCount + 1
                            val updateDataRating = HashMap<String, Any>()
                            updateDataRating["ratingSum"] = ratingSum
                            updateDataRating["ratingCount"] = ratingCount
                            foodModel.ratingCount = ratingCount
                            foodModel.ratingSum = ratingSum
                            snapshot.ref.updateChildren(updateDataRating).addOnCompleteListener {
                                EventBus.getDefault().postSticky(
                                    MenuClick(true)
                                )
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT)
                                .show()
                        }

                    })
            }
    }

}