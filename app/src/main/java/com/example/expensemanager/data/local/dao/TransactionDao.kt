package com.example.expensemanager.data.local.dao

import androidx.room.*
import com.example.expensemanager.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {


    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getTransactionById(id: Int): Flow<TransactionEntity?>

    @Query("""
        SELECT * FROM transactions
        WHERE strftime('%m', date / 1000, 'unixepoch', 'localtime') = :month
        AND strftime('%Y', date / 1000, 'unixepoch', 'localtime') = :year
        ORDER BY date DESC
    """)
    fun getTransactionsByMonth(month: String, year: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE type = 'EXPENSE' AND description != 'Budget Overwrite'
        AND strftime('%m', date / 1000, 'unixepoch', 'localtime') = :month
        AND strftime('%Y', date / 1000, 'unixepoch', 'localtime') = :year
    """)
    fun getTotalExpenseByMonth(month: String, year: String): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM transactions
        WHERE type = 'INCOME'
        AND strftime('%m', date / 1000, 'unixepoch', 'localtime') = :month
        AND strftime('%Y', date / 1000, 'unixepoch', 'localtime') = :year
    """)
    fun getTotalIncomeByMonth(month: String, year: String): Flow<Double>

    @Query("UPDATE transactions SET category = :newName WHERE category = :oldName")
    suspend fun updateCategoryName(oldName: String, newName: String)

    @Query("UPDATE transactions SET categoryIcon = :newIcon WHERE category = :categoryName")
    suspend fun updateIconByCategory(categoryName: String, newIcon: String)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)


    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransactionById(transactionId: Int)

    @Query("DELETE FROM transactions WHERE category = :name")
    suspend fun deleteTransactionByCategory(name: String)

    //Delete a specific transaction.
    @Query("""
        DELETE FROM transactions
        WHERE category = :category
        AND description = 'Budget Overwrite'
        AND strftime('%m', date / 1000, 'unixepoch', 'localtime') = :month
        AND strftime('%Y', date / 1000, 'unixepoch', 'localtime') = :year
    """)
    suspend fun deleteOnlyBudget(category: String, month: String, year: String)


     //Delete all transactions for a specific category and month.
    @Query("""
        DELETE FROM transactions
        WHERE category = :category
        AND strftime('%m', date / 1000, 'unixepoch', 'localtime') = :month
        AND strftime('%Y', date / 1000, 'unixepoch', 'localtime') = :year
    """)
    suspend fun deleteTransactionsByCategoryAndMonth(category: String, month: String, year: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()


}