package com.edu.tlucontact.ui.activities

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.edu.tlucontact.R
import com.edu.tlucontact.data.models.Staff
import com.edu.tlucontact.databinding.EditStaffFormBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import androidx.appcompat.app.AlertDialog
import com.edu.tlucontact.data.repositories.StaffRepository
import com.google.firebase.firestore.DocumentReference

class EditStaffFormActivity : AppCompatActivity() {

    private lateinit var binding: EditStaffFormBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null
    private var userId: String? = null
    private var staff: Staff? = null
    private var unitRef: DocumentReference? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditStaffFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        userId = intent.getStringExtra("userId")

        setupToolbar()
        loadStaffData()
        setupChangeAvatarButton()
        setupSaveButton()
        setupUnitSelection()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.editStaffToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadStaffData() {
        userId?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { userDocument ->
                    if (userDocument.exists()) {
                        val userName = userDocument.getString("displayName")
                        val photoUrl = userDocument.getString("photoURL")
                        val email = userDocument.getString("email")

                        binding.staffName.setText(userName)
                        binding.staffName.isEnabled = false
                        binding.staffEmail.setText(email)
                        binding.staffEmail.isEnabled = false

                        Glide.with(this)
                            .load(photoUrl)
                            .placeholder(R.drawable.ic_default_avatar)
                            .error(R.drawable.ic_default_avatar)
                            .into(binding.staffAvatar)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("EditStaffFormActivity", "Error loading user data: ${e.message}")
                }

            firestore.collection("staff").whereEqualTo("userId", uid).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        staff = Staff.fromMap(querySnapshot.documents[0].data)

                        staff?.let { staffInfo ->
                            binding.staffId.setText(staffInfo.staffId)
                            binding.staffPhone.setText(staffInfo.phone)
                            unitRef = staffInfo.unit

                            unitRef?.get()?.addOnSuccessListener { unitDocument ->
                                if (unitDocument.exists()) {
                                    val unitName = unitDocument.getString("name")
                                    binding.staffUnit.setText(unitName)
                                } else {
                                    binding.staffUnit.setText("Không tìm thấy đơn vị")
                                }
                            }?.addOnFailureListener {
                                binding.staffUnit.setText("Lỗi tải đơn vị")
                            }
                        }
                    } else {
                        Log.w("EditStaffFormActivity", "No staff data found for userId: $uid")
                    }
                    Log.d("EditStaffFormActivity", "querySnapshot size: ${querySnapshot.size()}")
                }
                .addOnFailureListener { e ->
                    Log.e("EditStaffFormActivity", "Error loading staff data: ${e.message}")
                }
        }
    }

    private fun setupChangeAvatarButton() {
        binding.changeStaffAvatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            Glide.with(this).load(selectedImageUri).into(binding.staffAvatar)
        }
    }

    private fun setupSaveButton() {
        binding.saveStaffInfo.setOnClickListener {
            saveStaffData()
        }
    }

    private fun saveStaffData() {
        userId?.let { uid ->
            val staffId = binding.staffId.text.toString()
            val phone = binding.staffPhone.text.toString()

            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { userDocument ->
                    if (userDocument.exists()) {
                        val email = userDocument.getString("email")
                        val fullName = userDocument.getString("displayName")

                        firestore.collection("staff").whereEqualTo("userId", uid).get()
                            .addOnSuccessListener { querySnapshot ->
                                val staffData = hashMapOf(
                                    "staffId" to staffId,
                                    "phone" to phone,
                                    "userId" to uid,
                                    "email" to email,
                                    "fullName" to fullName,
                                    "unit" to unitRef
                                )

                                if (querySnapshot.isEmpty()) {
                                    firestore.collection("staff").add(staffData)
                                        .addOnSuccessListener {
                                            updateUserData(uid, phone)
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("EditStaffFormActivity", "Error adding staff data: ${e.message}")
                                        }
                                } else {
                                    val staffDocument = querySnapshot.documents[0]
                                    firestore.collection("staff").document(staffDocument.id).update(staffData as Map<String, Any>)
                                        .addOnSuccessListener {
                                            updateUserData(uid, phone)
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("EditStaffFormActivity", "Error updating staff data: ${e.message}")
                                        }
                                }

                                selectedImageUri?.let { uri ->
                                    uploadImage(uri, uid)
                                } ?: run {
                                    updateUserData(uid, phone)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("EditStaffFormActivity", "Error getting staff data: ${e.message}")
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("EditStaffFormActivity", "Error getting user data: ${e.message}")
                }
        }
    }

    private fun updateUserData(userId: String, phone: String) {
        firestore.collection("users").document(userId).update("phoneNumber", phone)
            .addOnSuccessListener {
                showSuccessDialog()
                setResult(RESULT_OK)
                StaffRepository().clearCache()
            }
            .addOnFailureListener { e ->
                Log.e("EditStaffFormActivity", "Error updating user phone number: ${e.message}")
                showSuccessDialog()
                setResult(RESULT_OK)
            }
    }

    private fun uploadImage(uri: Uri, userId: String) {
        val ref = storage.reference.child("images/$userId/${UUID.randomUUID().toString()}")
        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    firestore.collection("users").document(userId).update("photoURL", downloadUri.toString())
                        .addOnSuccessListener {
                            updateUserData(userId, binding.staffPhone.text.toString())
                        }
                        .addOnFailureListener { e ->
                            Log.e("EditStaffFormActivity", "Error updating photoURL: ${e.message}")
                            updateUserData(userId, binding.staffPhone.text.toString())
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditStaffFormActivity", "Error uploading image: ${e.message}")
                updateUserData(userId, binding.staffPhone.text.toString())
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

    private fun setupUnitSelection() {
        binding.staffUnit.setOnClickListener {
            fetchUnitsAndShowDialog()
        }
    }

    private fun fetchUnitsAndShowDialog() {
        firestore.collection("units").get()
            .addOnSuccessListener { querySnapshot ->
                val unitNames = mutableListOf<String>()
                val unitRefs = mutableListOf<DocumentReference>()

                for (document in querySnapshot.documents) {
                    val unitName = document.getString("name")
                    val unitRef = document.reference
                    if (unitName != null) {
                        unitNames.add(unitName)
                        unitRefs.add(unitRef)
                    }
                }

                showUnitSelectionDialog(unitNames, unitRefs)
            }
            .addOnFailureListener { e ->
                Log.e("EditStaffFormActivity", "Error fetching units: ${e.message}")
            }
    }

    private fun showUnitSelectionDialog(unitNames: List<String>, unitRefs: List<DocumentReference>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Chọn đơn vị")
        builder.setItems(unitNames.toTypedArray()) { _, which ->
            unitRef = unitRefs[which]
            binding.staffUnit.setText(unitNames[which])
        }
        builder.show()
    }
}