package com.edu.tlucontact.data.models

data class User(
    val uid: String = "",
    val email: String = "",
    val role: String = "",
    val displayName: String = "",
    val photoURL: String? = null,
    val phoneNumber: String? = null
) {
    // Phương thức chuyển đổi từ Map sang User
    companion object {
        fun fromMap(map: Map<String, Any>?) = if (map != null) {
            User(
                uid = map["uid"] as? String ?: "",
                email = map["email"] as? String ?: "",
                role = map["role"] as? String ?: "",
                displayName = map["displayName"] as? String ?: "",
                photoURL = map["photoURL"] as? String,
                phoneNumber = map["phoneNumber"] as? String
            )
        } else {
            null
        }
    }

    // Phương thức chuyển đổi từ User sang Map
    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "uid" to uid,
            "email" to email,
            "role" to role,
            "displayName" to displayName,
            "photoURL" to photoURL,
            "phoneNumber" to phoneNumber
        )
    }
}