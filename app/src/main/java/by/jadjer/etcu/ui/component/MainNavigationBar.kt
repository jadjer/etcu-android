package by.jadjer.etcu.ui.component

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import by.jadjer.etcu.ui.navigation.ScreenItem
import by.jadjer.etcu.ui.theme.ETCUTheme

@Composable
fun MainNavigationBar(
    pagerState: PagerState,
    navItems: List<ScreenItem>,
    onScreenSelected: (ScreenItem) -> Unit
) {
    val selectedPageIndex by remember { derivedStateOf { pagerState.currentPage } }

    NavigationBar {
        navItems.forEachIndexed { index, screen ->
            val title = stringResource(screen.titleResId)
            NavigationBarItem(
                icon = { Icon(screen.icon, title) },
                label = { Text(title) },
                selected = selectedPageIndex == index,
                onClick = { onScreenSelected(screen) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainNavigationBarPreview() {
    ETCUTheme {
        MainNavigationBar(
            pagerState = rememberPagerState(pageCount = { 4 }),
            navItems = ScreenItem.mainItems,
            onScreenSelected = {}
        )
    }
}
