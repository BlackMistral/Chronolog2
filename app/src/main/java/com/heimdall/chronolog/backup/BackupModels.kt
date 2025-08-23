package com.heimdall.chronolog.data.backup

import com.heimdall.chronolog.data.LogEntry
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class BackupMetadata(
    @Json(name = "version") val version: Int,
    @Json(name = "creationTimestamp") val creationTimestamp: Date,
    @Json(name = "appVersion") val appVersion: String?
)

@JsonClass(generateAdapter = true)
data class BackupData(
    @Json(name = "logEntries") val logEntries: List<LogEntry>
)

@JsonClass(generateAdapter = true)
data class BackupFile(
    @Json(name = "metadata") val metadata: BackupMetadata,
    @Json(name = "dataHash") val dataHash: String,
    @Json(name = "encryptedData") val encryptedData: String // Encrypted BackupData serialized to String (e.g., Base64)
)