package com.example.revisedrecipecards

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY rowid DESC")
    fun getAll(): LiveData<List<Recipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(recipe: Recipe): Long

    @Delete
    fun delete(recipe: Recipe): Int
}