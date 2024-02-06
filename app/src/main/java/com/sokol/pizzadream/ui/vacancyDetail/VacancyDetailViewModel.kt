package com.sokol.pizzadream.ui.vacancyDetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Model.VacancyModel

class VacancyDetailViewModel : ViewModel() {
    private var vacancyMutableLiveData: MutableLiveData<VacancyModel>? = null
    fun getVacancyDetailMutableLiveData(): MutableLiveData<VacancyModel> {
        if (vacancyMutableLiveData == null) {
            vacancyMutableLiveData = MutableLiveData()
        }
        vacancyMutableLiveData!!.value = Common.vacancySelected
        return vacancyMutableLiveData!!
    }
}