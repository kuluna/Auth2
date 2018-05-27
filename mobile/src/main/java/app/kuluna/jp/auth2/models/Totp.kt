package app.kuluna.jp.auth2.models

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.*
import android.arch.persistence.room.Database
import android.arch.persistence.room.migration.Migration
import android.content.Context

@Entity(tableName = "totp")
data class Totp(
        @ColumnInfo(name = "Id") @PrimaryKey var id: Int?,
        @ColumnInfo(name = "account_id") var accountId: String?,
        @ColumnInfo(name = "issuer") var issuer: String?,
        @ColumnInfo(name = "list_order") var listOrder: Int?,
        @ColumnInfo(name = "secret") var secret: String?
) {
    fun now(): String = org.jboss.aerogear.security.otp.Totp(secret).now()
}

@Dao
interface TotpDao {
    @Query("select * from totp order by list_order desc;")
    fun getAll(): List<Totp>

    @Insert
    fun add(totp: Totp)

    @Update
    fun update(totp: Totp)

    @Delete
    fun delete(totp: Totp)
}

@Database(entities = [Totp::class], version = 3)
abstract class Db : RoomDatabase() {
    companion object {
        fun create(context: Context) = Room.databaseBuilder(context, Db::class.java, "auth2.db")
                .addMigrations(initRoomMigrate)
                .allowMainThreadQueries().build()

        private val initRoomMigrate = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {}
        }
    }

    abstract fun totpDao(): TotpDao
}
