package com.sokol.pizzadream.ui.comment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sokol.pizzadream.Callback.ICommentLoadCallback
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Model.CommentModel

class CommentViewModel : ViewModel(), ICommentLoadCallback {
    private var commentListMutableLiveData: MutableLiveData<List<CommentModel>>? = null
    private lateinit var messageError: MutableLiveData<String>
    private var commentLoadCallbackListener: ICommentLoadCallback = this
    val comments: LiveData<List<CommentModel>>
        get() {
            if (commentListMutableLiveData == null) {
                commentListMutableLiveData = MutableLiveData()
                messageError = MutableLiveData()
                loadCommentList()
            }
            return commentListMutableLiveData!!
        }

    private fun loadCommentList() {
        val tempList = ArrayList<CommentModel>()
        val newsRef = FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF)
            .child(Common.foodSelected?.id.toString())
        newsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemsSnapshot in snapshot.children) {
                    val model = itemsSnapshot.getValue(CommentModel::class.java)
                    tempList.add(model!!)
                }
                val sortedList = tempList.sortedByDescending { it.commentTimeStamp }
                commentLoadCallbackListener.onCommentLoadSuccess(sortedList)
            }

            override fun onCancelled(error: DatabaseError) {
                commentLoadCallbackListener.onCommentLoadFailed(error.message)
            }

        })
    }

    override fun onCommentLoadSuccess(commentsList: List<CommentModel>) {
        commentListMutableLiveData?.value = commentsList
    }

    override fun onCommentLoadFailed(message: String) {
        messageError.value = message
    }
}