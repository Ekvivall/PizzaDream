package com.sokol.pizzadream.ui.addCommentFood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sokol.pizzadream.Adapter.AddCommentAdapter
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.EventBus.MenuClick
import com.sokol.pizzadream.Model.CommentModel
import com.sokol.pizzadream.Model.FoodModel
import com.sokol.pizzadream.R
import org.greenrobot.eventbus.EventBus
import java.util.Calendar

class CommentFoodFragment : Fragment() {
    private lateinit var addCommentsRecycler: RecyclerView
    private lateinit var btnSend: Button
    private var comment = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val commentFoodViewModel = ViewModelProvider(this).get(CommentFoodViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_add_comment, container, false)
        initView(root)
        if (Common.isConnectedToInternet(requireContext())) {
            commentFoodViewModel.getFoodMutableLiveData().observe(viewLifecycleOwner, Observer {
                val adapter = AddCommentAdapter(it, requireContext())
                addCommentsRecycler.adapter = adapter
            })
        } else {
            Toast.makeText(
                requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
            ).show()
        }
        return root
    }


    private fun initView(root: View) {
        addCommentsRecycler = root.findViewById(R.id.add_comment_recycler)
        addCommentsRecycler.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        addCommentsRecycler.layoutManager = layoutManager
        addCommentsRecycler.addItemDecoration(
            DividerItemDecoration(
                requireContext(), layoutManager.orientation
            )
        )
        btnSend = root.findViewById(R.id.send)
        btnSend.setOnClickListener {
            if (Common.isConnectedToInternet(requireContext())) {
                val comments = mutableListOf<CommentModel>()
                for (i in 0 until addCommentsRecycler.childCount) {
                    val holder =
                        addCommentsRecycler.getChildViewHolder(addCommentsRecycler.getChildAt(i)) as AddCommentAdapter.MyViewHolder
                    holder.tilComment.error = null
                    if (holder.ratingBar.rating == 0f) {
                        holder.tilRatingBar.error = "Будь ласка, поставте оцінку"
                        return@setOnClickListener
                    }
                    val commentModel = CommentModel()
                    commentModel.comment = holder.edtComment.text.toString().trim()
                    commentModel.ratingValue = holder.ratingBar.rating.toInt()
                    commentModel.name = Common.currentUser!!.firstName
                    commentModel.uid = Common.currentUser!!.uid!!
                    commentModel.avatar = Common.currentUser!!.avatar
                    commentModel.foodId = holder.foodId
                    commentModel.categoryId = holder.categoryId
                    commentModel.commentTimeStamp = Calendar.getInstance().timeInMillis
                    comments.add(commentModel)
                }
                for (commentModel in comments) writeReviewToFirebase(commentModel)
            } else {
                Toast.makeText(
                    requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun writeReviewToFirebase(comment: CommentModel) {
        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF).child(comment.foodId).push()
            .setValue(comment).addOnFailureListener { e ->
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
        updateData["comment"] = true
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