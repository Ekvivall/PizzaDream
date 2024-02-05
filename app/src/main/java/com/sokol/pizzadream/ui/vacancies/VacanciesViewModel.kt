package com.sokol.pizzadream.ui.vacancies

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sokol.pizzadream.Callback.IVacanciesLoadCallback
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Model.VacancyModel

class VacanciesViewModel : ViewModel(), IVacanciesLoadCallback {
    private var vacanciesListMutableLiveData: MutableLiveData<List<VacancyModel>>? = null
    private lateinit var messageError: MutableLiveData<String>
    private var vacanciesLoadCallbackListener: IVacanciesLoadCallback = this
    val vacancies: LiveData<List<VacancyModel>>
        get() {
            if (vacanciesListMutableLiveData == null) {
                vacanciesListMutableLiveData = MutableLiveData()
                messageError = MutableLiveData()
                loadVacanciesList()
            }
            return vacanciesListMutableLiveData!!
        }

    private fun loadVacanciesList() {
        val tempList = ArrayList<VacancyModel>()
        val vacanciesRef = FirebaseDatabase.getInstance().getReference(Common.VACANCIES_REF)
        vacanciesRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemsSnapshot in snapshot.children){
                    val model = itemsSnapshot.getValue(VacancyModel::class.java)
                    tempList.add(model!!)
                }
                vacanciesListMutableLiveData!!.value = tempList
                vacanciesLoadCallbackListener.onVacanciesLoadSuccess(tempList)
            }

            override fun onCancelled(error: DatabaseError) {
                vacanciesLoadCallbackListener.onVacanciesLoadFailed(error.message)
            }

        })
    }

    override fun onVacanciesLoadSuccess(vacanciesList: List<VacancyModel>) {
        vacanciesListMutableLiveData?.value = vacanciesList
    }

    override fun onVacanciesLoadFailed(message: String) {
        messageError.value = message
    }
}