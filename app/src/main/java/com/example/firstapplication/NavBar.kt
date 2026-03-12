package com.example.firstapplication


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalOf
import com.example.firstapplication.ui.theme.MenuBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.firstapplication.ui.theme.FirstApplicationTheme

@Composable
fun NavBar(modifier: Modifier = Modifier){
    var selectedItem by remember { mutableIntStateOf(1) }
    var items = listOf("Settings", "Menu", "Recent")
    var icons = listOf(Icons.Default.Settings, Icons.Default.Home, Icons.Default.Refresh)

    Scaffold(
        contentWindowInsets = WindowInsets(0,0,0,0),
        bottomBar = {
            Box(modifier = Modifier.navigationBarsPadding().padding(10.dp)){
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ){
                    NavigationBar(
                        containerColor = Color.Transparent,
                        modifier = Modifier.height(60.dp),
                        windowInsets = WindowInsets(0,0,0,0)
                    ){
                        items.forEachIndexed {index, string ->
                            NavigationBarItem(
                                icon = {Icon(icons[index], contentDescription = string)},
                                selected = selectedItem == index,
                                onClick = {selectedItem = index},


                            )
                        }
                    }
                }

            }

        }
    ){innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)){
            when (selectedItem){
                0 -> SettingScreen();
                1 -> MainWindow();
            }
        }
    }
}

@Preview
@Composable
fun NavBarPrev(){
    FirstApplicationTheme {
        NavBar()
    }
}