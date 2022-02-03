package dev.fredag.sudokufocus

import android.view.MotionEvent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FloatingActionButton(
    @DrawableRes
    icon: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onTouchDown: () -> Unit = {},
    onTouchRelease: () -> Unit = {},
) {
    Image(
        painter = painterResource(id = icon),
        contentDescription = null,
        modifier = Modifier

            .padding(10.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .pointerInteropFilter {
                when (it.action) {
                    MotionEvent.ACTION_DOWN -> onTouchDown()
                    MotionEvent.ACTION_UP -> onTouchRelease()
                }
                true
            }
            .border(2.dp, MaterialTheme.colors.onBackground, CircleShape)
            .padding(10.dp)
            .then(modifier)
    )
}


@Composable
fun SettingsButton(navController: NavController, modifier: Modifier = Modifier) {
    FloatingActionButton(
        if (MaterialTheme.colors.isLight) R.drawable.ic_cog_black else R.drawable.ic_cog_white,
        modifier = modifier,
    ) {
        navController.navigate(
            "settings"
        )
    }

}

@Composable
fun PeekButton(onTouchDown: () -> Unit, onTouchRelease: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        if (MaterialTheme.colors.isLight) R.drawable.ic_eye_black else R.drawable.ic_eye_white,
        modifier = modifier,
        onTouchDown = onTouchDown,
        onTouchRelease = onTouchRelease,
    )
}