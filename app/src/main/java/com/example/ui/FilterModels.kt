package com.example.ui

data class JobFilter(
    val timeRange: TimeRange = TimeRange.ANY,
    val country: String = "",
    val city: String = "",
    val selectedCategories: List<String> = emptyList(), // Added
    val jobTypes: List<String> = emptyList(),
    val experience: String = "",
    val gender: String = "",
    val education: String = ""
)

data class WorkerFilter(
    val timeRange: TimeRange = TimeRange.ANY,
    val country: String = "",
    val city: String = "",
    val selectedCategories: List<String> = emptyList(), // Added
    val profession: String = "",
    val skills: List<String> = emptyList(),
    val availability: String = "",
    val gender: String = "",
    val experience: String = "",
    val minRating: Int = 0
)

enum class TimeRange(val label: String, val milliseconds: Long) {
    ANY("Any time", 0),
    LAST_HOUR("Last 1 hour", 60 * 60 * 1000L),
    LAST_24H("Last 24 hours", 24 * 60 * 60 * 1000L),
    LAST_3_DAYS("Last 3 days", 3 * 24 * 60 * 60 * 1000L),
    LAST_7_DAYS("Last 7 days", 7 * 24 * 60 * 60 * 1000L),
    LAST_30_DAYS("Last 30 days", 30 * 24 * 60 * 60 * 1000L)
}
