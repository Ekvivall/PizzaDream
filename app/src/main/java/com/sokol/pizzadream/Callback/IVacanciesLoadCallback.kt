package com.sokol.pizzadream.Callback

import com.sokol.pizzadream.Model.VacancyModel

interface IVacanciesLoadCallback {
    fun onVacanciesLoadSuccess(vacanciesList: List<VacancyModel>)
    fun onVacanciesLoadFailed(message: String)
}