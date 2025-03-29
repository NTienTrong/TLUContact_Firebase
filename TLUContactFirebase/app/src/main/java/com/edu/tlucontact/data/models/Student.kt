package com.edu.tlucontact.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Student(
    val studentId: String = "",
    val fullName: String = "",
    val photoURL: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val className: String = "",
    val userId: String? = null,
) : Parcelable{
    companion object {
        fun fromMap(map: Map<String, Any>?) = if (map != null) {
            Student(
                studentId = map["studentId"] as? String ?: "",
                fullName = map["fullName"] as? String ?: "",
                photoURL = map["photoURL"] as? String,
                phone = map["phone"] as? String,
                email = map["email"] as? String,
                address = map["address"] as? String,
                className = map["className"] as? String ?: "",
                userId = map["userId"] as? String
            )
        } else {
            null
        }
    }

    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "studentId" to studentId,
            "fullName" to fullName,
            "photoURL" to photoURL,
            "phone" to phone,
            "email" to email,
            "address" to address,
            "className" to className,
            "userId" to userId
        )
    }
}

data class StudentWithDisplayName(
    val student: Student,
    val displayName: String
)