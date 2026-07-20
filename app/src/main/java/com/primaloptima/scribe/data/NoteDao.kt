package com.primaloptima.scribe.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // ── Notes ────────────────────────────────────────────────────────────────

    @Query("SELECT * FROM notes ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<Note>>

    @Query("SELECT * FROM notes ORDER BY updated_at DESC")
    suspend fun getAll(): List<Note>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Note?

    @Query("SELECT * FROM notes WHERE folder_path = :folderPath ORDER BY updated_at DESC")
    suspend fun getByFolder(folderPath: String): List<Note>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<Note>)

    @Update
    suspend fun update(note: Note)

    @Query("UPDATE notes SET content = :content, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateContent(id: String, content: String, updatedAt: Long)

    @Query("UPDATE notes SET name = :name, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateName(id: String, name: String, updatedAt: Long)

    @Query("UPDATE notes SET folder_path = :folderPath, updated_at = :updatedAt WHERE id = :id")
    suspend fun moveNote(id: String, folderPath: String, updatedAt: Long)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Delete
    suspend fun delete(note: Note)

    @Query("DELETE FROM notes WHERE folder_path = :folderPath")
    suspend fun deleteByFolder(folderPath: String)

    @Query("DELETE FROM notes WHERE external_uri IS NOT NULL")
    suspend fun deleteAllExternal()

    @Query("DELETE FROM notes")
    suspend fun deleteAll()

    @Query("SELECT * FROM notes WHERE name LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY updated_at DESC LIMIT 200")
    suspend fun search(query: String): List<Note>

    // ── Folders ───────────────────────────────────────────────────────────────

    @Query("SELECT * FROM folders ORDER BY path ASC")
    fun observeFolders(): Flow<List<Folder>>

    @Query("SELECT * FROM folders ORDER BY path ASC")
    suspend fun getFolders(): List<Folder>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFolder(folder: Folder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<Folder>)

    @Query("DELETE FROM folders WHERE path = :path")
    suspend fun deleteFolder(path: String)

    @Query("DELETE FROM folders WHERE external_uri IS NOT NULL")
    suspend fun deleteAllExternalFolders()

    @Query("DELETE FROM folders WHERE path != '/'")
    suspend fun deleteAllFoldersExceptRoot()

    @Query("DELETE FROM folders")
    suspend fun deleteAllFolders()
}
