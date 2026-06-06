package com.example.ui

import android.content.Context
import android.content.SharedPreferences

class SearchPrefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("advanced_search_prefs", Context.MODE_PRIVATE)

    fun getRecentSearches(): List<String> {
        val str = prefs.getString("recent_searches", "") ?: ""
        if (str.isEmpty()) return emptyList()
        return str.split("|||").filter { it.isNotEmpty() }
    }

    fun saveSearch(query: String) {
        if (query.trim().isEmpty()) return
        val current = getRecentSearches().toMutableList()
        current.remove(query) // remove duplicates
        current.add(0, query) // add to top
        val limited = current.take(10)
        prefs.edit().putString("recent_searches", limited.joinToString("|||")).apply()
    }

    fun deleteRecentSearch(query: String) {
        val current = getRecentSearches().toMutableList()
        current.remove(query)
        prefs.edit().putString("recent_searches", current.joinToString("|||")).apply()
    }

    fun getSavedFilters(): List<SavedFilter> {
        val str = prefs.getString("saved_filters", "") ?: ""
        if (str.isEmpty()) return emptyList()
        return deserializeSavedFilters(str)
    }

    fun saveFilter(name: String, filter: FilterModel): Boolean {
        val currentList = getSavedFilters().toMutableList()
        val tier = getUserTier()
        if (tier == "Silver" && currentList.size >= 5) {
            return false // limit exceeded
        }

        val newSaved = SavedFilter(name = name, filter = filter)
        currentList.add(0, newSaved)
        prefs.edit().putString("saved_filters", serializeSavedFilters(currentList)).apply()
        return true
    }

    fun deleteSavedFilter(id: String) {
        val currentList = getSavedFilters().toMutableList()
        currentList.removeAll { it.id == id }
        prefs.edit().putString("saved_filters", serializeSavedFilters(currentList)).apply()
    }

    fun getUserTier(): String {
        return prefs.getString("user_tier", "Silver") ?: "Silver"
    }

    fun setUserTier(tier: String) {
        prefs.edit().putString("user_tier", tier).apply()
    }

    private fun serializeFilter(filter: FilterModel): String {
        val jobTypesStr = filter.jobTypes.joinToString(",")
        val specNatStr = filter.specificNationalities.joinToString(",")
        return listOf(
            filter.searchText,
            jobTypesStr,
            "0.0", // Formerly minSalary
            "0.0", // Formerly maxSalary
            "true", // Formerly anySalary
            filter.location,
            filter.experienceLevel,
            filter.education,
            filter.nationalityType,
            specNatStr,
            filter.gender,
            filter.postingDate,
            filter.jobCategory,
            filter.companyType,
            filter.sortBy
        ).joinToString("@@")
    }

    private fun deserializeFilter(str: String): FilterModel {
        val parts = str.split("@@")
        try {
            return FilterModel(
                searchText = parts.getOrNull(0) ?: "",
                jobTypes = parts.getOrNull(1)?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet(),
                // Indices 2, 3, 4 are ignored for salary
                location = parts.getOrNull(5) ?: "All",
                experienceLevel = parts.getOrNull(6) ?: "Any",
                education = parts.getOrNull(7) ?: "Any",
                nationalityType = parts.getOrNull(8) ?: "Any",
                specificNationalities = parts.getOrNull(9)?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet(),
                gender = parts.getOrNull(10) ?: "Any",
                postingDate = parts.getOrNull(11) ?: "Any time",
                jobCategory = parts.getOrNull(12) ?: "All",
                companyType = parts.getOrNull(13) ?: "Any",
                sortBy = parts.getOrNull(14) ?: "Most Recent"
            )
        } catch (e: Exception) {
            return FilterModel()
        }
    }

    private fun serializeSavedFilters(list: List<SavedFilter>): String {
        return list.joinToString("###") { sf ->
            "${sf.id}##${sf.name}##${serializeFilter(sf.filter)}##${sf.timestamp}"
        }
    }

    private fun deserializeSavedFilters(str: String): List<SavedFilter> {
        if (str.isEmpty()) return emptyList()
        return str.split("###").mapNotNull { sfStr ->
            val parts = sfStr.split("##")
            if (parts.size >= 4) {
                SavedFilter(
                    id = parts[0],
                    name = parts[1],
                    filter = deserializeFilter(parts[2]),
                    timestamp = parts[3].toLongOrNull() ?: System.currentTimeMillis()
                )
            } else null
        }
    }
}
