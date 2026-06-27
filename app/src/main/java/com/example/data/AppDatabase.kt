package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TransactionEntity::class, 
        CategoryEntity::class, 
        OrderEntity::class, 
        PieceCalculationEntity::class, 
        UserEntity::class, 
        BrandConfigEntity::class, 
        InvestmentEntity::class,
        ClientEntity::class,
        EmployeeEntity::class,
        EmployeePaymentEntity::class,
        ProductModelEntity::class,
        ClosedMonthEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val transactionDao: TransactionDao
    abstract val categoryDao: CategoryDao
    abstract val orderDao: OrderDao
    abstract val pieceCalculationDao: PieceCalculationDao
    abstract val userDao: UserDao
    abstract val brandConfigDao: BrandConfigDao
    abstract val investmentDao: InvestmentDao
    abstract val clientDao: ClientDao
    abstract val employeeDao: EmployeeDao
    abstract val employeePaymentDao: EmployeePaymentDao
    abstract val productModelDao: ProductModelDao
    abstract val closedMonthDao: ClosedMonthDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Safe migration path for older versions
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Safe migration path for older versions
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Safe migration path for older versions
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Safe migration path for older versions
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `businessArea` TEXT NOT NULL DEFAULT 'Geral'")
                db.execSQL("ALTER TABLE `orders` ADD COLUMN `status` TEXT NOT NULL DEFAULT 'Pendente'")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `investments` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `description` TEXT NOT NULL, 
                        `totalAmount` REAL NOT NULL, 
                        `abatedAmount` REAL NOT NULL, 
                        `timestamp` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `clients` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `phone` TEXT NOT NULL DEFAULT ''
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `employees` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `role` TEXT NOT NULL DEFAULT 'Costureira'
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `employee_payments` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `employeeId` INTEGER NOT NULL, 
                        `employeeName` TEXT NOT NULL, 
                        `amount` REAL NOT NULL, 
                        `week` TEXT NOT NULL, 
                        `paymentDate` TEXT NOT NULL, 
                        `status` TEXT NOT NULL DEFAULT 'Pendente', 
                        `timestamp` INTEGER NOT NULL,
                        `transactionId` INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `product_models` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `name` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `brand_config` ADD COLUMN `isDarkMode` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `brand_config` ADD COLUMN `fontSizeScale` REAL NOT NULL DEFAULT 1.0")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `closed_months` (
                        `monthYear` TEXT NOT NULL, 
                        `closedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`monthYear`)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ms_modaintima_database"
                )
                    .addMigrations(
                        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                        MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun clearInstance() {
            synchronized(this) {
                try {
                    INSTANCE?.close()
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "Error closing database during reset", e)
                }
                INSTANCE = null
            }
        }
    }
}
