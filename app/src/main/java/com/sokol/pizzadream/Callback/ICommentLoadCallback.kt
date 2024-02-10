package com.sokol.pizzadream.Callback

import com.sokol.pizzadream.Model.CategoryModel
import com.sokol.pizzadream.Model.CommentModel

interface ICommentLoadCallback {
    fun onCommentLoadSuccess(commentsList: List<CommentModel>)
    fun onCommentLoadFailed(message:String)
}