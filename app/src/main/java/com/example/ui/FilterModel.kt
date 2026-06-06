package com.example.ui

import java.io.Serializable

data class FilterModel(
    val searchText: String = "",
    val jobTypes: Set<String> = emptySet(),
    val location: String = "All", // "Remote / Work from home", "Near me", or a specific City name
    val experienceLevel: String = "Any", // No Experience, Less than 1 year, 1-3 years, 3-5 years, 5-10 years, 10+ years
    val education: String = "Any", // High School, Diploma, Bachelor's, Master's, PhD
    val nationalityType: String = "Any", // Any, Saudi Only, Open to Expats
    val specificNationalities: Set<String> = emptySet(),
    val gender: String = "Any", // Any, Male, Female
    val postingDate: String = "Any time", // Any time, Last 24 hours, Last 3 days, Last week, Last month
    val jobCategory: String = "All", // from our 100 categories
    val companyType: String = "Any", // Any, Verified Only, Premium Company, Government, Private, Startup
    val sortBy: String = "Most Recent" // Most Recent, Most Applicants, Most Viewed, Nearest to me
) : Serializable {

    fun getActiveFiltersCount(): Int {
        var count = 0
        if (jobTypes.isNotEmpty()) count++
        if (location != "All") count++
        if (experienceLevel != "Any") count++
        if (education != "Any") count++
        if (nationalityType != "Any") count++
        if (specificNationalities.isNotEmpty()) count++
        if (gender != "Any") count++
        if (postingDate != "Any time") count++
        if (jobCategory != "All") count++
        if (companyType != "Any") count++
        return count
    }

    fun hasActiveFilters(): Boolean = getActiveFiltersCount() > 0
}

data class SavedFilter(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val filter: FilterModel,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
