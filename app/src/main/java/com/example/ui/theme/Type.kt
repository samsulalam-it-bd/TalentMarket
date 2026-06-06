package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.R

// Google Font Provider Setup
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.app_custom_google_fonts_certs
)

val CairoFontName = GoogleFont("Cairo")
val InterFontName = GoogleFont("Inter")

val CairoFontFamily = FontFamily(
    Font(googleFont = CairoFontName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = CairoFontName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = CairoFontName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = CairoFontName, fontProvider = provider, weight = FontWeight.Bold)
)

val InterFontFamily = FontFamily(
    Font(googleFont = InterFontName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = InterFontName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = InterFontName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = InterFontName, fontProvider = provider, weight = FontWeight.Bold)
)

// Temporary alias to fix build
val PoppinsFontFamily = InterFontFamily

// Premium typography configuration
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    titleSmall = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )
)
