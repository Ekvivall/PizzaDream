package com.sokol.pizzadream.ui.editProfile

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.EventBus.ProfileClick
import com.sokol.pizzadream.R
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus

class EditProfileFragment : Fragment() {
    private lateinit var profileImage: ImageView
    private lateinit var editAvatar: Button
    private lateinit var profileName: EditText
    private lateinit var profileLastName: EditText
    private lateinit var profilePhone: EditText
    private lateinit var tilName: TextInputLayout
    private lateinit var tilLastName: TextInputLayout
    private lateinit var tilPhone: TextInputLayout
    private lateinit var saveEditProfile: Button
    private val PICK_IMAGE_REQUEST = 7272
    private var imageUri: Uri? = null
    private lateinit var waitingDialog: AlertDialog
    private lateinit var storageReference: StorageReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        initView(root)
        return root
    }

    private fun initView(root: View) {
        storageReference = FirebaseStorage.getInstance().reference
        waitingDialog =
            SpotsDialog.Builder().setContext(requireContext()).setCancelable(false).build()
        profileImage = root.findViewById(R.id.profile_image)
        if (Common.currentUser!!.avatar.isNotEmpty()) {
            Glide.with(this).load(Common.currentUser!!.avatar).into(profileImage)
        }
        editAvatar = root.findViewById(R.id.edit_avatar)
        profileImage.setOnClickListener {
            editAvatar()
        }
        editAvatar.setOnClickListener {
            editAvatar()
        }
        profileName = root.findViewById(R.id.edt_name)
        profileLastName = root.findViewById(R.id.edt_last_name)
        profilePhone = root.findViewById(R.id.edt_phone)
        tilName = root.findViewById(R.id.til_name)
        tilLastName = root.findViewById(R.id.til_last_name)
        tilPhone = root.findViewById(R.id.til_phone)
        saveEditProfile = root.findViewById(R.id.btn_save_edit_profile)
        profileName.setText(Common.currentUser?.firstName)
        profileLastName.setText(Common.currentUser?.lastName)
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
                        profilePhone.removeTextChangedListener(this)
                        profilePhone.setText(formattedText.toString())
                        profilePhone.setSelection(formattedText.length)
                        profilePhone.addTextChangedListener(this)
                    }
                }
            }
        }
        profilePhone.addTextChangedListener(textWatcher)
        profilePhone.setText(Common.currentUser?.phone!!.replace(" ", ""))
        saveEditProfile.setOnClickListener {
            if (Common.isConnectedToInternet(requireContext())) {
                val name = profileName.text.toString().trim()
                val lastName = profileLastName.text.toString().trim()
                val phone = "+380 " + profilePhone.text.toString().trim()
                tilName.error = null
                tilLastName.error = null
                tilPhone.error = null
                if (name.isEmpty()) {
                    tilName.error = "Будь ласка, введіть своє ім'я"
                    return@setOnClickListener
                }
                if (phone.length in 6..11) {
                    tilPhone.error = "Будь ласка, введіть свій повний номер телефону"
                    return@setOnClickListener
                }
                val updateData = HashMap<String, Any>()
                updateData["firstName"] = name
                updateData["lastName"] = lastName
                updateData["phone"] = profilePhone.text.toString()
                updateProfile(updateData)
            } else {
                Toast.makeText(
                    requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateProfile(updateData: HashMap<String, Any>) {
        val user = FirebaseAuth.getInstance().currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName("${updateData["firstName"]} ${updateData["lastName"]}")
            .build()
        user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCE)
                    .child(Common.currentUser!!.uid!!).updateChildren(updateData)
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                    }.addOnSuccessListener {
                        Common.currentUser!!.firstName = updateData["firstName"].toString()
                        Common.currentUser!!.lastName = updateData["lastName"].toString()
                        Common.currentUser!!.phone = updateData["phone"].toString()

                        Toast.makeText(requireContext(), "Успішно оновлено інформацію!", Toast.LENGTH_SHORT)
                            .show()
                        EventBus.getDefault().postSticky(ProfileClick(true))
                    }
            } else {
                Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun editAvatar() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                imageUri = data.data
                showDialogUpload()
            }
        }
    }

    private fun showDialogUpload() {
        val builder =
            androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
        builder.setTitle("Змінити аватар").setMessage("Ви дійсно хочете змінити аватар?")
            .setNegativeButton("Відміна") { dialogInterface, _ -> dialogInterface.dismiss() }
            .setPositiveButton("Так") { dialogInterface, _ ->
                if (imageUri != null) {
                    if (Common.isConnectedToInternet(requireContext())) {
                        waitingDialog.show()
                        val avatarFolder =
                            storageReference.child("avatars/" + Common.currentUser!!.uid)
                        avatarFolder.putFile(imageUri!!).addOnFailureListener { e ->
                            waitingDialog.dismiss()
                            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                        }.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                avatarFolder.downloadUrl.addOnSuccessListener { uri ->
                                    val updateData = HashMap<String, Any>()
                                    updateData["avatar"] = uri.toString()
                                    updateUser(updateData)
                                }
                                waitingDialog.dismiss()
                            }
                        }.addOnProgressListener { taskSnapshot ->
                            waitingDialog.setMessage("Завантаження")
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Будь ласка, перевірте своє з'єднання!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }.setCancelable(false)
        val dialog = builder.create()
        dialog.show()
        val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
        val negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }

    private fun updateUser(updateData: Map<String, Any>) {
        FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCE)
            .child(Common.currentUser!!.uid!!).updateChildren(updateData)
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }.addOnSuccessListener {
                Common.currentUser!!.avatar = updateData["avatar"].toString()
                Toast.makeText(requireContext(), "Успішно оновлено інформацію!", Toast.LENGTH_SHORT)
                    .show()
                EventBus.getDefault().postSticky(ProfileClick(true))
            }
    }
}