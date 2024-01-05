package com.financetracking.budgetbuddy.models

import androidx.room.*

@Dao
interface TransactionDao {
    @Query("SELECT * from transactions ORDER BY date DESC")
    fun getAll(): List<Transaction>

    @Insert
    fun insertAll(vararg transaction: Transaction)

    @Delete
    fun delete(transaction: Transaction)

    @Update
    fun update(vararg transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate")
    fun getTransactionsForTimePeriod(startDate: Long, endDate: Long): List<Transaction>
}