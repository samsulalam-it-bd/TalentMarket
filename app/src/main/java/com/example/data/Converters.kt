package com.example.data

import androidx.room.TypeConverter
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        return value?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    @TypeConverter
    fun fromReportsList(value: List<Map<String, Any>>?): String {
        if (value == null) return ""
        val array = JSONArray()
        for (map in value) {
            val obj = JSONObject()
            for ((key, v) in map) {
                obj.put(key, v)
            }
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toReportsList(value: String?): List<Map<String, Any>> {
        if (value.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<Map<String, Any>>()
        try {
            val array = JSONArray(value)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val map = mutableMapOf<String, Any>()
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    map[key] = obj.get(key)
                }
                list.add(map)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
