package de.selfmade4u.tucanplus

import android.content.Context
import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class CredentialSettings(
    val encryptedUserName: Pair<String, String>,
    val encryptedPassword: Pair<String, String>,
    val encryptedSessionId: Pair<String, String>,
    val encryptedSessionCookie: Pair<String, String>,
)

@Serializable
data class OptionalCredentialSettings(val inner: CredentialSettings?)

// https://developer.android.com/topic/libraries/architecture/datastore
object CredentialSettingsSerializer : Serializer<OptionalCredentialSettings> {
    override val defaultValue: OptionalCredentialSettings = OptionalCredentialSettings(null)

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun readFrom(input: InputStream): OptionalCredentialSettings {
        try {
            return Json.decodeFromStream(input)
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read Settings", serialization)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun writeTo(
        t:  OptionalCredentialSettings,
        output: OutputStream
    ) = Json.encodeToStream(t, output)
}

val Context.credentialSettingsDataStore: DataStore<OptionalCredentialSettings> by dataStore(
    fileName = "credential-settings.pb",
    serializer = CredentialSettingsSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler {
        Log.e("CredentialSettings", "CorruptionHandler", it)
        OptionalCredentialSettings(null)
    }
)