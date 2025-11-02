package de.selfmade4u.tucanplus

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore

val Context.credentialSettingsDataStore: DataStore<OptionalCredentialSettings> by dataStore(
    fileName = "credential-settings.pb",
    serializer = CredentialSettingsSerializer(CipherManager),
    corruptionHandler = ReplaceFileCorruptionHandler {
        OptionalCredentialSettings(null)
    }
)