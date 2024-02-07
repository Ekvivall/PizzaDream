package com.sokol.pizzadream.ui.reviewPizzeria

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.EventBus.MenuClick
import com.sokol.pizzadream.Model.ReviewModel
import com.sokol.pizzadream.R
import org.greenrobot.eventbus.EventBus
import java.util.Calendar

class ReviewPizzeriaFragment : Fragment() {
    private lateinit var tilComment: TextInputLayout
    private lateinit var edtComment: EditText
    private lateinit var spnRestaurant: Spinner
    private lateinit var btnSend: Button
    private var selectedRestaurantId: String = ""
    private var comment = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val reviewPizzeriaViewModel =
            ViewModelProvider(this).get(ReviewPizzeriaViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_add_review_pizzeria, container, false)
        initView(root)
        if (Common.isConnectedToInternet(requireContext())) {
            reviewPizzeriaViewModel.getAddressListMutableLiveData()
                .observe(viewLifecycleOwner, Observer {
                    val addressNames = Array(it.size) { i -> it[i].name }
                    val adapter = ArrayAdapter(
                        requireContext(), R.layout.spinner_item, addressNames
                    )
                    adapter.setDropDownViewResource(R.layout.spinner_item)
                    spnRestaurant.adapter = adapter
                    spnRestaurant.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?, view: View?, position: Int, id: Long
                            ) {
                                selectedRestaurantId = it[position].id
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                            }
                        }
                })
        } else {
            Toast.makeText(
                requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
            ).show()
        }
        return root
    }

    private fun initView(root: View) {
        spnRestaurant = root.findViewById(R.id.restaurant_spinner)
        tilComment = root.findViewById(R.id.til_comment)
        edtComment = root.findViewById(R.id.edt_comment)
        btnSend = root.findViewById(R.id.send)
        btnSend.setOnClickListener {
            if (Common.isConnectedToInternet(requireContext())) {
                comment = edtComment.text.toString().trim()
                tilComment.error = null
                if (comment.isEmpty()) {
                    tilComment.error = "Будь ласка, введіть свій відгук"
                    return@setOnClickListener
                } else {
                    val review = ReviewModel()
                    review.comment = comment
                    review.name = Common.currentUser!!.firstName
                    review.uid = Common.currentUser!!.uid!!
                    review.commentTimeStamp = Calendar.getInstance().timeInMillis
                    writeReviewToFirebase(review)
                }
            } else {
                Toast.makeText(
                    requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun writeReviewToFirebase(review: ReviewModel) {
        FirebaseDatabase.getInstance().getReference(Common.REVIEW_REF).child(selectedRestaurantId)
            .push()
            .setValue(review).addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(), "" + e.message, Toast.LENGTH_SHORT
                ).show()
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        requireContext(), "Ваш відгук успішно відправлено", Toast.LENGTH_SHORT
                    ).show()
                    EventBus.getDefault().postSticky(
                        MenuClick(true)
                    )
                }
            }
    }
}