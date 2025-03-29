package com.edu.tlucontact.data.models

import android.os.Parcelable
import com.google.firebase.firestore.DocumentReference
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Unit(
    val unitId: String = "",
    val code: String = "",
    val name: String = "",
    val address: String = "",
    val logoURL: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val fax: String? = null,
    val parentUnit: @RawValue DocumentReference? = null,
    val type: String = ""
) : Parcelable { // Thêm : Parcelable
    companion object {
        fun fromMap(map: Map<String, Any>?) = if (map != null) {
            Unit(
                unitId = map["unitId"] as? String ?: "",
                code = map["code"] as? String ?: "",
                name = map["name"] as? String ?: "",
                address = map["address"] as? String ?: "",
                logoURL = map["logoURL"] as? String,
                phone = map["phone"] as? String,
                email = map["email"] as? String,
                fax = map["fax"] as? String,
                parentUnit = map["parentUnit"] as? DocumentReference,
                type = map["type"] as? String ?: ""
            )
        } else {
            null
        }
    }

    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "unitId" to unitId,
            "code" to code,
            "name" to name,
            "address" to address,
            "logoURL" to logoURL,
            "phone" to phone,
            "email" to email,
            "fax" to fax,
            "parentUnit" to parentUnit,
            "type" to type
        )
    }
}