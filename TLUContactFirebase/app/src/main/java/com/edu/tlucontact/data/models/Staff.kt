package com.edu.tlucontact.data.models

import com.google.firebase.firestore.DocumentReference
import kotlinx.parcelize.Parcelize


data class Staff(
    val staffId: String = "",
    val fullName: String = "",
    val position: String = "",
    val phone: String? = null,
    val email: String? = null,
    val photoURL: String? = null,
    val unit: DocumentReference? = null,
    val userId: String? = null
) {
    companion object {
        fun fromMap(map: Map<String, Any>?) = if (map != null) {
            Staff(
                staffId = map["staffId"] as? String ?: "",
                fullName = map["fullName"] as? String ?: "",
                position = map["position"] as? String ?: "",
                phone = map["phone"] as? String,
                email = map["email"] as? String,
                photoURL = map["photoURL"] as? String,
                unit = map["unit"] as? DocumentReference,
                userId = map["userId"] as? String
            )
        } else {
            null
        }
    }

    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "staffId" to staffId,
            "fullName" to fullName,
            "position" to position,
            "phone" to phone,
            "email" to email,
            "photoURL" to photoURL,
            "unit" to unit,
            "userId" to userId
        )
    }
}