package com.edu.tlucontact.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.edu.tlucontact.data.models.StudentWithDisplayName
import com.google.firebase.firestore.FirebaseFirestore

class StudentRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun getStudentsWithDisplayName(): LiveData<List<StudentWithDisplayName>> {
        val studentsLiveData = MutableLiveData<List<StudentWithDisplayName>>()
        firestore.collection("students").addSnapshotListener { studentDocuments, exception -> // Sử dụng addSnapshotListener()
            if (exception != null) {
                Log.e("StudentRepository", "Error getting students", exception)
                studentsLiveData.value = emptyList()
                return@addSnapshotListener
            }

            if (studentDocuments != null) {
                val studentList = studentDocuments.mapNotNull { studentDoc ->
                    val student = com.edu.tlucontact.data.models.Student.fromMap(studentDoc.data)
                    student?.let {
                        // Lấy displayName từ bảng users
                        firestore.collection("users").document(it.userId ?: "").get()
                            .addOnSuccessListener { userDoc ->
                                val displayName = userDoc.getString("displayName") ?: ""
                                studentsLiveData.value = (studentsLiveData.value ?: emptyList()) + listOf(StudentWithDisplayName(it, displayName))
                            }
                            .addOnFailureListener {
                            }
                        null // Trả về null để tránh thêm trùng lặp vào danh sách tạm thời
                    }
                }
            }
        }
        return studentsLiveData
    }
}