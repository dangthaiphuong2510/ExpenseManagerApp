package com.example.expensemanager.data.remote.repository.impl

import com.example.expensemanager.data.local.dao.BudgetDao
import com.example.expensemanager.data.local.dao.CategoryDao
import com.example.expensemanager.data.local.dao.TransactionDao
import com.example.expensemanager.data.local.entity.*
import com.example.expensemanager.data.model.TransactionResponse
import com.example.expensemanager.data.model.UserResponse
import com.example.expensemanager.data.remote.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepositoryImpl @Inject constructor(
    private val authRepo: AuthRepoImpl,
    private val syncRepo: SyncRepoImpl,
    private val categoryRepo: CategoryRepoImpl,
    private val budgetRepo: BudgetRepoImpl,
    private val transactionRepo: TransactionRepoImpl,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao
) : AppRepository {

    //auth & user logic
    override fun getCurrentUserId(): String? = authRepo.getCurrentUserId()
    override fun getUserProfile(): Flow<Result<UserResponse>> = authRepo.getUserProfile()

    override suspend fun clearAllLocalData() {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Clear all tables in Room Database
                transactionDao.deleteAllTransactions()
                categoryDao.deleteAllCategories()
                budgetDao.deleteAllBudgets()

                android.util.Log.d("LOGOUT", "Room Database cleared successfully!")
            } catch (e: Exception) {
                android.util.Log.e("LOGOUT", "Error clearing Room Database: ${e.message}")
            }
        }
    }

    override suspend fun syncCloudData(): Result<Unit> = syncRepo.syncCloudData()

    //cate logic
    override fun getAllCategories(userId: String): Flow<List<CategoryEntity>> =
        categoryRepo.getAllCategories(userId)

    override suspend fun addCategory(
        name: String,
        iconName: String,
        isExpense: Boolean,
        month: Int?,
        year: Int?,
        userId: String
    ) {
        categoryRepo.addCategory(name, iconName, isExpense, month, year, userId)
    }

    override fun getCategoriesByTime(month: Int, year: Int, userId: String): Flow<List<CategoryEntity>> {
        return categoryRepo.getCategoriesByTime(month, year, userId)
    }

    override suspend fun deleteCategory(name: String, userId: String) =
        categoryRepo.deleteCategory(name, userId)

    override fun getBudgets(month: Int, year: Int, userId: String): Flow<List<BudgetEntity>> =
        budgetRepo.getBudgets(month, year, userId)

    override suspend fun upsertBudget(category: String, amount: Double, month: Int, year: Int, userId: String) =
        budgetRepo.upsertBudget(category, amount, month, year, userId)

    override suspend fun deleteBudget(category: String, month: Int, year: Int, userId: String) =
        budgetRepo.deleteBudget(category, month, year, userId)

    override suspend fun updateBudgetCategoryName(oldName: String, newName: String, userId: String) =
        budgetRepo.updateBudgetCategoryName(oldName, newName, userId)

    override suspend fun deleteAllBudgets() = budgetRepo.deleteAllBudgets()

    override fun getTransactionsByMonth(month: Int, year: Int, userId: String): Flow<List<TransactionEntity>> =
        transactionRepo.getTransactionsByMonth(month, year, userId)

    override fun getTotalExpenseByMonth(month: Int, year: Int, userId: String): Flow<Double> =
        transactionRepo.getTotalExpenseByMonth(month, year, userId)

    override fun getTotalIncomeByMonth(month: Int, year: Int, userId: String): Flow<Double> =
        transactionRepo.getTotalIncomeByMonth(month, year, userId)

    override fun getTransactionById(id: String, userId: String): Flow<TransactionEntity?> =
        transactionRepo.getTransactionById(id, userId)

    override suspend fun insertTransaction(transaction: TransactionEntity) =
        transactionRepo.insertTransaction(transaction)

    override suspend fun updateLocalTransaction(transaction: TransactionEntity) =
        transactionRepo.updateLocalTransaction(transaction)

    override fun addTransaction(
        description: String,
        amount: Double,
        date: Long,
        category: String,
        categoryIcon: String,
        type: String
    ): Flow<Result<TransactionResponse>> =
        transactionRepo.addTransaction(description, amount, date, category, categoryIcon, type)

    override suspend fun deleteTransactionById(id: String, userId: String) =
        transactionRepo.deleteTransactionById(id, userId)

    override fun getTransactions(): Flow<Result<List<TransactionResponse>>> =
        transactionRepo.getTransactions()

    override suspend fun deleteOnlyBudget(category: String, month: Int, year: Int, userId: String) =
        transactionRepo.deleteOnlyBudget(category, month, year, userId)

    override suspend fun deleteTransactionsByCategoryAndMonth(
        categoryName: String,
        month: Int,
        year: Int,
        userId: String
    ) = transactionRepo.deleteTransactionsByCategoryAndMonth(categoryName, month, year, userId)

    override suspend fun updateTransactionCategoryName(oldName: String, newName: String, userId: String) =
        transactionRepo.updateTransactionCategoryName(oldName, newName, userId)

    override suspend fun updateTransactionIconByCategory(categoryName: String, newIcon: String, userId: String) =
        transactionRepo.updateTransactionIconByCategory(categoryName, newIcon, userId)

    override fun getAllLocalTransactions(userId: String): Flow<List<TransactionEntity>> =
        transactionRepo.getAllLocalTransactions(userId)

    override suspend fun deleteAllTransactions() = transactionRepo.deleteAllTransactions()

    override suspend fun deleteTransactionsByCategory(categoryName: String, userId: String) =
        transactionRepo.deleteTransactionsByCategory(categoryName, userId)
}