package com.sokol.pizzadream.Model

class VacancyModel {
    var id: String = ""
    var name: String = ""
    var desc: String = ""
    var shortDesc: String = ""
    var image: String = ""
    var resumes: MutableList<ResumeModel>? = ArrayList()
}