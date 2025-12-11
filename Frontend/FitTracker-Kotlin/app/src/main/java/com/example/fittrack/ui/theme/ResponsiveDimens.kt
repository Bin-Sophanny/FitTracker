package com.example.fittrack.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object ResponsiveDimens {

    @Composable
    fun horizontalPadding(): Dp {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 12.dp  // Small phones
            screenWidth < 400 -> 16.dp  // Medium phones
            screenWidth < 600 -> 20.dp  // Large phones
            else -> 24.dp               // Tablets
        }
    }

    @Composable
    fun spacingSmall(): Dp {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 6.dp
            screenWidth < 600 -> 8.dp
            else -> 12.dp
        }
    }

    @Composable
    fun spacingMedium(): Dp {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 10.dp
            screenWidth < 600 -> 12.dp
            else -> 16.dp
        }
    }

    @Composable
    fun spacingLarge(): Dp {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 16.dp
            screenWidth < 600 -> 20.dp
            else -> 24.dp
        }
    }

    @Composable
    fun textSizeTitle(): TextUnit {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 20.sp  // Small phones
            screenWidth < 400 -> 22.sp  // Medium phones
            screenWidth < 600 -> 24.sp  // Large phones
            else -> 28.sp               // Tablets
        }
    }

    @Composable
    fun textSizeSubtitle(): TextUnit {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 16.sp
            screenWidth < 600 -> 18.sp
            else -> 20.sp
        }
    }

    @Composable
    fun textSizeBody(): TextUnit {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 13.sp
            screenWidth < 600 -> 14.sp
            else -> 16.sp
        }
    }

    @Composable
    fun textSizeCaption(): TextUnit {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 10.sp
            screenWidth < 600 -> 11.sp
            else -> 12.sp
        }
    }

    @Composable
    fun avatarSizeSmall(): Dp {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 40.dp
            screenWidth < 600 -> 48.dp
            else -> 56.dp
        }
    }

    @Composable
    fun cardPadding(): Dp {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 12.dp
            screenWidth < 600 -> 16.dp
            else -> 20.dp
        }
    }

    @Composable
    fun cardPaddingLarge(): Dp {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 16.dp
            screenWidth < 600 -> 20.dp
            else -> 24.dp
        }
    }

    @Composable
    fun iconSizeSmall(): Dp {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 20.dp
            screenWidth < 600 -> 24.dp
            else -> 28.dp
        }
    }

    @Composable
    fun iconSizeMedium(): Dp {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 28.dp
            screenWidth < 600 -> 32.dp
            else -> 36.dp
        }
    }

    @Composable
    fun iconBoxSize(): Dp {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 44.dp
            screenWidth < 600 -> 48.dp
            else -> 56.dp
        }
    }

    @Composable
    fun iconBoxSizeLarge(): Dp {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 56.dp
            screenWidth < 600 -> 64.dp
            else -> 72.dp
        }
    }

    @Composable
    fun avatarSizeMedium(): Dp {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 80.dp
            screenWidth < 600 -> 96.dp
            else -> 120.dp
        }
    }

    @Composable
    fun textSizeHeading(): TextUnit {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 32.sp
            screenWidth < 600 -> 36.sp
            else -> 42.sp
        }
    }

    @Composable
    fun textSizeSmall(): TextUnit {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 11.sp
            screenWidth < 600 -> 12.sp
            else -> 14.sp
        }
    }

    @Composable
    fun buttonHeight(): Dp {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 44.dp
            screenWidth < 600 -> 50.dp
            else -> 56.dp
        }
    }

    @Composable
    fun cornerRadius(): Dp {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 8.dp
            screenWidth < 600 -> 12.dp
            else -> 16.dp
        }
    }

    @Composable
    fun cardElevation(): Dp {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        return when {
            screenWidth < 360 -> 3.dp
            screenWidth < 600 -> 4.dp
            else -> 6.dp
        }
    }
}

