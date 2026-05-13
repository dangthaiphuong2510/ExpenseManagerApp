package com.example.expensemanager.data.local.dao

import androidx.room.*
import com.example.expensemanager.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE syncStatus = 'PENDING' AND userId = :userId")
    suspend fun getPendingTransactions(userId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getAllTransactions(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id AND userId = :userId")
    fun getTransactionById(id: String, userId: String): Flow<TransactionEntity?>

    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId
        AND strftime('%m', date / 1000, 'unixepoch', 'localtime') = :month
        AND strftime('%Y', date / 1000, 'unixepoch', 'localtime') = :year
        ORDER BY date DESC
    """)
    fun getTransactionsByMonth(month: String, year: String, userId: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE userId = :userId AND type = 'EXPENSE' AND description != 'Budget Overwrite'
        AND strftime('%m', date / 1000, 'unixepoch', 'localtime') = :month
        AND strftime('%Y', date / 1000, 'unixepoch', 'localtime') = :year
    """)
    fun getTotalExpenseByMonth(month: String, year: String, userId: String): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE userId = :userId AND type = 'INCOME'
        AND strftime('%m', date / 1000, 'unixepoch', 'localtime') = :month
        AND strftime('%Y', date / 1000, 'unixepoch', 'localtime') = :year
    """)
    fun getTotalIncomeByMonth(month: String, year: String, userId: String): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET category = :newName WHERE category = :oldName AND userId = :userId")
    suspend fun updateCategoryName(oldName: String, newName: String, userId: String)

    @Query("UPDATE transactions SET categoryIcon = :newIcon WHERE category = :categoryName AND userId = :userId")
    suspend fun updateIconByCategory(categoryName: String, newIcon: String, userId: String)

    @Query("DELETE FROM transactions WHERE id = :transactionId AND userId = :userId")
    suspend fun deleteTransactionById(transactionId: String, userId: String)

    @Query("DELETE FROM transactions WHERE category = :name AND userId = :userId")
    suspend fun deleteTransactionByCategory(name: String, userId: String)

    // Delete a specific budget overwrite transaction
    @Query("""
        DELETE FROM transactions
        WHERE userId = :userId
        AND category = :category
        AND description = 'Budget Overwrite'
        AND strftime('%m', date / 1000, 'unixepoch', 'localtime') = :month
        AND strftime('%Y', date / 1000, 'unixepoch', 'localtime') = :year
    """)
    suspend fun deleteOnlyBudget(category: String, month: String, year: String, userId: String)

    // Delete all transactions for a specific category and month
    @Query("""
        DELETE FROM transactions
        WHERE userId = :userId
        AND category = :category
        AND strftime('%m', date / 1000, 'unixepoch', 'localtime') = :month
        AND strftime('%Y', date / 1000, 'unixepoch', 'localtime') = :year
    """)
    suspend fun deleteTransactionsByCategoryAndMonth(category: String, month: String, year: String, userId: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}