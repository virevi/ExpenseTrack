package com.example.expensetrack.data.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.expensetrack.data.entity.Category

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY name")
    fun getAll(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE parentId IS NULL ORDER BY name")
    fun getTopLevel(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY name")
    fun getChildren(parentId: Long): Flow<List<Category>>

    @Insert
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)
}
