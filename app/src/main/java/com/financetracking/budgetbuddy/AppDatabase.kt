package com.financetracking.budgetbuddy

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.financetracking.budgetbuddy.models.Transaction
import com.financetracking.budgetbuddy.models.TransactionDao

@Database(
    entities = [Transaction::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)])
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao() : TransactionDao}