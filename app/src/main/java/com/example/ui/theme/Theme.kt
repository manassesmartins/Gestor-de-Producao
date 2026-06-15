package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke

fun parseHexToColor(hex: String): Color {
    val clean = hex.trim().replace("#", "")
    return try {
        if (clean.length == 6) {
            Color(android.graphics.Color.parseColor("#$clean"))
        } else if (clean.length == 8) {
            Color(android.graphics.Color.parseColor("#$clean"))
        } else {
            Color(0xFFF472B6) // Fallback pink
        }
    } catch (e: Exception) {
        Color(0xFFF472B6)
    }
}

fun blendColor(color1: Color, color2: Color, ratio: Float): Color {
    val r = color1.red + (color2.red - color1.red) * ratio
    val g = color1.green + (color2.green - color1.green) * ratio
    val b = color1.blue + (color2.blue - color1.blue) * ratio
    val a = color1.alpha + (color2.alpha - color1.alpha) * ratio
    return Color(r, g, b, a)
}

fun createCustomColorScheme(hex: String, isDark: Boolean): ColorScheme {
    val baseColor = parseHexToColor(hex)
    return if (isDark) {
        val bgValue = blendColor(baseColor, Color(0xFF000000), 0.93f)
        val surfaceValue = blendColor(baseColor, Color(0xFF000000), 0.88f)
        val surfaceVariantValue = blendColor(baseColor, Color(0xFF000000), 0.76f)
        val secondaryValue = blendColor(baseColor, Color(0xFFFFFFFF), 0.35f)
        val tertiaryValue = blendColor(baseColor, Color(0xFFFFFFFF), 0.50f)
        
        darkColorScheme(
            primary = baseColor,
            onPrimary = if (baseColor.isColorLight()) Color(0xFF1E0714) else Color(0xFFFFFFFF),
            secondary = secondaryValue,
            onSecondary = Color(0xFF1E0714),
            tertiary = tertiaryValue,
            onTertiary = Color(0xFF1E0714),
            error = Color(0xFFF87171),
            background = bgValue,
            onBackground = Color(0xFFFAF5FF),
            surface = surfaceValue,
            onSurface = Color(0xFFFAF5FF),
            surfaceVariant = surfaceVariantValue,
            onSurfaceVariant = secondaryValue
        )
    } else {
        val bgValue = blendColor(baseColor, Color(0xFFFFFFFF), 0.95f)
        val surfaceValue = Color(0xFFFFFFFF)
        val surfaceVariantValue = blendColor(baseColor, Color(0xFFFFFFFF), 0.85f)
        val secondaryValue = baseColor.copy(alpha = 0.85f)
        val tertiaryValue = baseColor.copy(alpha = 0.70f)

        lightColorScheme(
            primary = baseColor,
            onPrimary = if (baseColor.isColorLight()) Color(0xFF000000) else Color(0xFFFFFFFF),
            secondary = secondaryValue,
            onSecondary = Color(0xFFFFFFFF),
            tertiary = tertiaryValue,
            onTertiary = Color(0xFFFFFFFF),
            error = Color(0xFFDC2626),
            background = bgValue,
            onBackground = blendColor(baseColor, Color(0xFF000000), 0.85f),
            surface = surfaceValue,
            onSurface = blendColor(baseColor, Color(0xFF000000), 0.85f),
            surfaceVariant = surfaceVariantValue,
            onSurfaceVariant = blendColor(baseColor, Color(0xFF000000), 0.65f)
        )
    }
}

fun getDynamicColorScheme(schemeName: String, isDark: Boolean): ColorScheme {
    val clean = schemeName.trim()
    if (clean.startsWith("#") || (clean.length == 6 && clean.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' })) {
        return createCustomColorScheme(clean, isDark)
    }
    return if (isDark) {
        when (schemeName.uppercase()) {
            "BLUE" -> darkColorScheme(
                primary = Color(0xFF60A5FA),
                onPrimary = Color(0xFF0F172A),
                secondary = Color(0xFF93C5FD),
                onSecondary = Color(0xFF1E293B),
                tertiary = Color(0xFFA5F3FC),
                onTertiary = Color(0xFF0F172A),
                error = Color(0xFFF87171),
                background = Color(0xFF0C1324), // Rich Sapphire Blue-Black
                onBackground = Color(0xFFF8FAFC),
                surface = Color(0xFF131D33), // Deep naval surface match
                onSurface = Color(0xFFF8FAFC),
                surfaceVariant = Color(0xFF1F2E4D),
                onSurfaceVariant = Color(0xFFCBD5E1)
            )
            "EMERALD", "GREEN" -> darkColorScheme(
                primary = Color(0xFF34D399),
                onPrimary = Color(0xFF022C22),
                secondary = Color(0xFF6EE7B7),
                onSecondary = Color(0xFF042F2E),
                tertiary = Color(0xFFA7F3D0),
                onTertiary = Color(0xFF022C22),
                error = Color(0xFFF87171),
                background = Color(0xFF051711), // Rich Pine/Emerald Deep Black
                onBackground = Color(0xFFECFDF5),
                surface = Color(0xFF0B261D), // Deep rich green moss surface
                onSurface = Color(0xFFECFDF5),
                surfaceVariant = Color(0xFF153D30),
                onSurfaceVariant = Color(0xFFD1FAE5)
            )
            "PURPLE" -> darkColorScheme(
                primary = Color(0xFFC084FC),
                onPrimary = Color(0xFF2E1065),
                secondary = Color(0xFFD8B4FE),
                onSecondary = Color(0xFF3B0764),
                tertiary = Color(0xFFE9D5FF),
                onTertiary = Color(0xFF2E1065),
                error = Color(0xFFF87171),
                background = Color(0xFF160A24), // Deep Amethyst
                onBackground = Color(0xFFFAF5FF),
                surface = Color(0xFF200F33),
                onSurface = Color(0xFFFAF5FF),
                surfaceVariant = Color(0xFF3B1E63),
                onSurfaceVariant = Color(0xFFE9D5FF)
            )
            "ROSE" -> darkColorScheme( // LUXURIOUS OURO ROSÉ (Rose Gold Theme)
                primary = Color(0xFFF6A6B2), // Magnificent dusty Rose Gold
                onPrimary = Color(0xFF2E1C20),
                secondary = Color(0xFFFCD5CE), // Soft Peach Rose Gold
                onSecondary = Color(0xFF2E1C20),
                tertiary = Color(0xFFFEC89A), // Warm Golden Apricot glow
                onTertiary = Color(0xFF2E1C20),
                error = Color(0xFFF87171),
                background = Color(0xFF1E1114), // Exquisite Deep Wine Rose matching color scheme
                onBackground = Color(0xFFFAFAF9),
                surface = Color(0xFF2A191C), // Deep premium rose surface Match
                onSurface = Color(0xFFFAFAF9),
                surfaceVariant = Color(0xFF3B2428),
                onSurfaceVariant = Color(0xFFF6A6B2)
            )
            "RED" -> darkColorScheme(
                primary = Color(0xFFF87171),
                onPrimary = Color(0xFF451212),
                secondary = Color(0xFFFCA5A5),
                onSecondary = Color(0xFF1F1212),
                tertiary = Color(0xFFFEE2E2),
                onTertiary = Color(0xFF451212),
                error = Color(0xFFEF4444),
                background = Color(0xFF1D0909), // Rich Ruby Deep Black-Red
                onBackground = Color(0xFFFEF2F2),
                surface = Color(0xFF281111), // Rich blood-mahogany surface
                onSurface = Color(0xFFFEF2F2),
                surfaceVariant = Color(0xFF421E1E),
                onSurfaceVariant = Color(0xFFFCA5A5)
            )
            else -> darkColorScheme( // PINK - Default Original Elegant Dark Aesthetic
                primary = Color(0xFFF472B6),
                onPrimary = Color(0xFF3B071E),
                secondary = Color(0xFFFBCFE8),
                onSecondary = Color(0xFF4D052B),
                tertiary = Color(0xFFFDA4AF),
                onTertiary = Color(0xFF4E071A),
                error = Color(0xFFF43F5E),
                onError = Color(0xFF4C0519),
                background = Color(0xFF180812), // Deep luxurious orchid black-pink
                onBackground = Color(0xFFFFF0F5),
                surface = Color(0xFF210B19), // Rich plum surface match
                onSurface = Color(0xFFFFF0F5),
                surfaceVariant = Color(0xFF341126),
                onSurfaceVariant = Color(0xFFE2B7CE)
            )
        }
    } else {
        when (schemeName.uppercase()) {
            "BLUE" -> lightColorScheme(
                primary = Color(0xFF2563EB),
                onPrimary = Color(0xFFFFFFFF),
                secondary = Color(0xFF3B82F6),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFF06B6D4),
                onTertiary = Color(0xFFFFFFFF),
                error = Color(0xFFDC2626),
                background = Color(0xFFF0F4FF), // Cohesive gentle icy blue background
                onBackground = Color(0xFF0F172A),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF0F172A),
                surfaceVariant = Color(0xFFE2E8F0),
                onSurfaceVariant = Color(0xFF475569)
            )
            "EMERALD", "GREEN" -> lightColorScheme(
                primary = Color(0xFF059669),
                onPrimary = Color(0xFFFFFFFF),
                secondary = Color(0xFF10B981),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFF14B8A6),
                onTertiary = Color(0xFFFFFFFF),
                error = Color(0xFFDC2626),
                background = Color(0xFFECFDF5), // Cohesive mint light background
                onBackground = Color(0xFF064E3B),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF064E3B),
                surfaceVariant = Color(0xFFD1FAE5),
                onSurfaceVariant = Color(0xFF065F46)
            )
            "PURPLE" -> lightColorScheme(
                primary = Color(0xFF9333EA),
                onPrimary = Color(0xFFFFFFFF),
                secondary = Color(0xFFA855F7),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFFD8B4FE),
                onTertiary = Color(0xFF581C87),
                error = Color(0xFFDC2626),
                background = Color(0xFFFAF5FF), 
                onBackground = Color(0xFF3B0764),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF3B0764),
                surfaceVariant = Color(0xFFF3E8FF),
                onSurfaceVariant = Color(0xFF581C87)
            )
            "ROSE" -> lightColorScheme( // LUXURIOUS OURO ROSÉ (Rose Gold Theme Light)
                primary = Color(0xFFD8577A), // Cozy rich Rose Gold Pink
                onPrimary = Color(0xFFFFFFFF),
                secondary = Color(0xFFE5989B),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFFB56576),
                onTertiary = Color(0xFFFFFFFF),
                error = Color(0xFFDC2626),
                background = Color(0xFFFFF0F3), // Luxurious warm rose petal background
                onBackground = Color(0xFF2E1C20),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF2E1C20),
                surfaceVariant = Color(0xFFFFD7BA),
                onSurfaceVariant = Color(0xFFB56576)
            )
            "RED" -> lightColorScheme(
                primary = Color(0xFFDC2626),
                onPrimary = Color(0xFFFFFFFF),
                secondary = Color(0xFFEF4444),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFFF43F5E),
                onTertiary = Color(0xFFFFFFFF),
                error = Color(0xFFB91C1C),
                background = Color(0xFFFFF5F5), // Cohesive soft crimson red background
                onBackground = Color(0xFF451212),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF451212),
                surfaceVariant = Color(0xFFFEE2E2),
                onSurfaceVariant = Color(0xFF7F1D1D)
            )
            else -> lightColorScheme( // PINK - Default Original Light Aesthetic
                primary = Color(0xFFDB2777),
                onPrimary = Color(0xFFFFFFFF),
                secondary = Color(0xFFEC4899),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFFF43F5E),
                onTertiary = Color(0xFFFFFFFF),
                error = Color(0xFFE11D48),
                background = Color(0xFFFFF0F5), // Cohesive soft pink bubblegum background
                onBackground = Color(0xFF3B071E),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF3B071E),
                surfaceVariant = Color(0xFFFFE4E6),
                onSurfaceVariant = Color(0xFF9F1239)
            )
        }
    }
}

fun Color.isColorLight(): Boolean {
    return try {
        if (this == Color.Unspecified) false
        else (this.red + this.green + this.blue) > 1.5f
    } catch (e: Exception) {
        false
    }
}

@Composable
fun getGlassContainerColor(): Color {
    val isLight = MaterialTheme.colorScheme.background.isColorLight()
    val primary = MaterialTheme.colorScheme.primary
    return if (isLight) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    } else {
        // Dynamic premium glass look with warm dynamic ambient tint from primary color
        blendColor(Color(0xFF120E21).copy(alpha = 0.45f), primary, 0.05f).copy(alpha = 0.35f)
    }
}

@Composable
fun getGlassBorderColor(): Color {
    val isLight = MaterialTheme.colorScheme.background.isColorLight()
    val primary = MaterialTheme.colorScheme.primary
    return if (isLight) {
        primary.copy(alpha = 0.35f)
    } else {
        // Border gets elegant subtle glow based on chosen primary palette
        primary.copy(alpha = 0.18f)
    }
}

@Composable
fun getGlassBorderStroke(width: androidx.compose.ui.unit.Dp = 1.dp): BorderStroke {
    return BorderStroke(width, getGlassBorderColor())
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    colorSchemeName: String = "PINK",
    fontSizeScale: Float = 1.0f,
    content: @Composable () -> Unit,
) {
    val colorScheme = remember(darkTheme, colorSchemeName) {
        getDynamicColorScheme(colorSchemeName, darkTheme)
    }



    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        val currentDensity = LocalDensity.current
        val customDensity = remember(currentDensity.density, currentDensity.fontScale, fontSizeScale) {
            Density(
                density = currentDensity.density,
                fontScale = currentDensity.fontScale * fontSizeScale
            )
        }
        CompositionLocalProvider(LocalDensity provides customDensity) {
            content()
        }
    }
}
