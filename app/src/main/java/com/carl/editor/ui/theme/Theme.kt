package com.carl.editor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Carl's design tokens.
 *
 * Before this file, the app wrapped everything in a bare `MaterialTheme { }` - Compose's
 * default *light* color scheme - while every screen individually hardcoded
 * Surface(color = Color.Black) and Color.White text to fake a dark UI. That mismatch is what
 * caused the earlier invisible-controls bug (text relying on the light scheme's dark default
 * content color, rendering dark-on-black). This file gives Carl an actual dark ColorScheme so
 * that gap can be closed incrementally as screens migrate to it.
 *
 * NOTE: HomeScreen.kt and PreviewScreen.kt still hardcode Color.Black/Color.White directly
 * rather than reading from MaterialTheme.colorScheme. That still works correctly (the hardcoded
 * values already match this scheme), but it's technical debt to clean up screen-by-screen later,
 * not fixed by this file alone.
 */

// Carl's accent - already used throughout TimelineControls, GlobalTransformControls,
// CanvasControls, and ColorAdjustmentControls for selected/active states.
val CarlAccent = Color(0xFF00E5A0)

private val CarlDarkColorScheme = darkColorScheme(
    primary = CarlAccent,
    onPrimary = Color.Black,
    secondary = Color(0xFF8A8A8A),
    onSecondary = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF121212),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = Color(0xFFB3B3B3),
    error = Color(0xFFFF5449),
    onError = Color.Black
)

// Material3 defaults for now. A custom type ramp is a later, deliberate visual-identity step
// (see the redesign roadmap) - not part of this foundational fix.
private val CarlTypography = Typography()

@Composable
fun CarlTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CarlDarkColorScheme,
        typography = CarlTypography,
        content = content
    )
}
