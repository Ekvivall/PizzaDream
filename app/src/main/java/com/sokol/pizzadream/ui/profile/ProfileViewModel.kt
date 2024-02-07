package com.sokol.pizzadream.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Model.UserModel

class ProfileViewModel : ViewModel() {
    private var userMutableLiveData: MutableLiveData<UserModel>? = null
    fun getUserMutableLiveData(): MutableLiveData<UserModel> {
        if (userMutableLiveData == null) {
            userMutableLiveData = MutableLiveData()
        }
        userMutableLiveData!!.value = Common.currentUser
        return userMutableLiveData!!
    }
}