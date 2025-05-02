package com.example.revisedrecipecards

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String,
    val ingredients: List<Pair<String, String>>,
    val description: String = "",
    val notes: String = "",
    val steps: String = "",
    val servings: Int = 1
) : Parcelable