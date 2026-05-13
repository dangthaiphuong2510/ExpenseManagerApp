package com.example.expensemanager.data.remote.repository.impl

import com.example.expensemanager.data.local.dao.CategoryDao
import com.example.expensemanager.data.local.entity.CategoryEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoryRepoImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val categoryDao: CategoryDao
) {

    fun getCategoriesByTime(month: Int, year: Int, userId: String): Flow<List<CategoryEntity>> {
        return categoryDao.getCategoriesByTime(month, year, userId).flowOn(Dispatchers.IO)
    }

    fun getAllCategories(userId: String): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories(userId).flowOn(Dispatchers.IO)
    }

    suspend fun addCategory(
        name: String,
        iconName: String,
        isExpense: Boolean,
        month: Int? = null,
        year: Int? = null,
        userId: String // Đã thêm param userId
    ) {
        withContext(Dispatchers.IO) {
            val category = CategoryEntity(
                name = name,
                iconName = iconName,
                isExpense = isExpense,
                userId = userId,
                targetMonth = month,
                targetYear = year
            )

            // 1. Lưu vào Room local để UI cập nhật ngay lập tức
            categoryDao.insertCategory(category)

            // 2. Đồng bộ lên Supabase Cloud
            try {
                supabase.postgrest["categories"].insert(category)
            } catch (e: Exception) {
                // Log lỗi nếu cần thiết nhưng không làm crash app để user vẫn dùng được offline
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteCategory(name: String, userId: String) {
        withContext(Dispatchers.IO) {
            // Xóa ở local theo đúng userId
            categoryDao.deleteCategoryByName(name, userId)

            // Xóa ở Cloud dựa trên name và userId để đảm bảo an toàn
            try {
                supabase.postgrest["categories"].delete {
                    filter {
                        eq("name", name)
                        eq("user_id", userId)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}