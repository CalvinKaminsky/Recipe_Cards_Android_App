package com.example.revisedrecipecards

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class IngredientListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromList(list: List<Pair<String, String>>): String =
        gson.toJson(list)

    @TypeConverter
    fun toList(json: String): List<Pair<String, String>> {
        val type = object : TypeToken<List<Pair<String, String>>>() {}.type
        return gson.fromJson(json, type)
    }
}