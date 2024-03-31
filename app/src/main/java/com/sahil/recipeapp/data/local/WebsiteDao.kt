package com.sahil.recipeapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sahil.recipeapp.data.model.WebsiteData

@Dao
interface WebsiteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(website: List<WebsiteData>)

    @Query("SELECT * FROM websites")
    suspend fun getAll(): List<WebsiteData>
}