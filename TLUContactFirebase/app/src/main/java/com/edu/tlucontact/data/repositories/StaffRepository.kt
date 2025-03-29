package com.edu.tlucontact.data.repositories

import android.util.Log
import com.edu.tlucontact.data.models.Staff
import com.edu.tlucontact.data.models.Unit
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class StaffRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val unitCache = ConcurrentHashMap<String, Unit?>()

    suspend fun getStaffListAsync(): List<Staff> = withContext(Dispatchers.IO) {
        try {
            val staffDocuments = firestore.collection("staff").get().await()
            return@withContext staffDocuments.documents.mapNotNull { doc ->
                try {
                    Staff.fromMap(doc.data ?: mapOf())
                } catch (e: Exception) {
                    Log.e("StaffRepository", "Error parsing staff document", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("StaffRepository", "Error getting staff list", e)
            return@withContext emptyList<Staff>()
        }
    }

    suspend fun getUnitDetailsCoroutine(unitRef: DocumentReference): Unit? = withContext(Dispatchers.IO) {
        // Kiểm tra cache trước
        val path = unitRef.path
        unitCache[path]?.let { return@withContext it }

        try {
            val unitDocument = unitRef.get().await()
            val unit = Unit.fromMap(unitDocument.data)

            // Lưu vào cache
            unitCache[path] = unit
            return@withContext unit
        } catch (e: Exception) {
            Log.e("StaffRepository", "Error getting unit details", e)

            // Lưu null vào cache để tránh request lặp lại
            unitCache[path] = null
            return@withContext null
        }
    }

    fun getUnitDetailsSync(unitRef: DocumentReference): Unit? {
        // Kiểm tra cache trước
        val path = unitRef.path
        unitCache[path]?.let { return it }

        return try {
            val task = unitRef.get()
            val document = task.result
            val unit = if (document != null && document.exists()) {
                Unit.fromMap(document.data)
            } else null

            // Lưu vào cache
            unitCache[path] = unit
            unit
        } catch (e: Exception) {
            Log.e("StaffRepository", "Error getting unit details synchronously", e)
            null
        }
    }

    fun clearCache() {
        unitCache.clear()
    }
}
