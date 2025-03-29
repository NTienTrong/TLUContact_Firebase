package com.edu.tlucontact.ui.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.edu.tlucontact.R
import com.edu.tlucontact.databinding.EditStudentFormBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class EditStudentFormActivity : AppCompatActivity() {

    private lateinit var binding: EditStudentFormBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null
    private var userId: String? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditStudentFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        userId = intent.getStringExtra("userId")

        setupToolbar()
        loadStudentData()
        setupChangeAvatarButton()
        setupSaveButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.editStudentToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadStudentData() {
        userId?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { userDocument ->
                    if (userDocument.exists()) {
                        val userName = userDocument.getString("displayName")
                        val photoUrl = userDocument.getString("photoURL")
                        val email = userDocument.getString("email")
                        val phone = userDocument.getString("phoneNumber")

                        binding.studentName.setText(userName)
                        binding.studentName.isEnabled = false
                        binding.studentEmail.setText(email)
                        binding.studentEmail.isEnabled = false
                        binding.studentPhone.setText(phone)

                        Glide.with(this)
                            .load(photoUrl)
                            .placeholder(R.drawable.ic_default_avatar)
                            .error(R.drawable.ic_default_avatar)
                            .into(binding.studentAvatar)
                    }
                }

            firestore.collection("students").whereEqualTo("userId", uid).get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        return@addOnSuccessListener
                    }
                    val studentDocument = querySnapshot.documents[0]
                    val studentId = studentDocument.getString("studentId")
                    val address = studentDocument.getString("address")
                    val className = studentDocument.getString("className")

                    binding.studentId.setText(studentId)
                    binding.studentAddress.setText(address)
                    binding.studentClass.setText(className)
                }
        }
    }

    private fun setupChangeAvatarButton() {
        binding.changeStudentAvatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            Glide.with(this).load(selectedImageUri).into(binding.studentAvatar)
        }
    }

    private fun setupSaveButton() {
        binding.saveStudentInfo.setOnClickListener {
            saveStudentData()
        }
    }

    private fun saveStudentData() {
        userId?.let { uid ->
            val studentId = binding.studentId.text.toString()
            val address = binding.studentAddress.text.toString()
            val className = binding.studentClass.text.toString()
            val phone = binding.studentPhone.text.toString()

            val studentData = hashMapOf(
                "studentId" to studentId,
                "address" to address,
                "className" to className,
                "userId" to uid,
                "phone" to phone // Add phone to student data
            )

            firestore.collection("students").whereEqualTo("userId", uid).get()
                .addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.isEmpty) {
                        firestore.collection("students").add(studentData)
                            .addOnSuccessListener {
                                updateUserData(uid, phone, className)
                            }
                            .addOnFailureListener { e ->
                                Log.e("EditStudentFormActivity", "Error adding student data: ${e.message}")
                            }
                    } else {
                        val studentDocument = querySnapshot.documents[0]
                        firestore.collection("students").document(studentDocument.id).update(studentData as Map<String, Any>)
                            .addOnSuccessListener {
                                updateUserData(uid, phone, className)
                            }
                            .addOnFailureListener { e ->
                                Log.e("EditStudentFormActivity", "Error updating student data: ${e.message}")
                            }
                    }

                    selectedImageUri?.let { uri ->
                        uploadImage(uri, uid, phone, className)
                    } ?: run {
                        updateUserData(uid, phone, className)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("EditStudentFormActivity", "Error getting student data: ${e.message}")
                }
        }
    }

    private fun updateUserData(userId: String, phone: String, className: String) {
        firestore.collection("users").document(userId).update(
            mapOf(
                "phoneNumber" to phone,
                "className" to className,
            )
        )
            .addOnSuccessListener {
                showSuccessDialog()
            }
            .addOnFailureListener { e ->
                Log.e("EditStudentFormActivity", "Error updating user data: ${e.message}")
                showSuccessDialog()
            }
    }

    private fun uploadImage(uri: Uri, userId: String, phone: String, className: String) {
        val ref = storage.reference.child("images/$userId/${UUID.randomUUID().toString()}")
        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    firestore.collection("users").document(userId).update("photoURL", downloadUri.toString())
                        .addOnSuccessListener {
                            updateUserData(userId, phone, className)
                        }
                        .addOnFailureListener { e ->
                            Log.e("EditStudentFormActivity", "Error updating photoURL: ${e.message}")
                            updateUserData(userId, phone, className)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditStudentFormActivity", "Error uploading image: ${e.message}")
                updateUserData(userId, phone, className)
            }
    }

    private fun showSuccessDialog() {
        val dialog = SuccessDialogFragment()
        dialog.show(supportFragmentManager, "SuccessDialog")
    }

    class SuccessDialogFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return AlertDialog.Builder(requireContext())
                .setTitle("Thành công")
                .setMessage("Cập nhật thông tin thành công!")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    activity?.finish()
                }
                .create()
        }
    }
}