package com.example.expensemanager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.expensemanager.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("""
        SELECT * FROM categories 
        WHERE userId = :userId AND (
            targetMonth IS NULL 
            OR (targetMonth = :month AND targetYear = :year)
        )
    """)
    fun getCategoriesByTime(month: Int, year: Int, userId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE userId = :userId")
    fun getAllCategories(userId: String): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE name = :name AND userId = :userId")
    suspend fun deleteCategoryByName(name: String, userId: String)

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
}