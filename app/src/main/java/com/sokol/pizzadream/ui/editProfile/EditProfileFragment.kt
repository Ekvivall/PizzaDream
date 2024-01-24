package com.sokol.pizzadream.ui.editProfile

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
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
    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
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
//        profileName = root.findViewById(R.id.profile_name)
//        profileEmail = root.findViewById(R.id.profile_email)
//        profileName.text = Common.currentUser?.firstName
//        profileEmail.text = Common.currentUser?.email
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