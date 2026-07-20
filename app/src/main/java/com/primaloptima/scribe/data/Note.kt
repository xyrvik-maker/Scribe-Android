package com.primaloptima.scribe.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** A text note stored in the vault or backed by a SAF URI. */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "folder_path") val folderPath: String = "/",
    /** "md" or "txt" */
    val ext: String = "md",
    val content: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    /** Non-null when this note is backed by a SAF document URI. */
    @ColumnInfo(name = "external_uri") val externalUri: String? = null,
    /** True once the SAF file content has been read from disk. */
    val loaded: Boolean = true
)

/** A logical folder entry in the vault. */
@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey val path: String,
    @ColumnInfo(name = "external_uri") val externalUri: String? = null
)
