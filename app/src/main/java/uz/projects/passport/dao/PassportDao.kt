package uz.projects.passport.dao

import androidx.room.*
import uz.projects.passport.entity.Passport

@Dao
interface PassportDao {
    @Insert
    fun addPassport(passport: Passport)

    @Delete
    fun deletePassport(passport: Passport)

    @Update
    fun editPassport(passport: Passport)

    @Query("select * from passport")
    fun getPassportList(): List<Passport>

    @Query("select * from passport where name like :searchQuery or surname like :searchQuery")
    fun searchDatabase(searchQuery: String): List<Passport>

    @Query("select * from passport where :searchQuery = id")
    fun searchCitizen(searchQuery: Int): Passport
}