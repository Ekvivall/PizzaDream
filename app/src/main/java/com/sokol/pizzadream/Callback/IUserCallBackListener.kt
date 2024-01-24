package com.sokol.pizzadream.Callback

import com.sokol.pizzadream.Model.UserModel

interface IUserCallBackListener {
    fun onUserLoadSuccess(userModel: UserModel)
    fun onUserLoadFailed(message:String)
}