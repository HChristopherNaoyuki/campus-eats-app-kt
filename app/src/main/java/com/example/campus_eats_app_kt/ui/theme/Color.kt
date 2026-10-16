package com.example.campus_eats_app_kt.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Campus Eats Brand Palette
 * Aligned with website theme (src/index.css) and Apple HIG Dark Mode Specification:
 * - Light Mode: Warm Orange #ED5A1E, Accent Green #309C5E, Background #FAF7F5
 * - Dark Mode: Soft Dark Gray Canvas #1C1C1E, Black Navigation Bar #000000,
 *   Cream Content Boxes #F5F2EB with Dark Text #1C1C1E, Apple Red Error #FF453A.
 */

val CampusBlack = Color(0xFF1C1C1E)
val CampusWhite = Color(0xFFFFFFFF)

// Primary Brand Colors
val CampusOrange = Color(0xFFED5A1E)
val CampusOrangeGlow = Color(0xFFF97316)
val ActionBlue = Color(0xFF0A84FF)
val AccentGreen = Color(0xFF309C5E)

// App Icon Colors
val IconOrange = Color(0xFFED5A1E)

// Light Theme Palette (Website Parity)
val PrimaryLight = Color(0xFFED5A1E)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFFFDBCF)
val OnPrimaryContainerLight = Color(0xFF3B0B00)

val SecondaryLight = Color(0xFF309C5E)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFD2E8D8)
val OnSecondaryContainerLight = Color(0xFF0D381E)

val BackgroundLight = Color(0xFFFAF7F5)
val OnBackgroundLight = Color(0xFF231A16)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF231A16)
val SurfaceVariantLight = Color(0xFFF2ECE9)
val OnSurfaceVariantLight = Color(0xFF231A16)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

// Dark Theme Palette (Apple HIG Parity with Cream Content Panels)
val PrimaryDark = Color(0xFFED5A1E)
val OnPrimaryDark = Color(0xFFFFFFFF)
val PrimaryContainerDark = Color(0xFF2C2C2E)
val OnPrimaryContainerDark = Color(0xFFF5F2EB)

val SecondaryDark = Color(0xFF309C5E)
val OnSecondaryDark = Color(0xFFFFFFFF)
val SecondaryContainerDark = Color(0xFF1C1C1E)
val OnSecondaryContainerDark = Color(0xFFF5F2EB)

val BackgroundDark = Color(0xFF1C1C1E) // #1C1C1E soft system dark gray canvas
val OnBackgroundDark = Color(0xFFF5F5F4)
val SurfaceDark = Color(0xFF2C2C2E) // #2C2C2E elevated dark surface
val OnSurfaceDark = Color(0xFFF5F5F4)
val SurfaceVariantDark = Color(0xFFF5F2EB) // #F5F2EB cream content card fill
val OnSurfaceVariantDark = Color(0xFF1C1C1E) // #1C1C1E primary text on cream box

val ErrorDark = Color(0xFFFF453A) // #FF453A Apple system red error
val OnErrorDark = Color(0xFFFFFFFF)
val ErrorContainerDark = Color(0xFF3A0007)
val OnErrorContainerDark = Color(0xFFFFB4AB)
