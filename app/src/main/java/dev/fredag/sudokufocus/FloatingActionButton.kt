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
import com.guru.fontawesomecomposelib.FaIcon
import com.guru.fontawesomecomposelib.FaIconType
import com.guru.fontawesomecomposelib.FaIcons

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FloatingActionButton(
    icon: FaIconType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onTouchDown: () -> Unit = {},
    onTouchRelease: () -> Unit = {},
) {
    FaIcon(
        faIcon = icon,
        tint = MaterialTheme.colors.onBackground,
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
        FaIcons.Cog,
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
        FaIcons.Eye,
        modifier = modifier,
        onTouchDown = onTouchDown,
        onTouchRelease = onTouchRelease,
    )
}