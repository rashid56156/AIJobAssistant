package com.sample.aijobassistant.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.sample.aijobassistant.domain.repository.ApiKeyRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores the Gemini API key using EncryptedSharedPreferences, which is backed
 * by a key generated and held inside the Android Keystore (AES256-GCM for
 * values, AES256-SIV for keys). The raw AES key never leaves secure hardware
 * (StrongBox/TEE on supporting devices) — the app only ever holds a handle
 * to it via MasterKey, never the key material itself.
 *
 * This is the deliberate alternative to storing the key in plain
 * SharedPreferences or, worse, in BuildConfig where it would be visible to
 * anyone who decompiles the APK.
 */
@Singleton
class SecureApiKeyStorage @Inject constructor(
    @ApplicationContext private val context: Context
) : ApiKeyRepository {

    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun saveApiKey(key: String) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit().putString(KEY_API_KEY, key).apply()
    }

    override suspend fun getApiKey(): String? = withContext(Dispatchers.IO) {
        encryptedPrefs.getString(KEY_API_KEY, null)
    }

    override suspend fun hasApiKey(): Boolean = withContext(Dispatchers.IO) {
        !encryptedPrefs.getString(KEY_API_KEY, null).isNullOrBlank()
    }

    override suspend fun clearApiKey() = withContext(Dispatchers.IO) {
        encryptedPrefs.edit().remove(KEY_API_KEY).apply()
    }

    private companion object {
        const val PREFS_FILE_NAME = "secure_api_key_prefs"
        const val KEY_API_KEY = "gemini_api_key"
    }
}
