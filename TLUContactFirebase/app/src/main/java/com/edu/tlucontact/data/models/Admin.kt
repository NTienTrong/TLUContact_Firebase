package com.edu.tlucontact.data.models

data class Admin(
    val adminId: String = "",
    val fullName: String = "",
    val email: String? = null,
    val role: String = "",
    val userId: String? = null // Thêm trường userId
) {
    companion object {
        fun fromMap(map: Map<String, Any>?) = if (map != null) {
            Admin(
                adminId = map["adminId"] as? String ?: "",
                fullName = map["fullName"] as? String ?: "",
                email = map["email"] as? String,
                role = map["role"] as? String ?: "",
                userId = map["userId"] as? String
            )
        } else {
            null
        }
    }

    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "adminId" to adminId,
            "fullName" to fullName,
            "email" to email,
            "role" to role,
            "userId" to userId
        )
    }
}