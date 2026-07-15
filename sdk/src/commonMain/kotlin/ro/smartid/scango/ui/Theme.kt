package ro.smartid.scango.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Brand = Color(0xFF0B6BCB)
private val BrandDark = Color(0xFF62B0F5)

private val LightColors = lightColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    secondary = Color(0xFF00696D),
    surfaceVariant = Color(0xFFEEF2F7),
)

private val DarkColors = darkColorScheme(
    primary = BrandDark,
    onPrimary = Color(0xFF00325B),
    secondary = Color(0xFF4CDADF),
)

/**
 * The SDK's screens ship with their own theme so they look the same in every host.
 * Hosts that want their own branding can wrap the screens in their own [MaterialTheme]
 * and pass `useSdkTheme = false` to [ScanGoScreen].
 */
@Composable
internal fun ScanGoTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
