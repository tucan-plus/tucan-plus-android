package de.selfmade4u.tucanplus

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import de.selfmade4u.tucanplus.connector.Localizer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.time.ExperimentalTime

interface EncryptionHelper {
    fun encrypt(inputText: String): Pair<String, String>
    fun decrypt(data: Pair<String, String>): String
}

@Serializable
data class CredentialSettings @OptIn(ExperimentalTime::class) constructor(
    val username: String,
    val password: String,
    val sessionId: String,
    val sessionCookie: String,
    val menuLocalizer: Localizer,
    val lastRequestTime: Long
)

@Serializable
data class OptionalCredentialSettings(val inner: CredentialSettings?)

// https://developer.android.com/topic/libraries/architecture/datastore
class CredentialSettingsSerializer(val encryptionHelper: EncryptionHelper) : Serializer<OptionalCredentialSettings> {
    override val defaultValue: OptionalCredentialSettings = OptionalCredentialSettings(null)

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun readFrom(input: InputStream): OptionalCredentialSettings {
        try {
            val decrypted = encryptionHelper.decrypt(Json.decodeFromStream(input))
            return Json.decodeFromString(decrypted)
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read Settings", serialization)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun writeTo(
        t: OptionalCredentialSettings,
        output: OutputStream
    ) {
        val unencrypted = Json.encodeToString(t)
        Json.encodeToStream(encryptionHelper.encrypt(unencrypted), output)
    }
}
