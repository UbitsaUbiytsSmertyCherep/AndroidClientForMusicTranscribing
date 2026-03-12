package com.example.firstapplication


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.dp
import com.example.firstapplication.ui.theme.ActiveFontColor
import com.example.firstapplication.ui.theme.ButtonPressedBack
import com.example.firstapplication.ui.theme.MenuBack
import com.example.firstapplication.ui.theme.MenuVERYBack
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
@Composable
fun SideBar(modifier: Modifier = Modifier){
    var drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf("home") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Color(0xFF0F172B)) {
                Text("Menu", modifier = Modifier.padding(16.dp))
                NavigationDrawerItem(
                    label = { Text("MAin") },
                    selected = currentScreen == "home",
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = MenuBack,
                        selectedContainerColor = ButtonPressedBack,
                        unselectedTextColor = Color.Gray,
                        selectedTextColor = ActiveFontColor
                    ),
                    modifier = Modifier
                        .background(Color(0xFF0F172B)),
                    onClick = {
                        currentScreen = "home"
                        scope.launch { drawerState.close() }
                    }



                )
                NavigationDrawerItem(
                    label = { Text("settings") },
                    selected = currentScreen == "settings",
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = MenuBack,
                        selectedContainerColor = ButtonPressedBack,
                        unselectedTextColor = Color.Gray,
                        selectedTextColor = ActiveFontColor
                    ),
                    onClick = {
                        currentScreen = "settings"
                        scope.launch { drawerState.close() }
                    }
                )

            }
        }

    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Меню")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).background(MenuVERYBack)) {
                // ПЕРЕКЛЮЧАТЕЛЬ ЭКРАНОВ (как Router в Вебе или Frame в C#)
                when (currentScreen) {
                    "home" -> MainWindow()
                    "settings" -> InnerScreen()
                }
            }
        }
    }


}

