package com.example.campus_eats_app_kt.ui.theme

import androidx.compose.ui.graphics.Color

// Grayscale User Interface Palette
// Requirement: The application interface must use grayscale for color-blind accessibility.
// All colored constants are redefined as grayscale equivalents to maintain compatibility.

val CampusOrange = Color(0xFF4A4A4A) // Redefined as Dark Gray
val ActionBlue = Color(0xFF707070)   // Redefined as Medium Gray
val CampusBlack = Color(0xFF000000)
val CampusWhite = Color(0xFFFFFFFF)

// APP ICON EXCEPTION: Iconic colors for the application icon only.
val IconOrange = Color(0xFFFF5722)
val IconRed = Color(0xFFD32F2F)
val IconGreen = Color(0xFF388E3C)

// Grayscale Tones for semantic mapping
val Grayscale700 = Color(0xFF424242)
val Grayscale500 = Color(0xFF9E9E9E)
val Grayscale300 = Color(0xFFE0E0E0)
val Grayscale100 = Color(0xFFF5F5F5)

val PrimaryLight = Color(0xFF212121) // High contrast black/dark gray
val OnPrimaryLight = CampusWhite
val PrimaryContainerLight = Color(0xFFE0E0E0)
val OnPrimaryContainerLight = Color(0xFF212121)

val SecondaryLight = Color(0xFF616161)
val OnSecondaryLight = CampusWhite
val SecondaryContainerLight = Color(0xFFF5F5F5)
val OnSecondaryContainerLight = Color(0xFF212121)

val BackgroundLight = CampusWhite
val OnBackgroundLight = CampusBlack
val SurfaceLight = CampusWhite
val OnSurfaceLight = CampusBlack

val ErrorLight = Color(0xFF424242)
val OnErrorLight = CampusWhite
val ErrorContainerLight = Color(0xFFE0E0E0)
val OnErrorContainerLight = Color(0xFF212121)

// Dark Theme (Grayscale)
val PrimaryDark = CampusWhite
val OnPrimaryDark = CampusBlack
val PrimaryContainerDark = Color(0xFF424242)
val OnPrimaryContainerDark = CampusWhite

val SecondaryDark = Color(0xFFBDBDBD)
val OnSecondaryDark = CampusBlack
val SecondaryContainerDark = Color(0xFF212121)
val OnSecondaryContainerDark = CampusWhite

val BackgroundDark = CampusBlack
val OnBackgroundDark = CampusWhite
val SurfaceDark = Color(0xFF121212)
val OnSurfaceDark = CampusWhite

val ErrorDark = CampusWhite
val OnErrorDark = CampusBlack
val ErrorContainerDark = Color(0xFF424242)
val OnErrorContainerDark = CampusWhite
