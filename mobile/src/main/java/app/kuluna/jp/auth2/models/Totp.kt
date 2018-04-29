package app.kuluna.jp.auth2.models

import android.arch.persistence.room.*
import android.arch.persistence.room.Database

@Entity
data class Totp(
        @PrimaryKey var id: Int = 0,
        @ColumnInfo(name = "account_id") var accountId: String = "",
        @ColumnInfo(name = "issuer") var issuer: String = "",
        @ColumnInfo(name = "list_order") var listOrder: Int = 0,
        @ColumnInfo(name = "secret") var secret: String = ""
)

@Dao
interface TotpDao {
    @Query("select * from totp order by list_order desc")
    fun getAll(): List<Totp>
    @Insert
    fun add(totp: Totp)
    @Update
    fun update(totp: Totp)
    @Delete
    fun delete(totp: Totp)
}

@Database(entities = [Totp::class], version = 2)
abstract class Database : RoomDatabase() {
    abstract fun totpDao(): TotpDao
}
