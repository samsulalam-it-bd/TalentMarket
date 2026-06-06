package com.example.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageManager {

  const val LANG_EN = "en"
  const val LANG_AR = "ar"

  fun setLocale(context: Context, language: String): Context {
    val locale = Locale(language)
    Locale.setDefault(locale)
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    return context.createConfigurationContext(config)
  }

  fun getSavedLanguage(context: Context): String {
    val prefs = context.getSharedPreferences(
      "app_prefs", Context.MODE_PRIVATE
    )
    val defaultLocal = Locale.getDefault().language
    val defaultLang = if (defaultLocal == "ar") LANG_AR else LANG_EN
    return prefs.getString("language", defaultLang) ?: defaultLang
  }

  fun saveLanguage(context: Context, language: String) {
    context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
      .edit()
      .putString("language", language)
      .apply()
  }

  fun isArabic(context: Context): Boolean {
    return getSavedLanguage(context) == LANG_AR
  }
}
