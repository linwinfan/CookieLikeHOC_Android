package com.cooklikehoc.recipes.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            gson.fromJson(value, object : TypeToken<List<String>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}