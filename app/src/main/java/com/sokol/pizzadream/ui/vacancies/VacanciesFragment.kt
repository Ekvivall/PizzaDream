package com.sokol.pizzadream.ui.vacancies

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sokol.pizzadream.Adapter.VacancyAdapter
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.EventBus.VacanciesClick
import com.sokol.pizzadream.Model.ResumeModel
import com.sokol.pizzadream.R
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.Calendar

class VacanciesFragment : Fragment() {
    private lateinit var vacanciesRecycler: RecyclerView
    private var vacancyAdapter: VacancyAdapter? = null
    private lateinit var edtName: EditText
    private lateinit var tilName: TextInputLayout
    private lateinit var edtSurname: EditText
    private lateinit var tilSurname: TextInputLayout
    private lateinit var edtDateOfBirth: EditText
    private lateinit var tilDateOfBirth: TextInputLayout
    private lateinit var edtPhone: EditText
    private lateinit var tilPhone: TextInputLayout
    private lateinit var edtEmail: EditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var spnVacancy: Spinner
    private lateinit var btnSendResume: Button
    private val PICK_FILE_REQUEST_CODE = 4161
    private lateinit var storageReference: StorageReference
    private lateinit var waitingDialog: AlertDialog
    private var selectedFileUri: Uri? = null
    private var name = ""
    private var surname = ""
    private var phone = ""
    private var dateOfBirth = ""
    private var email = ""
    private var selectedVacancyId: String = ""
    private var listResume: MutableList<ResumeModel>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val vacanciesViewModel = ViewModelProvider(this).get(VacanciesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_vacancies, container, false)
        initView(root)
        Log.i("Hello", "onCreateView")
        if (Common.isConnectedToInternet(requireContext())) {
            vacanciesViewModel.vacancies.observe(viewLifecycleOwner, Observer {
                val listData = it
                vacancyAdapter = VacancyAdapter(listData, requireContext())
                vacanciesRecycler.adapter = vacancyAdapter
                val vacancyNames = Array(it.size) { i -> it[i].name }
                val adapter = ArrayAdapter(
                    requireContext(), android.R.layout.simple_spinner_dropdown_item, vacancyNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spnVacancy.adapter = adapter
                spnVacancy.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedVacancy = listData[position]
                        selectedVacancyId = selectedVacancy.id
                        listResume = selectedVacancy.resumes
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

    override fun onResume() {
        super.onResume()
        Log.i("Hello", "onResume")
    }

    override fun onPause() {
        super.onPause()

        Log.i("Hello", "onPause")
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.i("Hello", "onDestroy")
    }

    private fun initView(root: View) {
        storageReference = FirebaseStorage.getInstance().reference
        waitingDialog =
            SpotsDialog.Builder().setContext(requireContext()).setCancelable(false).build()
        vacanciesRecycler = root.findViewById(R.id.vacancies_recycler)
        vacanciesRecycler.setHasFixedSize(true)
        vacanciesRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        edtName = root.findViewById(R.id.edt_name)
        tilName = root.findViewById(R.id.til_name)
        edtName.setText(Common.currentUser!!.firstName)
        edtSurname = root.findViewById(R.id.edt_surname)
        tilSurname = root.findViewById(R.id.til_surname)
        edtSurname.setText(Common.currentUser!!.lastName)
        edtDateOfBirth = root.findViewById(R.id.edt_date_of_birth)
        tilDateOfBirth = root.findViewById(R.id.til_date_of_birth)
        edtDateOfBirth.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -16)
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                R.style.MyDatePickerStyle,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = "$selectedDay.${selectedMonth + 1}.$selectedYear"
                    edtDateOfBirth.setText(formattedDate)
                },
                year,
                month,
                day
            )
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
            datePickerDialog.show()
        }
        edtPhone = root.findViewById(R.id.edt_phone)
        tilPhone = root.findViewById(R.id.til_phone)
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                if (!p0.isNullOrEmpty()) {
                    val unmaskedText = StringBuilder()
                    val chars: CharArray = p0.toString().toCharArray()
                    for (x in chars.indices) {
                        if (Character.isDigit(chars[x])) {
                            unmaskedText.append(chars[x])
                        }
                    }
                    if (unmaskedText.length <= 9) {
                        val formattedText = StringBuilder()
                        for (i in unmaskedText.indices) {
                            if (i == 2 || i == 5 || i == 7) {
                                formattedText.append(" ")
                            }
                            formattedText.append(unmaskedText[i])
                        }
                        edtPhone.removeTextChangedListener(this)
                        edtPhone.setText(formattedText.toString())
                        edtPhone.setSelection(formattedText.length)
                        edtPhone.addTextChangedListener(this)
                    }
                }
            }
        }
        edtPhone.addTextChangedListener(textWatcher)
        edtPhone.setText(Common.currentUser?.phone!!.replace(" ", ""))
        edtEmail = root.findViewById(R.id.edt_email)
        tilEmail = root.findViewById(R.id.til_email)
        edtEmail.setText(Common.currentUser!!.email)
        spnVacancy = root.findViewById(R.id.spn_vacancy)
        btnSendResume = root.findViewById(R.id.btn_send_resume)
        btnSendResume.setOnClickListener {
            if (Common.isConnectedToInternet(requireContext())) {
                name = edtName.text.toString().trim()
                surname = edtSurname.text.toString().trim()
                dateOfBirth = edtDateOfBirth.text.toString().trim()
                phone = "+380 " + edtPhone.text.toString().trim()
                email = edtEmail.text.toString().trim()
                tilName.error = null
                tilSurname.error = null
                tilDateOfBirth.error = null
                tilPhone.error = null
                tilEmail.error = null
                if (name.isEmpty()) {
                    tilName.error = "Будь ласка, введіть своє ім'я"
                    return@setOnClickListener
                }
                if (surname.isEmpty()) {
                    tilSurname.error = "Будь ласка, введіть своє прізвище"
                    return@setOnClickListener
                }
                if (dateOfBirth.isEmpty()) {
                    tilDateOfBirth.error = "Будь ласка, введіть свій день народження"
                    return@setOnClickListener
                }
                if (phone.length == 5) {
                    tilPhone.error = "Будь ласка, введіть свій номер телефону"
                    return@setOnClickListener
                } else if (phone.length < 12) {
                    tilPhone.error = "Будь ласка, введіть свій повний номер телефону"
                    return@setOnClickListener
                }
                if (email.isEmpty()) {
                    tilEmail.error = "Будь ласка, введіть свою електронну адресу"
                    return@setOnClickListener
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(edtEmail.text.toString())
                        .matches()
                ) {
                    tilEmail.error = "Введіть коректну електронну адресу."
                    return@setOnClickListener
                } else {
                    chooseFile()
                }
            } else {
                Toast.makeText(
                    requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun chooseFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type =
            "application/pdf|application/msword|application/vnd.openxmlformats-officedocument.wordprocessingml.document|text/plain"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                selectedFileUri = data.data
                saveResume()
            }
        }
    }

    private fun saveResume() {
        waitingDialog.show()
        val resumeFolder = storageReference.child(
            "resumes/" + Common.currentUser!!.uid + "_" + spnVacancy.selectedItem + "_" + SimpleDateFormat(
                "yyyy_MM_dd_HH_mm"
            ).format(
                Calendar.getInstance().time
            )
        )
        resumeFolder.putFile(selectedFileUri!!).addOnFailureListener { e ->
            waitingDialog.dismiss()
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                resumeFolder.downloadUrl.addOnSuccessListener { uri ->
                    val resume = ResumeModel()
                    resume.name = name
                    resume.surname = surname
                    resume.dateOfBirth = dateOfBirth
                    resume.phone = phone
                    resume.email = email
                    resume.resumeFile = uri.toString()
                    if (listResume == null) listResume = ArrayList()
                    listResume!!.add(resume)
                    writeResumeToFirebase(listResume!!)
                }
                waitingDialog.dismiss()
            }
        }.addOnProgressListener { taskSnapshot ->
            waitingDialog.setMessage("Завантаження")
        }
    }

    private fun writeResumeToFirebase(resume: List<ResumeModel>) {
        val updateData = HashMap<String, Any>()
        updateData["resumes"] = resume
        FirebaseDatabase.getInstance().getReference(Common.VACANCIES_REF).child(selectedVacancyId)
            .updateChildren(updateData).addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(), "" + e.message, Toast.LENGTH_SHORT
                ).show()
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        requireContext(), "Ваше резюме успішно відправлено", Toast.LENGTH_SHORT
                    ).show()
                    EventBus.getDefault().postSticky(
                        VacanciesClick(true)
                    )
                }
            }
    }
}