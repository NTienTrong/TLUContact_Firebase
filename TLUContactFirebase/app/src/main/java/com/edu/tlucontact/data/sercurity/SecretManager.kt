package com.edu.tlucontact.data.sercurity

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object SecretManager {

    private const val SECRET_SHARED_PREFS = "secret_shared_prefs"
    private const val CLIENT_SECRET_KEY = "client_secret"

    private fun getEncryptedSharedPreferences(context: Context) =
        EncryptedSharedPreferences.create(
            SECRET_SHARED_PREFS,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    fun saveClientSecret(context: Context, clientSecret: String) {
        getEncryptedSharedPreferences(context)
            .edit()
            .putString(CLIENT_SECRET_KEY, clientSecret)
            .apply()
    }

    fun getClientSecret(context: Context): String? =
        getEncryptedSharedPreferences(context)
            .getString(CLIENT_SECRET_KEY, null)
}